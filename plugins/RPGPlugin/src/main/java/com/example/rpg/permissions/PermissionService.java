package com.example.rpg.permissions;

import com.example.rpg.db.DatabaseService;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

public class PermissionService {
    private final JavaPlugin plugin;
    private final PermissionRepository repository;
    private final PermissionAuditLog auditLog;
    private final Map<String, Role> roles = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerRoles> playerRoles = new ConcurrentHashMap<>();
    private final Map<UUID, PermissionAttachment> attachments = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, CacheEntry>> resolvedCache = new ConcurrentHashMap<>();
    private final PermissionDecision defaultDecision;
    private final String defaultRole;
    private final boolean opBypass;
    private final boolean enabled;
    private final boolean auditEnabled;
    private final long cacheTtlMillis;

    public PermissionService(JavaPlugin plugin, DatabaseService database, boolean enabled, String defaultRole,
                             PermissionDecision defaultDecision, boolean opBypass, boolean auditEnabled, long cacheTtlSeconds) {
        this.plugin = plugin;
        this.repository = new PostgresPermissionRepository(database);
        this.auditLog = new PermissionAuditLog(database);
        this.enabled = enabled;
        this.defaultRole = defaultRole;
        this.defaultDecision = defaultDecision;
        this.opBypass = opBypass;
        this.auditEnabled = auditEnabled;
        this.cacheTtlMillis = Math.max(0, cacheTtlSeconds) * 1000L;
        reload();
    }

    public void reload() {
        roles.clear();
        for (Role role : repository.loadAllRoles()) {
            roles.put(role.key(), role);
        }
        if (roles.isEmpty()) {
            bootstrapDefaults();
        }
        resolvedCache.clear();
        playerRoles.clear();
    }

    public boolean has(Player player, String node) {
        if (player == null) {
            return false;
        }
        if (!enabled) {
            return player.hasPermission(node);
        }
        if (opBypass && player.isOp()) {
            return true;
        }
        return resolve(player.getUniqueId(), node);
    }

    public PermissionExplanation explain(Player player, String node) {
        PlayerRoles roles = getPlayerRoles(player.getUniqueId());
        PermissionResolver resolver = new PermissionResolver(roles(), defaultDecision);
        return resolver.explain(roles, node);
    }

    public void applyAttachments(Player player) {
        removeAttachment(player.getUniqueId());
        if (!enabled) {
            return;
        }
        PermissionAttachment attachment = player.addAttachment(plugin);
        PlayerRoles roles = getPlayerRoles(player.getUniqueId());
        if (roles != null) {
            PermissionResolver resolver = new PermissionResolver(roles(), defaultDecision);
            Map<String, PermissionDecision> decisions = collectDecisions(roles);
            for (Map.Entry<String, PermissionDecision> entry : decisions.entrySet()) {
                if (entry.getValue() == PermissionDecision.INHERIT) {
                    continue;
                }
                boolean allowed = resolver.resolve(roles, entry.getKey());
                attachment.setPermission(entry.getKey(), allowed);
            }
        }
        attachments.put(player.getUniqueId(), attachment);
    }

    public void removeAttachment(UUID playerId) {
        PermissionAttachment attachment = attachments.remove(playerId);
        if (attachment != null) {
            attachment.remove();
        }
    }

    public void createRole(Player actor, String key, String displayName) {
        Role role = new Role(key, displayName);
        roles.put(key, role);
        repository.saveRole(role);
        audit(actor, "role.create", key, null, role);
        invalidateAll();
    }

    public void renameRole(Player actor, String key, String displayName) {
        Role role = roles.get(key);
        if (role == null) {
            return;
        }
        Role before = cloneRole(role);
        role.setDisplayName(displayName);
        repository.saveRole(role);
        audit(actor, "role.rename", key, before, role);
        invalidateAll();
    }

    public void deleteRole(Player actor, String key) {
        Role before = roles.remove(key);
        repository.deleteRole(key);
        audit(actor, "role.delete", key, before, null);
        invalidateAll();
    }

    public void setRoleNode(Player actor, String roleKey, String node, PermissionDecision decision) {
        Role role = roles.get(roleKey);
        if (role == null) {
            return;
        }
        Role before = cloneRole(role);
        if (decision == PermissionDecision.INHERIT) {
            role.nodes().remove(node);
        } else {
            role.nodes().put(node, decision);
        }
        repository.saveRole(role);
        audit(actor, "role.node", roleKey + ":" + node, before, role);
        invalidateAll();
    }

    public boolean addParent(Player actor, String roleKey, String parentKey) {
        Role role = roles.get(roleKey);
        Role parent = roles.get(parentKey);
        if (role == null || parent == null) {
            return false;
        }
        if (createsCycle(roleKey, parentKey)) {
            return false;
        }
        Role before = cloneRole(role);
        role.parents().add(parentKey);
        repository.saveRole(role);
        audit(actor, "role.parent.add", roleKey + "<-" + parentKey, before, role);
        invalidateAll();
        return true;
    }

    public void removeParent(Player actor, String roleKey, String parentKey) {
        Role role = roles.get(roleKey);
        if (role == null) {
            return;
        }
        Role before = cloneRole(role);
        role.parents().remove(parentKey);
        repository.saveRole(role);
        audit(actor, "role.parent.remove", roleKey + "<-" + parentKey, before, role);
        invalidateAll();
    }

    public void assignPrimary(Player actor, UUID playerId, String roleKey) {
        PlayerRoles roles = getPlayerRoles(playerId);
        PlayerRoles before = clonePlayerRoles(roles);
        roles.setPrimaryRole(roleKey);
        repository.savePlayerRoles(roles);
        audit(actor, "player.primary", playerId.toString(), before, roles);
        invalidatePlayer(playerId);
    }

    public void addRole(Player actor, UUID playerId, String roleKey) {
        PlayerRoles roles = getPlayerRoles(playerId);
        PlayerRoles before = clonePlayerRoles(roles);
        roles.extraRoles().add(roleKey);
        repository.savePlayerRoles(roles);
        audit(actor, "player.role.add", playerId + ":" + roleKey, before, roles);
        invalidatePlayer(playerId);
    }

    public void removeRole(Player actor, UUID playerId, String roleKey) {
        PlayerRoles roles = getPlayerRoles(playerId);
        PlayerRoles before = clonePlayerRoles(roles);
        roles.extraRoles().remove(roleKey);
        if (roleKey.equals(roles.primaryRole())) {
            roles.setPrimaryRole(null);
        }
        repository.savePlayerRoles(roles);
        audit(actor, "player.role.remove", playerId + ":" + roleKey, before, roles);
        invalidatePlayer(playerId);
    }

    public Map<String, Role> roles() {
        return roles;
    }

    public PlayerRoles getPlayerRoles(UUID playerId) {
        return playerRoles.computeIfAbsent(playerId, uuid -> repository.loadPlayerRoles(uuid).orElseGet(() -> {
            PlayerRoles roles = new PlayerRoles(uuid);
            roles.setPrimaryRole(getDefaultRole());
            repository.savePlayerRoles(roles);
            return roles;
        }));
    }

    public List<PlayerRoles> listPlayerRoles() {
        return repository.listPlayerRoles();
    }

    public PermissionExplanation explain(UUID playerId, String node) {
        PlayerRoles roles = getPlayerRoles(playerId);
        PermissionResolver resolver = new PermissionResolver(roles(), defaultDecision);
        return resolver.explain(roles, node);
    }

    public PermissionAuditLog auditLog() {
        return auditLog;
    }

    private boolean resolve(UUID playerId, String node) {
        if (cacheTtlMillis <= 0) {
            PlayerRoles roles = getPlayerRoles(playerId);
            PermissionResolver resolver = new PermissionResolver(roles(), defaultDecision);
            return resolver.resolve(roles, node);
        }
        Map<String, CacheEntry> cache = resolvedCache.computeIfAbsent(playerId, key -> new ConcurrentHashMap<>());
        long now = System.currentTimeMillis();
        CacheEntry entry = cache.get(node);
        if (entry != null && entry.expiresAt() > now) {
            return entry.allowed();
        }
        PlayerRoles roles = getPlayerRoles(playerId);
        PermissionResolver resolver = new PermissionResolver(roles(), defaultDecision);
        boolean allowed = resolver.resolve(roles, node);
        cache.put(node, new CacheEntry(allowed, now + cacheTtlMillis));
        return allowed;
    }

    private void invalidateAll() {
        resolvedCache.clear();
        for (UUID uuid : attachments.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                applyAttachments(player);
            }
        }
    }

    private void invalidatePlayer(UUID playerId) {
        resolvedCache.remove(playerId);
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            applyAttachments(player);
        }
    }

    private void audit(Player actor, String action, String target, Object before, Object after) {
        if (actor == null || !auditEnabled) {
            return;
        }
        auditLog.log(actor.getUniqueId(), actor.getName(), action, target, before, after);
    }

    private Map<String, PermissionDecision> collectDecisions(PlayerRoles roles) {
        Map<String, PermissionDecision> decisions = new HashMap<>();
        Set<String> visited = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        if (roles.primaryRole() != null) {
            queue.add(roles.primaryRole());
        }
        queue.addAll(roles.extraRoles());
        while (!queue.isEmpty()) {
            String key = queue.pop();
            if (!visited.add(key)) {
                continue;
            }
            Role role = this.roles.get(key);
            if (role == null) {
                continue;
            }
            decisions.putAll(role.nodes());
            queue.addAll(role.parents());
        }
        return decisions;
    }

    private boolean createsCycle(String roleKey, String parentKey) {
        Set<String> visited = new HashSet<>();
        Deque<String> stack = new ArrayDeque<>();
        stack.push(parentKey);
        while (!stack.isEmpty()) {
            String current = stack.pop();
            if (!visited.add(current)) {
                continue;
            }
            if (current.equals(roleKey)) {
                return true;
            }
            Role role = roles.get(current);
            if (role != null) {
                stack.addAll(role.parents());
            }
        }
        return false;
    }

    private Role cloneRole(Role role) {
        Role clone = new Role(role.key(), role.displayName());
        clone.parents().addAll(role.parents());
        clone.nodes().putAll(role.nodes());
        return clone;
    }

    private PlayerRoles clonePlayerRoles(PlayerRoles roles) {
        PlayerRoles clone = new PlayerRoles(roles.playerId());
        clone.setPrimaryRole(roles.primaryRole());
        clone.extraRoles().addAll(roles.extraRoles());
        return clone;
    }

    private String getDefaultRole() {
        if (defaultRole != null && roles.containsKey(defaultRole)) {
            return defaultRole;
        }
        return roles.containsKey("player") ? "player" : null;
    }

    private void bootstrapDefaults() {
        Role player = new Role("player", "Spieler");
        Role moderator = new Role("moderator", "Moderator");
        Role admin = new Role("admin", "Admin");
        admin.nodes().put("rpg.admin.*", PermissionDecision.ALLOW);
        roles.put(player.key(), player);
        roles.put(moderator.key(), moderator);
        roles.put(admin.key(), admin);
        repository.saveRole(player);
        repository.saveRole(moderator);
        repository.saveRole(admin);
    }

    private record CacheEntry(boolean allowed, long expiresAt) {
    }
}

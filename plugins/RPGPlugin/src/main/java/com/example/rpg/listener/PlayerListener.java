package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.util.Text;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
    private final RPGPlugin plugin;
    private final Map<UUID, Float> walkSpeed = new HashMap<>();
    private final Map<UUID, Float> flySpeed = new HashMap<>();

    public PlayerListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        freeze(player);
        plugin.playerDataManager().loadProfileAsync(player.getUniqueId()).whenComplete((profile, error) -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) {
                    return;
                }
                PlayerProfile resolved = profile != null ? profile : plugin.playerDataManager().getProfile(player);
                resolved.applyAttributes(player);
                unfreeze(player);
            });
        });
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (plugin.promptManager().handle(player, event.getMessage())) {
            event.setCancelled(true);
            return;
        }
        var profile = plugin.playerDataManager().getProfile(player);
        if (profile.title() != null && !profile.title().isBlank()) {
            event.setFormat("[" + profile.title() + "] " + player.getName() + ": " + event.getMessage());
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.setKeepInventory(true);
        event.getDrops().clear();
        event.setKeepLevel(true);
        event.setDroppedExp(0);
        plugin.dungeonManager().markDeath(event.getEntity());
    }

    @EventHandler
    public void onWandUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!plugin.permissionService().has(player, "rpg.editor")) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.STICK) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        if (!meta.getPersistentDataContainer().has(plugin.wandKey(), PersistentDataType.BYTE)) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            player.sendMessage(Text.mm("<green>Pos1 gesetzt: " + event.getClickedBlock().getLocation().toVector()));
            player.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "pos1"),
                org.bukkit.persistence.PersistentDataType.STRING,
                serializeLocation(event.getClickedBlock().getLocation())
            );
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            player.sendMessage(Text.mm("<green>Pos2 gesetzt: " + event.getClickedBlock().getLocation().toVector()));
            player.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "pos2"),
                org.bukkit.persistence.PersistentDataType.STRING,
                serializeLocation(event.getClickedBlock().getLocation())
            );
        }
    }

    @EventHandler
    public void onUseItem(PlayerInteractEvent event) {
        if (event.getItem() == null || event.getItem().getType().isAir()) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        String zoneId = plugin.zoneManager().getZoneAt(event.getPlayer().getLocation()) != null
            ? plugin.zoneManager().getZoneAt(event.getPlayer().getLocation()).id()
            : null;
        plugin.worldEventManager().handleUseItem(event.getPlayer(), event.getItem().getType().name(), zoneId);
    }

    private String serializeLocation(org.bukkit.Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    private void freeze(Player player) {
        walkSpeed.put(player.getUniqueId(), player.getWalkSpeed());
        flySpeed.put(player.getUniqueId(), player.getFlySpeed());
        player.setWalkSpeed(0f);
        player.setFlySpeed(0f);
        player.setInvulnerable(true);
        player.setCollidable(false);
    }

    private void unfreeze(Player player) {
        Float walk = walkSpeed.remove(player.getUniqueId());
        Float fly = flySpeed.remove(player.getUniqueId());
        player.setWalkSpeed(walk != null ? walk : 0.2f);
        player.setFlySpeed(fly != null ? fly : 0.1f);
        player.setInvulnerable(false);
        player.setCollidable(true);
    }
}

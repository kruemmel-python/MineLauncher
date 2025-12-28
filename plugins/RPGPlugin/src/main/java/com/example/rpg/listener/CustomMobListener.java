package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.behavior.BehaviorContext;
import com.example.rpg.behavior.BehaviorKeys;
import com.example.rpg.behavior.BehaviorNode;
import com.example.rpg.behavior.ThreatTable;
import com.example.rpg.dungeon.DungeonInstance;
import com.example.rpg.model.LootEntry;
import com.example.rpg.model.LootTable;
import com.example.rpg.model.MobDefinition;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.Rarity;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class CustomMobListener implements Listener {
    private final RPGPlugin plugin;
    private final NamespacedKey mobKey;
    private final Random random = new Random();
    private final Map<UUID, BukkitTask> behaviorTasks = new HashMap<>();
    private final Map<UUID, BehaviorContext> behaviorContexts = new HashMap<>();
    private final Map<UUID, TextDisplay> healthBars = new HashMap<>();

    public CustomMobListener(RPGPlugin plugin) {
        this.plugin = plugin;
        this.mobKey = new NamespacedKey(plugin, "custom_mob_id");
    }

    public NamespacedKey mobKey() {
        return mobKey;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (!(damager instanceof LivingEntity living)) {
            return;
        }
        String mobId = getMobId(living);
        if (mobId == null) {
            return;
        }
        MobDefinition mob = plugin.mobManager().getMob(mobId);
        if (mob != null) {
            double damage = mob.damage();
            DungeonInstance instance = plugin.dungeonManager().instanceForWorld(living.getWorld());
            if (instance != null) {
                damage *= instance.scale();
            }
            event.setDamage(damage);
        }
    }

    @EventHandler
    public void onThreatDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        String mobId = getMobId(living);
        if (mobId == null) {
            return;
        }
        Player source = resolvePlayerDamager(event.getDamager());
        if (source == null) {
            return;
        }
        BehaviorContext context = behaviorContexts.get(living.getUniqueId());
        if (context == null) {
            return;
        }
        ThreatTable threatTable = context.getState(BehaviorKeys.THREAT_TABLE, ThreatTable.class);
        if (threatTable == null) {
            threatTable = new ThreatTable();
            context.putState(BehaviorKeys.THREAT_TABLE, threatTable);
        }
        threatTable.addThreat(source, event.getFinalDamage());
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        String mobId = getMobId(entity);
        if (mobId == null) {
            return;
        }
        removeHealthBar(entity);
        BukkitTask task = behaviorTasks.remove(entity.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        behaviorContexts.remove(entity.getUniqueId());
        MobDefinition mob = plugin.mobManager().getMob(mobId);
        if (mob == null) {
            return;
        }
        DungeonInstance instance = plugin.dungeonManager().instanceForWorld(entity.getWorld());
        Player killer = entity.getKiller();
        if (killer != null) {
            PlayerProfile profile = plugin.playerDataManager().getProfile(killer);
            profile.addXp(mob.xp());
            profile.applyAttributes(killer, plugin.itemStatManager(), plugin.classManager());
            int gold = 8 + random.nextInt(8) + Math.max(1, profile.level());
            profile.setGold(profile.gold() + gold);
            killer.sendMessage(com.example.rpg.util.Text.mm("<gold>+ " + gold + " Gold"));
            String zoneId = plugin.zoneManager().getZoneAt(entity.getLocation()) != null
                ? plugin.zoneManager().getZoneAt(entity.getLocation()).id()
                : null;
            plugin.worldEventManager().handleKill(killer, mob.id(), zoneId);
            plugin.worldEventManager().handleKill(killer, entity.getType().name(), zoneId);
        }
        if (mob.lootTable() != null) {
            LootTable table = plugin.lootManager().getTable(mob.lootTable());
            if (table != null) {
                for (LootEntry entry : table.entries()) {
                    if (random.nextDouble() <= entry.chance()) {
                        Material material = Material.matchMaterial(entry.material());
                        if (material != null) {
                            ItemStack item = plugin.itemGenerator().createRpgItem(material, entry.rarity(), 1);
                            item.setAmount(entry.minAmount() + random.nextInt(Math.max(1, entry.maxAmount() - entry.minAmount() + 1)));
                            event.getDrops().add(item);
                            if (killer != null) {
                                plugin.broadcastLoot(killer, item);
                            }
                        }
                    }
                }
            }
        } else if (killer != null) {
            dropFallbackLoot(killer, event);
        }
        if (mob.boss() && instance != null) {
            plugin.dungeonManager().completeDungeon(instance);
        }
    }

    private void dropFallbackLoot(Player killer, EntityDeathEvent event) {
        PlayerProfile profile = plugin.playerDataManager().getProfile(killer);
        int level = Math.max(1, profile.level());
        Material material = selectMaterialForLevel(level);
        if (material == null) {
            return;
        }
        Rarity rarity = rollRarity();
        ItemStack item = plugin.itemGenerator().createRpgItem(material, rarity, level);
        event.getDrops().add(item);
        plugin.broadcastLoot(killer, item);
    }

    private Material selectMaterialForLevel(int level) {
        Material[] low = {
            Material.WOODEN_SWORD, Material.STONE_SWORD, Material.BOW,
            Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE,
            Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS
        };
        Material[] mid = {
            Material.IRON_SWORD, Material.CROSSBOW,
            Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE,
            Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS
        };
        Material[] high = {
            Material.DIAMOND_SWORD, Material.NETHERITE_SWORD,
            Material.IRON_HELMET, Material.IRON_CHESTPLATE,
            Material.IRON_LEGGINGS, Material.IRON_BOOTS
        };
        Material[] pool = level < 5 ? low : (level < 15 ? mid : high);
        return pool[random.nextInt(pool.length)];
    }

    private Rarity rollRarity() {
        double roll = random.nextDouble();
        double total = 0.0;
        for (Rarity rarity : Rarity.values()) {
            total += rarity.weight();
            if (roll <= total) {
                return rarity;
            }
        }
        return Rarity.COMMON;
    }

    public void applyDefinition(LivingEntity entity, MobDefinition mob) {
        String name = mob.name();
        entity.customName(null);
        entity.setCustomNameVisible(false);
        double health = mob.health();
        DungeonInstance instance = plugin.dungeonManager().instanceForWorld(entity.getWorld());
        if (instance != null) {
            health *= instance.scale();
        }
        if (entity.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
        }
        entity.setHealth(health);
        entity.getPersistentDataContainer().set(mobKey, PersistentDataType.STRING, mob.id());
        if (mob.mainHand() != null) {
            Material material = Material.matchMaterial(mob.mainHand());
            if (material != null) {
                entity.getEquipment().setItemInMainHand(new ItemStack(material));
            }
        }
        if (mob.helmet() != null) {
            Material material = Material.matchMaterial(mob.helmet());
            if (material != null) {
                entity.getEquipment().setHelmet(new ItemStack(material));
            }
        }
        attachHealthBar(entity, mob, entity.getHealth());
        startBehaviorLoop(entity, mob);
    }

    private String getMobId(LivingEntity entity) {
        if (!entity.getPersistentDataContainer().has(mobKey, PersistentDataType.STRING)) {
            return null;
        }
        return entity.getPersistentDataContainer().get(mobKey, PersistentDataType.STRING);
    }

    @EventHandler
    public void onMobDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        String mobId = getMobId(living);
        if (mobId == null) {
            return;
        }
        MobDefinition mob = plugin.mobManager().getMob(mobId);
        if (mob == null) {
            return;
        }
        double nextHealth = Math.max(0, living.getHealth() - event.getFinalDamage());
        updateHealthBar(living, mob, nextHealth);
    }

    @EventHandler
    public void onMobHeal(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        String mobId = getMobId(living);
        if (mobId == null) {
            return;
        }
        MobDefinition mob = plugin.mobManager().getMob(mobId);
        if (mob == null) {
            return;
        }
        double maxHealth = mob.health();
        double nextHealth = Math.min(maxHealth, living.getHealth() + event.getAmount());
        updateHealthBar(living, mob, nextHealth);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        for (BehaviorContext context : behaviorContexts.values()) {
            ThreatTable threatTable = context.getState(BehaviorKeys.THREAT_TABLE, ThreatTable.class);
            if (threatTable != null) {
                threatTable.remove(player);
            }
        }
    }

    private void startBehaviorLoop(LivingEntity entity, MobDefinition mob) {
        BehaviorNode root = plugin.behaviorTreeManager().getTree(mob.behaviorTree());
        BehaviorContext context = new BehaviorContext(plugin, entity, mob);
        behaviorContexts.put(entity.getUniqueId(), context);
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (entity.isDead() || !entity.isValid()) {
                BukkitTask running = behaviorTasks.remove(entity.getUniqueId());
                if (running != null) {
                    running.cancel();
                }
                behaviorContexts.remove(entity.getUniqueId());
                return;
            }
            if (!isTargetValid(entity, context.target())) {
                context.setTarget(findTarget(entity));
            }
            decayThreat(context);
            logDebugStateIfNeeded(context);
            root.tick(context);
        }, 1L, 1L);
        behaviorTasks.put(entity.getUniqueId(), task);
    }

    private Player findTarget(LivingEntity entity) {
        return entity.getWorld().getPlayers().stream()
            .filter(player -> player.getLocation().distanceSquared(entity.getLocation()) <= 400)
            .min((a, b) -> Double.compare(a.getLocation().distanceSquared(entity.getLocation()),
                b.getLocation().distanceSquared(entity.getLocation())))
            .orElse(null);
    }

    private Player resolvePlayerDamager(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
            return player;
        }
        return null;
    }

    private boolean isTargetValid(LivingEntity mob, Player target) {
        if (target == null || target.isDead() || !target.isValid()) {
            return false;
        }
        if (!mob.getWorld().equals(target.getWorld())) {
            return false;
        }
        return true;
    }

    private void logDebugStateIfNeeded(BehaviorContext context) {
        if (!context.definition().debugAi()) {
            return;
        }
        long now = System.currentTimeMillis();
        long lastLog = context.getStateLong(BehaviorKeys.DEBUG_LAST_LOG, 0);
        if (now - lastLog < 1000) {
            return;
        }
        context.putStateLong(BehaviorKeys.DEBUG_LAST_LOG, now);
        Player target = context.target();
        boolean hasLos = target != null && context.mob().hasLineOfSight(target);
        Object lastSeen = context.state().get(BehaviorKeys.LAST_SEEN);
        ThreatTable threatTable = context.getState(BehaviorKeys.THREAT_TABLE, ThreatTable.class);
        Player topThreat = threatTable != null
            ? threatTable.getTopThreatTarget(player -> isTargetValid(context.mob(), player))
            : null;
        String targetName = target != null ? target.getName() : "none";
        String lastSeenInfo = lastSeen != null ? lastSeen.toString() : "none";
        String threatInfo = topThreat != null ? topThreat.getName() : "none";
        plugin.getLogger().info(String.format(
            "[AI:%s] target=%s los=%s lastSeen=%s topThreat=%s",
            context.mobId(),
            targetName,
            hasLos,
            lastSeenInfo,
            threatInfo));
    }

    private void decayThreat(BehaviorContext context) {
        ThreatTable threatTable = context.getState(BehaviorKeys.THREAT_TABLE, ThreatTable.class);
        if (threatTable != null) {
            threatTable.decay(0.99);
        }
    }

    private void attachHealthBar(LivingEntity entity, MobDefinition mob, double health) {
        removeHealthBar(entity);
        TextDisplay display = entity.getWorld().spawn(entity.getLocation().add(0, 1.6, 0), TextDisplay.class);
        display.setBillboard(Display.Billboard.CENTER);
        display.setSeeThrough(true);
        display.setShadowed(true);
        display.text(net.kyori.adventure.text.Component.text(buildHealthText(mob, health)));
        entity.addPassenger(display);
        healthBars.put(entity.getUniqueId(), display);
    }

    private void updateHealthBar(LivingEntity entity, MobDefinition mob, double health) {
        TextDisplay display = healthBars.get(entity.getUniqueId());
        if (display == null || display.isDead()) {
            attachHealthBar(entity, mob, health);
            return;
        }
        display.text(net.kyori.adventure.text.Component.text(buildHealthText(mob, health)));
    }

    private void removeHealthBar(LivingEntity entity) {
        TextDisplay display = healthBars.remove(entity.getUniqueId());
        if (display != null && !display.isDead()) {
            display.remove();
        }
    }

    private String buildHealthText(MobDefinition mob, double health) {
        double maxHealth = Math.max(1, mob.health());
        int bars = 10;
        int filled = (int) Math.round((health / maxHealth) * bars);
        filled = Math.min(bars, Math.max(0, filled));
        int empty = bars - filled;
        StringBuilder bar = new StringBuilder();
        bar.append("§7[§a");
        bar.append("|".repeat(filled));
        bar.append("§c");
        bar.append("|".repeat(empty));
        bar.append("§7]");
        return mob.name() + " " + bar + " §f" + Math.round(health) + "/" + Math.round(maxHealth) + " HP";
    }
}

package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.behavior.BehaviorContext;
import com.example.rpg.behavior.BehaviorNode;
import com.example.rpg.model.LootEntry;
import com.example.rpg.model.LootTable;
import com.example.rpg.model.MobDefinition;
import com.example.rpg.model.PlayerProfile;
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
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
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
            event.setDamage(mob.damage());
        }
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
        Player killer = entity.getKiller();
        if (killer != null) {
            PlayerProfile profile = plugin.playerDataManager().getProfile(killer);
            profile.addXp(mob.xp());
            profile.applyAttributes(killer);
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
        }
    }

    public void applyDefinition(LivingEntity entity, MobDefinition mob) {
        String name = mob.name();
        entity.customName(null);
        entity.setCustomNameVisible(false);
        if (entity.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(mob.health());
        }
        entity.setHealth(mob.health());
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
            Player target = findTarget(entity);
            context.setTarget(target);
            if (target == null) {
                return;
            }
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

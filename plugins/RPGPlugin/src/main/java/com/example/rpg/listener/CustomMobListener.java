package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class CustomMobListener implements Listener {
    private final RPGPlugin plugin;
    private final NamespacedKey mobKey;
    private final Random random = new Random();
    private final Map<UUID, BukkitTask> skillTasks = new HashMap<>();

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
        BukkitTask task = skillTasks.remove(entity.getUniqueId());
        if (task != null) {
            task.cancel();
        }
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
                        }
                    }
                }
            }
        }
    }

    public void applyDefinition(LivingEntity entity, MobDefinition mob) {
        String name = mob.name();
        if (name != null) {
            entity.customName(net.kyori.adventure.text.Component.text(
                org.bukkit.ChatColor.translateAlternateColorCodes('&', name)));
        }
        entity.setCustomNameVisible(true);
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
        startSkillLoop(entity, mob);
    }

    private String getMobId(LivingEntity entity) {
        if (!entity.getPersistentDataContainer().has(mobKey, PersistentDataType.STRING)) {
            return null;
        }
        return entity.getPersistentDataContainer().get(mobKey, PersistentDataType.STRING);
    }

    private void startSkillLoop(LivingEntity entity, MobDefinition mob) {
        if (mob.skills().isEmpty() || mob.skillIntervalSeconds() <= 0) {
            return;
        }
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (entity.isDead() || !entity.isValid()) {
                BukkitTask running = skillTasks.remove(entity.getUniqueId());
                if (running != null) {
                    running.cancel();
                }
                return;
            }
            Player target = entity.getWorld().getPlayers().stream()
                .filter(player -> player.getLocation().distanceSquared(entity.getLocation()) <= 100)
                .findFirst()
                .orElse(null);
            if (target == null) {
                return;
            }
            String skillId = mob.skills().get(random.nextInt(mob.skills().size()));
            plugin.useMobSkill(entity, target, skillId);
        }, 20L, mob.skillIntervalSeconds() * 20L);
        skillTasks.put(entity.getUniqueId(), task);
    }
}

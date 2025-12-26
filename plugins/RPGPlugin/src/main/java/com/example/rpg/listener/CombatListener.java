package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.LootEntry;
import com.example.rpg.model.LootTable;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.Quest;
import com.example.rpg.model.QuestProgress;
import com.example.rpg.model.QuestStep;
import com.example.rpg.model.QuestStepType;
import com.example.rpg.model.Rarity;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

public class CombatListener implements Listener {
    private final RPGPlugin plugin;
    private final Random random = new Random();

    public CombatListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFriendlyFire(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) {
            return;
        }
        if (!(event.getEntity() instanceof Player target)) {
            return;
        }
        plugin.partyManager().getParty(damager.getUniqueId()).ifPresent(party -> {
            if (party.members().contains(target.getUniqueId())) {
                event.setCancelled(true);
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onCombatLog(EntityDamageByEntityEvent event) {
        Player attacker = resolveAttacker(event);
        Player victim = event.getEntity() instanceof Player player ? player : null;
        double damage = event.getFinalDamage();
        if (attacker != null && plugin.isCombatLogEnabled(attacker.getUniqueId())) {
            String targetName = event.getEntity().getName();
            attacker.sendMessage(com.example.rpg.util.Text.mm("<gray>Du triffst <white>" + targetName
                + "</white> für <red>" + Math.round(damage) + "</red> Schaden."));
        }
        if (victim != null && plugin.isCombatLogEnabled(victim.getUniqueId())) {
            String sourceName = attacker != null ? attacker.getName() : event.getDamager().getName();
            victim.sendMessage(com.example.rpg.util.Text.mm("<gray>Du bekommst <red>" + Math.round(damage)
                + "</red> Schaden von <white>" + sourceName + "</white>."));
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getPersistentDataContainer()
            .has(plugin.customMobListener().mobKey(), org.bukkit.persistence.PersistentDataType.STRING)) {
            return;
        }
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }
        if (event.getEntity() instanceof Player) {
            return;
        }
        int xp = 10 + event.getEntity().getType().ordinal() % 10;
        var partyOpt = plugin.partyManager().getParty(killer.getUniqueId());
        java.util.List<Player> recipients = new java.util.ArrayList<>();
        if (partyOpt.isPresent()) {
            for (java.util.UUID memberId : partyOpt.get().members()) {
                Player member = plugin.getServer().getPlayer(memberId);
                if (member != null && member.getWorld().equals(killer.getWorld())
                    && member.getLocation().distanceSquared(killer.getLocation()) <= 30 * 30) {
                    recipients.add(member);
                }
            }
        } else {
            recipients.add(killer);
        }
        boolean split = plugin.getConfig().getBoolean("rpg.party.xpSplit", true);
        int share = split ? Math.max(1, xp / Math.max(1, recipients.size())) : xp;
        for (Player member : recipients) {
            PlayerProfile profile = plugin.playerDataManager().getProfile(member);
            profile.addXp(share);
            profile.applyAttributes(member);
        }

        LootTable table = plugin.lootManager().getTableFor(event.getEntity().getType().name());
        if (table != null) {
            for (LootEntry entry : table.entries()) {
                if (random.nextDouble() <= entry.chance()) {
                    Material material = Material.matchMaterial(entry.material());
                    if (material != null) {
                        int level = plugin.playerDataManager().getProfile(killer).level();
                        ItemStack item = plugin.itemGenerator().createRpgItem(material, entry.rarity(), level);
                        item.setAmount(entry.minAmount() + random.nextInt(Math.max(1, entry.maxAmount() - entry.minAmount() + 1)));
                        event.getDrops().add(item);
                        plugin.broadcastLoot(killer, item);
                    }
                }
            }
        } else {
            dropGenericLoot(killer, event);
        }

        for (Player member : recipients) {
            PlayerProfile profile = plugin.playerDataManager().getProfile(member);
            for (QuestProgress progress : profile.activeQuests().values()) {
                Quest quest = plugin.questManager().getQuest(progress.questId());
                if (quest == null) {
                    continue;
                }
                for (int i = 0; i < quest.steps().size(); i++) {
                    QuestStep step = quest.steps().get(i);
                    if (step.type() == QuestStepType.KILL && step.target().equalsIgnoreCase(event.getEntity().getType().name())) {
                        progress.incrementStepClamped(i, 1, step.amount());
                    }
                }
                plugin.completeQuestIfReady(member, quest, progress);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        profile.addXp(1);
        profile.applyAttributes(player);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        profile.addXp(2);
        profile.applyAttributes(player);
    }

    private Player resolveAttacker(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            return player;
        }
        if (event.getDamager() instanceof org.bukkit.entity.Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Player player) {
                return player;
            }
        }
        return null;
    }

    private void dropGenericLoot(Player killer, EntityDeathEvent event) {
        PlayerProfile profile = plugin.playerDataManager().getProfile(killer);
        int level = Math.max(1, profile.level());
        int gold = 5 + random.nextInt(6) + level;
        profile.setGold(profile.gold() + gold);
        killer.sendMessage(com.example.rpg.util.Text.mm("<gold>+ " + gold + " Gold"));

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
}

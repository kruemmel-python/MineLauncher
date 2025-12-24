package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.LootEntry;
import com.example.rpg.model.LootTable;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.Quest;
import com.example.rpg.model.QuestProgress;
import com.example.rpg.model.QuestStep;
import com.example.rpg.model.QuestStepType;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class CombatListener implements Listener {
    private final RPGPlugin plugin;
    private final Random random = new Random();

    public CombatListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }
        PlayerProfile profile = plugin.playerDataManager().getProfile(killer);
        int xp = 10 + event.getEntity().getType().ordinal() % 10;
        profile.addXp(xp);
        profile.applyAttributes(killer);

        LootTable table = plugin.lootManager().getTableFor(event.getEntity().getType().name());
        if (table != null) {
            for (LootEntry entry : table.entries()) {
                if (random.nextDouble() <= entry.chance()) {
                    Material material = Material.matchMaterial(entry.material());
                    if (material != null) {
                        ItemStack item = plugin.itemGenerator().createRpgItem(material, entry.rarity(), profile.level());
                        item.setAmount(entry.minAmount() + random.nextInt(Math.max(1, entry.maxAmount() - entry.minAmount() + 1)));
                        event.getDrops().add(item);
                    }
                }
            }
        }

        for (QuestProgress progress : profile.activeQuests().values()) {
            Quest quest = plugin.questManager().getQuest(progress.questId());
            if (quest == null) {
                continue;
            }
            for (int i = 0; i < quest.steps().size(); i++) {
                QuestStep step = quest.steps().get(i);
                if (step.type() == QuestStepType.KILL && step.target().equalsIgnoreCase(event.getEntity().getType().name())) {
                    progress.incrementStep(i, 1);
                }
            }
            plugin.completeQuestIfReady(killer, quest, progress);
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
}

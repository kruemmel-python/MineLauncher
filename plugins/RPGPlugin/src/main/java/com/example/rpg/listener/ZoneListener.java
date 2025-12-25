package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.Quest;
import com.example.rpg.model.QuestProgress;
import com.example.rpg.model.QuestStep;
import com.example.rpg.model.QuestStepType;
import com.example.rpg.model.Zone;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ZoneListener implements Listener {
    private final RPGPlugin plugin;
    private final Map<UUID, String> lastZone = new HashMap<>();

    public ZoneListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Zone zone = plugin.zoneManager().getZoneAt(event.getTo());
        String zoneId = zone != null ? zone.id() : null;
        String previous = lastZone.get(player.getUniqueId());
        if ((zoneId == null && previous != null) || (zoneId != null && !zoneId.equals(previous))) {
            lastZone.put(player.getUniqueId(), zoneId);
            if (zone != null) {
                player.sendMessage(ChatColor.AQUA + "Zone betreten: " + zone.name());
                if (zone.slowMultiplier() < 1.0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 0));
                }
                handleExploreQuests(player, zone);
            } else {
                player.sendMessage(ChatColor.GRAY + "Zone verlassen.");
            }
        }
    }

    private void handleExploreQuests(Player player, Zone zone) {
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        for (QuestProgress progress : profile.activeQuests().values()) {
            Quest quest = plugin.questManager().getQuest(progress.questId());
            if (quest == null) {
                continue;
            }
            for (int i = 0; i < quest.steps().size(); i++) {
                QuestStep step = quest.steps().get(i);
                if (step.type() == QuestStepType.EXPLORE && step.target().equalsIgnoreCase(zone.id())) {
                    progress.incrementStepClamped(i, 1, step.amount());
                }
            }
            plugin.completeQuestIfReady(player, quest, progress);
        }
    }
}

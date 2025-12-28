package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.gui.BehaviorTreeEditorGui;
import com.example.rpg.gui.GuiHolders;
import com.example.rpg.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class BehaviorEditorListener implements Listener {
    private final RPGPlugin plugin;
    private final BehaviorTreeEditorGui gui;

    public BehaviorEditorListener(RPGPlugin plugin, BehaviorTreeEditorGui gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!(event.getInventory().getHolder() instanceof GuiHolders.BehaviorTreeEditorHolder holder)) {
            return;
        }
        event.setCancelled(true);
        String treeName = holder.treeName();
        switch (event.getSlot()) {
            case 10 -> promptEmergency(player, treeName);
            case 12 -> promptRanged(player, treeName);
            case 14 -> {
                plugin.behaviorTreeManager().addTemplate(treeName, Map.of("type", "melee_attack"));
                player.sendMessage(Text.mm("<green>Nahkampf hinzugefügt."));
                gui.open(player, treeName);
            }
            case 16 -> {
                plugin.behaviorTreeManager().resetTree(treeName);
                player.sendMessage(Text.mm("<yellow>Behavior Tree zurückgesetzt."));
                gui.open(player, treeName);
            }
            case 20 -> {
                plugin.behaviorTreeManager().addTemplate(treeName, buildAggroSearchTemplate());
                player.sendMessage(Text.mm("<green>Aggro/Suche-Vorlage hinzugefügt."));
                gui.open(player, treeName);
            }
            default -> {
            }
        }
    }

    private Map<String, Object> buildAggroSearchTemplate() {
        List<Map<String, Object>> children = new ArrayList<>();
        List<Map<String, Object>> aggroChildren = new ArrayList<>();
        aggroChildren.add(Map.of("type", "select_target_by_threat", "maxRange", 32.0));
        aggroChildren.add(Map.of("type", "has_los", "maxDistance", 32.0));
        aggroChildren.add(Map.of("type", "melee_attack"));
        children.add(Map.of("type", "sequence", "children", aggroChildren));
        List<Map<String, Object>> searchChildren = new ArrayList<>();
        searchChildren.add(Map.of("type", "remember_last_seen", "maxDistance", 32.0));
        searchChildren.add(Map.of("type", "search_last_seen", "arriveDistance", 2.0, "searchRadius", 12.0, "searchTicks", 80));
        children.add(Map.of("type", "sequence", "children", searchChildren));
        return Map.of("type", "memory_selector", "children", children);
    }

    private void promptEmergency(Player player, String treeName) {
        plugin.promptManager().prompt(player, Text.mm("<gray>Notfall-Heilung: <threshold> <skillId> <healAmount>"),
            input -> {
                String[] parts = input.split("\\s+");
                if (parts.length < 3) {
                    player.sendMessage(Text.mm("<red>Format: <threshold> <skillId> <healAmount>"));
                    return;
                }
                double threshold = parseDouble(parts[0], 0.2);
                String skillId = parts[1];
                double heal = parseDouble(parts[2], 6);
                List<Map<String, Object>> children = new ArrayList<>();
                children.add(Map.of("type", "health_below", "threshold", threshold));
                children.add(Map.of("type", "cast_skill", "skill", skillId));
                children.add(Map.of("type", "heal_self", "amount", heal));
                plugin.behaviorTreeManager().addTemplate(treeName, Map.of("type", "sequence", "children", children));
                player.sendMessage(Text.mm("<green>Notfall-Sequenz hinzugefügt."));
                gui.open(player, treeName);
            });
    }

    private void promptRanged(Player player, String treeName) {
        plugin.promptManager().prompt(player, Text.mm("<gray>Fernkampf: <distance> <skillId>"), input -> {
            String[] parts = input.split("\\s+");
            if (parts.length < 2) {
                player.sendMessage(Text.mm("<red>Format: <distance> <skillId>"));
                return;
            }
            double distance = parseDouble(parts[0], 10);
            String skillId = parts[1];
            List<Map<String, Object>> children = new ArrayList<>();
            children.add(Map.of("type", "target_distance_above", "distance", distance));
            children.add(Map.of("type", "cast_skill", "skill", skillId));
            plugin.behaviorTreeManager().addTemplate(treeName, Map.of("type", "sequence", "children", children));
            player.sendMessage(Text.mm("<green>Fernkampf-Sequenz hinzugefügt."));
            gui.open(player, treeName);
        });
    }

    private double parseDouble(String input, double fallback) {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}

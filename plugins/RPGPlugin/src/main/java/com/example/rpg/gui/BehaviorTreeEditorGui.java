package com.example.rpg.gui;

import com.example.rpg.RPGPlugin;
import com.example.rpg.util.ItemBuilder;
import com.example.rpg.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class BehaviorTreeEditorGui {
    private final RPGPlugin plugin;

    public BehaviorTreeEditorGui(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, String treeName) {
        Inventory inventory = Bukkit.createInventory(new GuiHolders.BehaviorTreeEditorHolder(treeName), 27,
            Component.text("Behavior Editor: " + treeName));

        inventory.setItem(10, new ItemBuilder(Material.TOTEM_OF_UNDYING)
            .name(Text.mm("<gold>Notfall-Heilung"))
            .loreLine(Text.mm("<gray>health_below + shield_wall + heal_self"))
            .loreLine(Text.mm("<yellow>Klick, um Parameter einzugeben"))
            .build());

        inventory.setItem(12, new ItemBuilder(Material.BLAZE_ROD)
            .name(Text.mm("<red>Fernkampf-Phase"))
            .loreLine(Text.mm("<gray>target_distance_above + cast_skill"))
            .loreLine(Text.mm("<yellow>Klick, um Parameter einzugeben"))
            .build());

        inventory.setItem(14, new ItemBuilder(Material.IRON_SWORD)
            .name(Text.mm("<green>Nahkampf"))
            .loreLine(Text.mm("<gray>melee_attack"))
            .loreLine(Text.mm("<yellow>Klick, um hinzuzufügen"))
            .build());

        inventory.setItem(16, new ItemBuilder(Material.BARRIER)
            .name(Text.mm("<red>Zurücksetzen"))
            .loreLine(Text.mm("<gray>Leert den Baum"))
            .build());

        player.openInventory(inventory);
    }
}

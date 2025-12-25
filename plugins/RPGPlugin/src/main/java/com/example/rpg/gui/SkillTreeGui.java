package com.example.rpg.gui;

import com.example.rpg.RPGPlugin;
import com.example.rpg.manager.SkillTreeManager;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.Skill;
import com.example.rpg.util.ItemBuilder;
import com.example.rpg.util.Text;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class SkillTreeGui {
    private final RPGPlugin plugin;

    public SkillTreeGui(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.SkillTreeHolder(), 54, Component.text("Skillbaum"));
        SkillTreeManager treeManager = plugin.skillTreeManager();
        treeManager.rebuild();
        Map<String, Integer> slots = layout(treeManager);
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        for (Map.Entry<String, Integer> entry : slots.entrySet()) {
            Skill skill = plugin.skillManager().getSkill(entry.getKey());
            if (skill == null) {
                continue;
            }
            boolean learned = profile.learnedSkills().containsKey(skill.id());
            boolean unlocked = skill.requiredSkill() == null
                || profile.learnedSkills().containsKey(skill.requiredSkill());
            Material material = learned ? Material.ENCHANTED_BOOK : unlocked ? Material.BOOK : Material.BARRIER;
            ItemBuilder builder = new ItemBuilder(material)
                .name(Text.mm(learned ? "<green>" + skill.name() : unlocked ? "<yellow>" + skill.name() : "<red>" + skill.name()))
                .loreLine(Text.mm("<gray>Mana: <white>" + skill.manaCost()))
                .loreLine(Text.mm("<gray>Cooldown: <white>" + skill.cooldown() + "s"))
                .loreLine(Text.mm("<gray>Voraussetzung: <white>" + (skill.requiredSkill() == null ? "Keine" : skill.requiredSkill())));
            if (learned) {
                builder.loreLine(Text.mm("<green>Bereits gelernt"));
            } else if (unlocked) {
                builder.loreLine(Text.mm("<yellow>Klick zum Lernen"));
            } else {
                builder.loreLine(Text.mm("<red>Gesperrt"));
            }
            ItemStack item = builder.build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(plugin.skillKey(), PersistentDataType.STRING, skill.id());
            item.setItemMeta(meta);
            inv.setItem(entry.getValue(), item);
        }
        for (int slot : slots.values()) {
            int linkSlot = slot + 1;
            if (linkSlot < inv.getSize() && inv.getItem(linkSlot) == null) {
                inv.setItem(linkSlot, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(Component.text(" ")).build());
            }
        }
        player.openInventory(inv);
    }

    private Map<String, Integer> layout(SkillTreeManager treeManager) {
        Map<String, Integer> slots = new HashMap<>();
        int[] depthIndex = new int[6];
        ArrayDeque<SkillTreeManager.SkillNode> queue = new ArrayDeque<>(treeManager.roots());
        while (!queue.isEmpty()) {
            SkillTreeManager.SkillNode node = queue.poll();
            int depth = depth(node);
            int row = Math.min(depth, 5);
            int col = depthIndex[row]++;
            int slot = row * 9 + Math.min(col * 2, 8);
            slots.put(node.skill().id(), slot);
            for (SkillTreeManager.SkillNode child : node.children()) {
                queue.add(child);
            }
        }
        return slots;
    }

    private int depth(SkillTreeManager.SkillNode node) {
        int depth = 0;
        SkillTreeManager.SkillNode current = node;
        while (current.parent() != null) {
            depth++;
            current = current.parent();
        }
        return depth;
    }
}

package com.example.rpg.gui;

import com.example.rpg.manager.ClassManager;
import com.example.rpg.manager.FactionManager;
import com.example.rpg.manager.PlayerDataManager;
import com.example.rpg.manager.QuestManager;
import com.example.rpg.manager.SkillManager;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.Quest;
import com.example.rpg.util.ItemBuilder;
import com.example.rpg.util.Text;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class GuiManager {
    private final PlayerDataManager playerDataManager;
    private final QuestManager questManager;
    private final SkillManager skillManager;
    private final ClassManager classManager;
    private final FactionManager factionManager;
    private final NamespacedKey questKey;
    private final NamespacedKey skillKey;

    public GuiManager(PlayerDataManager playerDataManager, QuestManager questManager, SkillManager skillManager,
                      ClassManager classManager, FactionManager factionManager, NamespacedKey questKey, NamespacedKey skillKey) {
        this.playerDataManager = playerDataManager;
        this.questManager = questManager;
        this.skillManager = skillManager;
        this.classManager = classManager;
        this.factionManager = factionManager;
        this.questKey = questKey;
        this.skillKey = skillKey;
    }

    public void openPlayerMenu(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.PlayerMenuHolder(), 27, Component.text("RPG Menü"));
        PlayerProfile profile = playerDataManager.getProfile(player);

        inv.setItem(10, new ItemBuilder(Material.PLAYER_HEAD)
            .name(Text.mm("<gold>Charakter"))
            .loreLine(Text.mm("<gray>Level: <white>" + profile.level()))
            .loreLine(Text.mm("<gray>XP: <white>" + profile.xp() + "/" + profile.xpNeeded()))
            .loreLine(Text.mm("<gray>Klasse: <white>" + resolveClassName(profile.classId())))
            .build());

        inv.setItem(12, new ItemBuilder(Material.NETHER_STAR)
            .name(Text.mm("<aqua>Skills"))
            .loreLine(Text.mm("<gray>Skillpunkte: <white>" + profile.skillPoints()))
            .build());

        inv.setItem(14, new ItemBuilder(Material.WRITABLE_BOOK)
            .name(Text.mm("<green>Quests"))
            .loreLine(Text.mm("<gray>Aktiv: <white>" + profile.activeQuests().size()))
            .build());

        inv.setItem(16, new ItemBuilder(Material.EMERALD)
            .name(Text.mm("<yellow>Fraktionen"))
            .loreLine(Text.mm("<gray>Ruf verwalten"))
            .loreLine(Text.mm("<gray>Aktiv: <white>" + profile.factionRep().size() + "/" + factionManager.factions().size()))
            .build());

        player.openInventory(inv);
    }

    public void openAdminMenu(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.AdminMenuHolder(), 27, Component.text("RPG Admin"));
        inv.setItem(10, new ItemBuilder(Material.COMPASS)
            .name(Text.mm("<gold>Zonen-Editor"))
            .loreLine(Text.mm("<gray>Regionen verwalten"))
            .build());
        inv.setItem(11, new ItemBuilder(Material.VILLAGER_SPAWN_EGG)
            .name(Text.mm("<green>NPC-Editor"))
            .loreLine(Text.mm("<gray>NPCs platzieren"))
            .build());
        inv.setItem(12, new ItemBuilder(Material.WRITABLE_BOOK)
            .name(Text.mm("<aqua>Quest-Editor"))
            .loreLine(Text.mm("<gray>Quests erstellen"))
            .build());
        inv.setItem(13, new ItemBuilder(Material.CHEST)
            .name(Text.mm("<yellow>Loot-Tabellen"))
            .loreLine(Text.mm("<gray>Loot konfigurieren"))
            .build());
        inv.setItem(14, new ItemBuilder(Material.BLAZE_POWDER)
            .name(Text.mm("<light_purple>Skills & Klassen"))
            .loreLine(Text.mm("<gray>Skills verwalten"))
            .build());
        inv.setItem(15, new ItemBuilder(Material.REDSTONE)
            .name(Text.mm("<red>Debug Overlay"))
            .loreLine(Text.mm("<gray>Region/Quest Debug"))
            .build());
        player.openInventory(inv);
    }

    public void openQuestList(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.QuestListHolder(), 27, Component.text("Quests"));
        int slot = 0;
        for (Quest quest : questManager.quests().values()) {
            if (slot >= inv.getSize()) {
                break;
            }
            ItemStack item = new ItemBuilder(Material.BOOK)
                .name(Text.mm("<green>" + quest.name()))
                .loreLine(Text.mm("<gray>" + quest.description()))
                .loreLine(Text.mm("<gray>Min Level: <white>" + quest.minLevel()))
                .loreLine(Text.mm("<yellow>Klicke zum Annehmen"))
                .build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(questKey, PersistentDataType.STRING, quest.id());
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        player.openInventory(inv);
    }

    public void openSkillList(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.SkillListHolder(), 27, Component.text("Skills"));
        PlayerProfile profile = playerDataManager.getProfile(player);
        int slot = 0;
        for (var entry : skillManager.skills().entrySet()) {
            if (slot >= inv.getSize()) {
                break;
            }
            String id = entry.getKey();
            var skill = entry.getValue();
            List<Component> lore = new ArrayList<>();
            lore.add(Text.mm("<gray>Kategorie: <white>" + skill.category()));
            lore.add(Text.mm("<gray>Typ: <white>" + skill.type()));
            lore.add(Text.mm("<gray>Cooldown: <white>" + skill.cooldown() + "s"));
            lore.add(Text.mm("<gray>Mana: <white>" + skill.manaCost()));
            lore.add(Text.mm("<gray>Rang: <white>" + profile.learnedSkills().getOrDefault(id, 0)));
            if (skill.requiredSkill() != null) {
                lore.add(Text.mm("<gray>Voraussetzung: <white>" + skill.requiredSkill()));
            }
            lore.add(Text.mm("<yellow>Klick: Skill lernen"));
            ItemStack item = new ItemBuilder(Material.ENCHANTED_BOOK)
                .name(Text.mm("<aqua>" + skill.name()))
                .loreLines(lore)
                .build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(skillKey, PersistentDataType.STRING, id);
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        player.openInventory(inv);
    }

    private String resolveClassName(String classId) {
        if (classId == null) {
            return "Keine";
        }
        var definition = classManager.getClass(classId);
        return definition != null ? definition.name() : classId;
    }
}

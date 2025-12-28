package com.example.rpg.gui;

import com.example.rpg.RPGPlugin;
import com.example.rpg.manager.ClassManager;
import com.example.rpg.manager.FactionManager;
import com.example.rpg.manager.PlayerDataManager;
import com.example.rpg.manager.QuestManager;
import com.example.rpg.manager.WorldEventManager;
import com.example.rpg.manager.BuildingManager;
import com.example.rpg.manager.SkillManager;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.Quest;
import com.example.rpg.model.QuestProgress;
import com.example.rpg.model.QuestStep;
import com.example.rpg.model.Rarity;
import com.example.rpg.model.ShopDefinition;
import com.example.rpg.model.ShopItem;
import com.example.rpg.model.BuildingCategory;
import com.example.rpg.model.BuildingDefinition;
import com.example.rpg.model.ClassDefinition;
import com.example.rpg.model.LootTable;
import com.example.rpg.model.Npc;
import com.example.rpg.model.Skill;
import com.example.rpg.util.ItemGenerator;
import com.example.rpg.util.ItemBuilder;
import com.example.rpg.util.Text;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private final RPGPlugin plugin;
    private final PlayerDataManager playerDataManager;
    private final QuestManager questManager;
    private final WorldEventManager worldEventManager;
    private final SkillManager skillManager;
    private final ClassManager classManager;
    private final FactionManager factionManager;
    private final BuildingManager buildingManager;
    private final com.example.rpg.permissions.PermissionService permissionService;
    private final com.example.rpg.manager.EnchantManager enchantManager;
    private final com.example.rpg.manager.ItemStatManager itemStatManager;
    private final ItemGenerator itemGenerator;
    private final NamespacedKey questKey;
    private final NamespacedKey skillKey;
    private final NamespacedKey buildingKey;
    private final NamespacedKey buildingCategoryKey;
    private final NamespacedKey zoneKey;
    private final NamespacedKey npcKey;
    private final NamespacedKey npcTemplateKey;
    private final NamespacedKey lootKey;
    private final NamespacedKey classKey;
    private final NamespacedKey permRoleKey;
    private final NamespacedKey permPlayerKey;
    private final NamespacedKey permNodeKey;
    private final NamespacedKey permActionKey;
    private final NamespacedKey enchantRecipeKey;

    public GuiManager(RPGPlugin plugin, PlayerDataManager playerDataManager, QuestManager questManager, WorldEventManager worldEventManager,
                      SkillManager skillManager, ClassManager classManager, FactionManager factionManager,
                      BuildingManager buildingManager, com.example.rpg.permissions.PermissionService permissionService,
                      com.example.rpg.manager.EnchantManager enchantManager, com.example.rpg.manager.ItemStatManager itemStatManager,
                      ItemGenerator itemGenerator,
                      NamespacedKey questKey, NamespacedKey skillKey, NamespacedKey buildingKey, NamespacedKey buildingCategoryKey,
                      NamespacedKey zoneKey, NamespacedKey npcKey, NamespacedKey npcTemplateKey, NamespacedKey lootKey, NamespacedKey classKey,
                      NamespacedKey permRoleKey, NamespacedKey permPlayerKey, NamespacedKey permNodeKey, NamespacedKey permActionKey,
                      NamespacedKey enchantRecipeKey) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.questManager = questManager;
        this.worldEventManager = worldEventManager;
        this.skillManager = skillManager;
        this.classManager = classManager;
        this.factionManager = factionManager;
        this.buildingManager = buildingManager;
        this.permissionService = permissionService;
        this.enchantManager = enchantManager;
        this.itemStatManager = itemStatManager;
        this.itemGenerator = itemGenerator;
        this.questKey = questKey;
        this.skillKey = skillKey;
        this.buildingKey = buildingKey;
        this.buildingCategoryKey = buildingCategoryKey;
        this.zoneKey = zoneKey;
        this.npcKey = npcKey;
        this.npcTemplateKey = npcTemplateKey;
        this.lootKey = lootKey;
        this.classKey = classKey;
        this.permRoleKey = permRoleKey;
        this.permPlayerKey = permPlayerKey;
        this.permNodeKey = permNodeKey;
        this.permActionKey = permActionKey;
        this.enchantRecipeKey = enchantRecipeKey;
    }

    public void openPlayerMenu(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.PlayerMenuHolder(), 27, Component.text("RPG Menü"));
        PlayerProfile profile = playerDataManager.getProfile(player);

        Map<com.example.rpg.model.RPGStat, Integer> totalStats = profile.totalStats(player, itemStatManager, classManager);
        inv.setItem(10, new ItemBuilder(Material.PLAYER_HEAD)
            .name(Text.mm("<gold>Charakter"))
            .loreLine(Text.mm("<gray>Level: <white>" + profile.level()))
            .loreLine(Text.mm("<gray>XP: <white>" + profile.xp() + "/" + profile.xpNeeded()))
            .loreLine(Text.mm("<gray>Klasse: <white>" + resolveClassName(profile.classId())))
            .loreLine(Text.mm("<gray>Gelernte Skills: <white>" + profile.learnedSkills().size()))
            .loreLine(Text.mm("<gray>Skillpunkte: <white>" + profile.skillPoints()))
            .loreLine(Text.mm("<gray>Stärke: <white>" + totalStats.getOrDefault(com.example.rpg.model.RPGStat.STRENGTH, 0)))
            .loreLine(Text.mm("<gray>Geschick: <white>" + totalStats.getOrDefault(com.example.rpg.model.RPGStat.DEXTERITY, 0)))
            .loreLine(Text.mm("<gray>Konstitution: <white>" + totalStats.getOrDefault(com.example.rpg.model.RPGStat.CONSTITUTION, 0)))
            .loreLine(Text.mm("<gray>Intelligenz: <white>" + totalStats.getOrDefault(com.example.rpg.model.RPGStat.INTELLIGENCE, 0)))
            .build());

        inv.setItem(12, new ItemBuilder(Material.NETHER_STAR)
            .name(Text.mm("<aqua>Skills"))
            .loreLine(Text.mm("<gray>Skillpunkte: <white>" + profile.skillPoints()))
            .build());

        inv.setItem(14, new ItemBuilder(Material.WRITABLE_BOOK)
            .name(Text.mm("<green>Quest-Log"))
            .loreLine(Text.mm("<gray>Aktiv: <white>" + profile.activeQuests().size()))
            .loreLine(Text.mm("<gray>Details zu angenommenen Quests"))
            .build());

        inv.setItem(15, new ItemBuilder(Material.BOOK)
            .name(Text.mm("<yellow>Quest-Angebote"))
            .loreLine(Text.mm("<gray>Neue Quests ansehen"))
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
        inv.setItem(16, new ItemBuilder(Material.BRICKS)
            .name(Text.mm("<gold>Bau-Manager"))
            .loreLine(Text.mm("<gray>Gebäude platzieren"))
            .build());
        inv.setItem(17, new ItemBuilder(Material.NAME_TAG)
            .name(Text.mm("<aqua>Permissions"))
            .loreLine(Text.mm("<gray>Rollen & Rechte verwalten"))
            .build());
        inv.setItem(18, new ItemBuilder(Material.DEEPSLATE_BRICKS)
            .name(Text.mm("<gold>Dungeons"))
            .loreLine(Text.mm("<gray>Dungeon-Generator"))
            .build());
        inv.setItem(19, new ItemBuilder(Material.GOLDEN_PICKAXE)
            .name(Text.mm("<gold>Worldbuilding"))
            .loreLine(Text.mm("<gray>Wand & Area-Fill"))
            .build());
        player.openInventory(inv);
    }

    public void openDungeonAdmin(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.DungeonAdminHolder(), 27, Component.text("Dungeon Admin"));
        boolean jigsawEnabled = plugin.getConfig().getBoolean("dungeon.jigsaw.enabled", false);
        boolean wfcFillEnabled = plugin.getConfig().getBoolean("dungeon.jigsaw.wfcFill", false);
        inv.setItem(7, new ItemBuilder(Material.WRITABLE_BOOK)
            .name(Text.mm("<gold>Schematic speichern"))
            .loreLine(Text.mm("<gray>Wand-Auswahl sichern"))
            .loreLine(Text.mm("<yellow>Klick: Pfad eingeben"))
            .build());
        inv.setItem(9, new ItemBuilder(Material.STRUCTURE_BLOCK)
            .name(Text.mm("<gold>Schematic platzieren"))
            .loreLine(Text.mm("<gray>Einzelnes .schem setzen"))
            .loreLine(Text.mm("<yellow>Klick: Dateiname eingeben"))
            .build());
        inv.setItem(11, new ItemBuilder(Material.JIGSAW)
            .name(Text.mm("<gold>Jigsaw Modus"))
            .loreLine(Text.mm("<gray>Status: <white>" + (jigsawEnabled ? "Aktiv" : "Inaktiv") + "</white>"))
            .loreLine(Text.mm("<yellow>Klick: umschalten"))
            .build());
        inv.setItem(13, new ItemBuilder(Material.MOSSY_COBBLESTONE)
            .name(Text.mm("<green>WFC Raum-Füllung"))
            .loreLine(Text.mm("<gray>Status: <white>" + (wfcFillEnabled ? "Aktiv" : "Inaktiv") + "</white>"))
            .loreLine(Text.mm("<yellow>Klick: umschalten"))
            .build());
        inv.setItem(15, new ItemBuilder(Material.NETHER_STAR)
            .name(Text.mm("<aqua>Dungeon generieren"))
            .loreLine(Text.mm("<gray>Erstellt eine neue Instanz"))
            .loreLine(Text.mm("<yellow>Klick: Theme angeben"))
            .build());
        inv.setItem(17, new ItemBuilder(Material.BOOK)
            .name(Text.mm("<yellow>Setup-Info"))
            .loreLine(Text.mm("<gray>Ordner & Socket-Hilfe"))
            .loreLine(Text.mm("<yellow>Klick: Chat-Info"))
            .build());
        inv.setItem(22, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Zurück"))
            .loreLine(Text.mm("<gray>Admin-Menü"))
            .build());
        player.openInventory(inv);
    }

    public void openWorldBuildingMenu(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.WorldBuildingHolder(), 27, Component.text("Worldbuilding"));
        inv.setItem(11, new ItemBuilder(Material.STICK)
            .name(Text.mm("<gold>Wand Tool"))
            .loreLine(Text.mm("<gray>Pos1/Pos2 setzen"))
            .build());
        inv.setItem(13, new ItemBuilder(Material.BRICKS)
            .name(Text.mm("<green>Bereich füllen"))
            .loreLine(Text.mm("<gray>Block auswählen"))
            .build());
        inv.setItem(22, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Zurück"))
            .loreLine(Text.mm("<gray>Admin-Menü"))
            .build());
        player.openInventory(inv);
    }

    public void openBlockFillMenu(Player player, int page) {
        List<Material> materials = fillMaterials();
        int pageSize = 45;
        int maxPage = materials.isEmpty() ? 0 : (materials.size() - 1) / pageSize;
        int safePage = Math.min(Math.max(page, 0), maxPage);
        Inventory inv = Bukkit.createInventory(new GuiHolders.BlockFillHolder(safePage), 54, Component.text("Block auswählen"));
        int startIndex = safePage * pageSize;
        int slot = 0;
        for (int i = startIndex; i < materials.size() && slot < pageSize; i++) {
            Material material = materials.get(i);
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(Text.mm("<yellow>" + material.name()));
                meta.lore(List.of(Text.mm("<gray>Klick: Bereich füllen")));
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }
        inv.setItem(45, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Vorherige Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        inv.setItem(49, new ItemBuilder(Material.BARRIER)
            .name(Text.mm("<red>Zurück"))
            .loreLine(Text.mm("<gray>Worldbuilding"))
            .build());
        inv.setItem(53, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Nächste Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        player.openInventory(inv);
    }

    private List<Material> fillMaterials() {
        return Arrays.stream(Material.values())
            .filter(Material::isBlock)
            .sorted(Comparator.comparing(Material::name))
            .toList();
    }

    public void openZoneEditor(Player player) {
        openZoneEditor(player, 0);
    }

    public void openZoneEditor(Player player, int page) {
        var zones = new java.util.ArrayList<>(com.example.rpg.RPGPlugin.getPlugin(com.example.rpg.RPGPlugin.class)
            .zoneManager().zones().values());
        zones.sort(java.util.Comparator.comparing(com.example.rpg.model.Zone::id));
        int pageSize = 45;
        int maxPage = zones.isEmpty() ? 0 : (zones.size() - 1) / pageSize;
        int safePage = Math.min(Math.max(page, 0), maxPage);
        Inventory inv = Bukkit.createInventory(new GuiHolders.ZoneEditorHolder(safePage), 54, Component.text("Zonen-Editor"));
        int slot = 0;
        int startIndex = safePage * pageSize;
        for (int i = startIndex; i < zones.size() && slot < pageSize; i++) {
            var zone = zones.get(i);
            ItemStack item = new ItemBuilder(Material.MAP)
                .name(Text.mm("<gold>" + zone.name()))
                .loreLine(Text.mm("<gray>ID: <white>" + zone.id()))
                .loreLine(Text.mm("<gray>World: <white>" + zone.world()))
                .loreLine(Text.mm("<gray>Level: <white>" + zone.minLevel() + "-" + zone.maxLevel()))
                .loreLine(Text.mm("<gray>Mod: <white>" + zone.slowMultiplier() + " / " + zone.damageMultiplier()))
                .loreLine(Text.mm("<yellow>Klick: bearbeiten"))
                .loreLine(Text.mm("<red>Rechtsklick: löschen"))
                .build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(zoneKey, PersistentDataType.STRING, zone.id());
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        inv.setItem(45, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Vorherige Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        inv.setItem(49, new ItemBuilder(Material.EMERALD_BLOCK)
            .name(Text.mm("<green>Zone erstellen"))
            .loreLine(Text.mm("<gray>Nutze die Wand (Pos1/Pos2)"))
            .build());
        inv.setItem(50, new ItemBuilder(Material.PAPER)
            .name(Text.mm("<gold>Seite " + (safePage + 1) + "/" + (maxPage + 1)))
            .build());
        inv.setItem(53, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Nächste Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        player.openInventory(inv);
    }

    public void openNpcEditor(Player player) {
        openNpcEditor(player, 0);
    }

    public void openNpcEditor(Player player, int page) {
        var npcs = new java.util.ArrayList<>(com.example.rpg.RPGPlugin.getPlugin(com.example.rpg.RPGPlugin.class)
            .npcManager().npcs().values());
        npcs.sort(java.util.Comparator.comparing(Npc::id));
        int pageSize = 36;
        int maxPage = npcs.isEmpty() ? 0 : (npcs.size() - 1) / pageSize;
        int safePage = Math.min(Math.max(page, 0), maxPage);
        Inventory inv = Bukkit.createInventory(new GuiHolders.NpcEditorHolder(safePage), 54, Component.text("NPC-Editor"));
        int slot = 0;
        int startIndex = safePage * pageSize;
        for (int i = startIndex; i < npcs.size() && slot < pageSize; i++) {
            var npc = npcs.get(i);
            ItemStack item = new ItemBuilder(Material.VILLAGER_SPAWN_EGG)
                .name(Text.mm("<green>" + npc.name()))
                .loreLine(Text.mm("<gray>ID: <white>" + npc.id()))
                .loreLine(Text.mm("<gray>Rolle: <white>" + npc.role()))
                .loreLine(Text.mm("<gray>Quest: <white>" + (npc.questLink() != null ? npc.questLink() : "-")))
                .loreLine(Text.mm("<gray>Shop: <white>" + (npc.shopId() != null ? npc.shopId() : "-")))
                .loreLine(Text.mm("<yellow>Klick: bearbeiten"))
                .loreLine(Text.mm("<red>Rechtsklick: löschen"))
                .build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(npcKey, PersistentDataType.STRING, npc.id());
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        inv.setItem(36, buildNpcTemplate(Material.WRITABLE_BOOK, "<gold>Questgiver",
            com.example.rpg.model.NpcRole.QUESTGIVER, "<gray>Quest-NPC erstellen"));
        inv.setItem(37, buildNpcTemplate(Material.CHEST, "<yellow>Shop (shops.yml)",
            com.example.rpg.model.NpcRole.VENDOR, "<gray>Shop-ID wird abgefragt"));
        inv.setItem(38, buildNpcTemplate(Material.IRON_SWORD, "<red>Waffenhändler",
            com.example.rpg.model.NpcRole.WEAPON_VENDOR, "<gray>Alle Waffen kaufen/verkaufen"));
        inv.setItem(39, buildNpcTemplate(Material.DIAMOND_CHESTPLATE, "<blue>Rüstungshändler",
            com.example.rpg.model.NpcRole.ARMOR_VENDOR, "<gray>Alle Rüstungen kaufen/verkaufen"));
        inv.setItem(40, buildNpcTemplate(Material.APPLE, "<green>Gegenstandshändler",
            com.example.rpg.model.NpcRole.ITEM_VENDOR, "<gray>Items & Verbrauchsgüter"));
        inv.setItem(41, buildNpcTemplate(Material.EMERALD, "<aqua>Rohstoffhändler",
            com.example.rpg.model.NpcRole.RESOURCE_VENDOR, "<gray>Erze & Rohstoffe"));
        inv.setItem(45, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Vorherige Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        inv.setItem(49, new ItemBuilder(Material.EMERALD_BLOCK)
            .name(Text.mm("<green>NPC erstellen"))
            .loreLine(Text.mm("<gray>Erstellt an deiner Position"))
            .build());
        inv.setItem(50, new ItemBuilder(Material.PAPER)
            .name(Text.mm("<gold>Seite " + (safePage + 1) + "/" + (maxPage + 1)))
            .build());
        inv.setItem(53, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Nächste Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        player.openInventory(inv);
    }

    private ItemStack buildNpcTemplate(Material material, String name, com.example.rpg.model.NpcRole role, String lore) {
        ItemStack item = new ItemBuilder(material)
            .name(Text.mm(name))
            .loreLine(Text.mm(lore))
            .loreLine(Text.mm("<yellow>Klick: Vorlage nutzen"))
            .build();
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(npcTemplateKey, PersistentDataType.STRING, role.name());
        item.setItemMeta(meta);
        return item;
    }

    public void openQuestEditor(Player player) {
        openQuestEditor(player, 0);
    }

    public void openQuestEditor(Player player, int page) {
        var quests = new java.util.ArrayList<>(questManager.quests().values());
        quests.sort(java.util.Comparator.comparing(Quest::id));
        int pageSize = 45;
        int maxPage = quests.isEmpty() ? 0 : (quests.size() - 1) / pageSize;
        int safePage = Math.min(Math.max(page, 0), maxPage);
        Inventory inv = Bukkit.createInventory(new GuiHolders.QuestEditorHolder(safePage), 54, Component.text("Quest-Editor"));
        int slot = 0;
        int startIndex = safePage * pageSize;
        for (int i = startIndex; i < quests.size() && slot < pageSize; i++) {
            Quest quest = quests.get(i);
            ItemStack item = new ItemBuilder(Material.BOOK)
                .name(Text.mm("<aqua>" + quest.name()))
                .loreLine(Text.mm("<gray>ID: <white>" + quest.id()))
                .loreLine(Text.mm("<gray>Level: <white>" + quest.minLevel()))
                .loreLine(Text.mm("<gray>Repeatable: <white>" + quest.repeatable()))
                .loreLine(Text.mm("<yellow>Klick: bearbeiten"))
                .loreLine(Text.mm("<red>Rechtsklick: löschen"))
                .build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(questKey, PersistentDataType.STRING, quest.id());
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        inv.setItem(45, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Vorherige Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        inv.setItem(49, new ItemBuilder(Material.EMERALD_BLOCK)
            .name(Text.mm("<green>Quest erstellen"))
            .build());
        inv.setItem(50, new ItemBuilder(Material.PAPER)
            .name(Text.mm("<gold>Seite " + (safePage + 1) + "/" + (maxPage + 1)))
            .build());
        inv.setItem(53, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Nächste Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        player.openInventory(inv);
    }

    public void openLootEditor(Player player) {
        openLootEditor(player, 0);
    }

    public void openLootEditor(Player player, int page) {
        var tables = new java.util.ArrayList<>(com.example.rpg.RPGPlugin.getPlugin(com.example.rpg.RPGPlugin.class)
            .lootManager().tables().values());
        tables.sort(java.util.Comparator.comparing(LootTable::id));
        int pageSize = 45;
        int maxPage = tables.isEmpty() ? 0 : (tables.size() - 1) / pageSize;
        int safePage = Math.min(Math.max(page, 0), maxPage);
        Inventory inv = Bukkit.createInventory(new GuiHolders.LootEditorHolder(safePage), 54, Component.text("Loot-Tabellen"));
        int slot = 0;
        int startIndex = safePage * pageSize;
        for (int i = startIndex; i < tables.size() && slot < pageSize; i++) {
            var table = tables.get(i);
            ItemStack item = new ItemBuilder(Material.CHEST)
                .name(Text.mm("<yellow>" + table.id()))
                .loreLine(Text.mm("<gray>Applies: <white>" + table.appliesTo()))
                .loreLine(Text.mm("<gray>Entries: <white>" + table.entries().size()))
                .loreLine(Text.mm("<yellow>Klick: bearbeiten"))
                .loreLine(Text.mm("<red>Rechtsklick: löschen"))
                .build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(lootKey, PersistentDataType.STRING, table.id());
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        inv.setItem(45, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Vorherige Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        inv.setItem(49, new ItemBuilder(Material.EMERALD_BLOCK)
            .name(Text.mm("<green>Loot-Tabelle erstellen"))
            .build());
        inv.setItem(50, new ItemBuilder(Material.PAPER)
            .name(Text.mm("<gold>Seite " + (safePage + 1) + "/" + (maxPage + 1)))
            .build());
        inv.setItem(53, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Nächste Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        player.openInventory(inv);
    }

    public void openSkillAdmin(Player player) {
        openSkillAdmin(player, 0);
    }

    public void openSkillAdmin(Player player, int page) {
        var skills = new java.util.ArrayList<>(skillManager.skills().values());
        skills.sort(java.util.Comparator.comparing(Skill::id));
        int pageSize = 45;
        int maxPage = skills.isEmpty() ? 0 : (skills.size() - 1) / pageSize;
        int safePage = Math.min(Math.max(page, 0), maxPage);
        Inventory inv = Bukkit.createInventory(new GuiHolders.SkillAdminHolder(safePage), 54, Component.text("Skills verwalten"));
        int slot = 0;
        int startIndex = safePage * pageSize;
        for (int i = startIndex; i < skills.size() && slot < pageSize; i++) {
            var skill = skills.get(i);
            ItemStack item = new ItemBuilder(Material.ENCHANTED_BOOK)
                .name(Text.mm("<light_purple>" + skill.name()))
                .loreLine(Text.mm("<gray>ID: <white>" + skill.id()))
                .loreLine(Text.mm("<gray>Typ: <white>" + skill.type()))
                .loreLine(Text.mm("<gray>Kategorie: <white>" + skill.category()))
                .loreLine(Text.mm("<yellow>Klick: bearbeiten"))
                .loreLine(Text.mm("<red>Rechtsklick: löschen"))
                .build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(skillKey, PersistentDataType.STRING, skill.id());
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        inv.setItem(45, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Vorherige Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        inv.setItem(48, new ItemBuilder(Material.WRITABLE_BOOK)
            .name(Text.mm("<aqua>Klassen verwalten"))
            .loreLine(Text.mm("<yellow>Klick: öffnen"))
            .build());
        inv.setItem(49, new ItemBuilder(Material.EMERALD_BLOCK)
            .name(Text.mm("<green>Skill erstellen"))
            .build());
        inv.setItem(50, new ItemBuilder(Material.PAPER)
            .name(Text.mm("<gold>Seite " + (safePage + 1) + "/" + (maxPage + 1)))
            .build());
        inv.setItem(53, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Nächste Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        player.openInventory(inv);
    }

    public void openClassAdmin(Player player) {
        openClassAdmin(player, 0);
    }

    public void openClassAdmin(Player player, int page) {
        var classes = new java.util.ArrayList<>(classManager.classes().values());
        classes.sort(java.util.Comparator.comparing(ClassDefinition::id));
        int pageSize = 45;
        int maxPage = classes.isEmpty() ? 0 : (classes.size() - 1) / pageSize;
        int safePage = Math.min(Math.max(page, 0), maxPage);
        Inventory inv = Bukkit.createInventory(new GuiHolders.ClassAdminHolder(safePage), 54, Component.text("Klassen verwalten"));
        int slot = 0;
        int startIndex = safePage * pageSize;
        for (int i = startIndex; i < classes.size() && slot < pageSize; i++) {
            var definition = classes.get(i);
            ItemStack item = new ItemBuilder(Material.BOOKSHELF)
                .name(Text.mm("<gold>" + definition.name()))
                .loreLine(Text.mm("<gray>ID: <white>" + definition.id()))
                .loreLine(Text.mm("<gray>Startskills: <white>" + String.join(", ", definition.startSkills())))
                .loreLine(Text.mm("<gray>Presets: <white>" + definition.presets().size()))
                .loreLine(Text.mm("<yellow>Klick: bearbeiten"))
                .loreLine(Text.mm("<red>Rechtsklick: löschen"))
                .build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(classKey, PersistentDataType.STRING, definition.id());
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        inv.setItem(45, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Vorherige Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        inv.setItem(49, new ItemBuilder(Material.EMERALD_BLOCK)
            .name(Text.mm("<green>Klasse erstellen"))
            .build());
        inv.setItem(50, new ItemBuilder(Material.PAPER)
            .name(Text.mm("<gold>Seite " + (safePage + 1) + "/" + (maxPage + 1)))
            .build());
        inv.setItem(53, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Nächste Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        player.openInventory(inv);
    }

    public void openBuildingCategories(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.BuildingCategoryHolder(), 27, Component.text("Gebäude Kategorien"));
        int slot = 10;
        for (BuildingCategory category : BuildingCategory.values()) {
            ItemStack item = new ItemBuilder(Material.BOOKSHELF)
                .name(Text.mm("<yellow>" + category.displayName()))
                .loreLine(Text.mm("<gray>Kategorie öffnen"))
                .build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(buildingCategoryKey, PersistentDataType.STRING, category.name());
            item.setItemMeta(meta);
            inv.setItem(slot, item);
            slot += 2;
        }
        ItemStack single = new ItemBuilder(Material.PAPER)
            .name(Text.mm("<gold>Einzel-Schema"))
            .loreLine(Text.mm("<gray>Nur ein Schema platzieren"))
            .build();
        ItemMeta singleMeta = single.getItemMeta();
        singleMeta.getPersistentDataContainer().set(buildingCategoryKey, PersistentDataType.STRING, "SINGLE");
        single.setItemMeta(singleMeta);
        inv.setItem(22, single);
        player.openInventory(inv);
    }

    public void openBuildingList(Player player, BuildingCategory category) {
        openBuildingList(player, category, 0);
    }

    public void openBuildingList(Player player, BuildingCategory category, int page) {
        var buildings = new java.util.ArrayList<>(buildingManager.byCategory().getOrDefault(category, List.of()));
        buildings.sort(java.util.Comparator.comparing(BuildingDefinition::id));
        int pageSize = 45;
        int maxPage = buildings.isEmpty() ? 0 : (buildings.size() - 1) / pageSize;
        int safePage = Math.min(Math.max(page, 0), maxPage);
        Inventory inv = Bukkit.createInventory(new GuiHolders.BuildingListHolder(category.name(), safePage), 54,
            Component.text(category.displayName()));
        int slot = 0;
        int startIndex = safePage * pageSize;
        for (int i = startIndex; i < buildings.size() && slot < pageSize; i++) {
            BuildingDefinition building = buildings.get(i);
            ItemStack item = new ItemBuilder(Material.OAK_DOOR)
                .name(Text.mm("<green>" + building.name()))
                .loreLine(Text.mm("<gray>ID: <white>" + building.id()))
                .loreLine(Text.mm("<yellow>Klick: platzieren"))
                .build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(buildingKey, PersistentDataType.STRING, building.id());
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        inv.setItem(45, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Vorherige Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        inv.setItem(50, new ItemBuilder(Material.PAPER)
            .name(Text.mm("<gold>Seite " + (safePage + 1) + "/" + (maxPage + 1)))
            .build());
        inv.setItem(53, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Nächste Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        player.openInventory(inv);
    }

    public void openQuestList(Player player) {
        openQuestList(player, 0);
    }

    public void openQuestList(Player player, int page) {
        var quests = questManager.quests().values().stream()
            .filter(quest -> quest.requiredEvent() == null || worldEventManager.isCompleted(quest.requiredEvent()))
            .sorted(java.util.Comparator.comparing(Quest::id))
            .toList();
        int pageSize = 18;
        int maxPage = quests.isEmpty() ? 0 : (quests.size() - 1) / pageSize;
        int safePage = Math.min(Math.max(page, 0), maxPage);
        Inventory inv = Bukkit.createInventory(new GuiHolders.QuestListHolder(safePage), 27, Component.text("Quests"));
        int slot = 0;
        int startIndex = safePage * pageSize;
        for (int i = startIndex; i < quests.size() && slot < pageSize; i++) {
            Quest quest = quests.get(i);
            List<Component> lore = new ArrayList<>();
            lore.add(Text.mm("<gray>" + quest.description()));
            lore.add(Text.mm("<gray>Min Level: <white>" + quest.minLevel()));
            lore.addAll(questStepLore(quest, null));
            lore.add(Text.mm("<yellow>Klick: Details"));
            ItemStack item = new ItemBuilder(Material.BOOK)
                .name(Text.mm("<green>" + quest.name()))
                .loreLines(lore)
                .build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(questKey, PersistentDataType.STRING, quest.id());
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        inv.setItem(18, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Vorherige Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        inv.setItem(22, new ItemBuilder(Material.PAPER)
            .name(Text.mm("<gold>Seite " + (safePage + 1) + "/" + (maxPage + 1)))
            .build());
        inv.setItem(26, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Nächste Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        player.openInventory(inv);
    }

    public void openQuestLog(Player player) {
        openQuestLog(player, 0);
    }

    public void openQuestLog(Player player, int page) {
        PlayerProfile profile = playerDataManager.getProfile(player);
        var activeIds = new java.util.ArrayList<>(profile.activeQuests().keySet());
        activeIds.sort(String::compareToIgnoreCase);
        int pageSize = 45;
        int maxPage = activeIds.isEmpty() ? 0 : (activeIds.size() - 1) / pageSize;
        int safePage = Math.min(Math.max(page, 0), maxPage);
        Inventory inv = Bukkit.createInventory(new GuiHolders.QuestLogHolder(safePage), 54, Component.text("Quest-Log"));
        int slot = 0;
        int startIndex = safePage * pageSize;
        for (int i = startIndex; i < activeIds.size() && slot < pageSize; i++) {
            String questId = activeIds.get(i);
            Quest quest = questManager.getQuest(questId);
            QuestProgress progress = profile.activeQuests().get(questId);
            if (quest == null) {
                continue;
            }
            List<Component> lore = new ArrayList<>();
            lore.add(Text.mm("<gray>" + quest.description()));
            lore.addAll(questStepLore(quest, progress));
            lore.add(Text.mm("<yellow>Klick: Details"));
            ItemStack item = new ItemBuilder(Material.WRITABLE_BOOK)
                .name(Text.mm("<aqua>" + quest.name()))
                .loreLines(lore)
                .build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(questKey, PersistentDataType.STRING, quest.id());
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        inv.setItem(45, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Vorherige Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        inv.setItem(49, new ItemBuilder(Material.BOOK)
            .name(Text.mm("<yellow>Quest-Angebote"))
            .loreLine(Text.mm("<gray>Neue Quests ansehen"))
            .build());
        inv.setItem(50, new ItemBuilder(Material.PAPER)
            .name(Text.mm("<gold>Seite " + (safePage + 1) + "/" + (maxPage + 1)))
            .build());
        inv.setItem(53, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Nächste Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        player.openInventory(inv);
    }

    public void openQuestDetails(Player player, String questId, boolean active, int page) {
        Quest quest = questManager.getQuest(questId);
        if (quest == null) {
            player.sendMessage(Text.mm("<red>Quest nicht gefunden."));
            return;
        }
        Inventory inv = Bukkit.createInventory(new GuiHolders.QuestDetailHolder(quest.id(), active, page), 27,
            Component.text("Quest: " + quest.name()));
        PlayerProfile profile = playerDataManager.getProfile(player);
        QuestProgress progress = active ? profile.activeQuests().get(quest.id()) : null;
        List<Component> lore = new ArrayList<>();
        lore.add(Text.mm("<gray>" + quest.description()));
        lore.add(Text.mm("<gray>Min Level: <white>" + quest.minLevel()));
        if (quest.repeatable()) {
            lore.add(Text.mm("<yellow>Wiederholbar"));
        }
        lore.addAll(questStepLore(quest, progress));
        ItemStack info = new ItemBuilder(Material.BOOK)
            .name(Text.mm("<green>" + quest.name()))
            .loreLines(lore)
            .build();
        inv.setItem(13, info);
        if (active) {
            inv.setItem(22, new ItemBuilder(Material.LIME_DYE)
                .name(Text.mm("<green>Aktive Quest"))
                .loreLine(Text.mm("<gray>Diese Quest läuft bereits."))
                .build());
        } else {
            inv.setItem(22, new ItemBuilder(Material.ANVIL)
                .name(Text.mm("<green>Quest annehmen"))
                .loreLine(Text.mm("<gray>Klicke, um die Quest zu starten"))
                .build());
        }
        inv.setItem(26, new ItemBuilder(Material.ARROW).name(Text.mm("<yellow>Zurück")).build());
        player.openInventory(inv);
    }

    private List<Component> questStepLore(Quest quest, QuestProgress progress) {
        List<Component> lore = new ArrayList<>();
        int index = 0;
        for (QuestStep step : quest.steps()) {
            int current = progress != null ? progress.getStepProgress(index) : 0;
            String line = step.type() + " " + step.target();
            if (step.amount() > 0) {
                line += " (" + current + "/" + step.amount() + ")";
            }
            lore.add(Text.mm("<gray>• <white>" + line));
            index++;
        }
        if (quest.steps().isEmpty()) {
            lore.add(Text.mm("<gray>• <white>Keine Aufgaben definiert"));
        }
        return lore;
    }

    public void openSkillList(Player player) {
        openSkillList(player, 0);
    }

    public void openSkillList(Player player, int page) {
        var skills = new java.util.ArrayList<>(skillManager.skills().values());
        skills.sort(java.util.Comparator.comparing(Skill::id));
        int pageSize = 18;
        int maxPage = skills.isEmpty() ? 0 : (skills.size() - 1) / pageSize;
        int safePage = Math.min(Math.max(page, 0), maxPage);
        Inventory inv = Bukkit.createInventory(new GuiHolders.SkillListHolder(safePage), 27, Component.text("Skills"));
        PlayerProfile profile = playerDataManager.getProfile(player);
        int slot = 0;
        int startIndex = safePage * pageSize;
        for (int i = startIndex; i < skills.size() && slot < pageSize; i++) {
            var skill = skills.get(i);
            String id = skill.id();
            List<Component> lore = new ArrayList<>();
            lore.add(Text.mm("<gray>Kategorie: <white>" + skill.category()));
            lore.add(Text.mm("<gray>Typ: <white>" + skill.type()));
            lore.add(Text.mm("<gray>Cooldown: <white>" + skill.cooldown() + "s"));
            lore.add(Text.mm("<gray>Mana: <white>" + skill.manaCost()));
            lore.add(Text.mm("<gray>Rang: <white>" + profile.learnedSkills().getOrDefault(id, 0)));
            if (skill.requiredSkill() != null) {
                lore.add(Text.mm("<gray>Voraussetzung: <white>" + skill.requiredSkill()));
            }
            if (!skill.effects().isEmpty()) {
                for (var effect : skill.effects()) {
                    lore.add(Text.mm("<gray>Effekt: <white>" + effect.describe()));
                }
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
        inv.setItem(18, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Vorherige Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        inv.setItem(22, new ItemBuilder(Material.PAPER)
            .name(Text.mm("<gold>Seite " + (safePage + 1) + "/" + (maxPage + 1)))
            .build());
        inv.setItem(26, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Nächste Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        player.openInventory(inv);
    }

    public void openSchematicMoveGui(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.SchematicMoveHolder(), 27, Component.text("Schematic verschieben"));
        inv.setItem(11, new ItemBuilder(Material.ARROW).name(Text.mm("<yellow>Links")).build());
        inv.setItem(15, new ItemBuilder(Material.ARROW).name(Text.mm("<yellow>Rechts")).build());
        inv.setItem(13, new ItemBuilder(Material.FEATHER).name(Text.mm("<yellow>Hoch")).build());
        inv.setItem(22, new ItemBuilder(Material.ANVIL).name(Text.mm("<yellow>Runter")).build());
        inv.setItem(26, new ItemBuilder(Material.BARRIER).name(Text.mm("<red>Fertig")).build());
        player.openInventory(inv);
    }

    public void openEnchanting(Player player, String selectedRecipeId) {
        openEnchanting(player, selectedRecipeId, 0);
    }

    public void openEnchanting(Player player, String selectedRecipeId, int page) {
        ItemStack target = player.getInventory().getItemInMainHand();
        List<com.example.rpg.model.EnchantmentRecipe> available = enchantManager.availableRecipes(player, target);
        if (available.isEmpty()) {
            player.sendMessage(Text.mm("<red>Keine Verzauberungen verfügbar."));
        }
        String recipeId = selectedRecipeId;
        if (recipeId == null && !available.isEmpty()) {
            recipeId = available.get(0).id();
        }
        int pageSize = 9;
        int maxPage = available.isEmpty() ? 0 : (available.size() - 1) / pageSize;
        int safePage = Math.min(Math.max(page, 0), maxPage);
        Inventory inv = Bukkit.createInventory(new GuiHolders.EnchantingHolder(recipeId, safePage), 27,
            Component.text("Verzauberungen"));
        ItemStack displayTarget = target == null ? null : target.clone();
        if (displayTarget != null) {
            displayTarget.setAmount(1);
            inv.setItem(10, displayTarget);
        } else {
            inv.setItem(10, new ItemBuilder(Material.BARRIER).name(Text.mm("<red>Kein Ziel-Item")).build());
        }
        int startIndex = safePage * pageSize;
        for (int i = 0; i < pageSize && startIndex + i < available.size(); i++) {
            var recipe = available.get(startIndex + i);
            ItemBuilder builder = new ItemBuilder(Material.ENCHANTED_BOOK)
                .name(Text.mm("<yellow>" + recipe.id()))
                .loreLine(Text.mm("<gray>Typ: <white>" + recipe.type()))
                .loreLine(Text.mm("<gray>Ziel: <white>" + recipe.targetSlot()));
            if (recipe.statToImprove() != null) {
                builder.loreLine(Text.mm("<gray>Stat: <white>" + recipe.statToImprove()));
            }
            if (recipe.costGold() > 0) {
                builder.loreLine(Text.mm("<gold>Kosten: " + recipe.costGold() + " Gold"));
            }
            if (recipe.costMaterial() != null && recipe.costAmount() > 0) {
                builder.loreLine(Text.mm("<gray>Material: <white>" + recipe.costMaterial() + " x" + recipe.costAmount()));
            }
            ItemStack item = builder.build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(enchantRecipeKey, PersistentDataType.STRING, recipe.id());
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }
        com.example.rpg.model.EnchantmentRecipe selected = recipeId != null ? enchantManager.recipes().get(recipeId) : null;
        if (selected != null) {
            if (selected.costMaterial() != null) {
                inv.setItem(14, new ItemBuilder(selected.costMaterial())
                    .name(Text.mm("<yellow>Material: <white>" + selected.costMaterial()))
                    .loreLine(Text.mm("<gray>Menge: <white>" + selected.costAmount()))
                    .build());
            } else {
                inv.setItem(14, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(Text.mm("<gray>Kein Material")).build());
            }
            inv.setItem(15, new ItemBuilder(Material.GOLD_INGOT)
                .name(Text.mm("<gold>Goldkosten"))
                .loreLine(Text.mm("<gray>Benötigt: <white>" + selected.costGold()))
                .build());
            inv.setItem(22, new ItemBuilder(Material.ANVIL).name(Text.mm("<green>Verzaubern")).build());
        } else {
            inv.setItem(22, new ItemBuilder(Material.BARRIER).name(Text.mm("<red>Kein Rezept ausgewählt")).build());
        }
        inv.setItem(18, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Vorherige Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        inv.setItem(24, new ItemBuilder(Material.PAPER)
            .name(Text.mm("<gold>Seite " + (safePage + 1) + "/" + (maxPage + 1)))
            .build());
        inv.setItem(25, new ItemBuilder(Material.BARRIER).name(Text.mm("<red>Schließen")).build());
        inv.setItem(26, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Nächste Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        player.openInventory(inv);
    }

    public void openPermissionsMain(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.PermissionsMainHolder(), 27, Component.text("Permissions"));
        inv.setItem(11, new ItemBuilder(Material.BOOK)
            .name(Text.mm("<yellow>Rollen"))
            .loreLine(Text.mm("<gray>Rollen verwalten"))
            .build());
        inv.setItem(13, new ItemBuilder(Material.PLAYER_HEAD)
            .name(Text.mm("<green>Spieler"))
            .loreLine(Text.mm("<gray>Rollen zuweisen"))
            .build());
        inv.setItem(15, new ItemBuilder(Material.PAPER)
            .name(Text.mm("<light_purple>Audit Log"))
            .loreLine(Text.mm("<gray>Letzte Änderungen"))
            .build());
        player.openInventory(inv);
    }

    public void openRoleList(Player player, int page) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.RoleListHolder(page), 54, Component.text("Rollen"));
        int start = page * 45;
        int slot = 0;
        List<com.example.rpg.permissions.Role> roles = new ArrayList<>(permissionService.roles().values());
        roles.sort(java.util.Comparator.comparing(com.example.rpg.permissions.Role::key));
        for (int i = start; i < roles.size() && slot < 45; i++) {
            var role = roles.get(i);
            ItemStack item = new ItemBuilder(Material.BOOK)
                .name(Text.mm("<yellow>" + role.displayName()))
                .loreLine(Text.mm("<gray>Key: <white>" + role.key()))
                .loreLine(Text.mm("<gray>Nodes: <white>" + role.nodes().size()))
                .build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(permRoleKey, PersistentDataType.STRING, role.key());
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        inv.setItem(45, new ItemBuilder(Material.ARROW).name(Text.mm("<yellow>Zurück")).build());
        inv.setItem(53, new ItemBuilder(Material.ANVIL).name(Text.mm("<green>Neue Rolle")).build());
        player.openInventory(inv);
    }

    public void openRoleDetails(Player player, String roleKey) {
        var role = permissionService.roles().get(roleKey);
        if (role == null) {
            player.sendMessage(Text.mm("<red>Rolle nicht gefunden."));
            return;
        }
        Inventory inv = Bukkit.createInventory(new GuiHolders.RoleDetailHolder(roleKey), 27, Component.text("Rolle: " + role.displayName()));
        inv.setItem(11, new ItemBuilder(Material.NAME_TAG).name(Text.mm("<yellow>Eltern"))
            .loreLine(Text.mm("<gray>Vererbung verwalten")).build());
        inv.setItem(13, new ItemBuilder(Material.WRITABLE_BOOK).name(Text.mm("<yellow>Nodes"))
            .loreLine(Text.mm("<gray>Rechte verwalten")).build());
        inv.setItem(15, new ItemBuilder(Material.ANVIL).name(Text.mm("<yellow>Umbenennen"))
            .loreLine(Text.mm("<gray>Display-Name ändern")).build());
        inv.setItem(26, new ItemBuilder(Material.BARRIER).name(Text.mm("<red>Löschen")).build());
        player.openInventory(inv);
    }

    public void openRoleNodes(Player player, String roleKey, int page) {
        var role = permissionService.roles().get(roleKey);
        if (role == null) {
            player.sendMessage(Text.mm("<red>Rolle nicht gefunden."));
            return;
        }
        Inventory inv = Bukkit.createInventory(new GuiHolders.RoleNodesHolder(roleKey, page), 54, Component.text("Nodes: " + role.displayName()));
        List<String> nodes = new ArrayList<>(role.nodes().keySet());
        nodes.sort(String::compareToIgnoreCase);
        int start = page * 45;
        int slot = 0;
        for (int i = start; i < nodes.size() && slot < 45; i++) {
            String node = nodes.get(i);
            var decision = role.nodes().get(node);
            ItemStack item = new ItemBuilder(Material.PAPER)
                .name(Text.mm("<yellow>" + node))
                .loreLine(Text.mm("<gray>Status: <white>" + decision))
                .build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(permRoleKey, PersistentDataType.STRING, roleKey);
            meta.getPersistentDataContainer().set(permNodeKey, PersistentDataType.STRING, node);
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        inv.setItem(45, new ItemBuilder(Material.ARROW).name(Text.mm("<yellow>Zurück")).build());
        inv.setItem(53, new ItemBuilder(Material.ANVIL).name(Text.mm("<green>Node hinzufügen")).build());
        player.openInventory(inv);
    }

    public void openRoleParents(Player player, String roleKey) {
        var role = permissionService.roles().get(roleKey);
        if (role == null) {
            player.sendMessage(Text.mm("<red>Rolle nicht gefunden."));
            return;
        }
        Inventory inv = Bukkit.createInventory(new GuiHolders.RoleParentsHolder(roleKey), 54, Component.text("Eltern: " + role.displayName()));
        int slot = 0;
        for (var entry : permissionService.roles().values()) {
            if (slot >= 45) {
                break;
            }
            boolean active = role.parents().contains(entry.key());
            ItemStack item = new ItemBuilder(active ? Material.EMERALD_BLOCK : Material.GRAY_STAINED_GLASS_PANE)
                .name(Text.mm("<yellow>" + entry.displayName()))
                .loreLine(Text.mm(active ? "<green>Aktiv" : "<gray>Inaktiv"))
                .build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(permRoleKey, PersistentDataType.STRING, roleKey);
            meta.getPersistentDataContainer().set(permNodeKey, PersistentDataType.STRING, entry.key());
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        inv.setItem(45, new ItemBuilder(Material.ARROW).name(Text.mm("<yellow>Zurück")).build());
        player.openInventory(inv);
    }

    public void openPlayerList(Player player) {
        openPlayerList(player, 0);
    }

    public void openPlayerList(Player player, int page) {
        var players = new java.util.ArrayList<>(Bukkit.getOnlinePlayers());
        players.sort(java.util.Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));
        int pageSize = 45;
        int maxPage = players.isEmpty() ? 0 : (players.size() - 1) / pageSize;
        int safePage = Math.min(Math.max(page, 0), maxPage);
        Inventory inv = Bukkit.createInventory(new GuiHolders.PlayerListHolder(safePage), 54, Component.text("Spieler Rollen"));
        int slot = 0;
        int startIndex = safePage * pageSize;
        for (int i = startIndex; i < players.size() && slot < pageSize; i++) {
            Player online = players.get(i);
            ItemStack item = new ItemBuilder(Material.PLAYER_HEAD)
                .name(Text.mm("<yellow>" + online.getName()))
                .loreLine(Text.mm("<gray>UUID: <white>" + online.getUniqueId()))
                .build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(permPlayerKey, PersistentDataType.STRING, online.getUniqueId().toString());
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        inv.setItem(45, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Vorherige Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        inv.setItem(47, new ItemBuilder(Material.ARROW).name(Text.mm("<yellow>Zurück")).build());
        inv.setItem(49, new ItemBuilder(Material.ANVIL).name(Text.mm("<green>Spieler suchen")).build());
        inv.setItem(50, new ItemBuilder(Material.PAPER)
            .name(Text.mm("<gold>Seite " + (safePage + 1) + "/" + (maxPage + 1)))
            .build());
        inv.setItem(53, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Nächste Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        player.openInventory(inv);
    }

    public void openPlayerRoles(Player player, java.util.UUID targetId) {
        var roles = permissionService.getPlayerRoles(targetId);
        String name = Bukkit.getOfflinePlayer(targetId).getName();
        Inventory inv = Bukkit.createInventory(new GuiHolders.PlayerRoleHolder(targetId), 27,
            Component.text("Rollen: " + (name != null ? name : targetId.toString())));
        String primaryRole = roles.primaryRole() != null ? roles.primaryRole() : "Keine";
        inv.setItem(10, new ItemBuilder(Material.BOOK).name(Text.mm("<yellow>Primary: <white>" + primaryRole)).build());
        inv.setItem(12, new ItemBuilder(Material.ANVIL).name(Text.mm("<green>Primary setzen")).build());
        inv.setItem(14, new ItemBuilder(Material.EMERALD).name(Text.mm("<green>Rolle hinzufügen")).build());
        inv.setItem(16, new ItemBuilder(Material.BARRIER).name(Text.mm("<red>Rolle entfernen")).build());
        inv.setItem(22, new ItemBuilder(Material.PAPER).name(Text.mm("<yellow>Node prüfen")).build());
        player.openInventory(inv);
    }

    public void openAuditLog(Player player) {
        openAuditLog(player, 0);
    }

    public void openAuditLog(Player player, int page) {
        List<String> entries = permissionService.auditLog().recent(200);
        int pageSize = 45;
        int maxPage = entries.isEmpty() ? 0 : (entries.size() - 1) / pageSize;
        int safePage = Math.min(Math.max(page, 0), maxPage);
        Inventory inv = Bukkit.createInventory(new GuiHolders.PermissionAuditHolder(safePage), 54,
            Component.text("Audit Log"));
        int slot = 0;
        int startIndex = safePage * pageSize;
        for (int i = startIndex; i < entries.size() && slot < pageSize; i++) {
            String line = entries.get(i);
            inv.setItem(slot++, new ItemBuilder(Material.PAPER).name(Text.mm("<gray>" + line)).build());
        }
        inv.setItem(45, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Vorherige Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        inv.setItem(49, new ItemBuilder(Material.ARROW).name(Text.mm("<yellow>Zurück")).build());
        inv.setItem(50, new ItemBuilder(Material.PAPER)
            .name(Text.mm("<gold>Seite " + (safePage + 1) + "/" + (maxPage + 1)))
            .build());
        inv.setItem(53, new ItemBuilder(Material.ARROW)
            .name(Text.mm("<yellow>Nächste Seite"))
            .loreLine(Text.mm("<gray>Seite " + (safePage + 1) + " von " + (maxPage + 1)))
            .build());
        player.openInventory(inv);
    }

    public void openShop(Player player, ShopDefinition shop) {
        int maxSlot = shop.items().values().stream().mapToInt(ShopItem::slot).max().orElse(0);
        int size = maxSlot >= 27 ? 54 : 27;
        Inventory inv = Bukkit.createInventory(new GuiHolders.ShopHolder(shop.id()), size, Component.text(shop.title()));
        for (ShopItem item : shop.items().values()) {
            Material material = Material.matchMaterial(item.material());
            if (material == null) {
                continue;
            }
            ItemStack displayItem;
            if (item.rpgItem()) {
                Rarity rarity = parseRarity(item.rarity());
                displayItem = itemGenerator.createRpgItem(material, rarity, Math.max(1, item.minLevel()));
            } else {
                ItemBuilder builder = new ItemBuilder(material);
                if (item.name() != null && !item.name().isBlank()) {
                    builder.name(net.kyori.adventure.text.Component.text(
                        org.bukkit.ChatColor.translateAlternateColorCodes('&', item.name())));
                }
                displayItem = builder.build();
            }
            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
                if (item.buyPrice() > 0) {
                    lore.add(Text.mm("<gray>Kaufen: <gold>" + item.buyPrice() + " Gold"));
                }
                if (item.sellPrice() > 0) {
                    lore.add(Text.mm("<gray>Verkaufen: <gold>" + item.sellPrice() + " Gold"));
                }
                meta.lore(lore);
                displayItem.setItemMeta(meta);
            }
            inv.setItem(item.slot(), displayItem);
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

    private Rarity parseRarity(String raw) {
        if (raw == null) {
            return Rarity.COMMON;
        }
        try {
            return Rarity.valueOf(raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return Rarity.COMMON;
        }
    }
}

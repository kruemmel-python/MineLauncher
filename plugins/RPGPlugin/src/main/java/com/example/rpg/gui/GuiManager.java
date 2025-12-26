package com.example.rpg.gui;

import com.example.rpg.manager.ClassManager;
import com.example.rpg.manager.FactionManager;
import com.example.rpg.manager.PlayerDataManager;
import com.example.rpg.manager.QuestManager;
import com.example.rpg.manager.WorldEventManager;
import com.example.rpg.manager.BuildingManager;
import com.example.rpg.manager.SkillManager;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.Quest;
import com.example.rpg.model.Rarity;
import com.example.rpg.model.ShopDefinition;
import com.example.rpg.model.ShopItem;
import com.example.rpg.model.BuildingCategory;
import com.example.rpg.model.BuildingDefinition;
import com.example.rpg.util.ItemGenerator;
import com.example.rpg.util.ItemBuilder;
import com.example.rpg.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
    private final WorldEventManager worldEventManager;
    private final SkillManager skillManager;
    private final ClassManager classManager;
    private final FactionManager factionManager;
    private final BuildingManager buildingManager;
    private final com.example.rpg.permissions.PermissionService permissionService;
    private final com.example.rpg.manager.EnchantManager enchantManager;
    private final ItemGenerator itemGenerator;
    private final NamespacedKey questKey;
    private final NamespacedKey skillKey;
    private final NamespacedKey buildingKey;
    private final NamespacedKey buildingCategoryKey;
    private final NamespacedKey permRoleKey;
    private final NamespacedKey permPlayerKey;
    private final NamespacedKey permNodeKey;
    private final NamespacedKey permActionKey;
    private final NamespacedKey enchantRecipeKey;

    public GuiManager(PlayerDataManager playerDataManager, QuestManager questManager, WorldEventManager worldEventManager,
                      SkillManager skillManager, ClassManager classManager, FactionManager factionManager,
                      BuildingManager buildingManager, com.example.rpg.permissions.PermissionService permissionService,
                      com.example.rpg.manager.EnchantManager enchantManager, ItemGenerator itemGenerator,
                      NamespacedKey questKey, NamespacedKey skillKey, NamespacedKey buildingKey, NamespacedKey buildingCategoryKey,
                      NamespacedKey permRoleKey, NamespacedKey permPlayerKey, NamespacedKey permNodeKey, NamespacedKey permActionKey,
                      NamespacedKey enchantRecipeKey) {
        this.playerDataManager = playerDataManager;
        this.questManager = questManager;
        this.worldEventManager = worldEventManager;
        this.skillManager = skillManager;
        this.classManager = classManager;
        this.factionManager = factionManager;
        this.buildingManager = buildingManager;
        this.permissionService = permissionService;
        this.enchantManager = enchantManager;
        this.itemGenerator = itemGenerator;
        this.questKey = questKey;
        this.skillKey = skillKey;
        this.buildingKey = buildingKey;
        this.buildingCategoryKey = buildingCategoryKey;
        this.permRoleKey = permRoleKey;
        this.permPlayerKey = permPlayerKey;
        this.permNodeKey = permNodeKey;
        this.permActionKey = permActionKey;
        this.enchantRecipeKey = enchantRecipeKey;
    }

    public void openPlayerMenu(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.PlayerMenuHolder(), 27, Component.text("RPG Menü"));
        PlayerProfile profile = playerDataManager.getProfile(player);

        inv.setItem(10, new ItemBuilder(Material.PLAYER_HEAD)
            .name(Text.mm("<gold>Charakter"))
            .loreLine(Text.mm("<gray>Level: <white>" + profile.level()))
            .loreLine(Text.mm("<gray>XP: <white>" + profile.xp() + "/" + profile.xpNeeded()))
            .loreLine(Text.mm("<gray>Klasse: <white>" + resolveClassName(profile.classId())))
            .loreLine(Text.mm("<gray>Gelernte Skills: <white>" + profile.learnedSkills().size()))
            .loreLine(Text.mm("<gray>Skillpunkte: <white>" + profile.skillPoints()))
            .loreLine(Text.mm("<gray>Stärke: <white>" + profile.stats().getOrDefault(com.example.rpg.model.RPGStat.STRENGTH, 0)))
            .loreLine(Text.mm("<gray>Geschick: <white>" + profile.stats().getOrDefault(com.example.rpg.model.RPGStat.DEXTERITY, 0)))
            .loreLine(Text.mm("<gray>Konstitution: <white>" + profile.stats().getOrDefault(com.example.rpg.model.RPGStat.CONSTITUTION, 0)))
            .loreLine(Text.mm("<gray>Intelligenz: <white>" + profile.stats().getOrDefault(com.example.rpg.model.RPGStat.INTELLIGENCE, 0)))
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
        inv.setItem(16, new ItemBuilder(Material.BRICKS)
            .name(Text.mm("<gold>Bau-Manager"))
            .loreLine(Text.mm("<gray>Gebäude platzieren"))
            .build());
        inv.setItem(17, new ItemBuilder(Material.NAME_TAG)
            .name(Text.mm("<aqua>Permissions"))
            .loreLine(Text.mm("<gray>Rollen & Rechte verwalten"))
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
        Inventory inv = Bukkit.createInventory(new GuiHolders.BuildingListHolder(category.name()), 54, Component.text(category.displayName()));
        int slot = 0;
        for (BuildingDefinition building : buildingManager.byCategory().getOrDefault(category, List.of())) {
            if (slot >= inv.getSize()) {
                break;
            }
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
        player.openInventory(inv);
    }

    public void openQuestList(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.QuestListHolder(), 27, Component.text("Quests"));
        int slot = 0;
        for (Quest quest : questManager.quests().values()) {
            if (slot >= inv.getSize()) {
                break;
            }
            if (quest.requiredEvent() != null && !worldEventManager.isCompleted(quest.requiredEvent())) {
                continue;
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
        ItemStack target = player.getInventory().getItemInMainHand();
        List<com.example.rpg.model.EnchantmentRecipe> available = enchantManager.availableRecipes(player, target);
        if (available.isEmpty()) {
            player.sendMessage(Text.mm("<red>Keine Verzauberungen verfügbar."));
        }
        String recipeId = selectedRecipeId;
        if (recipeId == null && !available.isEmpty()) {
            recipeId = available.get(0).id();
        }
        Inventory inv = Bukkit.createInventory(new GuiHolders.EnchantingHolder(recipeId), 27, Component.text("Verzauberungen"));
        ItemStack displayTarget = target == null ? null : target.clone();
        if (displayTarget != null) {
            displayTarget.setAmount(1);
            inv.setItem(10, displayTarget);
        } else {
            inv.setItem(10, new ItemBuilder(Material.BARRIER).name(Text.mm("<red>Kein Ziel-Item")).build());
        }
        for (int i = 0; i < Math.min(available.size(), 9); i++) {
            var recipe = available.get(i);
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
        inv.setItem(26, new ItemBuilder(Material.BARRIER).name(Text.mm("<red>Schließen")).build());
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
        Inventory inv = Bukkit.createInventory(new GuiHolders.PlayerListHolder(), 54, Component.text("Spieler Rollen"));
        int slot = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (slot >= 45) {
                break;
            }
            ItemStack item = new ItemBuilder(Material.PLAYER_HEAD)
                .name(Text.mm("<yellow>" + online.getName()))
                .loreLine(Text.mm("<gray>UUID: <white>" + online.getUniqueId()))
                .build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(permPlayerKey, PersistentDataType.STRING, online.getUniqueId().toString());
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        inv.setItem(45, new ItemBuilder(Material.ARROW).name(Text.mm("<yellow>Zurück")).build());
        inv.setItem(53, new ItemBuilder(Material.ANVIL).name(Text.mm("<green>Spieler suchen")).build());
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
        Inventory inv = Bukkit.createInventory(new GuiHolders.PermissionAuditHolder(), 54, Component.text("Audit Log"));
        List<String> entries = permissionService.auditLog().recent(50);
        int slot = 0;
        for (String line : entries) {
            if (slot >= 45) {
                break;
            }
            inv.setItem(slot++, new ItemBuilder(Material.PAPER).name(Text.mm("<gray>" + line)).build());
        }
        inv.setItem(45, new ItemBuilder(Material.ARROW).name(Text.mm("<yellow>Zurück")).build());
        player.openInventory(inv);
    }

    public void openShop(Player player, ShopDefinition shop) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.ShopHolder(shop.id()), 27, Component.text(shop.title()));
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

package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.gui.GuiHolders;
import com.example.rpg.model.ClassDefinition;
import com.example.rpg.model.LootEntry;
import com.example.rpg.model.LootTable;
import com.example.rpg.model.Npc;
import com.example.rpg.model.NpcRole;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.Quest;
import com.example.rpg.model.QuestProgress;
import com.example.rpg.model.QuestStep;
import com.example.rpg.model.QuestStepType;
import com.example.rpg.model.Rarity;
import com.example.rpg.model.Skill;
import com.example.rpg.model.SkillCategory;
import com.example.rpg.model.SkillType;
import com.example.rpg.schematic.Transform;
import com.example.rpg.skill.SkillEffectConfig;
import com.example.rpg.skill.SkillEffectType;
import com.example.rpg.util.ItemBuilder;
import com.example.rpg.util.Text;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.Material;
import org.bukkit.Sound;

public class GuiListener implements Listener {
    private final RPGPlugin plugin;

    public GuiListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack current = event.getCurrentItem();
        if (current == null) {
            return;
        }
        var holder = event.getInventory().getHolder();
        if (holder instanceof GuiHolders.PlayerMenuHolder) {
            event.setCancelled(true);
            switch (event.getSlot()) {
                case 12 -> plugin.guiManager().openSkillList(player);
                case 14 -> plugin.guiManager().openQuestLog(player);
                case 15 -> plugin.guiManager().openQuestList(player);
                default -> {
                }
            }
            return;
        }
        if (holder instanceof GuiHolders.AdminMenuHolder) {
            event.setCancelled(true);
            switch (event.getSlot()) {
                case 10 -> plugin.guiManager().openZoneEditor(player);
                case 11 -> plugin.guiManager().openNpcEditor(player);
                case 12 -> plugin.guiManager().openQuestEditor(player);
                case 13 -> plugin.guiManager().openLootEditor(player);
                case 14 -> plugin.guiManager().openSkillAdmin(player);
                case 15 -> {
                    boolean enabled = plugin.toggleDebug(player.getUniqueId());
                    player.sendMessage(Text.mm(enabled ? "<green>Debug aktiviert." : "<red>Debug deaktiviert."));
                }
                case 16 -> plugin.guiManager().openBuildingCategories(player);
                case 17 -> plugin.guiManager().openPermissionsMain(player);
                case 18 -> plugin.guiManager().openDungeonAdmin(player);
                case 19 -> plugin.guiManager().openWorldBuildingMenu(player);
                default -> {
                }
            }
            return;
        }
        if (holder instanceof GuiHolders.WorldBuildingHolder) {
            event.setCancelled(true);
            switch (event.getSlot()) {
                case 11 -> giveWand(player);
                case 13 -> plugin.guiManager().openBlockFillMenu(player, 0);
                case 22 -> plugin.guiManager().openAdminMenu(player);
                default -> {
                }
            }
            return;
        }
        if (holder instanceof GuiHolders.BlockFillHolder fillHolder) {
            event.setCancelled(true);
            int page = fillHolder.page();
            if (event.getSlot() == 45) {
                plugin.guiManager().openBlockFillMenu(player, page - 1);
                return;
            }
            if (event.getSlot() == 53) {
                plugin.guiManager().openBlockFillMenu(player, page + 1);
                return;
            }
            if (event.getSlot() == 49) {
                plugin.guiManager().openWorldBuildingMenu(player);
                return;
            }
            ItemStack item = event.getCurrentItem();
            if (item == null) {
                return;
            }
            Material material = item.getType();
            if (!material.isBlock()) {
                return;
            }
            fillSelection(player, material);
            plugin.guiManager().openBlockFillMenu(player, page);
            return;
        }
        if (holder instanceof GuiHolders.DungeonAdminHolder) {
            event.setCancelled(true);
            switch (event.getSlot()) {
                case 7 -> promptSchematicSave(player);
                case 9 -> promptSchematicPlacement(player);
                case 11 -> toggleDungeonSetting(player, "dungeon.jigsaw.enabled");
                case 13 -> toggleDungeonSetting(player, "dungeon.jigsaw.wfcFill");
                case 15 -> promptDungeonTheme(player);
                case 17 -> sendDungeonSetupInfo(player);
                case 22 -> plugin.guiManager().openAdminMenu(player);
                default -> {
                }
            }
            return;
        }
        if (holder instanceof GuiHolders.ZoneEditorHolder zoneHolder) {
            event.setCancelled(true);
            int page = zoneHolder.page();
            if (event.getSlot() == 45) {
                plugin.guiManager().openZoneEditor(player, page - 1);
                return;
            }
            if (event.getSlot() == 53) {
                plugin.guiManager().openZoneEditor(player, page + 1);
                return;
            }
            if (event.getSlot() == 49) {
                plugin.promptManager().prompt(player, Text.mm("<yellow>Zone erstellen: <id>"), input -> {
                    String id = input.trim();
                    if (id.isBlank()) {
                        player.sendMessage(Text.mm("<red>ID darf nicht leer sein."));
                        return;
                    }
                    if (plugin.zoneManager().getZone(id) != null) {
                        player.sendMessage(Text.mm("<red>Zone existiert bereits."));
                        return;
                    }
                    Location pos1 = readPosition(player, "pos1");
                    Location pos2 = readPosition(player, "pos2");
                    if (pos1 == null || pos2 == null) {
                        player.sendMessage(Text.mm("<red>Setze Pos1/Pos2 mit der Wand."));
                        return;
                    }
                    com.example.rpg.model.Zone zone = new com.example.rpg.model.Zone(id);
                    zone.setName(id);
                    zone.setWorld(pos1.getWorld().getName());
                    zone.setBounds(pos1, pos2);
                    plugin.zoneManager().zones().put(id, zone);
                    plugin.zoneManager().saveZone(zone);
                    plugin.auditLog().log(player, "Zone erstellt (GUI): " + id);
                    player.sendMessage(Text.mm("<green>Zone erstellt: " + id));
                    plugin.guiManager().openZoneEditor(player, page);
                });
                return;
            }
            String zoneId = resolveZoneId(current);
            if (zoneId == null) {
                return;
            }
            if (event.isRightClick()) {
                if (plugin.zoneManager().zones().remove(zoneId) != null) {
                    plugin.zoneManager().saveAll();
                    plugin.auditLog().log(player, "Zone gelöscht (GUI): " + zoneId);
                    player.sendMessage(Text.mm("<red>Zone gelöscht: " + zoneId));
                    plugin.guiManager().openZoneEditor(player, page);
                }
                return;
            }
            var zone = plugin.zoneManager().getZone(zoneId);
            if (zone == null) {
                return;
            }
            plugin.promptManager().prompt(player, Text.mm("<yellow>Zone bearbeiten: <name|level|mod|bounds|world> ..."), input -> {
                String[] parts = input.trim().split("\\s+");
                if (parts.length == 0 || parts[0].isBlank()) {
                    player.sendMessage(Text.mm("<red>Ungültige Eingabe."));
                    return;
                }
                String action = parts[0].toLowerCase(Locale.ROOT);
                switch (action) {
                    case "name" -> {
                        if (parts.length < 2) {
                            player.sendMessage(Text.mm("<red>Format: name <wert>"));
                            return;
                        }
                        String name = input.substring(input.indexOf(' ') + 1).trim();
                        if (name.isBlank()) {
                            player.sendMessage(Text.mm("<red>Name darf nicht leer sein."));
                            return;
                        }
                        zone.setName(name);
                    }
                    case "level" -> {
                        if (parts.length < 3) {
                            player.sendMessage(Text.mm("<red>Format: level <min> <max>"));
                            return;
                        }
                        Integer min = parseInt(parts[1]);
                        Integer max = parseInt(parts[2]);
                        if (min == null || max == null || min < 1 || max < min) {
                            player.sendMessage(Text.mm("<red>Ungültiger Levelbereich."));
                            return;
                        }
                        zone.setMinLevel(min);
                        zone.setMaxLevel(max);
                    }
                    case "mod" -> {
                        if (parts.length < 3) {
                            player.sendMessage(Text.mm("<red>Format: mod <slow> <damage>"));
                            return;
                        }
                        Double slow = parseDouble(parts[1]);
                        Double dmg = parseDouble(parts[2]);
                        if (slow == null || dmg == null || slow <= 0.0 || dmg <= 0.0) {
                            player.sendMessage(Text.mm("<red>Ungültige Mod-Werte."));
                            return;
                        }
                        zone.setSlowMultiplier(slow);
                        zone.setDamageMultiplier(dmg);
                    }
                    case "bounds" -> {
                        Location pos1 = readPosition(player, "pos1");
                        Location pos2 = readPosition(player, "pos2");
                        if (pos1 == null || pos2 == null) {
                            player.sendMessage(Text.mm("<red>Setze Pos1/Pos2 mit der Wand."));
                            return;
                        }
                        zone.setWorld(pos1.getWorld().getName());
                        zone.setBounds(pos1, pos2);
                    }
                    case "world" -> {
                        if (parts.length < 2) {
                            player.sendMessage(Text.mm("<red>Format: world <name>"));
                            return;
                        }
                        zone.setWorld(parts[1]);
                    }
                    default -> {
                        player.sendMessage(Text.mm("<red>Unbekannte Aktion."));
                        return;
                    }
                }
                plugin.zoneManager().saveZone(zone);
                plugin.auditLog().log(player, "Zone aktualisiert (GUI): " + zone.id());
                player.sendMessage(Text.mm("<green>Zone aktualisiert."));
                plugin.guiManager().openZoneEditor(player, page);
            });
            return;
        }
        if (holder instanceof GuiHolders.NpcEditorHolder npcHolder) {
            event.setCancelled(true);
            int page = npcHolder.page();
            if (event.getSlot() == 45) {
                plugin.guiManager().openNpcEditor(player, page - 1);
                return;
            }
            if (event.getSlot() == 53) {
                plugin.guiManager().openNpcEditor(player, page + 1);
                return;
            }
            NpcRole templateRole = resolveNpcTemplate(current);
            if (templateRole != null) {
                handleNpcTemplateClick(player, templateRole);
                return;
            }
            if (event.getSlot() == 49) {
                plugin.promptManager().prompt(player, Text.mm("<yellow>NPC erstellen: <id> <role> [shopId]"), input -> {
                    String[] parts = input.trim().split("\\s+");
                    runSync(() -> {
                        if (parts.length < 2) {
                            player.sendMessage(Text.mm("<red>Format: <id> <role> [shopId]"));
                            return;
                        }
                        String id = parts[0];
                        if (plugin.npcManager().getNpc(id) != null) {
                            player.sendMessage(Text.mm("<red>NPC existiert bereits."));
                            return;
                        }
                        Optional<NpcRole> roleOpt = parseEnum(NpcRole.class, parts[1]);
                        if (roleOpt.isEmpty()) {
                            player.sendMessage(Text.mm("<red>Unbekannte Rolle."));
                            return;
                        }
                        Npc npc = new Npc(id);
                        npc.setName(id);
                        npc.setRole(roleOpt.get());
                        npc.setLocation(player.getLocation());
                        npc.setDialog(java.util.List.of("Hallo!", "Ich habe eine Aufgabe für dich."));
                        if (npc.role() == NpcRole.VENDOR && parts.length >= 3) {
                            npc.setShopId(parts[2]);
                        }
                        plugin.npcManager().npcs().put(id, npc);
                        plugin.npcManager().spawnNpc(npc);
                        plugin.npcManager().saveNpc(npc);
                        plugin.auditLog().log(player, "NPC erstellt (GUI): " + id);
                        player.sendMessage(Text.mm("<green>NPC erstellt: " + id));
                        plugin.guiManager().openNpcEditor(player, page);
                    });
                });
                return;
            }
            String npcId = resolveNpcId(current);
            if (npcId == null) {
                return;
            }
            if (event.isRightClick()) {
                Npc npc = plugin.npcManager().getNpc(npcId);
                if (npc != null) {
                    runSync(() -> {
                        removeNpcEntity(npc);
                        plugin.npcManager().npcs().remove(npcId);
                        plugin.npcManager().saveAll();
                        plugin.auditLog().log(player, "NPC gelöscht (GUI): " + npcId);
                        player.sendMessage(Text.mm("<red>NPC gelöscht: " + npcId));
                        plugin.guiManager().openNpcEditor(player, page);
                    });
                }
                return;
            }
            Npc npc = plugin.npcManager().getNpc(npcId);
            if (npc == null) {
                return;
            }
            plugin.promptManager().prompt(player, Text.mm("<yellow>NPC bearbeiten: <name|role|dialog|quest|shop|faction|rank|move> ..."), input -> {
                String[] parts = input.trim().split("\\s+");
                runSync(() -> {
                    if (parts.length == 0 || parts[0].isBlank()) {
                        player.sendMessage(Text.mm("<red>Ungültige Eingabe."));
                        return;
                    }
                    String action = parts[0].toLowerCase(Locale.ROOT);
                    boolean needsRespawn = false;
                    switch (action) {
                        case "name" -> {
                            if (parts.length < 2) {
                                player.sendMessage(Text.mm("<red>Format: name <wert>"));
                                return;
                            }
                            String name = input.substring(input.indexOf(' ') + 1).trim();
                            if (name.isBlank()) {
                                player.sendMessage(Text.mm("<red>Name darf nicht leer sein."));
                                return;
                            }
                            npc.setName(name);
                            needsRespawn = true;
                        }
                        case "role" -> {
                            if (parts.length < 2) {
                                player.sendMessage(Text.mm("<red>Format: role <rolle>"));
                                return;
                            }
                            Optional<NpcRole> roleOpt = parseEnum(NpcRole.class, parts[1]);
                            if (roleOpt.isEmpty()) {
                                player.sendMessage(Text.mm("<red>Unbekannte Rolle."));
                                return;
                            }
                            npc.setRole(roleOpt.get());
                            needsRespawn = true;
                        }
                        case "dialog" -> {
                            if (parts.length < 2) {
                                player.sendMessage(Text.mm("<red>Format: dialog <text>"));
                                return;
                            }
                            String dialog = input.substring(input.indexOf(' ') + 1).trim();
                            if (dialog.isBlank()) {
                                player.sendMessage(Text.mm("<red>Dialog darf nicht leer sein."));
                                return;
                            }
                            npc.setDialog(java.util.List.of(dialog));
                        }
                        case "quest" -> {
                            if (parts.length < 2) {
                                player.sendMessage(Text.mm("<red>Format: quest <questId|none>"));
                                return;
                            }
                            String questId = parts[1];
                            if (!questId.equalsIgnoreCase("none") && plugin.questManager().getQuest(questId) == null) {
                                player.sendMessage(Text.mm("<red>Quest nicht gefunden."));
                                return;
                            }
                            npc.setQuestLink(questId.equalsIgnoreCase("none") ? null : questId);
                        }
                        case "shop" -> {
                            if (parts.length < 2) {
                                player.sendMessage(Text.mm("<red>Format: shop <shopId|none>"));
                                return;
                            }
                            String shopId = parts[1];
                            if (!shopId.equalsIgnoreCase("none") && plugin.shopManager().getShop(shopId) == null) {
                                player.sendMessage(Text.mm("<red>Shop nicht gefunden."));
                                return;
                            }
                            npc.setShopId(shopId.equalsIgnoreCase("none") ? null : shopId);
                        }
                        case "faction" -> {
                            if (parts.length < 2) {
                                player.sendMessage(Text.mm("<red>Format: faction <factionId|none>"));
                                return;
                            }
                            String factionId = parts[1];
                            if (!factionId.equalsIgnoreCase("none") && plugin.factionManager().getFaction(factionId) == null) {
                                player.sendMessage(Text.mm("<red>Fraktion nicht gefunden."));
                                return;
                            }
                            npc.setFactionId(factionId.equalsIgnoreCase("none") ? null : factionId);
                        }
                        case "rank" -> {
                            if (parts.length < 2) {
                                player.sendMessage(Text.mm("<red>Format: rank <rankId|none>"));
                                return;
                            }
                            String rankId = parts[1];
                            npc.setRequiredRankId(rankId.equalsIgnoreCase("none") ? null : rankId);
                        }
                        case "move" -> {
                            npc.setLocation(player.getLocation());
                            needsRespawn = true;
                        }
                        default -> {
                            player.sendMessage(Text.mm("<red>Unbekannte Aktion."));
                            return;
                        }
                    }
                    if (needsRespawn) {
                        respawnNpc(npc);
                    }
                    plugin.npcManager().saveNpc(npc);
                    plugin.auditLog().log(player, "NPC aktualisiert (GUI): " + npc.id());
                    player.sendMessage(Text.mm("<green>NPC aktualisiert."));
                    plugin.guiManager().openNpcEditor(player, page);
                });
            });
            return;
        }
        if (holder instanceof GuiHolders.QuestEditorHolder) {
            event.setCancelled(true);
            GuiHolders.QuestEditorHolder questHolder = (GuiHolders.QuestEditorHolder) holder;
            int page = questHolder.page();
            int questCount = plugin.questManager().quests().size();
            int maxPage = questCount == 0 ? 0 : (questCount - 1) / 45;
            if (event.getSlot() == 45) {
                if (page > 0) {
                    plugin.guiManager().openQuestEditor(player, page - 1);
                }
                return;
            }
            if (event.getSlot() == 53) {
                if (page < maxPage) {
                    plugin.guiManager().openQuestEditor(player, page + 1);
                }
                return;
            }
            if (event.getSlot() == 49) {
                plugin.promptManager().prompt(player, Text.mm("<yellow>Quest erstellen: <id> <name>"), input -> {
                    String[] parts = input.trim().split("\\s+", 2);
                    if (parts.length < 2) {
                        player.sendMessage(Text.mm("<red>Format: <id> <name>"));
                        return;
                    }
                    String id = parts[0];
                    if (plugin.questManager().getQuest(id) != null) {
                        player.sendMessage(Text.mm("<red>Quest existiert bereits."));
                        return;
                    }
                    Quest quest = new Quest(id);
                    quest.setName(parts[1]);
                    quest.setDescription("Neue Quest");
                    quest.setRepeatable(false);
                    quest.setMinLevel(1);
                    quest.setSteps(new java.util.ArrayList<>());
                    plugin.questManager().quests().put(id, quest);
                    plugin.questManager().saveQuest(quest);
                    plugin.auditLog().log(player, "Quest erstellt (GUI): " + id);
                    player.sendMessage(Text.mm("<green>Quest erstellt: " + id));
                    plugin.guiManager().openQuestEditor(player, page);
                });
                return;
            }
            String questId = resolveQuestId(current);
            if (questId == null) {
                return;
            }
            if (event.isRightClick()) {
                if (plugin.questManager().quests().remove(questId) != null) {
                    plugin.questManager().saveAll();
                    plugin.auditLog().log(player, "Quest gelöscht (GUI): " + questId);
                    player.sendMessage(Text.mm("<red>Quest gelöscht: " + questId));
                    plugin.guiManager().openQuestEditor(player, page);
                }
                return;
            }
            Quest quest = plugin.questManager().getQuest(questId);
            if (quest == null) {
                return;
            }
            plugin.promptManager().prompt(player, Text.mm("<yellow>Quest bearbeiten: <name|desc|minlevel|repeatable|event|addstep> ..."), input -> {
                String[] parts = input.trim().split("\\s+");
                if (parts.length == 0 || parts[0].isBlank()) {
                    player.sendMessage(Text.mm("<red>Ungültige Eingabe."));
                    return;
                }
                String action = parts[0].toLowerCase(Locale.ROOT);
                switch (action) {
                    case "name" -> {
                        if (parts.length < 2) {
                            player.sendMessage(Text.mm("<red>Format: name <wert>"));
                            return;
                        }
                        String name = input.substring(input.indexOf(' ') + 1).trim();
                        if (name.isBlank()) {
                            player.sendMessage(Text.mm("<red>Name darf nicht leer sein."));
                            return;
                        }
                        quest.setName(name);
                    }
                    case "desc" -> {
                        if (parts.length < 2) {
                            player.sendMessage(Text.mm("<red>Format: desc <text>"));
                            return;
                        }
                        String desc = input.substring(input.indexOf(' ') + 1).trim();
                        quest.setDescription(desc);
                    }
                    case "minlevel" -> {
                        if (parts.length < 2) {
                            player.sendMessage(Text.mm("<red>Format: minlevel <level>"));
                            return;
                        }
                        Integer level = parseInt(parts[1]);
                        if (level == null || level < 1) {
                            player.sendMessage(Text.mm("<red>Level ungültig."));
                            return;
                        }
                        quest.setMinLevel(level);
                    }
                    case "repeatable" -> {
                        if (parts.length < 2) {
                            player.sendMessage(Text.mm("<red>Format: repeatable <true|false>"));
                            return;
                        }
                        quest.setRepeatable(Boolean.parseBoolean(parts[1]));
                    }
                    case "event" -> {
                        if (parts.length < 2) {
                            player.sendMessage(Text.mm("<red>Format: event <eventId|none>"));
                            return;
                        }
                        String eventId = parts[1];
                        quest.setRequiredEvent(eventId.equalsIgnoreCase("none") ? null : eventId);
                    }
                    case "addstep" -> {
                        if (parts.length < 4) {
                            player.sendMessage(Text.mm("<red>Format: addstep <type> <target> <amount>"));
                            return;
                        }
                        Optional<QuestStepType> typeOpt = parseEnum(QuestStepType.class, parts[1]);
                        if (typeOpt.isEmpty()) {
                            player.sendMessage(Text.mm("<red>Unbekannter Step-Typ."));
                            return;
                        }
                        Integer amount = parseInt(parts[3]);
                        if (amount == null || amount < 1) {
                            player.sendMessage(Text.mm("<red>Amount muss >= 1 sein."));
                            return;
                        }
                        quest.steps().add(new QuestStep(typeOpt.get(), parts[2], amount));
                    }
                    default -> {
                        player.sendMessage(Text.mm("<red>Unbekannte Aktion."));
                        return;
                    }
                }
                plugin.questManager().saveQuest(quest);
                plugin.auditLog().log(player, "Quest aktualisiert (GUI): " + quest.id());
                player.sendMessage(Text.mm("<green>Quest aktualisiert."));
                plugin.guiManager().openQuestEditor(player, page);
            });
            return;
        }
        if (holder instanceof GuiHolders.LootEditorHolder lootHolder) {
            event.setCancelled(true);
            int page = lootHolder.page();
            if (event.getSlot() == 45) {
                plugin.guiManager().openLootEditor(player, page - 1);
                return;
            }
            if (event.getSlot() == 53) {
                plugin.guiManager().openLootEditor(player, page + 1);
                return;
            }
            if (event.getSlot() == 49) {
                plugin.promptManager().prompt(player, Text.mm("<yellow>Loot-Tabelle erstellen: <id> <appliesTo>"), input -> {
                    String[] parts = input.trim().split("\\s+", 2);
                    if (parts.length < 2) {
                        player.sendMessage(Text.mm("<red>Format: <id> <appliesTo>"));
                        return;
                    }
                    String id = parts[0];
                    if (plugin.lootManager().getTable(id) != null) {
                        player.sendMessage(Text.mm("<red>Loot-Tabelle existiert bereits."));
                        return;
                    }
                    LootTable table = new LootTable(id);
                    table.setAppliesTo(parts[1]);
                    plugin.lootManager().tables().put(id, table);
                    plugin.lootManager().saveTable(table);
                    plugin.auditLog().log(player, "Loot-Tabelle erstellt (GUI): " + id);
                    player.sendMessage(Text.mm("<green>Loot-Tabelle erstellt."));
                    plugin.guiManager().openLootEditor(player, page);
                });
                return;
            }
            String tableId = resolveLootId(current);
            if (tableId == null) {
                return;
            }
            if (event.isRightClick()) {
                if (plugin.lootManager().tables().remove(tableId) != null) {
                    plugin.lootManager().saveAll();
                    plugin.auditLog().log(player, "Loot-Tabelle gelöscht (GUI): " + tableId);
                    player.sendMessage(Text.mm("<red>Loot-Tabelle gelöscht: " + tableId));
                    plugin.guiManager().openLootEditor(player, page);
                }
                return;
            }
            LootTable table = plugin.lootManager().getTable(tableId);
            if (table == null) {
                return;
            }
            plugin.promptManager().prompt(player, Text.mm("<yellow>Loot bearbeiten: <applies|addentry|clear> ..."), input -> {
                String[] parts = input.trim().split("\\s+");
                if (parts.length == 0 || parts[0].isBlank()) {
                    player.sendMessage(Text.mm("<red>Ungültige Eingabe."));
                    return;
                }
                String action = parts[0].toLowerCase(Locale.ROOT);
                switch (action) {
                    case "applies" -> {
                        if (parts.length < 2) {
                            player.sendMessage(Text.mm("<red>Format: applies <target>"));
                            return;
                        }
                        table.setAppliesTo(parts[1]);
                    }
                    case "addentry" -> {
                        if (parts.length < 6) {
                            player.sendMessage(Text.mm("<red>Format: addentry <material> <chance> <min> <max> <rarity>"));
                            return;
                        }
                        Material mat = Material.matchMaterial(parts[1].toUpperCase(Locale.ROOT));
                        if (mat == null) {
                            player.sendMessage(Text.mm("<red>Material ungültig."));
                            return;
                        }
                        Double chance = parseDouble(parts[2]);
                        Integer min = parseInt(parts[3]);
                        Integer max = parseInt(parts[4]);
                        Optional<Rarity> rarityOpt = parseEnum(Rarity.class, parts[5]);
                        if (chance == null || min == null || max == null || rarityOpt.isEmpty()) {
                            player.sendMessage(Text.mm("<red>Parameter ungültig."));
                            return;
                        }
                        if (chance < 0.0 || chance > 1.0 || min < 1 || max < min) {
                            player.sendMessage(Text.mm("<red>Chance 0..1 und min/max prüfen."));
                            return;
                        }
                        table.entries().add(new LootEntry(mat.name(), chance, min, max, rarityOpt.get()));
                    }
                    case "clear" -> table.entries().clear();
                    default -> {
                        player.sendMessage(Text.mm("<red>Unbekannte Aktion."));
                        return;
                    }
                }
                plugin.lootManager().saveTable(table);
                plugin.auditLog().log(player, "Loot-Tabelle aktualisiert (GUI): " + table.id());
                player.sendMessage(Text.mm("<green>Loot-Tabelle aktualisiert."));
                plugin.guiManager().openLootEditor(player, page);
            });
            return;
        }
        if (holder instanceof GuiHolders.SkillAdminHolder skillAdminHolder) {
            event.setCancelled(true);
            int page = skillAdminHolder.page();
            if (event.getSlot() == 45) {
                plugin.guiManager().openSkillAdmin(player, page - 1);
                return;
            }
            if (event.getSlot() == 53) {
                plugin.guiManager().openSkillAdmin(player, page + 1);
                return;
            }
            if (event.getSlot() == 48) {
                plugin.guiManager().openClassAdmin(player, 0);
                return;
            }
            if (event.getSlot() == 49) {
                plugin.promptManager().prompt(player, Text.mm("<yellow>Skill erstellen: <id>"), input -> {
                    String id = input.trim().toLowerCase(Locale.ROOT);
                    if (id.isBlank()) {
                        player.sendMessage(Text.mm("<red>ID darf nicht leer sein."));
                        return;
                    }
                    if (plugin.skillManager().getSkill(id) != null) {
                        player.sendMessage(Text.mm("<red>Skill existiert bereits."));
                        return;
                    }
                    Skill skill = new Skill(id);
                    skill.setName(id);
                    skill.setType(SkillType.ACTIVE);
                    skill.setCategory(SkillCategory.ATTACK);
                    skill.setCooldown(10);
                    skill.setManaCost(10);
                    skill.setEffects(new java.util.ArrayList<>());
                    plugin.skillManager().skills().put(id, skill);
                    plugin.skillManager().saveSkill(skill);
                    plugin.auditLog().log(player, "Skill erstellt (GUI): " + id);
                    player.sendMessage(Text.mm("<green>Skill erstellt: " + id));
                    plugin.guiManager().openSkillAdmin(player, page);
                });
                return;
            }
            Skill skill = resolveSkill(current);
            if (skill == null) {
                return;
            }
            if (event.isRightClick()) {
                if (plugin.skillManager().skills().remove(skill.id()) != null) {
                    plugin.skillManager().saveAll();
                    removeSkillFromClasses(skill.id());
                    plugin.auditLog().log(player, "Skill gelöscht (GUI): " + skill.id());
                    player.sendMessage(Text.mm("<red>Skill gelöscht: " + skill.id()));
                    plugin.guiManager().openSkillAdmin(player, page);
                }
                return;
            }
            plugin.promptManager().prompt(player, Text.mm("<yellow>Skill bearbeiten: <name|cooldown|mana|category|type|requires|addeffect|cleareffects> ..."), input -> {
                String[] parts = input.trim().split("\\s+");
                if (parts.length == 0 || parts[0].isBlank()) {
                    player.sendMessage(Text.mm("<red>Ungültige Eingabe."));
                    return;
                }
                String action = parts[0].toLowerCase(Locale.ROOT);
                switch (action) {
                    case "name" -> {
                        if (parts.length < 2) {
                            player.sendMessage(Text.mm("<red>Format: name <wert>"));
                            return;
                        }
                        String name = input.substring(input.indexOf(' ') + 1).trim();
                        if (name.isBlank()) {
                            player.sendMessage(Text.mm("<red>Name darf nicht leer sein."));
                            return;
                        }
                        skill.setName(name);
                    }
                    case "cooldown" -> {
                        if (parts.length < 2) {
                            player.sendMessage(Text.mm("<red>Format: cooldown <wert>"));
                            return;
                        }
                        Integer value = parseInt(parts[1]);
                        if (value == null || value < 0) {
                            player.sendMessage(Text.mm("<red>Cooldown ungültig."));
                            return;
                        }
                        skill.setCooldown(value);
                    }
                    case "mana" -> {
                        if (parts.length < 2) {
                            player.sendMessage(Text.mm("<red>Format: mana <wert>"));
                            return;
                        }
                        Integer value = parseInt(parts[1]);
                        if (value == null || value < 0) {
                            player.sendMessage(Text.mm("<red>Mana ungültig."));
                            return;
                        }
                        skill.setManaCost(value);
                    }
                    case "category" -> {
                        if (parts.length < 2) {
                            player.sendMessage(Text.mm("<red>Format: category <kategorie>"));
                            return;
                        }
                        Optional<SkillCategory> category = parseEnum(SkillCategory.class, parts[1]);
                        if (category.isEmpty()) {
                            player.sendMessage(Text.mm("<red>Unbekannte Kategorie."));
                            return;
                        }
                        skill.setCategory(category.get());
                    }
                    case "type" -> {
                        if (parts.length < 2) {
                            player.sendMessage(Text.mm("<red>Format: type <typ>"));
                            return;
                        }
                        Optional<SkillType> type = parseEnum(SkillType.class, parts[1]);
                        if (type.isEmpty()) {
                            player.sendMessage(Text.mm("<red>Unbekannter Typ."));
                            return;
                        }
                        skill.setType(type.get());
                    }
                    case "requires" -> {
                        if (parts.length < 2) {
                            player.sendMessage(Text.mm("<red>Format: requires <skillId|none>"));
                            return;
                        }
                        skill.setRequiredSkill(parts[1].equalsIgnoreCase("none") ? null : parts[1]);
                    }
                    case "addeffect" -> {
                        if (parts.length < 2) {
                            player.sendMessage(Text.mm("<red>Format: addeffect <effectType> <param:value>..."));
                            return;
                        }
                        Optional<SkillEffectType> typeOpt = parseEnum(SkillEffectType.class, parts[1]);
                        if (typeOpt.isEmpty()) {
                            player.sendMessage(Text.mm("<red>Unbekannter Effekt-Typ."));
                            return;
                        }
                        java.util.Map<String, Object> params = new java.util.HashMap<>();
                        for (int i = 2; i < parts.length; i++) {
                            String token = parts[i];
                            if (!token.contains(":")) {
                                continue;
                            }
                            String[] pair = token.split(":", 2);
                            params.put(pair[0], parseParamValue(pair[1]));
                        }
                        skill.effects().add(new SkillEffectConfig(typeOpt.get(), params));
                    }
                    case "cleareffects" -> skill.effects().clear();
                    default -> {
                        player.sendMessage(Text.mm("<red>Unbekannte Aktion."));
                        return;
                    }
                }
                plugin.skillManager().saveSkill(skill);
                plugin.auditLog().log(player, "Skill aktualisiert (GUI): " + skill.id());
                player.sendMessage(Text.mm("<green>Skill aktualisiert."));
                plugin.guiManager().openSkillAdmin(player, page);
            });
            return;
        }
        if (holder instanceof GuiHolders.ClassAdminHolder classAdminHolder) {
            event.setCancelled(true);
            int page = classAdminHolder.page();
            if (event.getSlot() == 45) {
                plugin.guiManager().openClassAdmin(player, page - 1);
                return;
            }
            if (event.getSlot() == 53) {
                plugin.guiManager().openClassAdmin(player, page + 1);
                return;
            }
            if (event.getSlot() == 49) {
                plugin.promptManager().prompt(player, Text.mm("<yellow>Klasse erstellen: <id> <name>"), input -> {
                    String[] parts = input.trim().split("\\s+", 2);
                    if (parts.length < 2) {
                        player.sendMessage(Text.mm("<red>Format: <id> <name>"));
                        return;
                    }
                    String id = parts[0];
                    if (plugin.classManager().getClass(id) != null) {
                        player.sendMessage(Text.mm("<red>Klasse existiert bereits."));
                        return;
                    }
                    ClassDefinition definition = new ClassDefinition(id);
                    definition.setName(parts[1]);
                    definition.setStartSkills(new java.util.ArrayList<>());
                    plugin.classManager().classes().put(id, definition);
                    plugin.classManager().saveClass(definition);
                    plugin.auditLog().log(player, "Klasse erstellt (GUI): " + id);
                    player.sendMessage(Text.mm("<green>Klasse erstellt: " + id));
                    plugin.guiManager().openClassAdmin(player, page);
                });
                return;
            }
            String classId = resolveClassId(current);
            if (classId == null) {
                return;
            }
            if (event.isRightClick()) {
                if (plugin.classManager().classes().remove(classId) != null) {
                    plugin.classManager().saveAll();
                    plugin.auditLog().log(player, "Klasse gelöscht (GUI): " + classId);
                    player.sendMessage(Text.mm("<red>Klasse gelöscht: " + classId));
                    plugin.guiManager().openClassAdmin(player, page);
                }
                return;
            }
            ClassDefinition definition = plugin.classManager().getClass(classId);
            if (definition == null) {
                return;
            }
            plugin.promptManager().prompt(player, Text.mm("<yellow>Klasse bearbeiten: <name|addskill|removeskill> ..."), input -> {
                String[] parts = input.trim().split("\\s+");
                if (parts.length == 0 || parts[0].isBlank()) {
                    player.sendMessage(Text.mm("<red>Ungültige Eingabe."));
                    return;
                }
                String action = parts[0].toLowerCase(Locale.ROOT);
                switch (action) {
                    case "name" -> {
                        if (parts.length < 2) {
                            player.sendMessage(Text.mm("<red>Format: name <wert>"));
                            return;
                        }
                        String name = input.substring(input.indexOf(' ') + 1).trim();
                        if (name.isBlank()) {
                            player.sendMessage(Text.mm("<red>Name darf nicht leer sein."));
                            return;
                        }
                        definition.setName(name);
                    }
                    case "addskill" -> {
                        if (parts.length < 2) {
                            player.sendMessage(Text.mm("<red>Format: addskill <skillId>"));
                            return;
                        }
                        if (plugin.skillManager().getSkill(parts[1]) == null) {
                            player.sendMessage(Text.mm("<red>Skill nicht gefunden."));
                            return;
                        }
                        if (!definition.startSkills().contains(parts[1])) {
                            definition.startSkills().add(parts[1]);
                        }
                    }
                    case "removeskill" -> {
                        if (parts.length < 2) {
                            player.sendMessage(Text.mm("<red>Format: removeskill <skillId>"));
                            return;
                        }
                        definition.startSkills().remove(parts[1]);
                    }
                    default -> {
                        player.sendMessage(Text.mm("<red>Unbekannte Aktion."));
                        return;
                    }
                }
                plugin.classManager().saveClass(definition);
                plugin.auditLog().log(player, "Klasse aktualisiert (GUI): " + definition.id());
                player.sendMessage(Text.mm("<green>Klasse aktualisiert."));
                plugin.guiManager().openClassAdmin(player, page);
            });
            return;
        }
        if (holder instanceof GuiHolders.BuildingCategoryHolder) {
            event.setCancelled(true);
            String category = resolveBuildingCategory(current);
            if (category == null) {
                return;
            }
            if ("SINGLE".equalsIgnoreCase(category)) {
                player.closeInventory();
                plugin.promptManager().prompt(player, Text.mm("<yellow>Schematic-Dateiname eingeben (z.B. haus.schem):"), input -> {
                    plugin.buildingManager().beginSingleSchematicPlacement(player, input, com.example.rpg.schematic.Transform.Rotation.NONE);
                });
                return;
            }
            plugin.guiManager().openBuildingList(player, com.example.rpg.model.BuildingCategory.fromString(category));
            return;
        }
        if (holder instanceof GuiHolders.BuildingListHolder buildingListHolder) {
            event.setCancelled(true);
            int page = buildingListHolder.page();
            if (event.getSlot() == 45) {
                plugin.guiManager().openBuildingList(player,
                    com.example.rpg.model.BuildingCategory.fromString(buildingListHolder.category()), page - 1);
                return;
            }
            if (event.getSlot() == 53) {
                plugin.guiManager().openBuildingList(player,
                    com.example.rpg.model.BuildingCategory.fromString(buildingListHolder.category()), page + 1);
                return;
            }
            String buildingId = resolveBuilding(current);
            if (buildingId == null) {
                return;
            }
            plugin.buildingManager().beginPlacement(player, buildingId, com.example.rpg.schematic.Transform.Rotation.NONE);
            player.closeInventory();
            return;
        }
        if (holder instanceof GuiHolders.QuestListHolder questListHolder) {
            event.setCancelled(true);
            int page = questListHolder.page();
            if (event.getSlot() == 18) {
                plugin.guiManager().openQuestList(player, page - 1);
                return;
            }
            if (event.getSlot() == 26) {
                plugin.guiManager().openQuestList(player, page + 1);
                return;
            }
            Quest quest = resolveQuest(current);
            if (quest == null) {
                return;
            }
            plugin.guiManager().openQuestDetails(player, quest.id(), false, page);
            return;
        }
        if (holder instanceof GuiHolders.QuestLogHolder questLogHolder) {
            event.setCancelled(true);
            int page = questLogHolder.page();
            if (event.getSlot() == 45) {
                plugin.guiManager().openQuestLog(player, page - 1);
                return;
            }
            if (event.getSlot() == 53) {
                plugin.guiManager().openQuestLog(player, page + 1);
                return;
            }
            if (event.getSlot() == 49) {
                plugin.guiManager().openQuestList(player, 0);
                return;
            }
            Quest quest = resolveQuest(current);
            if (quest == null) {
                return;
            }
            plugin.guiManager().openQuestDetails(player, quest.id(), true, page);
            return;
        }
        if (holder instanceof GuiHolders.QuestDetailHolder questDetailHolder) {
            event.setCancelled(true);
            if (event.getSlot() == 26) {
                if (questDetailHolder.active()) {
                    plugin.guiManager().openQuestLog(player, questDetailHolder.page());
                } else {
                    plugin.guiManager().openQuestList(player, questDetailHolder.page());
                }
                return;
            }
            if (event.getSlot() != 22) {
                return;
            }
            if (questDetailHolder.active()) {
                return;
            }
            Quest quest = plugin.questManager().getQuest(questDetailHolder.questId());
            if (quest == null) {
                player.sendMessage(Text.mm("<red>Quest nicht gefunden."));
                return;
            }
            if (quest.requiredEvent() != null && !plugin.worldEventManager().isCompleted(quest.requiredEvent())) {
                player.sendMessage(Text.mm("<red>Quest noch gesperrt."));
                return;
            }
            PlayerProfile profile = plugin.playerDataManager().getProfile(player);
            if (profile.level() < quest.minLevel()) {
                player.sendMessage(Text.mm("<red>Du brauchst Level " + quest.minLevel() + "."));
                return;
            }
            if (profile.activeQuests().containsKey(quest.id())) {
                player.sendMessage(Text.mm("<yellow>Quest bereits aktiv."));
                return;
            }
            if (profile.completedQuests().contains(quest.id()) && !quest.repeatable()) {
                player.sendMessage(Text.mm("<red>Quest bereits abgeschlossen."));
                return;
            }
            profile.activeQuests().put(quest.id(), new QuestProgress(quest.id()));
            player.sendMessage(Text.mm("<green>Quest angenommen: " + quest.name()));
            plugin.guiManager().openQuestLog(player, 0);
            return;
        }
        if (holder instanceof GuiHolders.SkillListHolder skillListHolder) {
            event.setCancelled(true);
            int page = skillListHolder.page();
            if (event.getSlot() == 18) {
                plugin.guiManager().openSkillList(player, page - 1);
                return;
            }
            if (event.getSlot() == 26) {
                plugin.guiManager().openSkillList(player, page + 1);
                return;
            }
            Skill skill = resolveSkill(current);
            if (skill == null) {
                return;
            }
            PlayerProfile profile = plugin.playerDataManager().getProfile(player);
            if (profile.skillPoints() <= 0) {
                player.sendMessage(Text.mm("<red>Keine Skillpunkte."));
                return;
            }
            if (skill.requiredSkill() != null && !profile.learnedSkills().containsKey(skill.requiredSkill())) {
                player.sendMessage(Text.mm("<red>Du musst zuerst " + skill.requiredSkill() + " lernen."));
                return;
            }
            profile.learnedSkills().put(skill.id(), profile.learnedSkills().getOrDefault(skill.id(), 0) + 1);
            profile.setSkillPoints(profile.skillPoints() - 1);
            player.sendMessage(Text.mm("<green>Skill gelernt: " + skill.name()));
            plugin.guiManager().openSkillList(player, page);
            return;
        }
        if (holder instanceof GuiHolders.SkillTreeHolder) {
            event.setCancelled(true);
            Skill skill = resolveSkill(current);
            if (skill == null) {
                return;
            }
            PlayerProfile profile = plugin.playerDataManager().getProfile(player);
            if (profile.skillPoints() <= 0) {
                player.sendMessage(Text.mm("<red>Keine Skillpunkte."));
                return;
            }
            if (skill.requiredSkill() != null && !profile.learnedSkills().containsKey(skill.requiredSkill())) {
                player.sendMessage(Text.mm("<red>Du musst zuerst " + skill.requiredSkill() + " lernen."));
                return;
            }
            if (profile.learnedSkills().containsKey(skill.id())) {
                player.sendMessage(Text.mm("<yellow>Bereits gelernt."));
                return;
            }
            profile.learnedSkills().put(skill.id(), 1);
            profile.setSkillPoints(profile.skillPoints() - 1);
            player.sendMessage(Text.mm("<green>Skill gelernt: " + skill.name()));
            plugin.skillTreeGui().open(player);
            return;
        }
        if (holder instanceof GuiHolders.ShopHolder shopHolder) {
            event.setCancelled(true);
            handleShopClick(player, event.getInventory(), event.getSlot(), current, shopHolder, event.isRightClick());
        }
        if (holder instanceof GuiHolders.SchematicMoveHolder) {
            event.setCancelled(true);
            switch (event.getSlot()) {
                case 11 -> plugin.buildingManager().moveLastPlacement(player, -1, 0, 0);
                case 15 -> plugin.buildingManager().moveLastPlacement(player, 1, 0, 0);
                case 13 -> plugin.buildingManager().moveLastPlacement(player, 0, 1, 0);
                case 22 -> plugin.buildingManager().moveLastPlacement(player, 0, -1, 0);
                case 26 -> player.closeInventory();
                default -> {
                }
            }
            return;
        }
        if (holder instanceof GuiHolders.PermissionsMainHolder) {
            event.setCancelled(true);
            switch (event.getSlot()) {
                case 11 -> plugin.guiManager().openRoleList(player, 0);
                case 13 -> plugin.guiManager().openPlayerList(player);
                case 15 -> plugin.guiManager().openAuditLog(player);
                default -> {
                }
            }
            return;
        }
        if (holder instanceof GuiHolders.RoleListHolder roleListHolder) {
            event.setCancelled(true);
            if (event.getSlot() == 45) {
                plugin.guiManager().openPermissionsMain(player);
                return;
            }
            if (event.getSlot() == 53) {
                plugin.promptManager().prompt(player, Text.mm("<yellow>Rolle erstellen: <key> <displayName>"), input -> {
                    String[] parts = input.split("\\s+", 2);
                    if (parts.length < 2) {
                        player.sendMessage(Text.mm("<red>Format: <key> <displayName>"));
                        return;
                    }
                    plugin.permissionService().createRole(player, parts[0], parts[1]);
                    plugin.guiManager().openRoleList(player, roleListHolder.page());
                });
                return;
            }
            String roleKey = resolveRoleKey(current);
            if (roleKey != null) {
                plugin.guiManager().openRoleDetails(player, roleKey);
            }
            return;
        }
        if (holder instanceof GuiHolders.RoleDetailHolder roleDetailHolder) {
            event.setCancelled(true);
            String roleKey = roleDetailHolder.roleKey();
            switch (event.getSlot()) {
                case 11 -> plugin.guiManager().openRoleParents(player, roleKey);
                case 13 -> plugin.guiManager().openRoleNodes(player, roleKey, 0);
                case 15 -> plugin.promptManager().prompt(player, Text.mm("<yellow>Neuer Display-Name für " + roleKey + ":"), input -> {
                    if (input.isBlank()) {
                        player.sendMessage(Text.mm("<red>Display-Name darf nicht leer sein."));
                        return;
                    }
                    plugin.permissionService().renameRole(player, roleKey, input.trim());
                    plugin.guiManager().openRoleDetails(player, roleKey);
                });
                case 26 -> {
                    plugin.permissionService().deleteRole(player, roleKey);
                    plugin.guiManager().openRoleList(player, 0);
                }
                default -> {
                }
            }
            return;
        }
        if (holder instanceof GuiHolders.RoleParentsHolder roleParentsHolder) {
            event.setCancelled(true);
            if (event.getSlot() == 45) {
                plugin.guiManager().openRoleDetails(player, roleParentsHolder.roleKey());
                return;
            }
            String parentKey = resolveNodeKey(current);
            if (parentKey == null) {
                return;
            }
            String roleKey = roleParentsHolder.roleKey();
            var role = plugin.permissionService().roles().get(roleKey);
            if (role == null) {
                return;
            }
            if (role.parents().contains(parentKey)) {
                plugin.permissionService().removeParent(player, roleKey, parentKey);
            } else {
                plugin.permissionService().addParent(player, roleKey, parentKey);
            }
            plugin.guiManager().openRoleParents(player, roleKey);
            return;
        }
        if (holder instanceof GuiHolders.RoleNodesHolder roleNodesHolder) {
            event.setCancelled(true);
            if (event.getSlot() == 45) {
                plugin.guiManager().openRoleDetails(player, roleNodesHolder.roleKey());
                return;
            }
            if (event.getSlot() == 53) {
                plugin.promptManager().prompt(player, Text.mm("<yellow>Node setzen: <node> <allow|deny|inherit>"), input -> {
                    String[] parts = input.split("\\s+", 2);
                    if (parts.length < 2) {
                        player.sendMessage(Text.mm("<red>Format: <node> <allow|deny|inherit>"));
                        return;
                    }
                    var decision = parseDecision(parts[1]);
                    plugin.permissionService().setRoleNode(player, roleNodesHolder.roleKey(), parts[0], decision);
                    plugin.guiManager().openRoleNodes(player, roleNodesHolder.roleKey(), roleNodesHolder.page());
                });
                return;
            }
            String node = resolveNodeKey(current);
            if (node == null) {
                return;
            }
            var role = plugin.permissionService().roles().get(roleNodesHolder.roleKey());
            if (role == null) {
                return;
            }
            var currentDecision = role.nodes().getOrDefault(node, com.example.rpg.permissions.PermissionDecision.INHERIT);
            var nextDecision = switch (currentDecision) {
                case INHERIT -> com.example.rpg.permissions.PermissionDecision.ALLOW;
                case ALLOW -> com.example.rpg.permissions.PermissionDecision.DENY;
                case DENY -> com.example.rpg.permissions.PermissionDecision.INHERIT;
            };
            plugin.permissionService().setRoleNode(player, roleNodesHolder.roleKey(), node, nextDecision);
            plugin.guiManager().openRoleNodes(player, roleNodesHolder.roleKey(), roleNodesHolder.page());
            return;
        }
        if (holder instanceof GuiHolders.PlayerListHolder playerListHolder) {
            event.setCancelled(true);
            int page = playerListHolder.page();
            if (event.getSlot() == 45) {
                plugin.guiManager().openPlayerList(player, page - 1);
                return;
            }
            if (event.getSlot() == 53) {
                plugin.guiManager().openPlayerList(player, page + 1);
                return;
            }
            if (event.getSlot() == 47) {
                plugin.guiManager().openPermissionsMain(player);
                return;
            }
            if (event.getSlot() == 49) {
                plugin.promptManager().prompt(player, Text.mm("<yellow>Spielername eingeben:"), input -> {
                    var target = plugin.getServer().getOfflinePlayer(input);
                    if (target == null) {
                        player.sendMessage(Text.mm("<red>Spieler nicht gefunden."));
                        return;
                    }
                    plugin.guiManager().openPlayerRoles(player, target.getUniqueId());
                });
                return;
            }
            UUID targetId = resolvePlayerId(current);
            if (targetId != null) {
                plugin.guiManager().openPlayerRoles(player, targetId);
            }
            return;
        }
        if (holder instanceof GuiHolders.PlayerRoleHolder playerRoleHolder) {
            event.setCancelled(true);
            UUID targetId = playerRoleHolder.targetId();
            switch (event.getSlot()) {
                case 12 -> plugin.promptManager().prompt(player, Text.mm("<yellow>Primary Rolle setzen:"), input -> {
                    plugin.permissionService().assignPrimary(player, targetId, input.trim());
                    plugin.guiManager().openPlayerRoles(player, targetId);
                });
                case 14 -> plugin.promptManager().prompt(player, Text.mm("<yellow>Rolle hinzufügen:"), input -> {
                    plugin.permissionService().addRole(player, targetId, input.trim());
                    plugin.guiManager().openPlayerRoles(player, targetId);
                });
                case 16 -> plugin.promptManager().prompt(player, Text.mm("<yellow>Rolle entfernen:"), input -> {
                    plugin.permissionService().removeRole(player, targetId, input.trim());
                    plugin.guiManager().openPlayerRoles(player, targetId);
                });
                case 22 -> plugin.promptManager().prompt(player, Text.mm("<yellow>Node prüfen:"), input -> {
                    var explain = plugin.permissionService().explain(targetId, input.trim());
                    player.sendMessage(Text.mm("<yellow>Ergebnis: " + (explain.allowed() ? "ALLOW" : "DENY")));
                    if (explain.winningRole() != null) {
                        player.sendMessage(Text.mm("<gray>Role: " + explain.winningRole() + " Node: " + explain.winningNode()));
                    }
                });
                default -> {
                }
            }
            return;
        }
        if (holder instanceof GuiHolders.PermissionAuditHolder auditHolder) {
            event.setCancelled(true);
            int page = auditHolder.page();
            if (event.getSlot() == 45) {
                plugin.guiManager().openAuditLog(player, page - 1);
                return;
            }
            if (event.getSlot() == 53) {
                plugin.guiManager().openAuditLog(player, page + 1);
                return;
            }
            if (event.getSlot() == 49) {
                plugin.guiManager().openPermissionsMain(player);
            }
            return;
        }
        if (holder instanceof GuiHolders.EnchantingHolder enchantingHolder) {
            event.setCancelled(true);
            int page = enchantingHolder.page();
            if (event.getSlot() == 18) {
                plugin.guiManager().openEnchanting(player, enchantingHolder.recipeId(), page - 1);
                return;
            }
            if (event.getSlot() == 26) {
                plugin.guiManager().openEnchanting(player, enchantingHolder.recipeId(), page + 1);
                return;
            }
            if (event.getSlot() == 25) {
                player.closeInventory();
                return;
            }
            if (event.getSlot() == 22) {
                String recipeId = enchantingHolder.recipeId();
                if (recipeId == null) {
                    player.sendMessage(Text.mm("<red>Kein Rezept ausgewählt."));
                    return;
                }
                plugin.enchantManager().applyRecipe(player, recipeId);
                plugin.guiManager().openEnchanting(player, recipeId, page);
                return;
            }
            String recipeId = resolveEnchantRecipeId(current);
            if (recipeId != null) {
                plugin.guiManager().openEnchanting(player, recipeId, page);
            }
        }
    }

    private void toggleDungeonSetting(Player player, String path) {
        boolean current = plugin.getConfig().getBoolean(path, false);
        plugin.getConfig().set(path, !current);
        plugin.saveConfig();
        player.sendMessage(Text.mm("<green>" + path + " => " + (!current)));
        plugin.guiManager().openDungeonAdmin(player);
    }

    private void promptDungeonTheme(Player player) {
        plugin.promptManager().prompt(player, Text.mm("<yellow>Dungeon-Theme eingeben (z.B. wfc):"), input -> {
            String theme = input.trim();
            if (theme.isBlank()) {
                player.sendMessage(Text.mm("<red>Theme darf nicht leer sein."));
                return;
            }
            plugin.dungeonManager().generateDungeon(player, theme, List.of(player));
            player.sendMessage(Text.mm("<green>Dungeon-Generierung gestartet: " + theme));
            plugin.guiManager().openDungeonAdmin(player);
        });
    }

    private void promptSchematicPlacement(Player player) {
        plugin.promptManager().prompt(player, Text.mm("<yellow>Schematic-Dateiname (z.B. start_room.schem):"), input -> {
            String name = input.trim();
            if (name.isBlank()) {
                player.sendMessage(Text.mm("<red>Dateiname darf nicht leer sein."));
                return;
            }
            plugin.buildingManager().beginSingleSchematicPlacement(player, name, Transform.Rotation.NONE);
            player.sendMessage(Text.mm("<green>Platzierungsmodus aktiv für: " + name));
            player.sendMessage(Text.mm("<gray>Rechtsklick auf einen Block zum Platzieren."));
        });
    }

    private void promptSchematicSave(Player player) {
        Location pos1 = readPosition(player, "pos1");
        Location pos2 = readPosition(player, "pos2");
        if (pos1 == null || pos2 == null) {
            player.sendMessage(Text.mm("<red>Setze Pos1/Pos2 mit der Wand."));
            return;
        }
        plugin.promptManager().prompt(player,
            Text.mm("<yellow>Zielpfad (z.B. dungeon_rooms/crypt/start_room.schem):"),
            input -> {
                String path = input.trim();
                if (path.isBlank()) {
                    player.sendMessage(Text.mm("<red>Dateiname darf nicht leer sein."));
                    return;
                }
                plugin.buildingManager().saveSelectionAsSchematic(player, path, pos1, pos2);
                plugin.guiManager().openDungeonAdmin(player);
            });
    }

    private void sendDungeonSetupInfo(Player player) {
        player.sendMessage(Text.mm("<gold>Dungeon Setup (Kurzinfo)</gold>"));
        player.sendMessage(Text.mm("<gray>Wand: <white>/rpgadmin wand</white> -> Pos1/Pos2 setzen"));
        player.sendMessage(Text.mm("<gray>Speichern: <white>Dungeons → Schematic speichern</white>"));
        player.sendMessage(Text.mm("<gray>Schematics: <white>plugins/RPGPlugin/dungeon_rooms/<theme>/</white>"));
        player.sendMessage(Text.mm("<gray>Beispiele: <white>start_room.schem, boss_room.schem</white>"));
        player.sendMessage(Text.mm("<gray>Jigsaw-Socket: <white>name = corridor_ns</white>"));
        player.sendMessage(Text.mm("<gray>WFC-Patterns: <white>plugins/RPGPlugin/wfc/<theme>/</white>"));
    }

    private void giveWand(Player player) {
        ItemStack wand = new ItemBuilder(Material.STICK)
            .name(Text.mm("<yellow>Editor Wand"))
            .loreLine(Text.mm("<gray>Links: Pos1, Rechts: Pos2"))
            .build();
        var meta = wand.getItemMeta();
        meta.getPersistentDataContainer().set(plugin.wandKey(), PersistentDataType.BYTE, (byte) 1);
        wand.setItemMeta(meta);
        player.getInventory().addItem(wand);
        player.sendMessage(Text.mm("<green>Editor Wand erhalten."));
    }

    private void fillSelection(Player player, Material material) {
        Location pos1 = readPosition(player, "pos1");
        Location pos2 = readPosition(player, "pos2");
        if (pos1 == null || pos2 == null) {
            player.sendMessage(Text.mm("<red>Setze Pos1/Pos2 mit der Wand."));
            return;
        }
        if (!pos1.getWorld().equals(pos2.getWorld())) {
            player.sendMessage(Text.mm("<red>Pos1/Pos2 müssen in derselben Welt sein."));
            return;
        }
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    pos1.getWorld().getBlockAt(x, y, z).setType(material, false);
                }
            }
        }
        player.sendMessage(Text.mm("<green>Bereich gefüllt mit: " + material.name()));
    }

    private Quest resolveQuest(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        String questId = meta.getPersistentDataContainer().get(plugin.questKey(), PersistentDataType.STRING);
        if (questId == null) {
            return null;
        }
        return plugin.questManager().getQuest(questId);
    }

    private Skill resolveSkill(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        String skillId = meta.getPersistentDataContainer().get(plugin.skillKey(), PersistentDataType.STRING);
        if (skillId == null) {
            return null;
        }
        return plugin.skillManager().getSkill(skillId);
    }

    private String resolveBuilding(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(plugin.buildingKey(), PersistentDataType.STRING);
    }

    private String resolveBuildingCategory(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(plugin.buildingCategoryKey(), PersistentDataType.STRING);
    }

    private String resolveRoleKey(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(plugin.permRoleKey(), PersistentDataType.STRING);
    }

    private String resolveZoneId(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(plugin.zoneKey(), PersistentDataType.STRING);
    }

    private String resolveNpcId(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(plugin.npcGuiKey(), PersistentDataType.STRING);
    }

    private NpcRole resolveNpcTemplate(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        String value = meta.getPersistentDataContainer().get(plugin.npcTemplateKey(), PersistentDataType.STRING);
        if (value == null) {
            return null;
        }
        return parseEnum(NpcRole.class, value).orElse(null);
    }

    private String resolveQuestId(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(plugin.questKey(), PersistentDataType.STRING);
    }

    private String resolveLootId(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(plugin.lootKey(), PersistentDataType.STRING);
    }

    private String resolveClassId(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(plugin.classKey(), PersistentDataType.STRING);
    }

    private String resolveNodeKey(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(plugin.permNodeKey(), PersistentDataType.STRING);
    }

    private UUID resolvePlayerId(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        String value = meta.getPersistentDataContainer().get(plugin.permPlayerKey(), PersistentDataType.STRING);
        if (value == null) {
            return null;
        }
        return UUID.fromString(value);
    }

    private String resolveEnchantRecipeId(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(plugin.enchantRecipeKey(), PersistentDataType.STRING);
    }

    private com.example.rpg.permissions.PermissionDecision parseDecision(String value) {
        return switch (value.toLowerCase()) {
            case "allow" -> com.example.rpg.permissions.PermissionDecision.ALLOW;
            case "deny" -> com.example.rpg.permissions.PermissionDecision.DENY;
            default -> com.example.rpg.permissions.PermissionDecision.INHERIT;
        };
    }

    private Location readPosition(Player player, String key) {
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        String value = player.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
        if (value == null) {
            return null;
        }
        String[] parts = value.split(",");
        if (parts.length < 4) {
            return null;
        }
        org.bukkit.World world = plugin.getServer().getWorld(parts[0]);
        if (world == null) {
            return null;
        }
        return new Location(world,
            Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
    }

    private void respawnNpc(Npc npc) {
        runSync(() -> {
            removeNpcEntity(npc);
            plugin.npcManager().spawnNpc(npc);
            plugin.npcManager().saveNpc(npc);
        });
    }

    private void removeNpcEntity(Npc npc) {
        runSync(() -> {
            if (npc.uuid() == null) {
                return;
            }
            Entity entity = plugin.getServer().getEntity(npc.uuid());
            if (entity != null) {
                entity.remove();
            }
            npc.setUuid(null);
        });
    }

    private void removeSkillFromClasses(String skillId) {
        boolean updated = false;
        for (ClassDefinition definition : plugin.classManager().classes().values()) {
            if (definition.startSkills().remove(skillId)) {
                updated = true;
            }
        }
        if (updated) {
            plugin.classManager().saveAll();
        }
    }

    private void handleNpcTemplateClick(Player player, NpcRole templateRole) {
        if (templateRole == NpcRole.VENDOR) {
            plugin.promptManager().prompt(player, Text.mm("<yellow>Shop-NPC: <id> <shopId>"), input -> {
                String[] parts = input.trim().split("\\s+");
                runSync(() -> {
                    if (parts.length < 2) {
                        player.sendMessage(Text.mm("<red>Format: <id> <shopId>"));
                        return;
                    }
                    if (plugin.shopManager().getShop(parts[1]) == null) {
                        player.sendMessage(Text.mm("<red>Shop nicht gefunden."));
                        return;
                    }
                    createNpcFromTemplate(player, parts[0], templateRole, parts[1]);
                });
            });
            return;
        }
        plugin.promptManager().prompt(player, Text.mm("<yellow>NPC erstellen: <id>"), input -> {
            String id = input.trim();
            runSync(() -> {
                if (id.isBlank()) {
                    player.sendMessage(Text.mm("<red>ID darf nicht leer sein."));
                    return;
                }
                createNpcFromTemplate(player, id, templateRole, null);
            });
        });
    }

    private void createNpcFromTemplate(Player player, String id, NpcRole role, String shopId) {
        if (plugin.npcManager().getNpc(id) != null) {
            player.sendMessage(Text.mm("<red>NPC existiert bereits."));
            return;
        }
        Npc npc = new Npc(id);
        npc.setName(id);
        npc.setRole(role);
        npc.setLocation(player.getLocation());
        npc.setDialog(java.util.List.of("Hallo!", "Ich habe eine Aufgabe für dich."));
        if (shopId != null && !shopId.isBlank()) {
            npc.setShopId(shopId);
        }
        plugin.npcManager().npcs().put(id, npc);
        plugin.npcManager().spawnNpc(npc);
        plugin.npcManager().saveNpc(npc);
        plugin.auditLog().log(player, "NPC erstellt (GUI): " + id);
        player.sendMessage(Text.mm("<green>NPC erstellt: " + id));
        plugin.guiManager().openNpcEditor(player);
    }

    private static Integer parseInt(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double parseDouble(String raw) {
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static <E extends Enum<E>> Optional<E> parseEnum(Class<E> type, String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String key = raw.trim().toUpperCase(Locale.ROOT);
        try {
            return Optional.of(Enum.valueOf(type, key));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static Object parseParamValue(String raw) {
        try {
            if (raw.contains(".")) {
                return Double.parseDouble(raw);
            }
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return raw;
        }
    }

    private void runSync(Runnable action) {
        if (org.bukkit.Bukkit.isPrimaryThread()) {
            action.run();
        } else {
            plugin.getServer().getScheduler().runTask(plugin, action);
        }
    }

    private void handleShopClick(Player player, Inventory inventory, int slot, ItemStack clicked,
                                 GuiHolders.ShopHolder holder, boolean rightClick) {
        var shop = plugin.shopManager().getShop(holder.shopId());
        if (shop == null) {
            player.sendMessage(Text.mm("<red>Shop nicht gefunden."));
            return;
        }
        var shopItem = shop.items().get(slot);
        if (shopItem == null) {
            return;
        }
        var profile = plugin.playerDataManager().getProfile(player);
        Material material = Material.matchMaterial(shopItem.material());
        if (material == null) {
            player.sendMessage(Text.mm("<red>Item ungültig."));
            return;
        }
        if (rightClick) {
            int sellPrice = shopItem.sellPrice();
            if (sellPrice <= 0) {
                player.sendMessage(Text.mm("<red>Dieses Item kann nicht verkauft werden."));
                return;
            }
            boolean removed = false;
            ItemStack[] contents = player.getInventory().getContents();
            if (removeOneRpgItem(contents, material)) {
                player.getInventory().setContents(contents);
                removed = true;
            }
            if (!removed && removeOne(player.getInventory(), material)) {
                removed = true;
            }
            if (!removed) {
                player.sendMessage(Text.mm("<red>Du hast dieses Item nicht."));
                return;
            }
            profile.setGold(profile.gold() + sellPrice);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
            player.sendMessage(Text.mm("<green>Verkauft für <gold>" + sellPrice + "</gold> Gold."));
        } else {
            int buyPrice = shopItem.buyPrice();
            if (buyPrice <= 0) {
                player.sendMessage(Text.mm("<red>Dieses Item kann nicht gekauft werden."));
                return;
            }
            if (profile.gold() < buyPrice) {
                player.sendMessage(Text.mm("<red>Nicht genug Gold."));
                return;
            }
            profile.setGold(profile.gold() - buyPrice);
            if (shopItem.rpgItem()) {
                var rarity = parseRarity(shopItem.rarity());
                ItemStack item = plugin.itemGenerator().createRpgItem(material, rarity, Math.max(1, shopItem.minLevel()));
                player.getInventory().addItem(item);
            } else {
                player.getInventory().addItem(new ItemStack(material));
            }
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
            player.sendMessage(Text.mm("<green>Gekauft für <gold>" + buyPrice + "</gold> Gold."));
        }
        player.updateInventory();
        plugin.playerDataManager().saveProfile(profile);
    }

    private boolean removeOneRpgItem(ItemStack[] contents, Material material) {
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() != material) {
                continue;
            }
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                continue;
            }
            if (!meta.getPersistentDataContainer().has(plugin.itemGenerator().itemKey(), PersistentDataType.INTEGER)) {
                continue;
            }
            int amount = item.getAmount();
            if (amount <= 1) {
                contents[i] = null;
            } else {
                item.setAmount(amount - 1);
            }
            return true;
        }
        return false;
    }

    private com.example.rpg.model.Rarity parseRarity(String raw) {
        if (raw == null) {
            return com.example.rpg.model.Rarity.COMMON;
        }
        try {
            return com.example.rpg.model.Rarity.valueOf(raw.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return com.example.rpg.model.Rarity.COMMON;
        }
    }

    private boolean removeOne(Inventory inventory, Material material) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack == null || stack.getType() != material) {
                continue;
            }
            if (stack.getAmount() > 1) {
                stack.setAmount(stack.getAmount() - 1);
            } else {
                inventory.setItem(i, null);
            }
            return true;
        }
        return false;
    }
}

package de.yourname.rpg.command;

import de.yourname.rpg.core.PlayerData;
import de.yourname.rpg.core.PluginContext;
import de.yourname.rpg.loot.LootTable;
import de.yourname.rpg.loot.LootTableEntry;
import de.yourname.rpg.npc.RpgNpc;
import de.yourname.rpg.quest.Quest;
import de.yourname.rpg.quest.QuestStatus;
import de.yourname.rpg.quest.QuestStepData;
import de.yourname.rpg.quest.QuestStepType;
import de.yourname.rpg.util.PdcKeys;
import de.yourname.rpg.zone.EditorWandItem;
import de.yourname.rpg.zone.Zone;
import de.yourname.rpg.zone.ZonePosition;
import de.yourname.rpg.zone.ZoneSelection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class RpgAdminCommand implements CommandExecutor {
    private final Map<UUID, ZoneSelection> selections = new HashMap<>();
    private final EditorWandItem editorWandItem;
    private final PluginContext context;
    private final PdcKeys keys;

    public RpgAdminCommand(EditorWandItem editorWandItem, PluginContext context, PdcKeys keys) {
        this.editorWandItem = editorWandItem;
        this.context = context;
        this.keys = keys;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen.");
            return true;
        }
        if (args.length == 0) {
            sendAdminHelp(player);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "wand" -> {
                player.getInventory().addItem(editorWandItem.create());
                player.sendMessage("§aEditor-Wand erhalten.");
                return true;
            }
            case "zone" -> {
                return handleZone(player, args);
            }
            case "npc" -> {
                return handleNpc(player, args);
            }
            case "quest" -> {
                return handleQuest(player, args);
            }
            case "loottable" -> {
                return handleLootTable(player, args);
            }
            case "player" -> {
                return handlePlayer(player, args);
            }
            default -> {
                return false;
            }
        }
    }

    public ZoneSelection getSelection(Player player) {
        return selections.computeIfAbsent(player.getUniqueId(), key -> new ZoneSelection());
    }

    private void sendAdminHelp(Player player) {
        player.sendMessage("§6RPG Admin Befehle:");
        player.sendMessage("§e/rpgadmin wand §7Editor-Wand erhalten");
        player.sendMessage("§e/rpgadmin zone create <id> §7Zone erstellen");
        player.sendMessage("§e/rpgadmin zone edit <id> §7Zone aus Auswahl aktualisieren");
        player.sendMessage("§e/rpgadmin npc create <id> §7NPC an aktueller Position erstellen");
        player.sendMessage("§e/rpgadmin quest create <id> §7Quest erstellen");
        player.sendMessage("§e/rpgadmin quest edit <id> title|desc|addstep ...");
        player.sendMessage("§e/rpgadmin quest publish <id> §7Quest veröffentlichen");
        player.sendMessage("§e/rpgadmin loottable edit <id> add|remove ...");
        player.sendMessage("§e/rpgadmin player inspect <name> §7Spielerdaten anzeigen");
        player.sendMessage("§e/rpgadmin player fix <name> §7Spielerdaten reparieren");
    }

    private boolean handleZone(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsage: /rpgadmin zone create|edit <id>");
            return true;
        }
        String action = args[1].toLowerCase();
        String id = args[2];
        ZoneSelection selection = getSelection(player);
        if ("create".equals(action)) {
            if (!selection.isComplete()) {
                player.sendMessage("§cBitte Pos1 und Pos2 mit der Editor-Wand setzen.");
                return true;
            }
            Zone zone = new Zone(id, selection.getPos1().getWorld(), selection.getPos1(), selection.getPos2());
            context.getZoneService().registerZone(zone);
            context.getZoneService().save();
            player.sendMessage("§aZone " + id + " erstellt.");
            return true;
        }
        if ("edit".equals(action)) {
            return context.getZoneService().getZone(id).map(zone -> {
                if (selection.isComplete()) {
                    zone.setWorld(selection.getPos1().getWorld());
                    zone.setPos1(selection.getPos1());
                    zone.setPos2(selection.getPos2());
                    context.getZoneService().save();
                    player.sendMessage("§aZone " + id + " aktualisiert.");
                } else {
                    player.sendMessage("§7Zone " + id + " gefunden. Setze Pos1/Pos2 und führe den Befehl erneut aus.");
                }
                return true;
            }).orElseGet(() -> {
                player.sendMessage("§cZone nicht gefunden.");
                return true;
            });
        }
        return false;
    }

    private boolean handleNpc(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsage: /rpgadmin npc create <id> [role] [dialogId]");
            return true;
        }
        String action = args[1].toLowerCase();
        if (!"create".equals(action)) {
            return false;
        }
        String id = args[2];
        String role = args.length >= 4 ? args[3] : "villager";
        String dialogId = args.length >= 5 ? args[4] : "default";
        Entity entity = player.getWorld().spawn(player.getLocation(), Villager.class, villager -> {
            villager.setCustomName("§e" + id);
            villager.setCustomNameVisible(true);
            villager.setAI(false);
        });
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        pdc.set(keys.npcId(), PersistentDataType.STRING, id);
        RpgNpc npc = new RpgNpc(id, entity.getUniqueId());
        npc.setRole(role);
        npc.setDialogId(dialogId);
        context.getNpcService().registerNpc(npc);
        context.getNpcService().save();
        player.sendMessage("§aNPC " + id + " erstellt.");
        return true;
    }

    private boolean handleQuest(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsage: /rpgadmin quest create|edit|publish <id> ...");
            return true;
        }
        String action = args[1].toLowerCase();
        String id = args[2];
        if ("create".equals(action)) {
            Quest quest = new Quest(id, "Quest " + id, "Beschreibung fehlt.");
            context.getQuestService().registerQuest(quest);
            context.getQuestService().save();
            player.sendMessage("§aQuest " + id + " erstellt.");
            return true;
        }
        if ("publish".equals(action)) {
            return context.getQuestService().getQuest(id).map(quest -> {
                quest.setStatus(QuestStatus.PUBLISHED);
                context.getQuestService().save();
                player.sendMessage("§aQuest " + id + " veröffentlicht.");
                return true;
            }).orElseGet(() -> {
                player.sendMessage("§cQuest nicht gefunden.");
                return true;
            });
        }
        if ("edit".equals(action)) {
            return context.getQuestService().getQuest(id).map(quest -> {
                if (args.length < 5) {
                    player.sendMessage("§cUsage: /rpgadmin quest edit <id> title|desc|addstep ...");
                    return true;
                }
                String field = args[3].toLowerCase();
                if ("title".equals(field)) {
                    String title = joinArgs(args, 4);
                    quest.setTitle(title);
                    context.getQuestService().save();
                    player.sendMessage("§aTitel gesetzt.");
                    return true;
                }
                if ("desc".equals(field)) {
                    String desc = joinArgs(args, 4);
                    quest.setDescription(desc);
                    context.getQuestService().save();
                    player.sendMessage("§aBeschreibung gesetzt.");
                    return true;
                }
                if ("addstep".equals(field)) {
                    if (args.length < 7) {
                        player.sendMessage("§cUsage: /rpgadmin quest edit <id> addstep <kill|collect|talk|goto> <target> <amount>");
                        return true;
                    }
                    QuestStepType type = parseQuestStepType(args[4]);
                    if (type == null) {
                        player.sendMessage("§cUngültiger Step-Typ. Nutze kill, collect, talk oder goto.");
                        return true;
                    }
                    String target = args[5];
                    Integer amount = parseIntArg(args[6]);
                    if (amount == null) {
                        player.sendMessage("§cUngültige Anzahl.");
                        return true;
                    }
                    quest.getSteps().add(new QuestStepData(type, target, amount));
                    context.getQuestService().save();
                    player.sendMessage("§aQuest-Step hinzugefügt.");
                    return true;
                }
                player.sendMessage("§cUnbekanntes Feld.");
                return true;
            }).orElseGet(() -> {
                player.sendMessage("§cQuest nicht gefunden.");
                return true;
            });
        }
        return false;
    }

    private boolean handleLootTable(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsage: /rpgadmin loottable edit <id> [add|remove] ...");
            return true;
        }
        String action = args[1].toLowerCase();
        if (!"edit".equals(action)) {
            return false;
        }
        String id = args[2];
        LootTable table = context.getLootService().getTable(id).orElseGet(() -> {
            LootTable created = new LootTable(id);
            context.getLootService().registerTable(created);
            return created;
        });
        if (args.length == 3) {
            player.sendMessage("§aLootTable " + id + " geöffnet. Nutze add/remove.");
            context.getLootService().save();
            return true;
        }
        String sub = args[3].toLowerCase();
        if ("add".equals(sub)) {
            if (args.length < 6) {
                player.sendMessage("§cUsage: /rpgadmin loottable edit <id> add <itemId> <chance>");
                return true;
            }
            String itemId = args[4];
            Double chance = parseDoubleArg(args[5]);
            if (chance == null) {
                player.sendMessage("§cUngültige Chance.");
                return true;
            }
            table.getEntries().add(new LootTableEntry(itemId, chance));
            context.getLootService().save();
            player.sendMessage("§aLoot-Eintrag hinzugefügt.");
            return true;
        }
        if ("remove".equals(sub)) {
            if (args.length < 5) {
                player.sendMessage("§cUsage: /rpgadmin loottable edit <id> remove <itemId>");
                return true;
            }
            String itemId = args[4];
            table.getEntries().removeIf(entry -> entry.getItemId().equalsIgnoreCase(itemId));
            context.getLootService().save();
            player.sendMessage("§aLoot-Eintrag entfernt.");
            return true;
        }
        return false;
    }

    private boolean handlePlayer(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsage: /rpgadmin player inspect|fix <name>");
            return true;
        }
        String action = args[1].toLowerCase();
        String name = args[2];
        UUID uuid = player.getServer().getOfflinePlayer(name).getUniqueId();
        PlayerData data = context.getPlayerDataService().getOrCreate(uuid);
        if ("inspect".equals(action)) {
            player.sendMessage("§6Spielerdaten von " + name + ":");
            player.sendMessage("§eLevel: §7" + data.getLevel());
            player.sendMessage("§eXP: §7" + data.getXp());
            player.sendMessage("§eSkillpunkte: §7" + data.getSkillPoints());
            player.sendMessage("§eWährung: §7" + data.getCurrency());
            player.sendMessage("§eFlags: §7" + String.join(", ", data.getFlags()));
            return true;
        }
        if ("fix".equals(action)) {
            data.setLevel(Math.max(1, data.getLevel()));
            data.setXp(Math.max(0, data.getXp()));
            data.setSkillPoints(Math.max(0, data.getSkillPoints()));
            data.setCurrency(Math.max(0, data.getCurrency()));
            context.getPlayerDataService().save(data);
            player.sendMessage("§aSpielerdaten repariert.");
            return true;
        }
        return false;
    }

    private String joinArgs(String[] args, int startIndex) {
        return String.join(" ", Arrays.copyOfRange(args, startIndex, args.length));
    }

    private QuestStepType parseQuestStepType(String value) {
        return Arrays.stream(QuestStepType.values())
                .filter(type -> type.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }

    private Integer parseIntArg(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Double parseDoubleArg(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

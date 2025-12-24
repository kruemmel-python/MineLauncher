package de.yourname.rpg.command;

import de.yourname.rpg.core.PlayerData;
import de.yourname.rpg.core.PluginContext;
import de.yourname.rpg.gui.RpgMenu;
import de.yourname.rpg.quest.Quest;
import de.yourname.rpg.quest.QuestStatus;
import de.yourname.rpg.skill.SkillTree;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RpgCommand implements CommandExecutor {
    private final RpgMenu menu;
    private final PluginContext context;

    public RpgCommand(RpgMenu menu, PluginContext context) {
        this.menu = menu;
        this.context = context;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen.");
            return true;
        }
        if (args.length == 0) {
            menu.openMain(player);
            return true;
        }
        return switch (args[0].toLowerCase()) {
            case "quests" -> {
                yield handleQuests(player, args);
            }
            case "skills" -> {
                sendSkillOverview(player);
                yield true;
            }
            case "stats" -> {
                sendStats(player);
                yield true;
            }
            case "help" -> {
                sendHelp(player);
                yield true;
            }
            default -> false;
        };
    }

    private void sendHelp(Player player) {
        List<String> lines = List.of(
                "§6/rpg §7Hauptmenü",
                "§6/rpg quests §7Questlog",
                "§6/rpg skills §7Skills",
                "§6/rpg stats §7Charakterwerte",
                "§6/rpg help §7Hilfe"
        );
        lines.forEach(player::sendMessage);
    }

    private void sendQuestLog(Player player) {
        PlayerData data = context.getPlayerDataService().getOrCreate(player.getUniqueId());
        List<Quest> quests = context.getQuestService().listQuests().stream()
                .filter(quest -> quest.getStatus() == QuestStatus.PUBLISHED)
                .toList();
        player.sendMessage("§6Questlog:");
        if (quests.isEmpty()) {
            player.sendMessage("§7Keine Quests verfügbar.");
            return;
        }
        quests.forEach(quest -> {
            String state = data.getQuestStates().getOrDefault(quest.getId(), "offen");
            player.sendMessage("§e- " + quest.getTitle() + " §7[" + state + "]");
        });
    }

    private boolean handleQuests(Player player, String[] args) {
        if (args.length >= 3 && "start".equalsIgnoreCase(args[1])) {
            String questId = args[2];
            if (context.getQuestService().getQuest(questId).isEmpty()) {
                player.sendMessage("§cQuest nicht gefunden.");
                return true;
            }
            context.getQuestService().startQuest(player.getUniqueId(), questId);
            player.sendMessage("§aQuest gestartet: " + questId);
            return true;
        }
        sendQuestLog(player);
        player.sendMessage("§7Tipp: §e/rpg quests start <id> §7zum Starten einer Quest.");
        return true;
    }

    private void sendSkillOverview(Player player) {
        List<SkillTree> trees = context.getSkillService().listTrees();
        player.sendMessage("§6Skilltrees:");
        if (trees.isEmpty()) {
            player.sendMessage("§7Keine Skilltrees konfiguriert.");
            return;
        }
        trees.forEach(tree -> player.sendMessage("§e- " + tree.getName() + " §7(" + tree.getId() + ")"));
    }

    private void sendStats(Player player) {
        PlayerData data = context.getPlayerDataService().getOrCreate(player.getUniqueId());
        player.sendMessage("§6Charakterwerte:");
        player.sendMessage("§eLevel: §7" + data.getLevel());
        player.sendMessage("§eXP: §7" + data.getXp());
        player.sendMessage("§eSkillpunkte: §7" + data.getSkillPoints());
        player.sendMessage("§eWährung: §7" + data.getCurrency());
    }
}

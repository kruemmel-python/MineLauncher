package de.yourname.rpg.command;

import de.yourname.rpg.gui.RpgMenu;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RpgCommand implements CommandExecutor {
    private final RpgMenu menu;

    public RpgCommand(RpgMenu menu) {
        this.menu = menu;
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
                player.sendMessage("§eQuestlog wird geöffnet...");
                yield true;
            }
            case "skills" -> {
                player.sendMessage("§eSkilltree wird geöffnet...");
                yield true;
            }
            case "stats" -> {
                player.sendMessage("§eCharakterwerte werden geladen...");
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
}

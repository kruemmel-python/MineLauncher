package de.yourname.rpg.command;

import de.yourname.rpg.zone.EditorWandItem;
import de.yourname.rpg.zone.ZoneSelection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RpgAdminCommand implements CommandExecutor {
    private final Map<UUID, ZoneSelection> selections = new HashMap<>();
    private final EditorWandItem editorWandItem;

    public RpgAdminCommand(EditorWandItem editorWandItem) {
        this.editorWandItem = editorWandItem;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen.");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage("§eRPG-Admin-Panel geöffnet (GUI folgt)." );
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "wand" -> {
                player.getInventory().addItem(editorWandItem.create());
                player.sendMessage("§aEditor-Wand erhalten.");
                return true;
            }
            case "zone" -> {
                player.sendMessage("§aZone-Editor. Verwende /rpgadmin zone create <id>." );
                return true;
            }
            case "npc" -> {
                player.sendMessage("§aNPC-Editor. Verwende /rpgadmin npc create <id>." );
                return true;
            }
            case "quest" -> {
                player.sendMessage("§aQuest-Editor. Verwende /rpgadmin quest create <id>." );
                return true;
            }
            case "loottable" -> {
                player.sendMessage("§aLoot-Editor. Verwende /rpgadmin loottable edit <id>." );
                return true;
            }
            case "player" -> {
                player.sendMessage("§aPlayer-Tools. Verwende /rpgadmin player inspect <name>." );
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    public ZoneSelection getSelection(Player player) {
        return selections.computeIfAbsent(player.getUniqueId(), key -> new ZoneSelection());
    }
}

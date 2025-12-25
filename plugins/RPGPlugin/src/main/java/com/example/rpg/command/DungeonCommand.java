package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.util.Text;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DungeonCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public DungeonCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(Text.mm("<gray>/dungeon <enter|leave>"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "enter" -> enterDungeon(player);
            case "leave" -> leaveDungeon(player);
            default -> player.sendMessage(Text.mm("<gray>/dungeon <enter|leave>"));
        }
        return true;
    }

    private void enterDungeon(Player player) {
        Location spawn = plugin.dungeonManager().getEntrance();
        if (spawn == null) {
            player.sendMessage(Text.mm("<red>Dungeon nicht konfiguriert."));
            return;
        }
        plugin.dungeonManager().enterDungeon(player);
        player.sendMessage(Text.mm("<green>Dungeon betreten."));
    }

    private void leaveDungeon(Player player) {
        plugin.dungeonManager().leaveDungeon(player);
        player.sendMessage(Text.mm("<yellow>Dungeon verlassen."));
    }
}

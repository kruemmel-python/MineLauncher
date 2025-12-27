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
            player.sendMessage(Text.mm("<gray>/dungeon <enter|leave|generate|queue|role|leavequeue>"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "enter" -> enterDungeon(player);
            case "leave" -> leaveDungeon(player);
            case "generate" -> generateDungeon(player, args);
            case "queue" -> queueDungeon(player, args);
            case "leavequeue" -> plugin.dungeonManager().leaveQueue(player);
            case "role" -> setRole(player, args);
            default -> player.sendMessage(Text.mm("<gray>/dungeon <enter|leave|generate|queue|role|leavequeue>"));
        }
        return true;
    }

    private void enterDungeon(Player player) {
        Location spawn = plugin.dungeonManager().getEntrance();
        if (spawn == null) {
            player.sendMessage(Text.mm("<red>Dungeon nicht konfiguriert."));
            return;
        }
        if (!plugin.dungeonManager().hasFactionAccess(player, "default")) {
            player.sendMessage(Text.mm("<red>Dein Ruf reicht nicht aus."));
            return;
        }
        plugin.dungeonManager().enterDungeon(player);
        player.sendMessage(Text.mm("<green>Dungeon betreten."));
    }

    private void leaveDungeon(Player player) {
        plugin.dungeonManager().leaveDungeon(player);
        player.sendMessage(Text.mm("<yellow>Dungeon verlassen."));
    }

    private void generateDungeon(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/dungeon generate <theme>"));
            return;
        }
        String theme = args[1];
        var party = plugin.partyManager().getParty(player.getUniqueId());
        java.util.List<Player> members = new java.util.ArrayList<>();
        if (party.isPresent()) {
            for (java.util.UUID memberId : party.get().members()) {
                Player member = player.getServer().getPlayer(memberId);
                if (member != null) {
                    members.add(member);
                }
            }
            Player leader = player.getServer().getPlayer(party.get().leader());
            if (leader != null && !members.contains(leader)) {
                members.add(leader);
            }
        } else {
            members.add(player);
        }
        if (!plugin.dungeonManager().hasFactionAccess(player, theme)) {
            player.sendMessage(Text.mm("<red>Dein Ruf reicht nicht aus."));
            return;
        }
        plugin.dungeonManager().generateDungeon(player, theme, members);
    }

    private void queueDungeon(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/dungeon queue <theme>"));
            return;
        }
        plugin.dungeonManager().joinQueue(player, args[1]);
    }

    private void setRole(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/dungeon role <tank|heal|dps>"));
            return;
        }
        String role = args[1].toUpperCase();
        if (!role.equals("TANK") && !role.equals("HEAL") && !role.equals("DPS")) {
            player.sendMessage(Text.mm("<red>Rolle ungültig."));
            return;
        }
        var profile = plugin.playerDataManager().getProfile(player);
        profile.setDungeonRole(role);
        player.sendMessage(Text.mm("<green>Rolle gesetzt: " + role));
    }
}

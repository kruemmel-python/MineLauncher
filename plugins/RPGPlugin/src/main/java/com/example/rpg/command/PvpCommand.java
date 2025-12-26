package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.util.Text;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PvpCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public PvpCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(Text.mm("<gray>/pvp <join|top|season>"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "join" -> plugin.arenaManager().joinQueue(player);
            case "top" -> showTop(player);
            case "season" -> showSeason(player);
            default -> player.sendMessage(Text.mm("<gray>/pvp <join|top|season>"));
        }
        return true;
    }

    private void showTop(Player player) {
        List<PlayerProfile> profiles = plugin.arenaManager().topPlayers(10);
        player.sendMessage(Text.mm("<gold>PvP Rangliste:"));
        int index = 1;
        for (PlayerProfile profile : profiles) {
            String name = plugin.getServer().getOfflinePlayer(profile.uuid()).getName();
            if (name == null) {
                name = profile.uuid().toString().substring(0, 8);
            }
            player.sendMessage(Text.mm("<gray>" + index++ + ". <white>" + name
                + " <gold>(" + profile.elo() + ")"));
        }
    }

    private void showSeason(Player player) {
        var season = plugin.pvpSeasonManager().currentSeason();
        if (season == null) {
            player.sendMessage(Text.mm("<yellow>Keine aktive Saison."));
            return;
        }
        player.sendMessage(Text.mm("<gold>PvP-Saison:</gold> " + season.name()));
    }
}

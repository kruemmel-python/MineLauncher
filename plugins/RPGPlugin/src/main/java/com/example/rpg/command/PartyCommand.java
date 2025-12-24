package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.Party;
import com.example.rpg.util.Text;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public PartyCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(Text.mm("<gray>/party <create|invite|join|leave>"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "create" -> createParty(player);
            case "invite" -> invitePlayer(player, args);
            case "join" -> joinParty(player, args);
            case "leave" -> leaveParty(player);
            default -> player.sendMessage(Text.mm("<gray>/party <create|invite|join|leave>"));
        }
        return true;
    }

    private void createParty(Player player) {
        if (plugin.partyManager().getParty(player.getUniqueId()).isPresent()) {
            player.sendMessage(Text.mm("<yellow>Du bist bereits in einer Party."));
            return;
        }
        plugin.partyManager().createParty(player.getUniqueId());
        player.sendMessage(Text.mm("<green>Party erstellt."));
    }

    private void invitePlayer(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/party invite <player>"));
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(Text.mm("<red>Spieler nicht online."));
            return;
        }
        plugin.partyManager().getParty(player.getUniqueId()).ifPresentOrElse(party -> {
            target.sendMessage(Text.mm("<yellow>Party Einladung von " + player.getName() + ". Benutze /party join " + player.getName()));
        }, () -> player.sendMessage(Text.mm("<red>Du hast keine Party.")));
    }

    private void joinParty(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/party join <leader>"));
            return;
        }
        Player leader = Bukkit.getPlayer(args[1]);
        if (leader == null) {
            player.sendMessage(Text.mm("<red>Leader nicht online."));
            return;
        }
        plugin.partyManager().getParty(leader.getUniqueId()).ifPresentOrElse(party -> {
            if (plugin.partyManager().getParty(player.getUniqueId()).isPresent()) {
                player.sendMessage(Text.mm("<yellow>Du bist bereits in einer Party."));
                return;
            }
            plugin.partyManager().addMember(party, player.getUniqueId());
            leader.sendMessage(Text.mm("<green>" + player.getName() + " ist beigetreten."));
            player.sendMessage(Text.mm("<green>Du bist der Party beigetreten."));
        }, () -> player.sendMessage(Text.mm("<red>Party nicht gefunden.")));
    }

    private void leaveParty(Player player) {
        UUID uuid = player.getUniqueId();
        if (plugin.partyManager().getParty(uuid).isEmpty()) {
            player.sendMessage(Text.mm("<yellow>Du bist in keiner Party."));
            return;
        }
        plugin.partyManager().removeMember(uuid);
        player.sendMessage(Text.mm("<green>Party verlassen."));
    }
}

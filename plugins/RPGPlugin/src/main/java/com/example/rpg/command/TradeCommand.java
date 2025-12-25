package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.TradeRequest;
import com.example.rpg.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TradeCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public TradeCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(Text.mm("<gray>/trade <request|accept|offer|requestgold|ready|cancel>"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "request" -> requestTrade(player, args);
            case "accept" -> acceptTrade(player);
            case "offer" -> offerGold(player, args);
            case "requestgold" -> requestGold(player, args);
            case "ready" -> readyTrade(player);
            case "cancel" -> cancelTrade(player);
            default -> player.sendMessage(Text.mm("<gray>/trade <request|accept|offer|requestgold|ready|cancel>"));
        }
        return true;
    }

    private void requestTrade(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/trade request <player>"));
            return;
        }
        Player target = player.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(Text.mm("<red>Spieler nicht online."));
            return;
        }
        plugin.tradeManager().requestTrade(player.getUniqueId(), target.getUniqueId());
        player.sendMessage(Text.mm("<green>Handel angefragt."));
        target.sendMessage(Text.mm("<yellow>Handelsanfrage von " + player.getName() + ". /trade accept"));
    }

    private void acceptTrade(Player player) {
        TradeRequest request = plugin.tradeManager().getRequest(player.getUniqueId());
        if (request == null) {
            player.sendMessage(Text.mm("<red>Keine Anfrage."));
            return;
        }
        player.sendMessage(Text.mm("<green>Handel akzeptiert. Beide Seiten können Gold setzen."));
    }

    private void offerGold(Player player, String[] args) {
        TradeRequest request = plugin.tradeManager().getRequest(player.getUniqueId());
        if (request == null) {
            player.sendMessage(Text.mm("<red>Kein Handel aktiv."));
            return;
        }
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/trade offer <gold>"));
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(Text.mm("<red>Betrag ungültig."));
            return;
        }
        request.setGoldOffer(amount);
        player.sendMessage(Text.mm("<green>Du bietest " + amount + " Gold."));
    }

    private void requestGold(Player player, String[] args) {
        TradeRequest request = plugin.tradeManager().getRequest(player.getUniqueId());
        if (request == null) {
            player.sendMessage(Text.mm("<red>Kein Handel aktiv."));
            return;
        }
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/trade requestgold <gold>"));
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(Text.mm("<red>Betrag ungültig."));
            return;
        }
        request.setGoldRequest(amount);
        player.sendMessage(Text.mm("<green>Du verlangst " + amount + " Gold."));
    }

    private void readyTrade(Player player) {
        TradeRequest request = plugin.tradeManager().getRequest(player.getUniqueId());
        if (request == null) {
            player.sendMessage(Text.mm("<red>Kein Handel aktiv."));
            return;
        }
        if (request.requester().equals(player.getUniqueId())) {
            request.setRequesterReady(true);
        } else {
            request.setTargetReady(true);
        }
        if (request.requesterReady() && request.targetReady()) {
            completeTrade(request);
        } else {
            player.sendMessage(Text.mm("<green>Bereit gesetzt. Warte auf den Handelspartner."));
        }
    }

    private void completeTrade(TradeRequest request) {
        Player requester = plugin.getServer().getPlayer(request.requester());
        Player target = plugin.getServer().getPlayer(request.target());
        if (requester == null || target == null) {
            return;
        }
        var requesterProfile = plugin.playerDataManager().getProfile(requester);
        var targetProfile = plugin.playerDataManager().getProfile(target);
        if (requesterProfile.gold() < request.goldOffer() || targetProfile.gold() < request.goldRequest()) {
            requester.sendMessage(Text.mm("<red>Handel fehlgeschlagen: nicht genug Gold."));
            target.sendMessage(Text.mm("<red>Handel fehlgeschlagen: nicht genug Gold."));
            plugin.tradeManager().clear(request.requester());
            return;
        }
        requesterProfile.setGold(requesterProfile.gold() - request.goldOffer() + request.goldRequest());
        targetProfile.setGold(targetProfile.gold() - request.goldRequest() + request.goldOffer());
        requester.sendMessage(Text.mm("<green>Handel abgeschlossen."));
        target.sendMessage(Text.mm("<green>Handel abgeschlossen."));
        plugin.tradeManager().clear(request.requester());
    }

    private void cancelTrade(Player player) {
        plugin.tradeManager().clear(player.getUniqueId());
        player.sendMessage(Text.mm("<yellow>Handel abgebrochen."));
    }
}

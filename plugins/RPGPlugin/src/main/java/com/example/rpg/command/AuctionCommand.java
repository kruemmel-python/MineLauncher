package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.AuctionListing;
import com.example.rpg.util.Text;
import java.util.UUID;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AuctionCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public AuctionCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(Text.mm("<gray>/auction <list|sell|buy>"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "list" -> listAuctions(player);
            case "sell" -> sellAuction(player, args);
            case "buy" -> buyAuction(player, args);
            default -> player.sendMessage(Text.mm("<gray>/auction <list|sell|buy>"));
        }
        return true;
    }

    private void listAuctions(Player player) {
        if (plugin.auctionHouseManager().listings().isEmpty()) {
            player.sendMessage(Text.mm("<yellow>Keine Auktionen verfügbar."));
            return;
        }
        player.sendMessage(Text.mm("<gold>Auktionen:"));
        for (AuctionListing listing : plugin.auctionHouseManager().listings().values()) {
            player.sendMessage(Text.mm("<gray>" + listing.id() + " - <gold>" + listing.price() + "</gold> Gold"));
        }
    }

    private void sellAuction(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/auction sell <price>"));
            return;
        }
        int price;
        try {
            price = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(Text.mm("<red>Preis ungültig."));
            return;
        }
        if (price <= 0) {
            player.sendMessage(Text.mm("<red>Preis muss > 0 sein."));
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(Text.mm("<red>Halte ein Item in der Hand."));
            return;
        }
        String data = plugin.auctionHouseManager().serializeItem(item);
        if (data == null) {
            player.sendMessage(Text.mm("<red>Item konnte nicht gespeichert werden."));
            return;
        }
        String id = UUID.randomUUID().toString().substring(0, 8);
        AuctionListing listing = new AuctionListing(id);
        listing.setSeller(player.getUniqueId());
        listing.setPrice(price);
        listing.setItemData(data);
        plugin.auctionHouseManager().addListing(listing);
        player.getInventory().setItemInMainHand(null);
        player.sendMessage(Text.mm("<green>Auktion erstellt: " + id));
    }

    private void buyAuction(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/auction buy <id>"));
            return;
        }
        String id = args[1];
        AuctionListing listing = plugin.auctionHouseManager().getListing(id);
        if (listing == null) {
            player.sendMessage(Text.mm("<red>Auktion nicht gefunden."));
            return;
        }
        var buyerProfile = plugin.playerDataManager().getProfile(player);
        if (buyerProfile.gold() < listing.price()) {
            player.sendMessage(Text.mm("<red>Nicht genug Gold."));
            return;
        }
        ItemStack item = plugin.auctionHouseManager().deserializeItem(listing.itemData());
        if (item == null) {
            player.sendMessage(Text.mm("<red>Item nicht verfügbar."));
            return;
        }
        buyerProfile.setGold(buyerProfile.gold() - listing.price());
        player.getInventory().addItem(item);
        if (listing.seller() != null) {
            var seller = plugin.getServer().getPlayer(listing.seller());
            if (seller != null) {
                var sellerProfile = plugin.playerDataManager().getProfile(seller);
                sellerProfile.setGold(sellerProfile.gold() + listing.price());
                seller.sendMessage(Text.mm("<green>Dein Item wurde verkauft für " + listing.price() + " Gold."));
            }
        }
        plugin.auctionHouseManager().removeListing(id);
        player.sendMessage(Text.mm("<green>Item gekauft."));
    }
}

package de.yourname.rpg.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RpgMenu {
    public void openMain(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 27, "RPG-Menü");
        inventory.setItem(11, createItem(Material.BOOK, "Quests"));
        inventory.setItem(13, createItem(Material.NETHER_STAR, "Skills"));
        inventory.setItem(15, createItem(Material.PAPER, "Charakter"));
        player.openInventory(inventory);
    }

    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a" + name);
            item.setItemMeta(meta);
        }
        return item;
    }
}

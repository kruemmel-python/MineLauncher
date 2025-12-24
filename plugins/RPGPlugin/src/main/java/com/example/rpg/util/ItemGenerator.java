package com.example.rpg.util;

import com.example.rpg.model.Rarity;
import java.util.List;
import java.util.Random;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemGenerator {
    private final Random random = new Random();
    private final NamespacedKey itemKey;
    private final NamespacedKey rarityKey;

    public ItemGenerator(JavaPlugin plugin) {
        this.itemKey = new NamespacedKey(plugin, "rpg_item");
        this.rarityKey = new NamespacedKey(plugin, "rpg_rarity");
    }

    public ItemStack createRpgItem(Material material, Rarity rarity, int minLevel) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(rarity.name() + " " + material.name()).color(rarity.color()));
        meta.lore(List.of(
            Component.text("Rarity: " + rarity.name()).color(rarity.color()),
            Component.text("Level " + minLevel)
        ));
        meta.getPersistentDataContainer().set(itemKey, PersistentDataType.INTEGER, 1);
        meta.getPersistentDataContainer().set(rarityKey, PersistentDataType.STRING, rarity.name());
        item.setItemMeta(meta);
        item.setAmount(1 + random.nextInt(1));
        return item;
    }
}

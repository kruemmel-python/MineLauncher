package com.example.rpg.manager;

import com.example.rpg.model.AuctionListing;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class AuctionHouseManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, AuctionListing> listings = new HashMap<>();

    public AuctionHouseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "auctions.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public Map<String, AuctionListing> listings() {
        return listings;
    }

    public AuctionListing getListing(String id) {
        return listings.get(id);
    }

    public void addListing(AuctionListing listing) {
        listings.put(listing.id(), listing);
        saveListing(listing);
    }

    public void removeListing(String id) {
        listings.remove(id);
        config.set(id, null);
        save();
    }

    public void saveListing(AuctionListing listing) {
        ConfigurationSection section = config.createSection(listing.id());
        section.set("seller", listing.seller() != null ? listing.seller().toString() : null);
        section.set("price", listing.price());
        section.set("item", listing.itemData());
        save();
    }

    public String serializeItem(ItemStack item) {
        try (java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
             BukkitObjectOutputStream dataOut = new BukkitObjectOutputStream(output)) {
            dataOut.writeObject(item);
            return java.util.Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to serialize item: " + e.getMessage());
            return null;
        }
    }

    public ItemStack deserializeItem(String data) {
        if (data == null) {
            return null;
        }
        try (java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(java.util.Base64.getDecoder().decode(data));
             BukkitObjectInputStream dataIn = new BukkitObjectInputStream(input)) {
            Object obj = dataIn.readObject();
            return obj instanceof ItemStack item ? item : null;
        } catch (IOException | ClassNotFoundException e) {
            plugin.getLogger().warning("Failed to deserialize item: " + e.getMessage());
            return null;
        }
    }

    private void load() {
        listings.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            AuctionListing listing = new AuctionListing(id);
            String seller = section.getString("seller", null);
            listing.setSeller(seller != null ? UUID.fromString(seller) : null);
            listing.setPrice(section.getInt("price", 0));
            listing.setItemData(section.getString("item", null));
            listings.put(id, listing);
        }
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save auctions.yml: " + e.getMessage());
        }
    }
}

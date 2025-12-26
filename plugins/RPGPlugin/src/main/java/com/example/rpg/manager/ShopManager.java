package com.example.rpg.manager;

import com.example.rpg.model.ShopDefinition;
import com.example.rpg.model.ShopItem;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, ShopDefinition> shops = new HashMap<>();

    public ShopManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "shops.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public ShopDefinition getShop(String id) {
        return shops.get(id);
    }

    public Map<String, ShopDefinition> shops() {
        return shops;
    }

    public void saveShop(ShopDefinition shop) {
        ConfigurationSection section = config.createSection(shop.id());
        section.set("title", shop.title());
        List<Map<String, Object>> items = new java.util.ArrayList<>();
            for (ShopItem item : shop.items().values()) {
                Map<String, Object> map = new HashMap<>();
                map.put("slot", item.slot());
                map.put("material", item.material());
                map.put("name", item.name());
                map.put("buyPrice", item.buyPrice());
                map.put("sellPrice", item.sellPrice());
                map.put("rpgItem", item.rpgItem());
                map.put("rarity", item.rarity());
                map.put("minLevel", item.minLevel());
                items.add(map);
            }
        section.set("items", items);
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (ShopDefinition shop : shops.values()) {
            saveShop(shop);
        }
        save();
    }

    private void load() {
        shops.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            ShopDefinition shop = new ShopDefinition(id);
            shop.setTitle(section.getString("title", id));
            Map<Integer, ShopItem> items = new HashMap<>();
            for (Map<?, ?> raw : section.getMapList("items")) {
                ShopItem item = new ShopItem();
                Object slotValue = raw.containsKey("slot") ? raw.get("slot") : 0;
                Object materialValue = raw.containsKey("material") ? raw.get("material") : "STONE";
                Object nameValue = raw.containsKey("name") ? raw.get("name") : "";
                Object buyValue = raw.containsKey("buyPrice") ? raw.get("buyPrice") : 0;
                Object sellValue = raw.containsKey("sellPrice") ? raw.get("sellPrice") : 0;
                Object rpgValue = raw.containsKey("rpgItem") ? raw.get("rpgItem") : false;
                Object rarityValue = raw.containsKey("rarity") ? raw.get("rarity") : null;
                Object minLevelValue = raw.containsKey("minLevel") ? raw.get("minLevel") : 1;
                item.setSlot(Integer.parseInt(String.valueOf(slotValue)));
                item.setMaterial(String.valueOf(materialValue));
                item.setName(String.valueOf(nameValue));
                item.setBuyPrice(Integer.parseInt(String.valueOf(buyValue)));
                item.setSellPrice(Integer.parseInt(String.valueOf(sellValue)));
                item.setRpgItem(Boolean.parseBoolean(String.valueOf(rpgValue)));
                item.setRarity(rarityValue != null ? String.valueOf(rarityValue) : null);
                item.setMinLevel(Integer.parseInt(String.valueOf(minLevelValue)));
                items.put(item.slot(), item);
            }
            shop.setItems(items);
            shops.put(id, shop);
        }
    }

    private void seedDefaults() {
        ShopDefinition shop = new ShopDefinition("blacksmith");
        shop.setTitle("Dorfschmied");
        ShopItem sword = new ShopItem();
        sword.setSlot(0);
        sword.setMaterial("IRON_SWORD");
        sword.setName("&7Eisenschwert");
        sword.setBuyPrice(100);
        sword.setSellPrice(20);
        ShopItem potion = new ShopItem();
        potion.setSlot(1);
        potion.setMaterial("POTION");
        potion.setName("&aHeiltrank");
        potion.setBuyPrice(50);
        potion.setSellPrice(10);
        Map<Integer, ShopItem> items = new HashMap<>();
        items.put(sword.slot(), sword);
        items.put(potion.slot(), potion);
        shop.setItems(items);
        shops.put(shop.id(), shop);
        saveAll();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save shops.yml: " + e.getMessage());
        }
    }
}

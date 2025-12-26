package com.example.rpg.manager;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.CraftingOrder;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class CraftingOrderManager {
    private final RPGPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, CraftingOrder> orders = new HashMap<>();

    public CraftingOrderManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "crafting_orders.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public Map<String, CraftingOrder> orders() {
        return orders;
    }

    public CraftingOrder getOrder(String id) {
        return orders.get(id);
    }

    public void saveOrder(CraftingOrder order) {
        ConfigurationSection section = config.createSection(order.id());
        section.set("requester", order.requester() != null ? order.requester().toString() : null);
        section.set("material", order.material());
        section.set("amount", order.amount());
        section.set("rewardGold", order.rewardGold());
        save();
    }

    public void removeOrder(String id) {
        orders.remove(id);
        config.set(id, null);
        save();
    }

    private void load() {
        orders.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            CraftingOrder order = new CraftingOrder(id);
            String requester = section.getString("requester", null);
            if (requester != null) {
                order.setRequester(UUID.fromString(requester));
            }
            order.setMaterial(section.getString("material", ""));
            order.setAmount(section.getInt("amount", 1));
            order.setRewardGold(section.getInt("rewardGold", 10));
            orders.put(id, order);
        }
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save crafting_orders.yml: " + e.getMessage());
        }
    }
}

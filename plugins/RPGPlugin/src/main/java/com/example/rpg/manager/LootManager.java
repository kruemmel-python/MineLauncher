package com.example.rpg.manager;

import com.example.rpg.model.LootEntry;
import com.example.rpg.model.LootTable;
import com.example.rpg.model.Rarity;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class LootManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, LootTable> tables = new HashMap<>();

    public LootManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "loot.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public Map<String, LootTable> tables() {
        return tables;
    }

    public LootTable getTable(String id) {
        return tables.get(id);
    }

    public LootTable getTableFor(String key) {
        for (LootTable table : tables.values()) {
            if (table.appliesTo().equalsIgnoreCase(key)) {
                return table;
            }
        }
        return null;
    }

    public void saveTable(LootTable table) {
        ConfigurationSection section = config.createSection(table.id());
        section.set("appliesTo", table.appliesTo());
        List<Map<String, Object>> entries = new ArrayList<>();
        for (LootEntry entry : table.entries()) {
            Map<String, Object> map = new HashMap<>();
            map.put("material", entry.material());
            map.put("chance", entry.chance());
            map.put("minAmount", entry.minAmount());
            map.put("maxAmount", entry.maxAmount());
            map.put("rarity", entry.rarity().name());
            entries.add(map);
        }
        section.set("entries", entries);
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (LootTable table : tables.values()) {
            saveTable(table);
        }
        save();
    }

    private void load() {
        tables.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            LootTable table = new LootTable(id);
            table.setAppliesTo(section.getString("appliesTo", "ZOMBIE"));
            List<LootEntry> entries = new ArrayList<>();
            for (Map<?, ?> map : section.getMapList("entries")) {
                String material = String.valueOf(map.getOrDefault("material", "IRON_NUGGET"));
                double chance = Double.parseDouble(String.valueOf(map.getOrDefault("chance", 0.3)));
                int minAmount = Integer.parseInt(String.valueOf(map.getOrDefault("minAmount", 1)));
                int maxAmount = Integer.parseInt(String.valueOf(map.getOrDefault("maxAmount", 1)));
                Rarity rarity = Rarity.valueOf(String.valueOf(map.getOrDefault("rarity", "COMMON")));
                entries.add(new LootEntry(material, chance, minAmount, maxAmount, rarity));
            }
            table.setEntries(entries);
            tables.put(id, table);
        }
    }

    private void seedDefaults() {
        LootTable table = new LootTable("forest_mobs");
        table.setAppliesTo("ZOMBIE");
        table.setEntries(List.of(
            new LootEntry("IRON_NUGGET", 0.5, 1, 3, Rarity.COMMON),
            new LootEntry("EMERALD", 0.15, 1, 1, Rarity.RARE)
        ));
        tables.put(table.id(), table);
        saveAll();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save loot.yml: " + e.getMessage());
        }
    }
}

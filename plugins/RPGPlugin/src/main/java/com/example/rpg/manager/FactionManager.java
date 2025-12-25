package com.example.rpg.manager;

import com.example.rpg.model.Faction;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class FactionManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Faction> factions = new HashMap<>();

    public FactionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "factions.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public Faction getFaction(String id) {
        return factions.get(id);
    }

    public Map<String, Faction> factions() {
        return factions;
    }

    public void saveFaction(Faction faction) {
        ConfigurationSection section = config.createSection(faction.id());
        section.set("name", faction.name());
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (Faction faction : factions.values()) {
            saveFaction(faction);
        }
        save();
    }

    private void load() {
        factions.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Faction faction = new Faction(id);
            faction.setName(section.getString("name", id));
            factions.put(id, faction);
        }
    }

    private void seedDefaults() {
        Faction faction = new Faction("adventurers");
        faction.setName("Abenteurergilde");
        factions.put(faction.id(), faction);
        saveAll();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save factions.yml: " + e.getMessage());
        }
    }
}

package com.example.rpg.manager;

import com.example.rpg.model.Zone;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ZoneManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Zone> zones = new HashMap<>();

    public ZoneManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "zones.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public Map<String, Zone> zones() {
        return zones;
    }

    public Zone getZone(String id) {
        return zones.get(id);
    }

    public Zone getZoneAt(Location location) {
        for (Zone zone : zones.values()) {
            if (zone.contains(location)) {
                return zone;
            }
        }
        return null;
    }

    public void saveZone(Zone zone) {
        ConfigurationSection section = config.createSection(zone.id());
        section.set("name", zone.name());
        section.set("world", zone.world());
        section.set("minLevel", zone.minLevel());
        section.set("maxLevel", zone.maxLevel());
        section.set("slowMultiplier", zone.slowMultiplier());
        section.set("damageMultiplier", zone.damageMultiplier());
        section.set("x1", zone.x1());
        section.set("y1", zone.y1());
        section.set("z1", zone.z1());
        section.set("x2", zone.x2());
        section.set("y2", zone.y2());
        section.set("z2", zone.z2());
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (Zone zone : zones.values()) {
            saveZone(zone);
        }
        save();
    }

    private void load() {
        zones.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Zone zone = new Zone(id);
            zone.setName(section.getString("name", id));
            zone.setWorld(section.getString("world", "world"));
            zone.setMinLevel(section.getInt("minLevel", 1));
            zone.setMaxLevel(section.getInt("maxLevel", 60));
            zone.setSlowMultiplier(section.getDouble("slowMultiplier", 1.0));
            zone.setDamageMultiplier(section.getDouble("damageMultiplier", 1.0));
            zone.setCoordinates(
                section.getInt("x1"), section.getInt("y1"), section.getInt("z1"),
                section.getInt("x2"), section.getInt("y2"), section.getInt("z2")
            );
            zones.put(id, zone);
        }
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save zones.yml: " + e.getMessage());
        }
    }
}

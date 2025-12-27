package com.example.rpg.manager;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.ResourceNode;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class ResourceNodeManager {
    private final RPGPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, ResourceNode> nodes = new HashMap<>();

    public ResourceNodeManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "resource_nodes.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public Map<String, ResourceNode> nodes() {
        return nodes;
    }

    public ResourceNode nodeAt(Location location) {
        return nodes.values().stream()
            .filter(node -> node.world().equals(location.getWorld().getName())
                && node.x() == location.getBlockX()
                && node.y() == location.getBlockY()
                && node.z() == location.getBlockZ())
            .findFirst()
            .orElse(null);
    }

    public void createNode(Player player, String profession, Material material, int respawnSeconds, int xp) {
        String id = "node_" + nodes.size();
        Location loc = player.getLocation().getBlock().getLocation();
        ResourceNode node = new ResourceNode(id);
        node.setWorld(loc.getWorld().getName());
        node.setX(loc.getBlockX());
        node.setY(loc.getBlockY());
        node.setZ(loc.getBlockZ());
        node.setMaterial(material.name());
        node.setProfession(profession);
        node.setRespawnSeconds(respawnSeconds);
        node.setXp(xp);
        nodes.put(id, node);
        saveNode(node);
    }

    public void saveNode(ResourceNode node) {
        ConfigurationSection section = config.createSection(node.id());
        section.set("world", node.world());
        section.set("x", node.x());
        section.set("y", node.y());
        section.set("z", node.z());
        section.set("material", node.material());
        section.set("profession", node.profession());
        section.set("respawnSeconds", node.respawnSeconds());
        section.set("xp", node.xp());
        section.set("nextAvailableAt", node.nextAvailableAt());
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (ResourceNode node : nodes.values()) {
            saveNode(node);
        }
        save();
    }

    private void load() {
        nodes.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            ResourceNode node = new ResourceNode(id);
            node.setWorld(section.getString("world", "world"));
            node.setX(section.getInt("x"));
            node.setY(section.getInt("y"));
            node.setZ(section.getInt("z"));
            node.setMaterial(section.getString("material", "STONE"));
            node.setProfession(section.getString("profession", "mining"));
            node.setRespawnSeconds(section.getInt("respawnSeconds", 60));
            node.setXp(section.getInt("xp", 5));
            node.setNextAvailableAt(section.getLong("nextAvailableAt", 0));
            nodes.put(id, node);
        }
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save resource_nodes.yml: " + e.getMessage());
        }
    }
}

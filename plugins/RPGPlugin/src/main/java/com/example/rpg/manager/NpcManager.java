package com.example.rpg.manager;

import com.example.rpg.model.Npc;
import com.example.rpg.model.NpcRole;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class NpcManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Npc> npcs = new HashMap<>();
    private final NamespacedKey npcKey;

    public NpcManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "npcs.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        this.npcKey = new NamespacedKey(plugin, "npc_id");
        load();
    }

    public NamespacedKey npcKey() {
        return npcKey;
    }

    public Map<String, Npc> npcs() {
        return npcs;
    }

    public Npc getNpc(String id) {
        return npcs.get(id);
    }

    public void spawnAll() {
        for (Npc npc : npcs.values()) {
            spawnNpc(npc);
        }
    }

    public void spawnNpc(Npc npc) {
        World world = Bukkit.getWorld(npc.world());
        if (world == null) {
            return;
        }
        Location location = npc.toLocation(world);
        Villager villager = (Villager) world.spawnEntity(location, EntityType.VILLAGER);
        villager.customName(Component.text(npc.name()));
        villager.setCustomNameVisible(true);
        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.setSilent(true);
        villager.getPersistentDataContainer().set(npcKey, PersistentDataType.STRING, npc.id());
        npc.setUuid(villager.getUniqueId());
    }

    public void saveNpc(Npc npc) {
        ConfigurationSection section = config.createSection(npc.id());
        section.set("name", npc.name());
        section.set("role", npc.role().name());
        section.set("world", npc.world());
        section.set("x", npc.x());
        section.set("y", npc.y());
        section.set("z", npc.z());
        section.set("yaw", npc.yaw());
        section.set("pitch", npc.pitch());
        section.set("dialog", npc.dialog());
        section.set("questLink", npc.questLink());
        section.set("shopId", npc.shopId());
        section.set("uuid", npc.uuid() != null ? npc.uuid().toString() : null);
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (Npc npc : npcs.values()) {
            saveNpc(npc);
        }
        save();
    }

    private void load() {
        npcs.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Npc npc = new Npc(id);
            npc.setName(section.getString("name", id));
            npc.setRole(NpcRole.valueOf(section.getString("role", "QUESTGIVER")));
            npc.setWorld(section.getString("world", "world"));
            npc.setDialog(section.getStringList("dialog"));
            npc.setQuestLink(section.getString("questLink", null));
            npc.setShopId(section.getString("shopId", null));
            npc.setUuid(section.contains("uuid") ? UUID.fromString(section.getString("uuid")) : null);
            World world = Bukkit.getWorld(npc.world());
            double x = section.getDouble("x");
            double y = section.getDouble("y");
            double z = section.getDouble("z");
            float yaw = (float) section.getDouble("yaw");
            float pitch = (float) section.getDouble("pitch");
            if (world != null) {
                npc.setLocation(new Location(world, x, y, z, yaw, pitch));
            } else {
                npc.setRawLocation(npc.world(), x, y, z, yaw, pitch);
            }
            npcs.put(id, npc);
        }
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save npcs.yml: " + e.getMessage());
        }
    }
}

package com.example.rpg.manager;

import com.example.rpg.RPGPlugin;
import com.example.rpg.dungeon.DungeonGenerator;
import com.example.rpg.dungeon.DungeonInstance;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class DungeonManager {
    private final RPGPlugin plugin;
    private final FileConfiguration config;
    private Location entrance;
    private Location exit;
    private final Map<UUID, Location> returnLocations = new HashMap<>();
    private final DungeonGenerator generator;
    private final Map<UUID, DungeonInstance> activeInstances = new HashMap<>();

    public DungeonManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.generator = new DungeonGenerator(plugin);
        load();
    }

    public Location getEntrance() {
        return entrance;
    }

    public void enterDungeon(org.bukkit.entity.Player player) {
        returnLocations.put(player.getUniqueId(), player.getLocation());
        if (entrance != null) {
            player.teleport(entrance);
        }
    }

    public void leaveDungeon(org.bukkit.entity.Player player) {
        Location back = returnLocations.remove(player.getUniqueId());
        if (back != null) {
            player.teleport(back);
            return;
        }
        if (exit != null) {
            player.teleport(exit);
        }
    }

    public void generateDungeon(Player player, String theme, List<Player> party) {
        if ("wfc".equalsIgnoreCase(theme)) {
            generator.generateWfc(theme, party, instance -> {
                for (Player member : party) {
                    activeInstances.put(member.getUniqueId(), instance);
                }
            });
            return;
        }
        DungeonInstance instance = generator.generate(theme, party);
        for (Player member : party) {
            activeInstances.put(member.getUniqueId(), instance);
        }
    }

    private void load() {
        String world = config.getString("dungeon.entrance.world", null);
        if (world != null) {
            entrance = new Location(plugin.getServer().getWorld(world),
                config.getDouble("dungeon.entrance.x"),
                config.getDouble("dungeon.entrance.y"),
                config.getDouble("dungeon.entrance.z"));
        }
        String exitWorld = config.getString("dungeon.exit.world", null);
        if (exitWorld != null) {
            exit = new Location(plugin.getServer().getWorld(exitWorld),
                config.getDouble("dungeon.exit.x"),
                config.getDouble("dungeon.exit.y"),
                config.getDouble("dungeon.exit.z"));
        }
    }
}

package com.example.rpg.manager;

import com.example.rpg.RPGPlugin;
import com.example.rpg.dungeon.DungeonGenerator;
import com.example.rpg.dungeon.DungeonInstance;
import com.example.rpg.util.Text;
import com.example.rpg.util.WorldUtils;
import java.util.ArrayList;
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
    private final List<DungeonInstance> allInstances = new ArrayList<>();

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
        activeInstances.remove(player.getUniqueId());
        if (back != null) {
            player.teleport(back);
            return;
        }
        if (exit != null) {
            player.teleport(exit);
            return;
        }
        if (!plugin.getServer().getWorlds().isEmpty()) {
            player.teleport(plugin.getServer().getWorlds().get(0).getSpawnLocation());
        }
    }

    public void generateDungeon(Player player, String theme, List<Player> party) {
        if (!party.contains(player)) {
            party.add(player);
        }
        java.util.function.Consumer<DungeonInstance> onGenerated = instance -> {
            allInstances.add(instance);
            for (Player member : party) {
                returnLocations.put(member.getUniqueId(), member.getLocation());
                activeInstances.put(member.getUniqueId(), instance);
                if (instance.start() != null) {
                    member.teleport(instance.start());
                    member.sendMessage(Text.mm("<green>Dungeon generiert: " + theme));
                }
            }
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> closeDungeon(instance), 20L * 60L * 15L);
        };

        if ("wfc".equalsIgnoreCase(theme)) {
            generator.generateWfc(theme, party, onGenerated);
            return;
        }
        DungeonInstance instance = generator.generate(theme, party);
        onGenerated.accept(instance);
    }

    public void closeDungeon(DungeonInstance instance) {
        if (!allInstances.contains(instance)) {
            return;
        }
        WorldUtils.unloadAndDeleteWorld(instance.world(), exit != null ? exit : entrance);
        allInstances.remove(instance);
        activeInstances.values().removeIf(active -> active.equals(instance));
    }

    public void shutdown() {
        for (DungeonInstance instance : new ArrayList<>(allInstances)) {
            WorldUtils.unloadAndDeleteWorld(instance.world(), exit);
        }
        allInstances.clear();
        activeInstances.clear();
    }

    private void load() {
        String world = config.getString("dungeon.entrance.world", null);
        if (world != null && plugin.getServer().getWorld(world) != null) {
            entrance = new Location(plugin.getServer().getWorld(world),
                config.getDouble("dungeon.entrance.x"),
                config.getDouble("dungeon.entrance.y"),
                config.getDouble("dungeon.entrance.z"));
        }
        String exitWorld = config.getString("dungeon.exit.world", null);
        if (exitWorld != null && plugin.getServer().getWorld(exitWorld) != null) {
            exit = new Location(plugin.getServer().getWorld(exitWorld),
                config.getDouble("dungeon.exit.x"),
                config.getDouble("dungeon.exit.y"),
                config.getDouble("dungeon.exit.z"));
        }
    }
}

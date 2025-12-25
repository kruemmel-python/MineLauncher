package com.example.rpg.dungeon;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.MobDefinition;
import com.example.rpg.model.Spawner;
import com.example.rpg.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

public class DungeonGenerator {
    private final RPGPlugin plugin;
    private final Random random = new Random();

    public DungeonGenerator(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    public DungeonInstance generate(String theme, List<Player> party) {
        String worldName = "dungeon_" + System.currentTimeMillis();
        World world = plugin.getServer().createWorld(new WorldCreator(worldName));
        int roomSize = 9;
        int grid = 4;
        int baseY = 60;
        List<Location> roomCenters = new ArrayList<>();
        for (int x = 0; x < grid; x++) {
            for (int z = 0; z < grid; z++) {
                int startX = x * (roomSize + 2);
                int startZ = z * (roomSize + 2);
                carveRoom(world, startX, baseY, startZ, roomSize, Material.STONE_BRICKS);
                roomCenters.add(new Location(world, startX + roomSize / 2.0, baseY + 1, startZ + roomSize / 2.0));
                if (x > 0) {
                    carveCorridor(world, startX - 1, baseY, startZ + roomSize / 2, Material.COBBLESTONE);
                }
                if (z > 0) {
                    carveCorridor(world, startX + roomSize / 2, baseY, startZ - 1, Material.COBBLESTONE);
                }
            }
        }
        Location start = roomCenters.get(0).clone();
        Location bossRoom = roomCenters.get(roomCenters.size() - 1).clone();
        spawnSpawners(roomCenters);
        spawnBoss(bossRoom);
        spawnSigns(start, bossRoom, theme);
        for (Player player : party) {
            player.teleport(start);
            player.sendMessage(Text.mm("<green>Dungeon generiert: " + theme));
        }
        return new DungeonInstance(world, start, bossRoom);
    }

    private void carveRoom(World world, int startX, int startY, int startZ, int size, Material material) {
        for (int x = startX; x < startX + size; x++) {
            for (int z = startZ; z < startZ + size; z++) {
                world.getBlockAt(x, startY, z).setType(material);
                for (int y = 1; y <= 4; y++) {
                    world.getBlockAt(x, startY + y, z).setType(Material.AIR);
                }
            }
        }
        for (int x = startX; x < startX + size; x++) {
            for (int z = startZ; z < startZ + size; z++) {
                if (x == startX || x == startX + size - 1 || z == startZ || z == startZ + size - 1) {
                    world.getBlockAt(x, startY + 5, z).setType(material);
                }
            }
        }
    }

    private void carveCorridor(World world, int startX, int startY, int startZ, Material material) {
        for (int i = 0; i < 3; i++) {
            world.getBlockAt(startX + i, startY, startZ).setType(material);
            for (int y = 1; y <= 3; y++) {
                world.getBlockAt(startX + i, startY + y, startZ).setType(Material.AIR);
            }
        }
    }

    private void spawnSpawners(List<Location> roomCenters) {
        for (int i = 1; i < roomCenters.size() - 1; i++) {
            Location location = roomCenters.get(i);
            Spawner spawner = plugin.spawnerManager().spawners().values().stream().findFirst().orElse(null);
            if (spawner == null) {
                continue;
            }
            if (spawner.mobs().isEmpty()) {
                continue;
            }
            String mobId = spawner.mobs().keySet().iterator().next();
            MobDefinition mob = plugin.mobManager().getMob(mobId);
            if (mob == null) {
                continue;
            }
            var entity = location.getWorld().spawnEntity(location, EntityType.valueOf(mob.type().toUpperCase()));
            if (entity instanceof org.bukkit.entity.LivingEntity living) {
                plugin.customMobListener().applyDefinition(living, mob);
            }
        }
    }

    private void spawnBoss(Location bossRoom) {
        MobDefinition boss = plugin.mobManager().getMob("boss_zombie");
        if (boss == null) {
            return;
        }
        var entity = bossRoom.getWorld().spawnEntity(bossRoom, EntityType.valueOf(boss.type().toUpperCase()));
        if (entity instanceof org.bukkit.entity.LivingEntity living) {
            plugin.customMobListener().applyDefinition(living, boss);
            TextDisplay display = bossRoom.getWorld().spawn(bossRoom.clone().add(0, 2, 0), TextDisplay.class);
            display.text(Text.mm("<red>Boss: " + boss.name()));
        }
    }

    private void spawnSigns(Location start, Location bossRoom, String theme) {
        TextDisplay startSign = start.getWorld().spawn(start.clone().add(0, 2, 0), TextDisplay.class);
        startSign.text(Text.mm("<gold>Dungeon: " + theme));
        TextDisplay bossSign = bossRoom.getWorld().spawn(bossRoom.clone().add(0, 2, 0), TextDisplay.class);
        bossSign.text(Text.mm("<red>Boss-Raum"));
    }
}

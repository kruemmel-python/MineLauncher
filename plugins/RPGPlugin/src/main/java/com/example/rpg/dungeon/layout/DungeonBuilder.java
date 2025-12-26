package com.example.rpg.dungeon.layout;

import com.example.rpg.RPGPlugin;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class DungeonBuilder {
    private final RPGPlugin plugin;
    private final Random random;
    private final DungeonDecorator decorator;
    private final SpawnPlacer spawnPlacer;
    private final LootPlacer lootPlacer;

    public DungeonBuilder(RPGPlugin plugin, Random random) {
        this.plugin = plugin;
        this.random = random;
        this.decorator = new DungeonDecorator();
        this.spawnPlacer = new SpawnPlacer(plugin, random);
        this.lootPlacer = new LootPlacer(random);
    }

    public void build(World world, DungeonPlan plan, DungeonSettings settings) {
        BlockBuffer buffer = new BlockBuffer();
        for (Room room : plan.rooms()) {
            carveRoom(buffer, room, settings);
            decorator.decorateRoom(world, buffer, room, settings, random);
        }
        for (Corridor corridor : plan.corridors()) {
            carveCorridor(buffer, corridor, plan.bounds(), settings);
        }
        applyDoors(buffer, plan, settings);
        applyWater(buffer, plan, settings);
        buffer.flush(world);
        for (Room room : plan.rooms()) {
            spawnPlacer.spawnRoomMobs(room, settings, world);
            lootPlacer.placeLoot(room, settings, world);
        }
    }

    private void carveRoom(BlockBuffer buffer, Room room, DungeonSettings settings) {
        BoundingBox box = room.bounds();
        int minX = (int) box.getMinX();
        int maxX = (int) box.getMaxX();
        int minZ = (int) box.getMinZ();
        int maxZ = (int) box.getMaxZ();
        int baseY = (int) box.getMinY();
        int height = 5;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                buffer.set(x, baseY, z, settings.floorBlock());
                for (int y = 1; y <= height; y++) {
                    Material material = (x == minX || x == maxX || z == minZ || z == maxZ)
                        ? settings.wallBlock()
                        : Material.AIR;
                    buffer.set(x, baseY + y, z, material);
                }
            }
        }
    }

    private void carveCorridor(BlockBuffer buffer, Corridor corridor, BoundingBox bounds, DungeonSettings settings) {
        int width = settings.corridorWidth();
        int baseY = (int) bounds.getMinY();
        int height = 4;
        for (Vector step : corridor.path()) {
            int centerX = step.getBlockX();
            int centerZ = step.getBlockZ();
            for (int dx = -width / 2; dx <= width / 2; dx++) {
                for (int dz = -width / 2; dz <= width / 2; dz++) {
                    int x = centerX + dx;
                    int z = centerZ + dz;
                    buffer.set(x, baseY, z, settings.floorBlock());
                    for (int y = 1; y <= height; y++) {
                        buffer.set(x, baseY + y, z, Material.AIR);
                    }
                }
            }
        }
    }

    private void applyDoors(BlockBuffer buffer, DungeonPlan plan, DungeonSettings settings) {
        Material doorMaterial = settings.doorBlock();
        for (Room room : plan.rooms()) {
            for (Location door : room.doorPoints()) {
                int x = door.getBlockX();
                int y = door.getBlockY();
                int z = door.getBlockZ();
                buffer.set(x, y, z, settings.floorBlock());
                buffer.set(x, y + 1, z, doorMaterial);
                buffer.set(x, y + 2, z, doorMaterial);
            }
        }
    }

    private void applyWater(BlockBuffer buffer, DungeonPlan plan, DungeonSettings settings) {
        if (!settings.waterEnabled()) {
            return;
        }
        if (settings.canalChance() > 0) {
            for (Corridor corridor : plan.corridors()) {
                if (random.nextDouble() > settings.canalChance()) {
                    continue;
                }
                for (Vector step : corridor.path()) {
                    int x = step.getBlockX();
                    int z = step.getBlockZ();
                    int y = (int) plan.bounds().getMinY() + 1;
                    buffer.set(x + settings.corridorWidth(), y, z, Material.WATER);
                }
            }
        }
        for (Room room : plan.rooms()) {
            if (random.nextDouble() > settings.floodRoomChance()) {
                continue;
            }
            BoundingBox box = room.bounds();
            int minX = (int) box.getMinX() + 1;
            int maxX = (int) box.getMaxX() - 1;
            int minZ = (int) box.getMinZ() + 1;
            int maxZ = (int) box.getMaxZ() - 1;
            int y = (int) box.getMinY() + 1;
            for (int x = minX; x <= maxX; x++) {
                buffer.set(x, y, minZ, Material.WATER);
                buffer.set(x, y, maxZ, Material.WATER);
            }
            for (int z = minZ; z <= maxZ; z++) {
                buffer.set(minX, y, z, Material.WATER);
                buffer.set(maxX, y, z, Material.WATER);
            }
        }
    }
}

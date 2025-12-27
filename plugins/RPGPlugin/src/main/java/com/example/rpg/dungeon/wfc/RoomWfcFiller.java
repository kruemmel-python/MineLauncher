package com.example.rpg.dungeon.wfc;

import com.example.rpg.RPGPlugin;
import com.example.rpg.dungeon.layout.Room;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

public class RoomWfcFiller {
    private final RPGPlugin plugin;
    private final WfcGenerator wfcGenerator;

    public RoomWfcFiller(RPGPlugin plugin, WfcGenerator wfcGenerator) {
        this.plugin = plugin;
        this.wfcGenerator = wfcGenerator;
    }

    public void fillRoom(World world, Room room, String theme) {
        BoundingBox bounds = room.bounds();
        int minX = (int) bounds.getMinX() + 1;
        int minY = (int) bounds.getMinY() + 1;
        int minZ = (int) bounds.getMinZ() + 1;
        int maxX = (int) bounds.getMaxX() - 1;
        int maxY = (int) bounds.getMaxY() - 1;
        int maxZ = (int) bounds.getMaxZ() - 1;
        int interiorWidth = maxX - minX + 1;
        int interiorHeight = maxY - minY + 1;
        int interiorDepth = maxZ - minZ + 1;
        if (interiorWidth <= 1 || interiorHeight <= 1 || interiorDepth <= 1) {
            return;
        }
        int cellSize = 2;
        int gridWidth = Math.max(1, interiorWidth / cellSize);
        int gridHeight = Math.max(1, interiorHeight / cellSize);
        int gridDepth = Math.max(1, interiorDepth / cellSize);
        wfcGenerator.generate(theme, gridWidth, gridHeight, gridDepth).thenAccept(patterns -> {
            if (patterns == null) {
                return;
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    placePatterns(world, patterns, minX, minY, minZ, cellSize);
                }
            }.runTask(plugin);
        });
    }

    private void placePatterns(World world, Pattern[][][] patterns, int baseX, int baseY, int baseZ, int cellSize) {
        for (int x = 0; x < patterns.length; x++) {
            for (int y = 0; y < patterns[x].length; y++) {
                for (int z = 0; z < patterns[x][y].length; z++) {
                    Pattern pattern = patterns[x][y][z];
                    if (pattern == null) {
                        continue;
                    }
                    placePattern(world, pattern, baseX + x * cellSize, baseY + y * cellSize, baseZ + z * cellSize);
                }
            }
        }
    }

    private void placePattern(World world, Pattern pattern, int baseX, int baseY, int baseZ) {
        int index = 0;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    world.getBlockAt(baseX + x, baseY + y, baseZ + z).setType(pattern.blocks()[index++], false);
                }
            }
        }
    }
}

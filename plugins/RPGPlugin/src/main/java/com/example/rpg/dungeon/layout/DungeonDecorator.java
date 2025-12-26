package com.example.rpg.dungeon.layout;

import java.util.Random;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;

public class DungeonDecorator {
    public void decorateRoom(World world, BlockBuffer buffer, Room room, DungeonSettings settings, Random random) {
        BoundingBox box = room.bounds();
        int minX = (int) box.getMinX() + 1;
        int maxX = (int) box.getMaxX() - 1;
        int minZ = (int) box.getMinZ() + 1;
        int maxZ = (int) box.getMaxZ() - 1;
        int floorY = (int) box.getMinY();
        Material floorMaterial = settings.floorBlock();
        if (settings.debugEnabled()) {
            floorMaterial = debugFloor(room.type());
        }
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                buffer.set(x, floorY, z, floorMaterial);
                if (!settings.debugEnabled() && random.nextDouble() < 0.08) {
                    buffer.set(x, floorY, z, Material.CRACKED_STONE_BRICKS);
                }
            }
        }
        placeLights(buffer, settings, floorY + 2, minX, maxX, minZ, maxZ);
        if (room.type() == RoomType.ELITE || room.type() == RoomType.BOSS) {
            placePillars(buffer, settings, floorY + 1, minX, maxX, minZ, maxZ);
        }
    }

    private void placeLights(BlockBuffer buffer, DungeonSettings settings, int y, int minX, int maxX, int minZ, int maxZ) {
        buffer.set(minX, y, minZ, settings.lightBlock());
        buffer.set(maxX, y, minZ, settings.lightBlock());
        buffer.set(minX, y, maxZ, settings.lightBlock());
        buffer.set(maxX, y, maxZ, settings.lightBlock());
    }

    private void placePillars(BlockBuffer buffer, DungeonSettings settings, int y, int minX, int maxX, int minZ, int maxZ) {
        int midX = (minX + maxX) / 2;
        int midZ = (minZ + maxZ) / 2;
        for (int dy = 0; dy < 3; dy++) {
            buffer.set(midX, y + dy, midZ, settings.wallBlock());
        }
    }

    private Material debugFloor(RoomType type) {
        return switch (type) {
            case START -> Material.GREEN_WOOL;
            case BOSS -> Material.RED_WOOL;
            case EXIT -> Material.BLUE_WOOL;
            case LOOT -> Material.YELLOW_WOOL;
            case ELITE -> Material.PURPLE_WOOL;
            case PUZZLE -> Material.ORANGE_WOOL;
            case COMBAT -> Material.GRAY_WOOL;
        };
    }
}

package com.example.rpg.dungeon.wfc;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public class PatternLoader {
    public List<Pattern> loadDefaultPatterns() {
        List<Pattern> patterns = new ArrayList<>();

        Pattern air = new Pattern("air", fill(Material.AIR), 1.0);
        for (Direction direction : Direction.values()) {
            air.setSocket(direction, "AIR");
        }
        patterns.add(air);

        Pattern floor = new Pattern("floor", floorBlocks(), 1.5);
        floor.setSocket(Direction.DOWN, "FLOOR");
        floor.setSocket(Direction.UP, "AIR");
        floor.setSocket(Direction.NORTH, "AIR");
        floor.setSocket(Direction.SOUTH, "AIR");
        floor.setSocket(Direction.EAST, "AIR");
        floor.setSocket(Direction.WEST, "AIR");
        patterns.add(floor);

        Pattern wallNorth = new Pattern("wall_north", wallBlocks(Direction.NORTH), 1.0);
        wallNorth.setSocket(Direction.DOWN, "FLOOR");
        wallNorth.setSocket(Direction.UP, "AIR");
        wallNorth.setSocket(Direction.NORTH, "WALL");
        wallNorth.setSocket(Direction.SOUTH, "AIR");
        wallNorth.setSocket(Direction.EAST, "AIR");
        wallNorth.setSocket(Direction.WEST, "AIR");
        patterns.add(wallNorth);

        Pattern corridor = new Pattern("corridor_ns", corridorBlocks(), 1.2);
        corridor.setSocket(Direction.DOWN, "FLOOR");
        corridor.setSocket(Direction.UP, "AIR");
        corridor.setSocket(Direction.NORTH, "OPEN");
        corridor.setSocket(Direction.SOUTH, "OPEN");
        corridor.setSocket(Direction.EAST, "WALL");
        corridor.setSocket(Direction.WEST, "WALL");
        patterns.add(corridor);

        return patterns;
    }

    private Material[] fill(Material material) {
        Material[] blocks = new Material[8];
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = material;
        }
        return blocks;
    }

    private Material[] floorBlocks() {
        Material[] blocks = new Material[8];
        int index = 0;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    blocks[index++] = y == 0 ? Material.STONE_BRICKS : Material.AIR;
                }
            }
        }
        return blocks;
    }

    private Material[] wallBlocks(Direction direction) {
        Material[] blocks = floorBlocks();
        int index = 0;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    if (y == 1 && isWallCell(direction, x, z)) {
                        blocks[index] = Material.COBBLESTONE_WALL;
                    }
                    index++;
                }
            }
        }
        return blocks;
    }

    private boolean isWallCell(Direction direction, int x, int z) {
        return switch (direction) {
            case NORTH -> z == 0;
            case SOUTH -> z == 1;
            case EAST -> x == 1;
            case WEST -> x == 0;
            default -> false;
        };
    }

    private Material[] corridorBlocks() {
        return floorBlocks();
    }
}

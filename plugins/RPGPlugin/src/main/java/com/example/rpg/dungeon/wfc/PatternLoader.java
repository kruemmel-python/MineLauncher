package com.example.rpg.dungeon.wfc;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public class PatternLoader {
    private final JavaPlugin plugin;

    public PatternLoader(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public List<Pattern> loadPatterns(String themeName) {
        List<Pattern> patterns = new ArrayList<>();
        ThemeMaterials materials = resolveTheme(themeName);

        Pattern air = new Pattern("air", fill(Material.AIR), 1.0);
        for (Direction direction : Direction.values()) {
            air.setSocket(direction, "AIR");
        }
        patterns.add(air);

        Pattern floor = new Pattern("floor", floorBlocks(materials.floor()), 1.5);
        floor.setSocket(Direction.DOWN, "FLOOR");
        floor.setSocket(Direction.UP, "AIR");
        floor.setSocket(Direction.NORTH, "AIR");
        floor.setSocket(Direction.SOUTH, "AIR");
        floor.setSocket(Direction.EAST, "AIR");
        floor.setSocket(Direction.WEST, "AIR");
        patterns.add(floor);

        Pattern wallNorth = new Pattern("wall_north", wallBlocks(Direction.NORTH, materials.wall()), 1.0);
        wallNorth.setSocket(Direction.DOWN, "FLOOR");
        wallNorth.setSocket(Direction.UP, "AIR");
        wallNorth.setSocket(Direction.NORTH, "WALL");
        wallNorth.setSocket(Direction.SOUTH, "AIR");
        wallNorth.setSocket(Direction.EAST, "AIR");
        wallNorth.setSocket(Direction.WEST, "AIR");
        patterns.add(wallNorth);
        Pattern wallEast = rotateY(wallNorth, "wall_east");
        Pattern wallSouth = rotateY(wallEast, "wall_south");
        Pattern wallWest = rotateY(wallSouth, "wall_west");
        patterns.add(wallEast);
        patterns.add(wallSouth);
        patterns.add(wallWest);

        Pattern corridor = new Pattern("corridor_ns", corridorBlocks(materials.corridor()), 1.2);
        corridor.setSocket(Direction.DOWN, "FLOOR");
        corridor.setSocket(Direction.UP, "AIR");
        corridor.setSocket(Direction.NORTH, "OPEN");
        corridor.setSocket(Direction.SOUTH, "OPEN");
        corridor.setSocket(Direction.EAST, "WALL");
        corridor.setSocket(Direction.WEST, "WALL");
        patterns.add(corridor);
        patterns.add(rotateY(corridor, "corridor_ew"));

        return patterns;
    }

    private Material[] fill(Material material) {
        Material[] blocks = new Material[8];
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = material;
        }
        return blocks;
    }

    private Material[] floorBlocks(Material floorMaterial) {
        Material[] blocks = new Material[8];
        int index = 0;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    blocks[index++] = y == 0 ? floorMaterial : Material.AIR;
                }
            }
        }
        return blocks;
    }

    private Material[] wallBlocks(Direction direction, Material wallMaterial) {
        Material[] blocks = floorBlocks(wallMaterial);
        int index = 0;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    if (y == 1 && isWallCell(direction, x, z)) {
                        blocks[index] = wallMaterial;
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

    private Material[] corridorBlocks(Material corridorMaterial) {
        return floorBlocks(corridorMaterial);
    }

    private Pattern rotateY(Pattern base, String newId) {
        Material[] rotated = new Material[8];
        int index = 0;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    int rx = z;
                    int rz = 1 - x;
                    int targetIndex = rx * 4 + y * 2 + rz;
                    rotated[targetIndex] = base.blocks()[index++];
                }
            }
        }
        Pattern rotatedPattern = new Pattern(newId, rotated, base.weight());
        rotatedPattern.setSocket(Direction.UP, base.socket(Direction.UP));
        rotatedPattern.setSocket(Direction.DOWN, base.socket(Direction.DOWN));
        rotatedPattern.setSocket(Direction.NORTH, base.socket(Direction.WEST));
        rotatedPattern.setSocket(Direction.EAST, base.socket(Direction.NORTH));
        rotatedPattern.setSocket(Direction.SOUTH, base.socket(Direction.EAST));
        rotatedPattern.setSocket(Direction.WEST, base.socket(Direction.SOUTH));
        return rotatedPattern;
    }

    private ThemeMaterials resolveTheme(String themeName) {
        String key = themeName != null ? themeName.toLowerCase() : "crypt";
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("dungeon.themes." + key);
        Material floor = readMaterial(section, "floor_material", Material.STONE_BRICKS);
        Material wall = readMaterial(section, "wall_material", Material.COBBLESTONE);
        Material corridor = readMaterial(section, "corridor_material", Material.COBBLESTONE);
        return new ThemeMaterials(floor, wall, corridor);
    }

    private Material readMaterial(ConfigurationSection section, String path, Material fallback) {
        if (section == null) {
            return fallback;
        }
        String raw = section.getString(path, fallback.name());
        Material material = Material.matchMaterial(raw);
        return material != null ? material : fallback;
    }

    private record ThemeMaterials(Material floor, Material wall, Material corridor) {}
}

package com.example.rpg.dungeon.wfc;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import org.bukkit.plugin.java.JavaPlugin;

public class WfcGenerator {
    private final PatternLoader patternLoader;
    private final Random random = new Random();

    public WfcGenerator(JavaPlugin plugin) {
        this.patternLoader = new PatternLoader(plugin);
    }

    public CompletableFuture<Pattern[][][]> generate(String themeName, int width, int height, int depth) {
        return CompletableFuture.supplyAsync(() -> {
            List<Pattern> patterns = patternLoader.loadPatterns(themeName);
            for (int attempt = 0; attempt < 5; attempt++) {
                Pattern[][][] result = runAttempt(width, height, depth, patterns);
                if (result != null) {
                    return result;
                }
            }
            return null;
        });
    }

    private Pattern[][][] runAttempt(int width, int height, int depth, List<Pattern> patterns) {
        WaveGrid grid = new WaveGrid(width, height, depth, patterns);
        while (true) {
            int[] cell = findLowestEntropyCell(grid);
            if (cell == null) {
                break;
            }
            int x = cell[0];
            int y = cell[1];
            int z = cell[2];
            Pattern chosen = pickWeighted(grid.possibilities(x, y, z));
            grid.setPossibilities(x, y, z, List.of(chosen));
            grid.setCollapsed(x, y, z, true);
            if (!propagate(grid, x, y, z)) {
                return null;
            }
        }
        Pattern[][][] patternsResult = new Pattern[width][height][depth];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    List<Pattern> options = grid.possibilities(x, y, z);
                    patternsResult[x][y][z] = options.isEmpty() ? null : options.get(0);
                }
            }
        }
        return patternsResult;
    }

    private int[] findLowestEntropyCell(WaveGrid grid) {
        int bestX = -1;
        int bestY = -1;
        int bestZ = -1;
        int bestEntropy = Integer.MAX_VALUE;
        for (int x = 0; x < grid.width(); x++) {
            for (int y = 0; y < grid.height(); y++) {
                for (int z = 0; z < grid.depth(); z++) {
                    if (grid.collapsed(x, y, z)) {
                        continue;
                    }
                    int size = grid.possibilities(x, y, z).size();
                    if (size == 0) {
                        return new int[] {x, y, z};
                    }
                    if (size < bestEntropy) {
                        bestEntropy = size;
                        bestX = x;
                        bestY = y;
                        bestZ = z;
                    }
                }
            }
        }
        if (bestX == -1) {
            return null;
        }
        return new int[] {bestX, bestY, bestZ};
    }

    private Pattern pickWeighted(List<Pattern> options) {
        double total = options.stream().mapToDouble(Pattern::weight).sum();
        double roll = random.nextDouble() * total;
        double current = 0;
        for (Pattern pattern : options) {
            current += pattern.weight();
            if (roll <= current) {
                return pattern;
            }
        }
        return options.get(0);
    }

    private boolean propagate(WaveGrid grid, int startX, int startY, int startZ) {
        Queue<int[]> queue = new ArrayDeque<>();
        queue.add(new int[] {startX, startY, startZ});
        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            int x = cell[0];
            int y = cell[1];
            int z = cell[2];
            for (Direction direction : Direction.values()) {
                int nx = x + offsetX(direction);
                int ny = y + offsetY(direction);
                int nz = z + offsetZ(direction);
                if (!inside(grid, nx, ny, nz)) {
                    continue;
                }
                List<Pattern> neighborOptions = grid.possibilities(nx, ny, nz);
                List<Pattern> filtered = new ArrayList<>();
                for (Pattern option : neighborOptions) {
                    if (compatible(grid.possibilities(x, y, z), option, direction)) {
                        filtered.add(option);
                    }
                }
                if (filtered.isEmpty()) {
                    return false;
                }
                if (filtered.size() != neighborOptions.size()) {
                    grid.setPossibilities(nx, ny, nz, filtered);
                    queue.add(new int[] {nx, ny, nz});
                }
            }
        }
        return true;
    }

    private boolean compatible(List<Pattern> sourceOptions, Pattern neighbor, Direction direction) {
        for (Pattern source : sourceOptions) {
            String socketA = source.socket(direction);
            String socketB = neighbor.socket(direction.opposite());
            if (socketA.equals(socketB)) {
                return true;
            }
        }
        return false;
    }

    private boolean inside(WaveGrid grid, int x, int y, int z) {
        return x >= 0 && x < grid.width()
            && y >= 0 && y < grid.height()
            && z >= 0 && z < grid.depth();
    }

    private int offsetX(Direction direction) {
        return switch (direction) {
            case EAST -> 1;
            case WEST -> -1;
            default -> 0;
        };
    }

    private int offsetY(Direction direction) {
        return switch (direction) {
            case UP -> 1;
            case DOWN -> -1;
            default -> 0;
        };
    }

    private int offsetZ(Direction direction) {
        return switch (direction) {
            case SOUTH -> 1;
            case NORTH -> -1;
            default -> 0;
        };
    }
}

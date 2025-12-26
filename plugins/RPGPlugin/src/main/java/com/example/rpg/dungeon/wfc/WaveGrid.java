package com.example.rpg.dungeon.wfc;

import java.util.ArrayList;
import java.util.List;

public class WaveGrid {
    private final List<Pattern>[][][] possibilities;
    private final boolean[][][] collapsed;

    @SuppressWarnings("unchecked")
    public WaveGrid(int width, int height, int depth, List<Pattern> initial) {
        possibilities = new List[width][height][depth];
        collapsed = new boolean[width][height][depth];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    possibilities[x][y][z] = new ArrayList<>(initial);
                }
            }
        }
    }

    public List<Pattern> possibilities(int x, int y, int z) {
        return possibilities[x][y][z];
    }

    public void setPossibilities(int x, int y, int z, List<Pattern> list) {
        possibilities[x][y][z] = list;
    }

    public boolean collapsed(int x, int y, int z) {
        return collapsed[x][y][z];
    }

    public void setCollapsed(int x, int y, int z, boolean value) {
        collapsed[x][y][z] = value;
    }

    public int width() {
        return possibilities.length;
    }

    public int height() {
        return possibilities[0].length;
    }

    public int depth() {
        return possibilities[0][0].length;
    }
}

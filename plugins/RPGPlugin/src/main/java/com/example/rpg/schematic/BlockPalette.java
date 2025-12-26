package com.example.rpg.schematic;

import java.util.Map;

public class BlockPalette {
    private final Map<Integer, String> idToState;

    public BlockPalette(Map<Integer, String> idToState) {
        this.idToState = Map.copyOf(idToState);
    }

    public String getState(int id) {
        return idToState.get(id);
    }

    public int size() {
        return idToState.size();
    }
}

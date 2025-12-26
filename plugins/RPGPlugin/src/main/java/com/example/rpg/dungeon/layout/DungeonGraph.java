package com.example.rpg.dungeon.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DungeonGraph {
    private final Map<Room, List<Room>> adjacency = new HashMap<>();

    public void addEdge(Room a, Room b) {
        adjacency.computeIfAbsent(a, key -> new ArrayList<>()).add(b);
        adjacency.computeIfAbsent(b, key -> new ArrayList<>()).add(a);
    }

    public Map<Room, List<Room>> adjacency() {
        return adjacency;
    }
}

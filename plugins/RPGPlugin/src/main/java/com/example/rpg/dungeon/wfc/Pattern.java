package com.example.rpg.dungeon.wfc;

import java.util.EnumMap;
import java.util.Map;
import org.bukkit.Material;

public class Pattern {
    private final String id;
    private final Material[] blocks;
    private final Map<Direction, String> sockets = new EnumMap<>(Direction.class);
    private final double weight;

    public Pattern(String id, Material[] blocks, double weight) {
        this.id = id;
        this.blocks = blocks;
        this.weight = weight;
    }

    public String id() {
        return id;
    }

    public Material[] blocks() {
        return blocks;
    }

    public double weight() {
        return weight;
    }

    public void setSocket(Direction direction, String socket) {
        sockets.put(direction, socket);
    }

    public String socket(Direction direction) {
        return sockets.getOrDefault(direction, "AIR");
    }

    public String socketDown() {
        return socket(Direction.DOWN);
    }
}

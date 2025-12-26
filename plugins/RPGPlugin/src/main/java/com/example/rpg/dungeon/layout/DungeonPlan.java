package com.example.rpg.dungeon.layout;

import java.util.List;
import org.bukkit.util.BoundingBox;

public class DungeonPlan {
    private final long seed;
    private final BoundingBox bounds;
    private final List<Room> rooms;
    private final List<Corridor> corridors;
    private final DungeonGraph graph;
    private final Room startRoom;
    private final Room bossRoom;
    private final Room exitRoom;

    public DungeonPlan(long seed,
                       BoundingBox bounds,
                       List<Room> rooms,
                       List<Corridor> corridors,
                       DungeonGraph graph,
                       Room startRoom,
                       Room bossRoom,
                       Room exitRoom) {
        this.seed = seed;
        this.bounds = bounds;
        this.rooms = rooms;
        this.corridors = corridors;
        this.graph = graph;
        this.startRoom = startRoom;
        this.bossRoom = bossRoom;
        this.exitRoom = exitRoom;
    }

    public long seed() {
        return seed;
    }

    public BoundingBox bounds() {
        return bounds;
    }

    public List<Room> rooms() {
        return rooms;
    }

    public List<Corridor> corridors() {
        return corridors;
    }

    public DungeonGraph graph() {
        return graph;
    }

    public Room startRoom() {
        return startRoom;
    }

    public Room bossRoom() {
        return bossRoom;
    }

    public Room exitRoom() {
        return exitRoom;
    }
}

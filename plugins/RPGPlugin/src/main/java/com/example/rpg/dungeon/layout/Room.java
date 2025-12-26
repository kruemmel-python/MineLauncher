package com.example.rpg.dungeon.layout;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

public class Room {
    private final int id;
    private final BoundingBox bounds;
    private RoomType type;
    private final List<Location> doorPoints = new ArrayList<>();
    private final List<Location> spawnPoints = new ArrayList<>();

    public Room(int id, BoundingBox bounds, RoomType type) {
        this.id = id;
        this.bounds = bounds;
        this.type = type;
    }

    public int id() {
        return id;
    }

    public BoundingBox bounds() {
        return bounds;
    }

    public RoomType type() {
        return type;
    }

    public void setType(RoomType type) {
        this.type = type;
    }

    public List<Location> doorPoints() {
        return doorPoints;
    }

    public List<Location> spawnPoints() {
        return spawnPoints;
    }

    public Location center(org.bukkit.World world) {
        return new Location(world,
            (bounds.getMinX() + bounds.getMaxX()) / 2.0,
            bounds.getMinY() + 1,
            (bounds.getMinZ() + bounds.getMaxZ()) / 2.0);
    }
}

package com.example.rpg.dungeon.layout;

import com.example.rpg.dungeon.jigsaw.RoomTemplate;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

public class Room {
    private final int id;
    private BoundingBox bounds;
    private RoomType type;
    private final List<Location> doorPoints = new ArrayList<>();
    private final List<Location> spawnPoints = new ArrayList<>();
    private final List<RoomSocket> sockets = new ArrayList<>();
    private RoomTemplate template;

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

    public void setBounds(BoundingBox bounds) {
        this.bounds = bounds;
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

    public List<RoomSocket> sockets() {
        return sockets;
    }

    public RoomTemplate template() {
        return template;
    }

    public void setTemplate(RoomTemplate template) {
        this.template = template;
    }

    public Location center(org.bukkit.World world) {
        return new Location(world,
            (bounds.getMinX() + bounds.getMaxX()) / 2.0,
            bounds.getMinY() + 1,
            (bounds.getMinZ() + bounds.getMaxZ()) / 2.0);
    }
}

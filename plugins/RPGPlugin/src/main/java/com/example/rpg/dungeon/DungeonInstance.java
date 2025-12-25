package com.example.rpg.dungeon;

import org.bukkit.Location;
import org.bukkit.World;

public class DungeonInstance {
    private final World world;
    private final Location start;
    private final Location bossRoom;

    public DungeonInstance(World world, Location start, Location bossRoom) {
        this.world = world;
        this.start = start;
        this.bossRoom = bossRoom;
    }

    public World world() {
        return world;
    }

    public Location start() {
        return start;
    }

    public Location bossRoom() {
        return bossRoom;
    }
}

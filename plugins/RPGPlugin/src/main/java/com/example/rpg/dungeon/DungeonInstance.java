package com.example.rpg.dungeon;

import org.bukkit.Location;
import org.bukkit.World;

public class DungeonInstance {
    private final World world;
    private final Location start;
    private final Location bossRoom;
    private final java.util.Set<java.util.UUID> participants;
    private final double scale;
    private boolean noDeath = true;

    public DungeonInstance(World world, Location start, Location bossRoom, java.util.Set<java.util.UUID> participants, double scale) {
        this.world = world;
        this.start = start;
        this.bossRoom = bossRoom;
        this.participants = participants;
        this.scale = scale;
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

    public java.util.Set<java.util.UUID> participants() {
        return participants;
    }

    public double scale() {
        return scale;
    }

    public boolean noDeath() {
        return noDeath;
    }

    public void setNoDeath(boolean noDeath) {
        this.noDeath = noDeath;
    }
}

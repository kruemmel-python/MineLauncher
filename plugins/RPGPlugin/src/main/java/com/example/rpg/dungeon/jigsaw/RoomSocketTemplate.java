package com.example.rpg.dungeon.jigsaw;

import com.example.rpg.dungeon.layout.RoomSocket;
import org.bukkit.Location;

public record RoomSocketTemplate(String name, int x, int y, int z) {
    public RoomSocket toSocket(Location origin) {
        return new RoomSocket(name, origin.clone().add(x, y, z));
    }
}

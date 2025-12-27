package com.example.rpg.dungeon.jigsaw;

import com.example.rpg.dungeon.layout.RoomSocket;
import com.example.rpg.dungeon.layout.RoomType;
import com.example.rpg.schematic.Schematic;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

public record RoomTemplate(String name, RoomType type, Schematic schematic, List<RoomSocketTemplate> sockets) {
    public BoundingBox boundsAt(Location origin) {
        return new BoundingBox(
            origin.getX(),
            origin.getY(),
            origin.getZ(),
            origin.getX() + schematic.width() - 1,
            origin.getY() + schematic.height() - 1,
            origin.getZ() + schematic.length() - 1
        );
    }

    public List<RoomSocket> socketsAt(Location origin) {
        return sockets.stream()
            .map(socket -> socket.toSocket(origin))
            .toList();
    }
}

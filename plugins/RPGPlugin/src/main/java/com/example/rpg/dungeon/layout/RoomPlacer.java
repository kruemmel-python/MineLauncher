package com.example.rpg.dungeon.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.util.BoundingBox;

public class RoomPlacer {
    private final Random random;

    public RoomPlacer(Random random) {
        this.random = random;
    }

    public List<Room> placeRooms(BoundingBox bounds, DungeonSettings settings) {
        List<Room> rooms = new ArrayList<>();
        int attempts = settings.roomCount() * 15;
        int padding = 2;
        int id = 0;
        while (rooms.size() < settings.roomCount() && attempts-- > 0) {
            int width = nextBetween(settings.roomMinSizeX(), settings.roomMaxSizeX());
            int depth = nextBetween(settings.roomMinSizeZ(), settings.roomMaxSizeZ());
            int minX = (int) bounds.getMinX() + padding;
            int maxX = (int) bounds.getMaxX() - padding - width;
            int minZ = (int) bounds.getMinZ() + padding;
            int maxZ = (int) bounds.getMaxZ() - padding - depth;
            if (maxX <= minX || maxZ <= minZ) {
                break;
            }
            int x1 = nextBetween(minX, maxX);
            int z1 = nextBetween(minZ, maxZ);
            int y1 = (int) bounds.getMinY();
            BoundingBox candidate = new BoundingBox(x1, y1, z1, x1 + width, y1 + 5, z1 + depth);
            if (isOverlapping(candidate, rooms, padding)) {
                continue;
            }
            Room room = new Room(id++, candidate, RoomType.COMBAT);
            rooms.add(room);
        }
        return rooms;
    }

    private boolean isOverlapping(BoundingBox candidate, List<Room> rooms, int padding) {
        BoundingBox padded = new BoundingBox(
            candidate.getMinX() - padding,
            candidate.getMinY(),
            candidate.getMinZ() - padding,
            candidate.getMaxX() + padding,
            candidate.getMaxY(),
            candidate.getMaxZ() + padding
        );
        for (Room room : rooms) {
            if (padded.overlaps(room.bounds())) {
                return true;
            }
        }
        return false;
    }

    private int nextBetween(int min, int max) {
        if (max <= min) {
            return min;
        }
        return random.nextInt(max - min + 1) + min;
    }
}

package com.example.rpg.dungeon.jigsaw;

import com.example.rpg.dungeon.layout.DungeonSettings;
import com.example.rpg.dungeon.layout.Room;
import com.example.rpg.dungeon.layout.RoomType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

public class JigsawRoomPlacer {
    private final Random random;

    public JigsawRoomPlacer(Random random) {
        this.random = random;
    }

    public List<Room> placeRooms(BoundingBox bounds, DungeonSettings settings, List<RoomTemplate> templates) {
        if (templates.isEmpty()) {
            return List.of();
        }
        List<RoomType> types = buildRoomTypes(settings);
        Map<RoomType, List<RoomTemplate>> byType = groupByType(templates);
        List<Room> rooms = new ArrayList<>();
        int attempts = settings.roomCount() * 20;
        int padding = 2;
        int id = 0;
        for (RoomType type : types) {
            RoomTemplate template = chooseTemplate(type, templates, byType);
            if (template == null) {
                continue;
            }
            boolean placed = false;
            while (attempts-- > 0 && !placed) {
                BoundingBox candidate = randomBounds(bounds, template, padding);
                if (candidate == null || isOverlapping(candidate, rooms, padding)) {
                    continue;
                }
                Room room = new Room(id++, candidate, type);
                room.setTemplate(template);
                room.sockets().addAll(template.socketsAt(new Location(null,
                    candidate.getMinX(), candidate.getMinY(), candidate.getMinZ())));
                rooms.add(room);
                placed = true;
            }
            if (!placed) {
                return List.of();
            }
        }
        return rooms;
    }

    private List<RoomType> buildRoomTypes(DungeonSettings settings) {
        int count = Math.max(3, settings.roomCount());
        List<RoomType> types = new ArrayList<>();
        types.add(RoomType.START);
        types.add(RoomType.EXIT);
        types.add(RoomType.BOSS);
        for (int i = 3; i < count; i++) {
            if (settings.lootEnabled() && random.nextDouble() < 0.2) {
                types.add(RoomType.LOOT);
            } else if (settings.eliteChance() > 0 && random.nextDouble() < settings.eliteChance()) {
                types.add(RoomType.ELITE);
            } else {
                types.add(RoomType.COMBAT);
            }
        }
        return types;
    }

    private Map<RoomType, List<RoomTemplate>> groupByType(List<RoomTemplate> templates) {
        Map<RoomType, List<RoomTemplate>> map = new EnumMap<>(RoomType.class);
        for (RoomTemplate template : templates) {
            map.computeIfAbsent(template.type(), key -> new ArrayList<>()).add(template);
        }
        return map;
    }

    private RoomTemplate chooseTemplate(RoomType type, List<RoomTemplate> all, Map<RoomType, List<RoomTemplate>> byType) {
        List<RoomTemplate> options = byType.get(type);
        if (options == null || options.isEmpty()) {
            options = all;
        }
        if (options.isEmpty()) {
            return null;
        }
        return options.get(random.nextInt(options.size()));
    }

    private BoundingBox randomBounds(BoundingBox bounds, RoomTemplate template, int padding) {
        int width = template.schematic().width();
        int depth = template.schematic().length();
        int height = template.schematic().height();
        int minX = (int) bounds.getMinX() + padding;
        int maxX = (int) bounds.getMaxX() - padding - width;
        int minZ = (int) bounds.getMinZ() + padding;
        int maxZ = (int) bounds.getMaxZ() - padding - depth;
        if (maxX <= minX || maxZ <= minZ) {
            return null;
        }
        int x1 = nextBetween(minX, maxX);
        int z1 = nextBetween(minZ, maxZ);
        int y1 = (int) bounds.getMinY();
        return new BoundingBox(x1, y1, z1, x1 + width - 1, y1 + height - 1, z1 + depth - 1);
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

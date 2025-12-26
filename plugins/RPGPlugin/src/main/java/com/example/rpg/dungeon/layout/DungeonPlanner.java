package com.example.rpg.dungeon.layout;

import com.example.rpg.dungeon.layout.CorridorRouter.RoomEdge;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.bukkit.util.BoundingBox;

public class DungeonPlanner {
    private final Random random;
    private final RoomPlacer roomPlacer;
    private final CorridorRouter corridorRouter;
    private final DungeonValidator validator;

    public DungeonPlanner(Random random) {
        this.random = random;
        this.roomPlacer = new RoomPlacer(random);
        this.corridorRouter = new CorridorRouter(random);
        this.validator = new DungeonValidator();
    }

    public DungeonPlan plan(long seed, BoundingBox bounds, DungeonSettings settings) {
        List<Room> rooms = roomPlacer.placeRooms(bounds, settings);
        if (rooms.size() < 3) {
            return null;
        }
        assignRoomTypes(rooms, settings);
        List<RoomEdge> edges = buildEdges(rooms);
        List<Corridor> corridors = corridorRouter.routeCorridors(bounds, rooms, edges);
        DungeonGraph graph = new DungeonGraph();
        for (RoomEdge edge : edges) {
            graph.addEdge(edge.a(), edge.b());
        }
        Room start = rooms.stream().filter(room -> room.type() == RoomType.START).findFirst().orElse(rooms.get(0));
        Room boss = rooms.stream().filter(room -> room.type() == RoomType.BOSS).findFirst().orElse(rooms.get(rooms.size() - 1));
        Room exit = rooms.stream().filter(room -> room.type() == RoomType.EXIT).findFirst().orElse(rooms.get(rooms.size() - 2));
        DungeonPlan plan = new DungeonPlan(seed, bounds, rooms, corridors, graph, start, boss, exit);
        if (!validator.validate(plan)) {
            return null;
        }
        return plan;
    }

    private void assignRoomTypes(List<Room> rooms, DungeonSettings settings) {
        rooms.sort(Comparator.comparingDouble(room -> room.bounds().getMinX()));
        Room start = rooms.get(0);
        Room boss = rooms.get(rooms.size() - 1);
        Room exit = rooms.get(rooms.size() - 2);
        start.setType(RoomType.START);
        boss.setType(RoomType.BOSS);
        exit.setType(RoomType.EXIT);
        for (int i = 1; i < rooms.size() - 2; i++) {
            Room room = rooms.get(i);
            if (settings.lootEnabled() && random.nextDouble() < 0.2) {
                room.setType(RoomType.LOOT);
            } else if (settings.eliteChance() > 0 && random.nextDouble() < settings.eliteChance()) {
                room.setType(RoomType.ELITE);
            } else {
                room.setType(RoomType.COMBAT);
            }
        }
    }

    private List<RoomEdge> buildEdges(List<Room> rooms) {
        List<RoomEdge> edges = new ArrayList<>();
        List<RoomEdge> allEdges = new ArrayList<>();
        for (int i = 0; i < rooms.size(); i++) {
            for (int j = i + 1; j < rooms.size(); j++) {
                allEdges.add(new RoomEdge(rooms.get(i), rooms.get(j)));
            }
        }
        allEdges.sort(Comparator.comparingDouble(edge -> centerDistanceSquared(edge.a(), edge.b())));
        Set<Room> connected = new HashSet<>();
        if (!rooms.isEmpty()) {
            connected.add(rooms.get(0));
        }
        for (RoomEdge edge : allEdges) {
            if (connected.contains(edge.a()) && connected.contains(edge.b())) {
                continue;
            }
            edges.add(edge);
            connected.add(edge.a());
            connected.add(edge.b());
            if (connected.size() == rooms.size()) {
                break;
            }
        }
        int extraLoops = Math.min(2, allEdges.size());
        for (int i = 0; i < extraLoops; i++) {
            RoomEdge edge = allEdges.get(random.nextInt(allEdges.size()));
            if (!edges.contains(edge)) {
                edges.add(edge);
            }
        }
        return edges;
    }

    private double centerDistanceSquared(Room a, Room b) {
        double dx = a.bounds().getCenterX() - b.bounds().getCenterX();
        double dz = a.bounds().getCenterZ() - b.bounds().getCenterZ();
        return dx * dx + dz * dz;
    }
}

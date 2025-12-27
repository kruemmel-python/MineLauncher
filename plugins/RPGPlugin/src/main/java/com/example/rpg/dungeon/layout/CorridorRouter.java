package com.example.rpg.dungeon.layout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class CorridorRouter {
    private final Random random;

    public CorridorRouter(Random random) {
        this.random = random;
    }

    public List<Corridor> routeCorridors(BoundingBox bounds, List<Room> rooms, List<RoomEdge> edges) {
        List<Corridor> corridors = new ArrayList<>();
        for (RoomEdge edge : edges) {
            Location doorA = chooseDoor(edge.a(), edge.b());
            Location doorB = chooseDoor(edge.b(), edge.a(), doorA);
            edge.a().doorPoints().add(doorA);
            edge.b().doorPoints().add(doorB);
            Corridor corridor = new Corridor();
            List<Vector> path = findPath(bounds, rooms, doorA, doorB);
            corridor.path().addAll(path);
            corridors.add(corridor);
        }
        return corridors;
    }

    private Location chooseDoor(Room from, Room to) {
        return chooseDoor(from, to, null);
    }

    private Location chooseDoor(Room from, Room to, Location targetDoor) {
        if (!from.sockets().isEmpty()) {
            return selectSocket(from, to, targetDoor);
        }
        BoundingBox box = from.bounds();
        double centerX = (box.getMinX() + box.getMaxX()) / 2.0;
        double centerZ = (box.getMinZ() + box.getMaxZ()) / 2.0;
        double dx = to.bounds().getCenterX() - centerX;
        double dz = to.bounds().getCenterZ() - centerZ;
        double x;
        double z;
        if (Math.abs(dx) > Math.abs(dz)) {
            x = dx > 0 ? box.getMaxX() : box.getMinX();
            z = centerZ;
        } else {
            x = centerX;
            z = dz > 0 ? box.getMaxZ() : box.getMinZ();
        }
        return new Location(null, Math.round(x), box.getMinY() + 1, Math.round(z));
    }

    private Location selectSocket(Room from, Room to, Location targetDoor) {
        if (!to.sockets().isEmpty()) {
            for (RoomSocket socket : from.sockets()) {
                for (RoomSocket target : to.sockets()) {
                    if (socket.name().equalsIgnoreCase(target.name())) {
                        return socket.location();
                    }
                }
            }
        }
        Location target = targetDoor != null
            ? targetDoor
            : new Location(null, to.bounds().getCenterX(), to.bounds().getMinY() + 1, to.bounds().getCenterZ());
        RoomSocket best = null;
        double bestDistance = Double.MAX_VALUE;
        for (RoomSocket socket : from.sockets()) {
            double distance = distanceSquared(socket.location(), target);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = socket;
            }
        }
        return best != null
            ? best.location()
            : new Location(null, from.bounds().getCenterX(), from.bounds().getMinY() + 1, from.bounds().getCenterZ());
    }

    private double distanceSquared(Location a, Location b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double dz = a.getZ() - b.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private List<Vector> findPath(BoundingBox bounds, List<Room> rooms, Location start, Location goal) {
        Point startPoint = new Point(start.getBlockX(), start.getBlockZ());
        Point goalPoint = new Point(goal.getBlockX(), goal.getBlockZ());
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Map<Point, Node> all = new HashMap<>();
        Set<Point> closed = new HashSet<>();
        Node startNode = new Node(startPoint, null, 0, heuristic(startPoint, goalPoint));
        open.add(startNode);
        all.put(startPoint, startNode);
        int minX = (int) bounds.getMinX();
        int maxX = (int) bounds.getMaxX();
        int minZ = (int) bounds.getMinZ();
        int maxZ = (int) bounds.getMaxZ();
        while (!open.isEmpty()) {
            Node current = open.poll();
            if (current.point.equals(goalPoint)) {
                return reconstruct(current);
            }
            closed.add(current.point);
            for (int[] dir : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                int nx = current.point.x + dir[0];
                int nz = current.point.z + dir[1];
                if (nx < minX || nx > maxX || nz < minZ || nz > maxZ) {
                    continue;
                }
                Point next = new Point(nx, nz);
                if (closed.contains(next)) {
                    continue;
                }
                if (isBlocked(rooms, next, startPoint, goalPoint)) {
                    continue;
                }
                double cost = current.g + 1;
                Node existing = all.get(next);
                if (existing == null || cost < existing.g) {
                    Node node = new Node(next, current, cost, heuristic(next, goalPoint));
                    all.put(next, node);
                    open.add(node);
                }
            }
        }
        return fallbackManhattan(startPoint, goalPoint);
    }

    private boolean isBlocked(List<Room> rooms, Point point, Point start, Point goal) {
        if (point.equals(start) || point.equals(goal)) {
            return false;
        }
        for (Room room : rooms) {
            BoundingBox box = room.bounds();
            if (point.x > box.getMinX() && point.x < box.getMaxX()
                && point.z > box.getMinZ() && point.z < box.getMaxZ()) {
                return true;
            }
        }
        return false;
    }

    private List<Vector> fallbackManhattan(Point start, Point goal) {
        List<Vector> path = new ArrayList<>();
        int x = start.x;
        int z = start.z;
        while (x != goal.x) {
            x += Integer.signum(goal.x - x);
            path.add(new Vector(x, 0, z));
        }
        while (z != goal.z) {
            z += Integer.signum(goal.z - z);
            path.add(new Vector(x, 0, z));
        }
        return path;
    }

    private List<Vector> reconstruct(Node node) {
        List<Vector> path = new ArrayList<>();
        Node current = node;
        while (current.parent != null) {
            path.add(new Vector(current.point.x, 0, current.point.z));
            current = current.parent;
        }
        java.util.Collections.reverse(path);
        return path;
    }

    private double heuristic(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.z - b.z);
    }

    public record RoomEdge(Room a, Room b) {
    }

    private static class Point {
        private final int x;
        private final int z;

        private Point(int x, int z) {
            this.x = x;
            this.z = z;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Point point)) {
                return false;
            }
            return point.x == x && point.z == z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, z);
        }
    }

    private static class Node {
        private final Point point;
        private final Node parent;
        private final double g;
        private final double h;
        private final double f;

        private Node(Point point, Node parent, double g, double h) {
            this.point = point;
            this.parent = parent;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }
    }
}

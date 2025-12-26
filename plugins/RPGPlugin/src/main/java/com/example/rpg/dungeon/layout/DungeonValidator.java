package com.example.rpg.dungeon.layout;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class DungeonValidator {
    public boolean validate(DungeonPlan plan) {
        if (plan.rooms().isEmpty()) {
            return false;
        }
        if (!isReachable(plan.graph().adjacency(), plan.startRoom(), plan.bossRoom())) {
            return false;
        }
        if (!isReachable(plan.graph().adjacency(), plan.bossRoom(), plan.exitRoom())) {
            return false;
        }
        return true;
    }

    private boolean isReachable(Map<Room, java.util.List<Room>> graph, Room start, Room target) {
        if (start == null || target == null) {
            return false;
        }
        Queue<Room> queue = new ArrayDeque<>();
        Set<Room> visited = new HashSet<>();
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty()) {
            Room room = queue.poll();
            if (room.equals(target)) {
                return true;
            }
            for (Room neighbor : graph.getOrDefault(room, java.util.Collections.emptyList())) {
                if (visited.add(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }
        return false;
    }
}

package com.example.rpg.model;

import java.util.HashMap;
import java.util.Map;

public class Spawner {
    private final String id;
    private String zoneId;
    private int maxMobs;
    private int spawnInterval;
    private Map<String, Double> mobs = new HashMap<>();

    public Spawner(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String zoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public int maxMobs() {
        return maxMobs;
    }

    public void setMaxMobs(int maxMobs) {
        this.maxMobs = maxMobs;
    }

    public int spawnInterval() {
        return spawnInterval;
    }

    public void setSpawnInterval(int spawnInterval) {
        this.spawnInterval = spawnInterval;
    }

    public Map<String, Double> mobs() {
        return mobs;
    }

    public void setMobs(Map<String, Double> mobs) {
        this.mobs = mobs;
    }
}

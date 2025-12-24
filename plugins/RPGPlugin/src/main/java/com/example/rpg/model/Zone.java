package com.example.rpg.model;

import org.bukkit.Location;

public class Zone {
    private final String id;
    private String name;
    private String world;
    private int minLevel;
    private int maxLevel;
    private double slowMultiplier = 1.0;
    private double damageMultiplier = 1.0;
    private int x1;
    private int y1;
    private int z1;
    private int x2;
    private int y2;
    private int z2;

    public Zone(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String world() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public int minLevel() {
        return minLevel;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public int maxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public double slowMultiplier() {
        return slowMultiplier;
    }

    public void setSlowMultiplier(double slowMultiplier) {
        this.slowMultiplier = slowMultiplier;
    }

    public double damageMultiplier() {
        return damageMultiplier;
    }

    public void setDamageMultiplier(double damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }

    public void setBounds(Location pos1, Location pos2) {
        this.x1 = Math.min(pos1.getBlockX(), pos2.getBlockX());
        this.y1 = Math.min(pos1.getBlockY(), pos2.getBlockY());
        this.z1 = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        this.x2 = Math.max(pos1.getBlockX(), pos2.getBlockX());
        this.y2 = Math.max(pos1.getBlockY(), pos2.getBlockY());
        this.z2 = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
    }

    public boolean contains(Location location) {
        if (location == null || !location.getWorld().getName().equals(world)) {
            return false;
        }
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        return x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2;
    }

    public int x1() {
        return x1;
    }

    public int y1() {
        return y1;
    }

    public int z1() {
        return z1;
    }

    public int x2() {
        return x2;
    }

    public int y2() {
        return y2;
    }

    public int z2() {
        return z2;
    }

    public void setCoordinates(int x1, int y1, int z1, int x2, int y2, int z2) {
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }
}

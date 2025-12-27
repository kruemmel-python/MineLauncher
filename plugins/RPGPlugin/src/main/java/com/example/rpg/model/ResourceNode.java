package com.example.rpg.model;

public class ResourceNode {
    private final String id;
    private String world;
    private int x;
    private int y;
    private int z;
    private String material;
    private String profession;
    private int respawnSeconds;
    private int xp;
    private long nextAvailableAt;

    public ResourceNode(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String world() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public int x() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int y() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int z() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public String material() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String profession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public int respawnSeconds() {
        return respawnSeconds;
    }

    public void setRespawnSeconds(int respawnSeconds) {
        this.respawnSeconds = respawnSeconds;
    }

    public int xp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public long nextAvailableAt() {
        return nextAvailableAt;
    }

    public void setNextAvailableAt(long nextAvailableAt) {
        this.nextAvailableAt = nextAvailableAt;
    }
}

package com.example.rpg.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Location;

public class Npc {
    private final String id;
    private UUID uuid;
    private String name;
    private NpcRole role;
    private String world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private List<String> dialog = new ArrayList<>();
    private String questLink;
    private String shopId;

    public Npc(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public UUID uuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NpcRole role() {
        return role;
    }

    public void setRole(NpcRole role) {
        this.role = role;
    }

    public String world() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public void setLocation(Location location) {
        this.world = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    public void setRawLocation(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Location toLocation(org.bukkit.World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    public float yaw() {
        return yaw;
    }

    public float pitch() {
        return pitch;
    }

    public List<String> dialog() {
        return dialog;
    }

    public void setDialog(List<String> dialog) {
        this.dialog = dialog;
    }

    public String questLink() {
        return questLink;
    }

    public void setQuestLink(String questLink) {
        this.questLink = questLink;
    }

    public String shopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }
}

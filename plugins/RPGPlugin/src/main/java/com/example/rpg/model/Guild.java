package com.example.rpg.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Guild {
    private final String id;
    private String name;
    private UUID leader;
    private int bankGold;
    private final Map<UUID, GuildMemberRole> members = new HashMap<>();
    private final Map<String, GuildQuest> quests = new HashMap<>();
    private String hallWorld;
    private double hallX;
    private double hallY;
    private double hallZ;
    private final Map<String, Integer> hallUpgrades = new HashMap<>();

    public Guild(String id) {
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

    public UUID leader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
    }

    public int bankGold() {
        return bankGold;
    }

    public void setBankGold(int bankGold) {
        this.bankGold = Math.max(0, bankGold);
    }

    public Map<UUID, GuildMemberRole> members() {
        return members;
    }

    public Map<String, GuildQuest> quests() {
        return quests;
    }

    public String hallWorld() {
        return hallWorld;
    }

    public void setHall(String world, double x, double y, double z) {
        this.hallWorld = world;
        this.hallX = x;
        this.hallY = y;
        this.hallZ = z;
    }

    public double hallX() {
        return hallX;
    }

    public double hallY() {
        return hallY;
    }

    public double hallZ() {
        return hallZ;
    }

    public Map<String, Integer> hallUpgrades() {
        return hallUpgrades;
    }
}

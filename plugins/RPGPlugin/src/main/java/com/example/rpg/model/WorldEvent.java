package com.example.rpg.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WorldEvent {
    private final String id;
    private String name;
    private String zoneId;
    private boolean active;
    private final List<QuestStep> steps = new ArrayList<>();
    private final Map<Integer, Integer> progress = new HashMap<>();
    private int rewardXp;
    private int rewardGold;
    private final Map<String, Integer> rewardFactionRep = new HashMap<>();
    private final List<String> unlockQuests = new ArrayList<>();
    private final Set<UUID> participants = new HashSet<>();

    public WorldEvent(String id) {
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

    public String zoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public boolean active() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<QuestStep> steps() {
        return steps;
    }

    public Map<Integer, Integer> progress() {
        return progress;
    }

    public int rewardXp() {
        return rewardXp;
    }

    public void setRewardXp(int rewardXp) {
        this.rewardXp = rewardXp;
    }

    public int rewardGold() {
        return rewardGold;
    }

    public void setRewardGold(int rewardGold) {
        this.rewardGold = rewardGold;
    }

    public Map<String, Integer> rewardFactionRep() {
        return rewardFactionRep;
    }

    public List<String> unlockQuests() {
        return unlockQuests;
    }

    public Set<UUID> participants() {
        return participants;
    }
}

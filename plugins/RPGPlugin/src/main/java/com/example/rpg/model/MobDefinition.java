package com.example.rpg.model;

import java.util.ArrayList;
import java.util.List;

public class MobDefinition {
    private final String id;
    private String name;
    private String type;
    private double health;
    private double damage;
    private String mainHand;
    private String helmet;
    private List<String> skills = new ArrayList<>();
    private int skillIntervalSeconds;
    private int xp;
    private String lootTable;
    private String behaviorTree;

    public MobDefinition(String id) {
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

    public String type() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double health() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public double damage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public String mainHand() {
        return mainHand;
    }

    public void setMainHand(String mainHand) {
        this.mainHand = mainHand;
    }

    public String helmet() {
        return helmet;
    }

    public void setHelmet(String helmet) {
        this.helmet = helmet;
    }

    public List<String> skills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public int skillIntervalSeconds() {
        return skillIntervalSeconds;
    }

    public void setSkillIntervalSeconds(int skillIntervalSeconds) {
        this.skillIntervalSeconds = skillIntervalSeconds;
    }

    public int xp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public String lootTable() {
        return lootTable;
    }

    public void setLootTable(String lootTable) {
        this.lootTable = lootTable;
    }

    public String behaviorTree() {
        return behaviorTree;
    }

    public void setBehaviorTree(String behaviorTree) {
        this.behaviorTree = behaviorTree;
    }
}

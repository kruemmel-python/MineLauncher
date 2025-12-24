package com.example.rpg.model;

public class Skill {
    private final String id;
    private String name;
    private SkillType type;
    private int cooldown;
    private int manaCost;
    private String effect;

    public Skill(String id) {
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

    public SkillType type() {
        return type;
    }

    public void setType(SkillType type) {
        this.type = type;
    }

    public int cooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public int manaCost() {
        return manaCost;
    }

    public void setManaCost(int manaCost) {
        this.manaCost = manaCost;
    }

    public String effect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }
}

package com.example.rpg.model;

public class Skill {
    private final String id;
    private String name;
    private SkillType type;
    private SkillCategory category;
    private int cooldown;
    private int manaCost;
    private String requiredSkill;
    private java.util.List<com.example.rpg.skill.SkillEffectConfig> effects = new java.util.ArrayList<>();

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

    public SkillCategory category() {
        return category;
    }

    public void setCategory(SkillCategory category) {
        this.category = category;
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

    public java.util.List<com.example.rpg.skill.SkillEffectConfig> effects() {
        return effects;
    }

    public void setEffects(java.util.List<com.example.rpg.skill.SkillEffectConfig> effects) {
        this.effects = effects;
    }

    public String requiredSkill() {
        return requiredSkill;
    }

    public void setRequiredSkill(String requiredSkill) {
        this.requiredSkill = requiredSkill;
    }
}

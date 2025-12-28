package com.example.rpg.model;

public class Skill {
    private final String id;
    private String name;
    private SkillType type;
    private SkillCategory category;
    private int cooldown;
    private int manaCost;
    private String classId;
    private int minLevel;
    private int maxRank;
    private java.util.Map<String, Object> scaling = new java.util.HashMap<>();
    private java.util.List<String> tags = new java.util.ArrayList<>();
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

    public String classId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public int minLevel() {
        return minLevel;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public int maxRank() {
        return maxRank;
    }

    public void setMaxRank(int maxRank) {
        this.maxRank = maxRank;
    }

    public java.util.Map<String, Object> scaling() {
        return scaling;
    }

    public void setScaling(java.util.Map<String, Object> scaling) {
        this.scaling = scaling;
    }

    public java.util.List<String> tags() {
        return tags;
    }

    public void setTags(java.util.List<String> tags) {
        this.tags = tags;
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

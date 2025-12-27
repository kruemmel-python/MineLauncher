package com.example.rpg.model;

import com.example.rpg.skill.SkillEffectConfig;
import java.util.ArrayList;
import java.util.List;

public class SkillSynergy {
    private final String id;
    private List<String> skills = new ArrayList<>();
    private String scope;
    private double radius;
    private int windowSeconds;
    private final List<SkillEffectConfig> effects = new ArrayList<>();

    public SkillSynergy(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public List<String> skills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public String scope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public double radius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public int windowSeconds() {
        return windowSeconds;
    }

    public void setWindowSeconds(int windowSeconds) {
        this.windowSeconds = windowSeconds;
    }

    public List<SkillEffectConfig> effects() {
        return effects;
    }
}

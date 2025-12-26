package com.example.rpg.model;

import java.util.ArrayList;
import java.util.List;

public class Quest {
    private final String id;
    private String name;
    private String description;
    private boolean repeatable;
    private int minLevel;
    private List<QuestStep> steps = new ArrayList<>();
    private QuestReward reward = new QuestReward();

    public Quest(String id) {
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

    public String description() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean repeatable() {
        return repeatable;
    }

    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }

    public int minLevel() {
        return minLevel;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public List<QuestStep> steps() {
        return steps;
    }

    public void setSteps(List<QuestStep> steps) {
        this.steps = steps;
    }

    public QuestReward reward() {
        return reward;
    }

    public void setReward(QuestReward reward) {
        this.reward = reward;
    }
}

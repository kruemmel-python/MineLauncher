package com.example.rpg.model;

public class GuildQuest {
    private final String id;
    private String name;
    private String description;
    private int goal;
    private int progress;
    private boolean completed;

    public GuildQuest(String id) {
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

    public int goal() {
        return goal;
    }

    public void setGoal(int goal) {
        this.goal = Math.max(1, goal);
    }

    public int progress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, progress);
    }

    public boolean completed() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}

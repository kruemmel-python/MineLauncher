package com.example.rpg.model;

import java.util.HashMap;
import java.util.Map;

public class QuestProgress {
    private final String questId;
    private final Map<Integer, Integer> stepProgress = new HashMap<>();
    private boolean completed;

    public QuestProgress(String questId) {
        this.questId = questId;
    }

    public String questId() {
        return questId;
    }

    public Map<Integer, Integer> stepProgress() {
        return stepProgress;
    }

    public void incrementStep(int index, int amount) {
        stepProgress.put(index, stepProgress.getOrDefault(index, 0) + amount);
    }

    public int getStepProgress(int index) {
        return stepProgress.getOrDefault(index, 0);
    }

    public boolean completed() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}

package com.example.rpg.model;

public class QuestStep {
    private QuestStepType type;
    private String target;
    private int amount;

    public QuestStep(QuestStepType type, String target, int amount) {
        this.type = type;
        this.target = target;
        this.amount = amount;
    }

    public QuestStepType type() {
        return type;
    }

    public void setType(QuestStepType type) {
        this.type = type;
    }

    public String target() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public int amount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}

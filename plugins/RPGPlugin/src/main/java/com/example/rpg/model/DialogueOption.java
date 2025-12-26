package com.example.rpg.model;

public class DialogueOption {
    private String text;
    private String nextId;
    private String requiredFactionId;
    private int minRep;
    private String requiredQuestId;
    private boolean requireQuestCompleted;
    private String grantQuestId;

    public String text() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String nextId() {
        return nextId;
    }

    public void setNextId(String nextId) {
        this.nextId = nextId;
    }

    public String requiredFactionId() {
        return requiredFactionId;
    }

    public void setRequiredFactionId(String requiredFactionId) {
        this.requiredFactionId = requiredFactionId;
    }

    public int minRep() {
        return minRep;
    }

    public void setMinRep(int minRep) {
        this.minRep = minRep;
    }

    public String requiredQuestId() {
        return requiredQuestId;
    }

    public void setRequiredQuestId(String requiredQuestId) {
        this.requiredQuestId = requiredQuestId;
    }

    public boolean requireQuestCompleted() {
        return requireQuestCompleted;
    }

    public void setRequireQuestCompleted(boolean requireQuestCompleted) {
        this.requireQuestCompleted = requireQuestCompleted;
    }

    public String grantQuestId() {
        return grantQuestId;
    }

    public void setGrantQuestId(String grantQuestId) {
        this.grantQuestId = grantQuestId;
    }
}

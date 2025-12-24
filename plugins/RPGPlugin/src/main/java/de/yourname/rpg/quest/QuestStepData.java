package de.yourname.rpg.quest;

public class QuestStepData {
    private QuestStepType type;
    private String target;
    private int amount;

    public QuestStepData() {
    }

    public QuestStepData(QuestStepType type, String target, int amount) {
        this.type = type;
        this.target = target;
        this.amount = amount;
    }

    public QuestStepType getType() {
        return type;
    }

    public String getTarget() {
        return target;
    }

    public int getAmount() {
        return amount;
    }
}

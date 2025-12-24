package de.yourname.rpg.quest;

public class QuestState {
    private final String questId;
    private final QuestProgress progress;
    private boolean completed;

    public QuestState(String questId) {
        this.questId = questId;
        this.progress = new QuestProgress();
        this.completed = false;
    }

    public String getQuestId() {
        return questId;
    }

    public QuestProgress getProgress() {
        return progress;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}

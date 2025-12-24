package de.yourname.rpg.quest;

import java.util.ArrayList;
import java.util.List;

public class Quest {
    private String id;
    private String title;
    private String description;
    private List<QuestStep> steps;
    private List<String> requirements;
    private List<String> rewards;
    private QuestStatus status;
    private int version;

    public Quest() {
        this.steps = new ArrayList<>();
        this.requirements = new ArrayList<>();
        this.rewards = new ArrayList<>();
        this.status = QuestStatus.DRAFT;
        this.version = 1;
    }

    public Quest(String id, String title, String description) {
        this();
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<QuestStep> getSteps() {
        return steps;
    }

    public List<String> getRequirements() {
        return requirements;
    }

    public List<String> getRewards() {
        return rewards;
    }

    public QuestStatus getStatus() {
        return status;
    }

    public void setStatus(QuestStatus status) {
        this.status = status;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}

package de.yourname.rpg.skill;

public class Skill {
    private String id;
    private String name;
    private String description;
    private boolean active;

    public Skill() {
    }

    public Skill(String id, String name, String description, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }
}

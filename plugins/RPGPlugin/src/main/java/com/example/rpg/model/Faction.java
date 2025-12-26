package com.example.rpg.model;

public class Faction {
    private final String id;
    private String name;

    public Faction(String id) {
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
}

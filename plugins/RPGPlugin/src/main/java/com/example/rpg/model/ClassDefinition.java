package com.example.rpg.model;

import java.util.ArrayList;
import java.util.List;

public class ClassDefinition {
    private final String id;
    private String name;
    private List<String> startSkills = new ArrayList<>();

    public ClassDefinition(String id) {
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

    public List<String> startSkills() {
        return startSkills;
    }

    public void setStartSkills(List<String> startSkills) {
        this.startSkills = startSkills;
    }
}

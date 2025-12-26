package com.example.rpg.model;

import java.util.ArrayList;
import java.util.List;

public class LootTable {
    private final String id;
    private String appliesTo;
    private List<LootEntry> entries = new ArrayList<>();

    public LootTable(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String appliesTo() {
        return appliesTo;
    }

    public void setAppliesTo(String appliesTo) {
        this.appliesTo = appliesTo;
    }

    public List<LootEntry> entries() {
        return entries;
    }

    public void setEntries(List<LootEntry> entries) {
        this.entries = entries;
    }
}

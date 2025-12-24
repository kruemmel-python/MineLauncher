package de.yourname.rpg.loot;

import java.util.ArrayList;
import java.util.List;

public class LootTable {
    private String id;
    private List<LootTableEntry> entries = new ArrayList<>();

    public LootTable() {
    }

    public LootTable(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public List<LootTableEntry> getEntries() {
        return entries;
    }
}

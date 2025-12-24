package de.yourname.rpg.loot;

public class LootTableEntry {
    private String itemId;
    private double chance;

    public LootTableEntry() {
    }

    public LootTableEntry(String itemId, double chance) {
        this.itemId = itemId;
        this.chance = chance;
    }

    public String getItemId() {
        return itemId;
    }

    public double getChance() {
        return chance;
    }
}

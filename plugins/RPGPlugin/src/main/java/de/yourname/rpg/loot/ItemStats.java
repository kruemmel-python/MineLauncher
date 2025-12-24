package de.yourname.rpg.loot;

public class ItemStats {
    private int damage;
    private int defense;
    private String rarity;

    public ItemStats() {
    }

    public ItemStats(int damage, int defense, String rarity) {
        this.damage = damage;
        this.defense = defense;
        this.rarity = rarity;
    }

    public int getDamage() {
        return damage;
    }

    public int getDefense() {
        return defense;
    }

    public String getRarity() {
        return rarity;
    }
}

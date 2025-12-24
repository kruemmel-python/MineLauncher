package com.example.rpg.model;

public class LootEntry {
    private String material;
    private double chance;
    private int minAmount;
    private int maxAmount;
    private Rarity rarity;

    public LootEntry(String material, double chance, int minAmount, int maxAmount, Rarity rarity) {
        this.material = material;
        this.chance = chance;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.rarity = rarity;
    }

    public String material() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public double chance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

    public int minAmount() {
        return minAmount;
    }

    public void setMinAmount(int minAmount) {
        this.minAmount = minAmount;
    }

    public int maxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(int maxAmount) {
        this.maxAmount = maxAmount;
    }

    public Rarity rarity() {
        return rarity;
    }

    public void setRarity(Rarity rarity) {
        this.rarity = rarity;
    }
}

package com.example.rpg.model;

import net.kyori.adventure.text.format.NamedTextColor;

public enum Rarity {
    COMMON(NamedTextColor.WHITE, 1.0),
    UNCOMMON(NamedTextColor.GREEN, 0.6),
    RARE(NamedTextColor.BLUE, 0.35),
    EPIC(NamedTextColor.DARK_PURPLE, 0.15),
    LEGENDARY(NamedTextColor.GOLD, 0.05);

    private final NamedTextColor color;
    private final double weight;

    Rarity(NamedTextColor color, double weight) {
        this.color = color;
        this.weight = weight;
    }

    public NamedTextColor color() {
        return color;
    }

    public double weight() {
        return weight;
    }
}

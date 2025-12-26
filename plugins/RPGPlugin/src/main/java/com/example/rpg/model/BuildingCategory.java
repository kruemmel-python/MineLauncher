package com.example.rpg.model;

import java.util.Locale;

public enum BuildingCategory {
    RESIDENTIAL("Wohngebäude"),
    SHOP("Geschäfte"),
    PUBLIC("Öffentliche Einrichtungen"),
    CRAFTING("Hersteller");

    private final String displayName;

    BuildingCategory(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static BuildingCategory fromString(String raw) {
        if (raw == null) {
            return RESIDENTIAL;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        for (BuildingCategory category : values()) {
            if (category.name().equals(normalized)) {
                return category;
            }
        }
        return RESIDENTIAL;
    }
}

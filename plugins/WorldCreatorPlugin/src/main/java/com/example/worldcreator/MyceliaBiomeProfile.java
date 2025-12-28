package com.example.worldcreator;

import org.bukkit.Material;
import org.bukkit.block.Biome;

public record MyceliaBiomeProfile(
        String name,
        double minHumidity,
        double maxHumidity,
        double minTemperature,
        double maxTemperature,
        Material base,
        Material surface,
        Material ore,
        Biome biome
) {
    public boolean matches(double humidity, double temperature) {
        return humidity >= minHumidity
                && humidity <= maxHumidity
                && temperature >= minTemperature
                && temperature <= maxTemperature;
    }
}

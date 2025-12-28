package com.example.worldcreator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;

public final class MyceliaBiomeRegistry {
    private final List<MyceliaBiomeProfile> profiles = new ArrayList<>();

    public void load(FileConfiguration config) {
        profiles.clear();
        var rawProfiles = config.getMapList("mycelia.biomes");
        if (rawProfiles.isEmpty()) {
            profiles.addAll(defaultProfiles());
            return;
        }

        for (Map<String, Object> entry : rawProfiles) {
            var name = stringValue(entry, "name", "custom");
            var humidity = mapValue(entry, "humidity");
            var temperature = mapValue(entry, "temperature");
            var base = materialValue(entry, "base", Material.DEEPSLATE);
            var surface = materialValue(entry, "surface", Material.MYCELIUM);
            var ore = materialValue(entry, "ore", Material.AMETHYST_BLOCK);
            var biome = biomeValue(entry, "biome", Biome.MUSHROOM_FIELDS);

            profiles.add(new MyceliaBiomeProfile(
                    name,
                    rangeValue(humidity, "min", 0.0),
                    rangeValue(humidity, "max", 1.0),
                    rangeValue(temperature, "min", 0.0),
                    rangeValue(temperature, "max", 1.0),
                    base,
                    surface,
                    ore,
                    biome
            ));
        }
    }

    public List<MyceliaBiomeProfile> profiles() {
        return List.copyOf(profiles);
    }

    private static List<MyceliaBiomeProfile> defaultProfiles() {
        return List.of(
                new MyceliaBiomeProfile(
                        "mycelia",
                        0.0,
                        1.0,
                        0.0,
                        1.0,
                        Material.DEEPSLATE,
                        Material.MYCELIUM,
                        Material.AMETHYST_BLOCK,
                        Biome.MUSHROOM_FIELDS
                )
        );
    }

    private static String stringValue(Map<String, Object> entry, String key, String fallback) {
        var value = entry.get(key);
        return value instanceof String str ? str : fallback;
    }

    private static Map<String, Object> mapValue(Map<String, Object> entry, String key) {
        var value = entry.get(key);
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new java.util.HashMap<>();
            map.forEach((mapKey, mapValue) -> {
                if (mapKey instanceof String stringKey) {
                    result.put(stringKey, mapValue);
                }
            });
            return result;
        }
        return Map.of();
    }

    private static double rangeValue(Map<String, Object> entry, String key, double fallback) {
        var value = entry.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String text) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private static Material materialValue(Map<String, Object> entry, String key, Material fallback) {
        var value = entry.get(key);
        if (value instanceof String text) {
            var material = Material.matchMaterial(text);
            if (material != null) {
                return material;
            }
        }
        return fallback;
    }

    private static Biome biomeValue(Map<String, Object> entry, String key, Biome fallback) {
        var value = entry.get(key);
        if (value instanceof String text) {
            try {
                return Biome.valueOf(text);
            } catch (IllegalArgumentException ignored) {
                return fallback;
            }
        }
        return fallback;
    }
}

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
        List<Map<?, ?>> rawProfiles = config.getMapList("mycelia.biomes");
        if (rawProfiles == null || rawProfiles.isEmpty()) {
            profiles.addAll(defaultProfiles());
            return;
        }

        for (Map<?, ?> entry : rawProfiles) {
            String name = stringValue(entry, "name", "custom");

            Map<?, ?> humidity = mapValue(entry, "humidity");
            Map<?, ?> temperature = mapValue(entry, "temperature");

            Material base = materialValue(entry, "base", Material.DEEPSLATE);
            Material surface = materialValue(entry, "surface", Material.MYCELIUM);
            Material ore = materialValue(entry, "ore", Material.AMETHYST_BLOCK);
            Biome biome = biomeValue(entry, "biome", Biome.MUSHROOM_FIELDS);

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
                        "mycelia-core",
                        0.0,
                        0.4,
                        0.0,
                        0.4,
                        Material.DEEPSLATE,
                        Material.MYCELIUM,
                        Material.AMETHYST_BLOCK,
                        Biome.MUSHROOM_FIELDS
                ),
                new MyceliaBiomeProfile(
                        "snowy-wastes",
                        0.4,
                        1.0,
                        0.0,
                        0.35,
                        Material.PACKED_ICE,
                        Material.SNOW_BLOCK,
                        Material.BLUE_ICE,
                        Biome.SNOWY_PLAINS
                ),
                new MyceliaBiomeProfile(
                        "forest",
                        0.35,
                        0.75,
                        0.4,
                        0.75,
                        Material.STONE,
                        Material.GRASS_BLOCK,
                        Material.EMERALD_BLOCK,
                        Biome.FOREST
                ),
                new MyceliaBiomeProfile(
                        "jungle",
                        0.75,
                        1.0,
                        0.6,
                        1.0,
                        Material.STONE,
                        Material.GRASS_BLOCK,
                        Material.MOSS_BLOCK,
                        Biome.JUNGLE
                ),
                new MyceliaBiomeProfile(
                        "desert",
                        0.0,
                        0.35,
                        0.75,
                        1.0,
                        Material.SANDSTONE,
                        Material.SAND,
                        Material.RAW_GOLD_BLOCK,
                        Biome.DESERT
                ),
                new MyceliaBiomeProfile(
                        "lava-fields",
                        0.0,
                        0.4,
                        0.4,
                        0.75,
                        Material.BLACKSTONE,
                        Material.BASALT,
                        Material.MAGMA_BLOCK,
                        Biome.BASALT_DELTAS
                )
        );
    }

    /**
     * Liest einen String aus einer Map<?,?>.
     * Warum so: YAML liefert Map-Schlüssel meistens als String, aber der Typ ist nicht garantiert.
     * Deshalb prüfen wir strikt und fallen sauber auf fallback zurück.
     */
    private static String stringValue(Map<?, ?> entry, String key, String fallback) {
        Object value = entry.get(key);
        return (value instanceof String str) ? str : fallback;
    }

    /**
     * Holt eine verschachtelte Map (z.B. humidity: {min: 0.2, max: 0.8}).
     * Warum so: Bukkit/YAML gibt Map<?,?> zurück; wir behalten die Wildcards und werten später sicher aus.
     */
    private static Map<?, ?> mapValue(Map<?, ?> entry, String key) {
        Object value = entry.get(key);
        return (value instanceof Map<?, ?> map) ? map : Map.of();
    }

    /**
     * Liest double-Werte aus einer Map<?,?> für Keys wie "min"/"max".
     * Unterstützt Number und String ("0.25").
     */
    private static double rangeValue(Map<?, ?> entry, String key, double fallback) {
        Object value = entry.get(key);

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

    /**
     * Liest Materials aus String-Namen (z.B. "DEEPSLATE").
     */
    private static Material materialValue(Map<?, ?> entry, String key, Material fallback) {
        Object value = entry.get(key);

        if (value instanceof String text) {
            Material material = Material.matchMaterial(text);
            if (material != null) {
                return material;
            }
        }

        return fallback;
    }

    /**
     * Liest Biome Enum aus String (z.B. "MUSHROOM_FIELDS").
     */
    private static Biome biomeValue(Map<?, ?> entry, String key, Biome fallback) {
        Object value = entry.get(key);

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

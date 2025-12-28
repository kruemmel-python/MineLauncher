package com.example.worldcreator;

import java.util.List;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.noise.SimplexNoiseGenerator;

public final class MyceliaChunkGenerator extends ChunkGenerator {
    private static final List<MyceliaTheme> THEMES = List.of(
            new MyceliaTheme(Material.DEEPSLATE, Material.MYCELIUM, Material.AMETHYST_BLOCK, 0.02),
            new MyceliaTheme(Material.PACKED_ICE, Material.SNOW_BLOCK, Material.BLUE_ICE, 0.035),
            new MyceliaTheme(Material.BLACKSTONE, Material.BASALT, Material.MAGMA_BLOCK, 0.015),
            new MyceliaTheme(Material.NETHERRACK, Material.WARPED_NYLIUM, Material.NETHER_WART_BLOCK, 0.025),
            new MyceliaTheme(Material.MOSSY_COBBLESTONE, Material.MOSS_BLOCK, Material.RAW_GOLD_BLOCK, 0.03)
    );

    private final MyceliaBiomeRegistry biomeRegistry;
    private SimplexNoiseGenerator terrainNoise;
    private SimplexNoiseGenerator oreNoise;
    private SimplexNoiseGenerator humidityNoise;
    private SimplexNoiseGenerator temperatureNoise;
    private Material baseMat;
    private Material surfaceMat;
    private Material oreMat;
    private double scale;
    private boolean initialized;

    public MyceliaChunkGenerator(MyceliaBiomeRegistry biomeRegistry) {
        this.biomeRegistry = biomeRegistry;
    }

    @Override
    public ChunkData generateChunkData(
            World world,
            Random random,
            int chunkX,
            int chunkZ,
            BiomeGrid biome
    ) {
        ensureInitialized(world);
        var chunkData = createChunkData(world);
        int seaLevel = world.getSeaLevel();
        int minHeight = world.getMinHeight();
        int maxHeight = world.getMaxHeight();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;

                double humidity = normalize(humidityNoise.noise(worldX * 0.01, worldZ * 0.01));
                double temperature = normalize(temperatureNoise.noise(worldX * 0.01, worldZ * 0.01));

                MyceliaBiomeProfile profile = resolveBiome(humidity, temperature);
                Material base = profile != null ? profile.base() : baseMat;
                Material surface = profile != null ? profile.surface() : surfaceMat;
                Material ore = profile != null ? profile.ore() : oreMat;
                Biome targetBiome = profile != null ? profile.biome() : Biome.MUSHROOM_FIELDS;

                biome.setBiome(x, z, targetBiome);

                double heightNoise = terrainNoise.noise(worldX * scale, worldZ * scale);
                int surfaceHeight = clampHeight(seaLevel + (int) Math.round(heightNoise * 20.0), minHeight + 1, maxHeight - 1);

                boolean surfacePlaced = false;
                for (int y = minHeight; y <= surfaceHeight; y++) {
                    if (y == surfaceHeight) {
                        chunkData.setBlock(x, y, z, surface);
                        surfacePlaced = true;
                        continue;
                    }

                    double oreValue = oreNoise.noise(worldX * 0.08, y * 0.08, worldZ * 0.08);
                    if (!surfacePlaced && y > seaLevel && oreValue > 0.8) {
                        chunkData.setBlock(x, y, z, ore);
                    } else {
                        chunkData.setBlock(x, y, z, base);
                    }
                }
            }
        }

        return chunkData;
    }

    private void ensureInitialized(World world) {
        if (initialized) {
            return;
        }
        long seed = world.getSeed();
        var themeSelector = new Random(seed);
        var theme = THEMES.get(themeSelector.nextInt(THEMES.size()));
        baseMat = theme.base();
        surfaceMat = theme.surface();
        oreMat = theme.ore();
        scale = theme.scale();

        terrainNoise = new SimplexNoiseGenerator(seed);
        oreNoise = new SimplexNoiseGenerator(seed ^ 0x5f3759df);
        humidityNoise = new SimplexNoiseGenerator(seed ^ 0x9e3779b97f4a7c15L);
        temperatureNoise = new SimplexNoiseGenerator(seed ^ 0x85ebca6b);
        initialized = true;
    }

    private MyceliaBiomeProfile resolveBiome(double humidity, double temperature) {
        for (var profile : biomeRegistry.profiles()) {
            if (profile.matches(humidity, temperature)) {
                return profile;
            }
        }
        return null;
    }

    private static double normalize(double value) {
        return (value + 1.0) / 2.0;
    }

    private static int clampHeight(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}

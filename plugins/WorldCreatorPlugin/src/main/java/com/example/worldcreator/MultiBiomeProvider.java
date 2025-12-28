package com.example.worldcreator;

import java.util.List;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

public class MultiBiomeProvider extends BiomeProvider {
    private static final int REGION_SIZE = 512;
    private static final List<Biome> BIOMES = List.of(
        Biome.FOREST,
        Biome.DESERT,
        Biome.FROZEN_PEAKS,
        Biome.OCEAN,
        Biome.BASALT_DELTAS
    );

    @Override
    public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
        int regionX = Math.floorDiv(x, REGION_SIZE);
        int regionZ = Math.floorDiv(z, REGION_SIZE);
        int index = Math.floorMod(regionX * 31 + regionZ * 17, BIOMES.size());
        return BIOMES.get(index);
    }

    @Override
    public List<Biome> getBiomes(WorldInfo worldInfo) {
        return BIOMES;
    }
}

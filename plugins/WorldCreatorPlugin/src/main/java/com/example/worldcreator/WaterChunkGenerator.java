package com.example.worldcreator;

import java.util.Random;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

public final class WaterChunkGenerator extends ChunkGenerator {
    private static final int SEA_LEVEL = 62;

    @Override
    public ChunkData generateChunkData(
            World world,
            Random random,
            int chunkX,
            int chunkZ,
            BiomeGrid biome
    ) {
        var chunkData = createChunkData(world);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunkData.setBlock(x, 0, z, Material.BEDROCK);
                for (int y = 1; y <= SEA_LEVEL; y++) {
                    chunkData.setBlock(x, y, z, Material.WATER);
                }
            }
        }

        return chunkData;
    }
}

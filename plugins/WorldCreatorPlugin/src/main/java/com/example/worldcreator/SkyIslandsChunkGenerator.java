package com.example.worldcreator;

import java.util.Random;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

public final class SkyIslandsChunkGenerator extends ChunkGenerator {

    @Override
    public ChunkData generateChunkData(
            World world,
            Random random,
            int chunkX,
            int chunkZ,
            BiomeGrid biome
    ) {
        var chunkData = createChunkData(world);

        // deterministische Chunk-Zufallsquelle
        var seededRandom = new Random(world.getSeed() ^ (chunkX * 341873128712L) ^ (chunkZ * 132897987541L));

        if (seededRandom.nextDouble() < 0.35) {
            int centerX = seededRandom.nextInt(16);
            int centerZ = seededRandom.nextInt(16);
            int centerY = 90 + seededRandom.nextInt(30);
            int radius = 4 + seededRandom.nextInt(5);

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = Math.max(1, centerY - radius); y <= centerY + radius; y++) {
                        double dx = x - centerX;
                        double dy = y - centerY;
                        double dz = z - centerZ;

                        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                        if (dist <= radius) {
                            chunkData.setBlock(
                                    x, y, z,
                                    (y == centerY + radius - 1) ? Material.GRASS_BLOCK : Material.STONE
                            );
                        }
                    }
                }
            }
        }

        return chunkData;
    }
}

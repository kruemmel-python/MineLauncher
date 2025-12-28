package com.example.worldcreator;

import java.util.Random;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.CaveVinesPlant;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.noise.SimplexNoiseGenerator;

public final class MyceliaIslandsPopulator extends BlockPopulator {

    private final SimplexNoiseGenerator islandNoise;

    public MyceliaIslandsPopulator(long seed) {
        // eigener Noise-Stream, deterministisch pro Weltseed
        this.islandNoise = new SimplexNoiseGenerator(seed ^ 0x27d4eb2d);
    }

    @Override
    public void populate(World world, Random random, org.bukkit.Chunk source) {
        int chunkX = source.getX();
        int chunkZ = source.getZ();

        int centerX = (chunkX << 4) + 8;
        int centerZ = (chunkZ << 4) + 8;

        double noise = islandNoise.noise(centerX * 0.01, centerZ * 0.01);
        if (noise < 0.78) {
            return;
        }

        int baseY = world.getSeaLevel() + 35 + (int) Math.round((noise - 0.78) * 20.0);
        int radius = 3 + (int) Math.round((noise - 0.78) * 6.0);

        // Schutz gegen Out-of-World
        int minY = world.getMinHeight() + 2;
        int maxY = world.getMaxHeight() - 3;
        if (baseY < minY || baseY > maxY) {
            return;
        }

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int distance = Math.abs(dx) + Math.abs(dz);
                if (distance > radius) {
                    continue;
                }

                int x = centerX + dx;
                int z = centerZ + dz;

                // Insel-Körper (3 Schichten)
                setBlockSafe(world, x, baseY - 2, z, Material.DIRT);
                setBlockSafe(world, x, baseY - 1, z, Material.DIRT);
                setBlockSafe(world, x, baseY, z, Material.DIRT);

                // Oberfläche
                setBlockSafe(world, x, baseY + 1, z, Material.MYCELIUM);

                // Hängende Wurzeln + Glowberries (korrekt über Cave Vines mit berries=true)
                int hangingLength = 2 + Math.max(0, radius - distance);
                for (int i = 1; i <= hangingLength; i++) {
                    int y = baseY - 2 - i;
                    setBlockSafe(world, x, y, z, Material.HANGING_ROOTS);

                    if (i == hangingLength) {
                        placeGlowBerries(world, x, y - 1, z);
                    }
                }
            }
        }
    }

    /**
     * Setzt "Glow Berries" korrekt.
     *
     * Wichtig: Material.GLOW_BERRIES ist KEIN Block (sondern Item/Feature) und crasht bei setType().
     * In Minecraft hängen Glowberries an Cave Vines. Dafür setzen wir Cave Vines und aktivieren berries=true.
     */
    private static void placeGlowBerries(World world, int x, int y, int z) {
        if (y <= world.getMinHeight() || y >= world.getMaxHeight()) {
            return;
        }

        BlockData data = Material.CAVE_VINES.createBlockData();

        // In 1.20.4 ist der BlockData-Typ für Cave Vines ein CaveVinesPlant (berries-Property vorhanden)
        if (data instanceof CaveVinesPlant vines) {
            vines.setBerries(true);
            world.getBlockAt(x, y, z).setBlockData(vines, false);
            return;
        }

        // Fallback: falls sich die API/Typen ändern, zumindest etwas Leuchtendes setzen statt zu crashen
        world.getBlockAt(x, y, z).setType(Material.GLOW_LICHEN, false);
    }

    private static void setBlockSafe(World world, int x, int y, int z, Material material) {
        if (y <= world.getMinHeight() || y >= world.getMaxHeight()) {
            return;
        }
        world.getBlockAt(x, y, z).setType(material, false);
    }
}

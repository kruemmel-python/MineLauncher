package com.example.rpg.schematic;

import com.example.rpg.schematic.nbt.NbtCompound;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.block.BlockFace;

public class SchematicPaster {
    public record PasteOptions(boolean includeAir, Transform transform) {
    }

    private final JavaPlugin plugin;
    private final Logger logger;

    public SchematicPaster(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public CompletableFuture<Void> pasteInBatches(World world, Location origin, Schematic schematic, PasteOptions options, int batchSize) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        int width = schematic.width();
        int height = schematic.height();
        int length = schematic.length();
        int[] blocks = schematic.blocks();
        BlockPalette palette = schematic.palette();
        ensureChunksLoaded(world, origin, schematic, options.transform());
        Iterator<BlockPlacement> iterator = new BlockIterator(width, height, length, blocks, palette, options.transform());
        new BukkitRunnable() {
            @Override
            public void run() {
                int placed = 0;
                while (iterator.hasNext() && placed < batchSize) {
                    BlockPlacement placement = iterator.next();
                    if (placement.blockData() == null) {
                        logger.warning("Missing palette entry for block index.");
                        continue;
                    }
                    if (!options.includeAir() && placement.isAir()) {
                        continue;
                    }
                    Block block = world.getBlockAt(origin.getBlockX() + placement.x(), origin.getBlockY() + placement.y(), origin.getBlockZ() + placement.z());
                    try {
                        BlockData data = Bukkit.createBlockData(placement.blockData());
                        data = rotateBlockData(data, options.transform().rotation());
                        block.setBlockData(data, false);
                    } catch (IllegalArgumentException ex) {
                        logger.warning("Invalid block data: " + placement.blockData());
                    }
                    placed++;
                }
                if (!iterator.hasNext()) {
                    applyBlockEntities(world, origin, schematic, options.transform());
                    spawnEntities(world, origin, schematic, options.transform());
                    future.complete(null);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
        return future;
    }

    private void applyBlockEntities(World world, Location origin, Schematic schematic, Transform transform) {
        List<NbtCompound> blockEntities = schematic.blockEntities();
        if (blockEntities == null || blockEntities.isEmpty()) {
            return;
        }
        BlockEntityApplier applier = new BlockEntityApplier(logger);
        for (NbtCompound blockEntity : blockEntities) {
            applier.apply(world, origin, blockEntity, transform, schematic.width(), schematic.length());
        }
    }

    private void spawnEntities(World world, Location origin, Schematic schematic, Transform transform) {
        List<NbtCompound> entities = schematic.entities();
        if (entities == null || entities.isEmpty()) {
            return;
        }
        EntitySpawner spawner = new EntitySpawner(logger);
        for (NbtCompound entity : entities) {
            spawner.spawn(world, origin, entity, transform, schematic.width(), schematic.length());
        }
    }

    private void ensureChunksLoaded(World world, Location origin, Schematic schematic, Transform transform) {
        int width = schematic.width();
        int length = schematic.length();
        int[] min = transform.apply(0, 0, 0, width, length);
        int[] max = transform.apply(width - 1, 0, length - 1, width, length);
        int minX = Math.min(min[0], max[0]) + origin.getBlockX();
        int maxX = Math.max(min[0], max[0]) + origin.getBlockX();
        int minZ = Math.min(min[2], max[2]) + origin.getBlockZ();
        int maxZ = Math.max(min[2], max[2]) + origin.getBlockZ();
        for (int x = minX >> 4; x <= maxX >> 4; x++) {
            for (int z = minZ >> 4; z <= maxZ >> 4; z++) {
                if (!world.isChunkLoaded(x, z)) {
                    world.getChunkAt(x, z);
                }
            }
        }
    }

    private static class BlockPlacement {
        private final int x;
        private final int y;
        private final int z;
        private final String blockData;
        private final boolean air;

        private BlockPlacement(int x, int y, int z, String blockData) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.blockData = blockData;
            this.air = blockData != null && (blockData.equals("minecraft:air") || blockData.equals("minecraft:cave_air")
                || blockData.equals("minecraft:void_air"));
        }

        public int x() {
            return x;
        }

        public int y() {
            return y;
        }

        public int z() {
            return z;
        }

        public String blockData() {
            return blockData;
        }

        public boolean isAir() {
            return air;
        }
    }

    private static class BlockIterator implements Iterator<BlockPlacement> {
        private final int width;
        private final int height;
        private final int length;
        private final int[] blocks;
        private final BlockPalette palette;
        private final Transform transform;
        private int index;

        private BlockIterator(int width, int height, int length, int[] blocks, BlockPalette palette, Transform transform) {
            this.width = width;
            this.height = height;
            this.length = length;
            this.blocks = blocks;
            this.palette = palette;
            this.transform = transform;
        }

        @Override
        public boolean hasNext() {
            return index < blocks.length;
        }

        @Override
        public BlockPlacement next() {
            int i = index++;
            int x = i % width;
            int z = (i / width) % length;
            int y = i / (width * length);
            int[] transformed = transform.apply(x, y, z, width, length);
            String blockData = palette.getState(blocks[i]);
            return new BlockPlacement(transformed[0], transformed[1], transformed[2], blockData);
        }
    }

    private BlockData rotateBlockData(BlockData data, Transform.Rotation rotation) {
        if (rotation == Transform.Rotation.NONE) {
            return data;
        }
        if (data instanceof Directional directional) {
            BlockFace face = directional.getFacing();
            BlockFace rotated = rotateFace(face, rotation);
            if (rotated != null) {
                directional.setFacing(rotated);
            }
        } else if (data instanceof Rotatable rotatable) {
            BlockFace face = rotatable.getRotation();
            BlockFace rotated = rotateFace(face, rotation);
            if (rotated != null) {
                rotatable.setRotation(rotated);
            }
        }
        return data;
    }

    private BlockFace rotateFace(BlockFace face, Transform.Rotation rotation) {
        if (face == null) {
            return null;
        }
        return switch (rotation) {
            case CLOCKWISE_90 -> rotateOnce(face);
            case CLOCKWISE_180 -> rotateOnce(rotateOnce(face));
            case CLOCKWISE_270 -> rotateOnce(rotateOnce(rotateOnce(face)));
            default -> face;
        };
    }

    private BlockFace rotateOnce(BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.EAST;
            case EAST -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.WEST;
            case WEST -> BlockFace.NORTH;
            default -> face;
        };
    }
}

package com.example.rpg.schematic;

import com.example.rpg.schematic.nbt.NbtCompound;
import com.example.rpg.schematic.nbt.NbtList;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpongeSchemLoader {
    public Schematic load(File file) throws IOException {
        NbtCompound root = NbtIO.read(file);
        int width = root.getInt("Width", -1);
        int height = root.getInt("Height", -1);
        int length = root.getInt("Length", -1);
        if (width <= 0 || height <= 0 || length <= 0) {
            throw new IOException("Unsupported schematic format");
        }
        NbtCompound paletteTag = root.getCompound("Palette");
        if (paletteTag == null) {
            throw new IOException("Unsupported schematic format");
        }
        Map<Integer, String> paletteMap = new HashMap<>();
        for (var entry : paletteTag.values().entrySet()) {
            String blockState = entry.getKey();
            var tag = entry.getValue();
            if (tag instanceof com.example.rpg.schematic.nbt.NbtInt nbtInt) {
                paletteMap.put(nbtInt.value(), blockState);
            } else if (tag instanceof com.example.rpg.schematic.nbt.NbtShort nbtShort) {
                paletteMap.put((int) nbtShort.value(), blockState);
            }
        }
        BlockPalette palette = new BlockPalette(paletteMap);
        int total = width * height * length;
        int[] blocks = readBlocks(root, palette.size(), total);
        if (blocks.length != total) {
            throw new IOException("Unsupported schematic format");
        }
        List<NbtCompound> blockEntities = List.of();
        List<NbtCompound> entities = List.of();
        NbtList blockEntityList = root.getList("BlockEntities");
        if (blockEntityList != null) {
            blockEntities = blockEntityList.compounds();
        }
        NbtList entityList = root.getList("Entities");
        if (entityList != null) {
            entities = entityList.compounds();
        }
        return new Schematic(width, height, length, palette, blocks, blockEntities, entities);
    }

    private int[] readBlocks(NbtCompound root, int paletteSize, int totalBlocks) throws IOException {
        byte[] byteData = root.getByteArray("BlockData");
        if (byteData != null) {
            return decodeVarIntArray(byteData, totalBlocks);
        }
        long[] longData = root.getLongArray("BlockData");
        if (longData != null) {
            int bits = Math.max(4, 32 - Integer.numberOfLeadingZeros(Math.max(paletteSize - 1, 1)));
            return unpackLongArray(longData, bits, totalBlocks);
        }
        throw new IOException("Unsupported schematic format");
    }

    private int[] decodeVarIntArray(byte[] data, int expected) throws IOException {
        int[] values = new int[expected];
        int index = 0;
        int i = 0;
        while (i < data.length && index < expected) {
            int value = 0;
            int position = 0;
            byte current;
            do {
                if (i >= data.length) {
                    throw new IOException("Unexpected end of block data");
                }
                current = data[i++];
                value |= (current & 0x7F) << position;
                position += 7;
            } while ((current & 0x80) != 0);
            values[index++] = value;
        }
        if (index != expected) {
            throw new IOException("Block data length mismatch");
        }
        return values;
    }

    private int[] unpackLongArray(long[] data, int bits, int expected) {
        int[] values = new int[expected];
        long mask = (1L << bits) - 1L;
        int index = 0;
        int bitIndex = 0;
        while (index < expected) {
            int startLong = bitIndex >> 6;
            int startOffset = bitIndex & 63;
            if (startLong >= data.length) {
                break;
            }
            long value = data[startLong] >>> startOffset;
            int bitsLeft = 64 - startOffset;
            if (bitsLeft < bits && startLong + 1 < data.length) {
                value |= data[startLong + 1] << bitsLeft;
            }
            values[index++] = (int) (value & mask);
            bitIndex += bits;
        }
        return values;
    }
}

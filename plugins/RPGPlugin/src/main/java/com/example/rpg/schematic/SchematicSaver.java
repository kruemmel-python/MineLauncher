package com.example.rpg.schematic;

import com.example.rpg.schematic.nbt.NbtCompound;
import com.example.rpg.schematic.nbt.NbtInt;
import com.example.rpg.schematic.nbt.NbtIntArray;
import com.example.rpg.schematic.nbt.NbtList;
import com.example.rpg.schematic.nbt.NbtString;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Jigsaw;
import org.bukkit.block.data.BlockData;

public class SchematicSaver {
    public void saveSelection(World world, Location pos1, Location pos2, File file) throws IOException {
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        int length = maxZ - minZ + 1;

        Map<String, Integer> paletteIndex = new LinkedHashMap<>();
        List<String> paletteById = new ArrayList<>();
        int[] blocks = new int[width * height * length];
        List<NbtCompound> blockEntities = new ArrayList<>();
        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    Block block = world.getBlockAt(minX + x, minY + y, minZ + z);
                    BlockData data = block.getBlockData();
                    String state = data.getAsString(true);
                    int paletteId = paletteIndex.computeIfAbsent(state, key -> {
                        paletteById.add(key);
                        return paletteById.size() - 1;
                    });
                    blocks[index++] = paletteId;
                    BlockState stateBlock = block.getState();
                    if (stateBlock instanceof Jigsaw jigsaw) {
                        blockEntities.add(jigsawEntity(x, y, z, jigsaw));
                    }
                }
            }
        }

        NbtCompound root = new NbtCompound();
        root.put("Version", new NbtInt(2));
        root.put("DataVersion", new NbtInt(Bukkit.getUnsafe().getDataVersion()));
        root.put("Width", new NbtInt(width));
        root.put("Height", new NbtInt(height));
        root.put("Length", new NbtInt(length));
        root.put("PaletteMax", new NbtInt(paletteById.size()));
        root.put("Palette", buildPalette(paletteById));
        root.put("BlockData", new com.example.rpg.schematic.nbt.NbtByteArray(encodeVarInts(blocks)));
        root.put("BlockEntities", new NbtList(com.example.rpg.schematic.nbt.NbtType.COMPOUND, new ArrayList<>(blockEntities)));
        root.put("Entities", new NbtList(com.example.rpg.schematic.nbt.NbtType.COMPOUND, List.of()));

        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        NbtIO.write(file, root);
    }

    private NbtCompound buildPalette(List<String> palette) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < palette.size(); i++) {
            map.put(palette.get(i), i);
        }
        NbtCompound paletteTag = new NbtCompound();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            paletteTag.put(entry.getKey(), new NbtInt(entry.getValue()));
        }
        return paletteTag;
    }

    private NbtCompound jigsawEntity(int x, int y, int z, Jigsaw jigsaw) {
        NbtCompound tag = new NbtCompound();
        tag.put("id", new NbtString("minecraft:jigsaw"));
        tag.put("Pos", new NbtIntArray(new int[] {x, y, z}));
        tag.put("name", new NbtString(readJigsawName(jigsaw)));
        return tag;
    }

    private String readJigsawName(Jigsaw jigsaw) {
        try {
            var method = jigsaw.getClass().getMethod("getName");
            Object value = method.invoke(jigsaw);
            if (value instanceof NamespacedKey key) {
                return key.getKey();
            }
            if (value != null) {
                return value.toString();
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return "socket";
    }

    private byte[] encodeVarInts(int[] values) {
        List<Byte> bytes = new ArrayList<>();
        for (int value : values) {
            int v = value;
            while ((v & 0xFFFFFF80) != 0) {
                bytes.add((byte) ((v & 0x7F) | 0x80));
                v >>>= 7;
            }
            bytes.add((byte) (v & 0x7F));
        }
        byte[] result = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            result[i] = bytes.get(i);
        }
        return result;
    }
}

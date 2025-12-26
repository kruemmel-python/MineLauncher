package com.example.rpg.dungeon.layout;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.World;

public class BlockBuffer {
    private final Map<Long, Material> buffer = new HashMap<>();

    public void set(int x, int y, int z, Material material) {
        buffer.put(toKey(x, y, z), material);
    }

    public void flush(World world) {
        for (Map.Entry<Long, Material> entry : buffer.entrySet()) {
            long key = entry.getKey();
            int x = (int) (key >> 42);
            int y = (int) (key >> 21) & 0x1FFFFF;
            int z = (int) (key & 0x1FFFFF);
            world.getBlockAt(x, y, z).setType(entry.getValue(), false);
        }
        buffer.clear();
    }

    private long toKey(int x, int y, int z) {
        long lx = ((long) x & 0x1FFFFF) << 42;
        long ly = ((long) y & 0x1FFFFF) << 21;
        long lz = (long) z & 0x1FFFFF;
        return lx | ly | lz;
    }
}

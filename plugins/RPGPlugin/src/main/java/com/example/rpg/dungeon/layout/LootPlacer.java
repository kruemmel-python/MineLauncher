package com.example.rpg.dungeon.layout;

import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.loot.Lootable;
import org.bukkit.loot.LootTable;
import org.bukkit.util.BoundingBox;

public class LootPlacer {
    private final Random random;

    public LootPlacer(Random random) {
        this.random = random;
    }

    public void placeLoot(Room room, DungeonSettings settings, org.bukkit.World world) {
        if (!settings.lootEnabled() || room.type() != RoomType.LOOT) {
            return;
        }
        int count = nextBetween(settings.lootMin(), settings.lootMax());
        for (int i = 0; i < count; i++) {
            Location location = randomPoint(room, world);
            world.getBlockAt(location).setType(Material.CHEST, false);
            BlockState state = world.getBlockAt(location).getState();
            if (state instanceof Lootable lootable) {
                LootTable table = lookupTable(settings.lootTable(), world);
                if (table != null) {
                    lootable.setLootTable(table);
                    lootable.update();
                }
            }
        }
    }

    private LootTable lookupTable(String tableKey, org.bukkit.World world) {
        if (tableKey == null || tableKey.isBlank()) {
            return null;
        }
        NamespacedKey key = NamespacedKey.fromString(tableKey);
        if (key == null) {
            key = NamespacedKey.minecraft(tableKey);
        }
        return Bukkit.getLootTable(key);
    }

    private Location randomPoint(Room room, org.bukkit.World world) {
        BoundingBox box = room.bounds();
        int x = nextBetween((int) box.getMinX() + 1, (int) box.getMaxX() - 1);
        int z = nextBetween((int) box.getMinZ() + 1, (int) box.getMaxZ() - 1);
        return new Location(world, x, box.getMinY() + 1, z);
    }

    private int nextBetween(int min, int max) {
        if (max <= min) {
            return min;
        }
        return random.nextInt(max - min + 1) + min;
    }
}

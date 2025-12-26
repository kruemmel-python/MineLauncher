package com.example.rpg.dungeon.layout;

import com.example.rpg.RPGPlugin;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;

public class SpawnPlacer {
    private final RPGPlugin plugin;
    private final Random random;

    public SpawnPlacer(RPGPlugin plugin, Random random) {
        this.plugin = plugin;
        this.random = random;
    }

    public void spawnRoomMobs(Room room, DungeonSettings settings, org.bukkit.World world) {
        if (!settings.mobsEnabled()) {
            return;
        }
        if (room.type() == RoomType.START || room.type() == RoomType.EXIT || room.type() == RoomType.LOOT) {
            return;
        }
        int count = nextBetween(settings.mobsMin(), settings.mobsMax());
        for (int i = 0; i < count; i++) {
            Location spawn = randomPoint(room, world);
            if (room.type() == RoomType.BOSS) {
                if (!settings.bossEnabled()) {
                    return;
                }
                var boss = plugin.mobManager().getMob("boss_zombie");
                if (boss != null) {
                    var entity = spawn.getWorld().spawnEntity(spawn, EntityType.valueOf(boss.type().toUpperCase()));
                    if (entity instanceof LivingEntity living) {
                        plugin.customMobListener().applyDefinition(living, boss);
                    }
                    return;
                }
                world.spawnEntity(spawn, EntityType.ZOMBIE);
                return;
            }
            if (room.type() == RoomType.ELITE) {
                world.spawnEntity(spawn, EntityType.HUSK);
            } else {
                world.spawnEntity(spawn, EntityType.ZOMBIE);
            }
        }
    }

    private Location randomPoint(Room room, org.bukkit.World world) {
        BoundingBox box = room.bounds();
        int x = nextBetween((int) box.getMinX() + 1, (int) box.getMaxX() - 1);
        int z = nextBetween((int) box.getMinZ() + 1, (int) box.getMaxZ() - 1);
        return new Location(world, x + 0.5, box.getMinY() + 1, z + 0.5);
    }

    private int nextBetween(int min, int max) {
        if (max <= min) {
            return min;
        }
        return random.nextInt(max - min + 1) + min;
    }
}

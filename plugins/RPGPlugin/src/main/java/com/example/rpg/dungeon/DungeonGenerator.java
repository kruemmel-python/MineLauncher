package com.example.rpg.dungeon;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.MobDefinition;
import com.example.rpg.model.Spawner;
import com.example.rpg.dungeon.wfc.Pattern;
import com.example.rpg.dungeon.wfc.WfcGenerator;
import com.example.rpg.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

public class DungeonGenerator {
    private final RPGPlugin plugin;
    private final Random random = new Random();
    private final WfcGenerator wfcGenerator;

    public DungeonGenerator(RPGPlugin plugin) {
        this.plugin = plugin;
        this.wfcGenerator = new WfcGenerator(plugin);
    }

    public DungeonInstance generate(String theme, List<Player> party) {
        String worldName = "dungeon_" + System.currentTimeMillis();
        World world = plugin.getServer().createWorld(new WorldCreator(worldName));
        int roomSize = 9;
        int grid = 4;
        int baseY = plugin.getConfig().getInt("dungeon.fixedBaseY", 150);
        int wallHeight = plugin.getConfig().getInt("dungeon.wfc.boundaryHeight", 10);
        List<Location> roomCenters = new ArrayList<>();
        for (int x = 0; x < grid; x++) {
            for (int z = 0; z < grid; z++) {
                int startX = x * (roomSize + 2);
                int startZ = z * (roomSize + 2);
                carveRoom(world, startX, baseY, startZ, roomSize, Material.STONE_BRICKS);
                roomCenters.add(new Location(world, startX + roomSize / 2.0, baseY + 1, startZ + roomSize / 2.0));
                if (x > 0) {
                    carveCorridor(world, startX - 1, baseY, startZ + roomSize / 2, Material.COBBLESTONE);
                }
                if (z > 0) {
                    carveCorridor(world, startX + roomSize / 2, baseY, startZ - 1, Material.COBBLESTONE);
                }
            }
        }
        Location start = roomCenters.get(0).clone();
        Location bossRoom = roomCenters.get(roomCenters.size() - 1).clone();
        spawnSpawners(roomCenters, theme);
        spawnBoss(bossRoom);
        spawnSigns(start, bossRoom, theme);
        int maxX = (grid - 1) * (roomSize + 2) + roomSize - 1;
        int maxZ = (grid - 1) * (roomSize + 2) + roomSize - 1;
        BoundingBox area = new BoundingBox(0, baseY, 0, maxX, baseY + 5, maxZ);
        createGlassBoundary(world, area, wallHeight);
        return new DungeonInstance(world, start, bossRoom);
    }

    public void generateWfc(String theme, List<Player> party, Consumer<DungeonInstance> callback) {
        String worldName = "dungeon_" + System.currentTimeMillis();
        World world = plugin.getServer().createWorld(new WorldCreator(worldName));
        int width = plugin.getConfig().getInt("dungeon.wfc.width", 10);
        int height = plugin.getConfig().getInt("dungeon.wfc.height", 3);
        int depth = plugin.getConfig().getInt("dungeon.wfc.depth", 10);
        int originY = plugin.getConfig().getInt("dungeon.wfc.originY", 150);
        int wallHeight = plugin.getConfig().getInt("dungeon.wfc.boundaryHeight", 10);
        wfcGenerator.generate(theme, width, height, depth).thenAccept(patterns -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (patterns == null) {
                        return;
                    }
                    Location start = new Location(world, 1, originY + 2, 1);
                    Location bossRoom = buildFromPatterns(world, patterns, originY);
                    int cellSize = 2;
                    int maxX = (width - 1) * cellSize + 2;
                    int maxZ = (depth - 1) * cellSize + 2;
                    int maxY = originY + (height - 1) * cellSize;
                    BoundingBox area = new BoundingBox(0, originY, 0, maxX, maxY, maxZ);
                    createGlassBoundary(world, area, wallHeight);
                    DungeonInstance instance = new DungeonInstance(world, start, bossRoom);
                    callback.accept(instance);
                }
            }.runTask(plugin);
        });
    }

    private Location buildFromPatterns(World world, Pattern[][][] patterns, int originY) {
        Location start = null;
        Location farthest = null;
        double bestDistance = 0;
        int cellSize = 2;
        for (int x = 0; x < patterns.length; x++) {
            for (int y = 0; y < patterns[x].length; y++) {
                for (int z = 0; z < patterns[x][y].length; z++) {
                    Pattern pattern = patterns[x][y][z];
                    if (pattern == null) {
                        continue;
                    }
                    int baseX = x * cellSize;
                    int baseY = originY + y * cellSize;
                    int baseZ = z * cellSize;
                    placePattern(world, pattern, baseX, baseY, baseZ);
                    if ("FLOOR".equals(pattern.socketDown())) {
                        Location center = new Location(world, baseX + 0.5, baseY + 1, baseZ + 0.5);
                        if (start == null) {
                            start = center;
                        }
                        double distance = start != null ? start.distanceSquared(center) : 0;
                        if (distance > bestDistance) {
                            bestDistance = distance;
                            farthest = center;
                        }
                    }
                }
            }
        }
        if (farthest == null) {
            farthest = new Location(world, 1, originY + 2, 1);
        }
        spawnBoss(farthest);
        return farthest;
    }

    private void placePattern(World world, Pattern pattern, int baseX, int baseY, int baseZ) {
        Material[] blocks = pattern.blocks();
        int index = 0;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    Material material = blocks[index++];
                    world.getBlockAt(baseX + x, baseY + y, baseZ + z).setType(material, false);
                }
            }
        }
    }

    private void carveRoom(World world, int startX, int startY, int startZ, int size, Material material) {
        for (int x = startX; x < startX + size; x++) {
            for (int z = startZ; z < startZ + size; z++) {
                world.getBlockAt(x, startY, z).setType(material);
                for (int y = 1; y <= 4; y++) {
                    world.getBlockAt(x, startY + y, z).setType(Material.AIR);
                }
            }
        }
        for (int x = startX; x < startX + size; x++) {
            for (int z = startZ; z < startZ + size; z++) {
                if (x == startX || x == startX + size - 1 || z == startZ || z == startZ + size - 1) {
                    world.getBlockAt(x, startY + 5, z).setType(material);
                }
            }
        }
    }

    private void carveCorridor(World world, int startX, int startY, int startZ, Material material) {
        for (int i = 0; i < 3; i++) {
            world.getBlockAt(startX + i, startY, startZ).setType(material);
            for (int y = 1; y <= 3; y++) {
                world.getBlockAt(startX + i, startY + y, startZ).setType(Material.AIR);
            }
        }
    }

    private void spawnSpawners(List<Location> roomCenters, String theme) {
        String spawnerId = theme + "_spawner";
        Spawner spawner = plugin.spawnerManager().getSpawner(spawnerId);
        for (int i = 1; i < roomCenters.size() - 1; i++) {
            Location location = roomCenters.get(i);
            if (spawner == null) {
                spawnFallbackMob(location);
                continue;
            }
            if (spawner.mobs().isEmpty()) {
                spawnFallbackMob(location);
                continue;
            }
            String mobId = spawner.mobs().keySet().iterator().next();
            MobDefinition mob = plugin.mobManager().getMob(mobId);
            if (mob == null) {
                spawnFallbackMob(location);
                continue;
            }
            var entity = location.getWorld().spawnEntity(location, EntityType.valueOf(mob.type().toUpperCase()));
            if (entity instanceof org.bukkit.entity.LivingEntity living) {
                plugin.customMobListener().applyDefinition(living, mob);
            }
        }
    }

    private void spawnFallbackMob(Location location) {
        location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
    }

    private void spawnBoss(Location bossRoom) {
        MobDefinition boss = plugin.mobManager().getMob("boss_zombie");
        if (boss == null) {
            return;
        }
        var entity = bossRoom.getWorld().spawnEntity(bossRoom, EntityType.valueOf(boss.type().toUpperCase()));
        if (entity instanceof org.bukkit.entity.LivingEntity living) {
            plugin.customMobListener().applyDefinition(living, boss);
            TextDisplay display = bossRoom.getWorld().spawn(bossRoom.clone().add(0, 2, 0), TextDisplay.class);
            Component bossName = LegacyComponentSerializer.legacySection().deserialize(boss.name());
            display.text(Component.text("Boss: ").append(bossName));
        }
    }

    private void spawnSigns(Location start, Location bossRoom, String theme) {
        TextDisplay startSign = start.getWorld().spawn(start.clone().add(0, 2, 0), TextDisplay.class);
        startSign.text(Text.mm("<gold>Dungeon: " + theme));
        TextDisplay bossSign = bossRoom.getWorld().spawn(bossRoom.clone().add(0, 2, 0), TextDisplay.class);
        bossSign.text(Text.mm("<red>Boss-Raum"));
    }

    private void createGlassBoundary(World world, BoundingBox area, int wallHeight) {
        int minX = (int) Math.floor(area.getMinX()) - 1;
        int minZ = (int) Math.floor(area.getMinZ()) - 1;
        int maxX = (int) Math.ceil(area.getMaxX()) + 1;
        int maxZ = (int) Math.ceil(area.getMaxZ()) + 1;
        int floorY = (int) Math.floor(area.getMinY()) - 1;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                world.getBlockAt(x, floorY, z).setType(Material.BLACK_STAINED_GLASS, false);
            }
        }

        int wallTop = floorY + wallHeight;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (x != minX && x != maxX && z != minZ && z != maxZ) {
                    continue;
                }
                for (int y = floorY + 1; y <= wallTop; y++) {
                    world.getBlockAt(x, y, z).setType(Material.GLASS, false);
                }
            }
        }

        int roofY = wallTop + 1;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                world.getBlockAt(x, roofY, z).setType(Material.GLASS, false);
            }
        }
    }
}

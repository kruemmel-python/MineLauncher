package com.example.rpg.manager;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.MobDefinition;
import com.example.rpg.model.Spawner;
import com.example.rpg.model.Zone;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.persistence.PersistentDataType;

public class SpawnerManager {
    private final RPGPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Spawner> spawners = new HashMap<>();
    private final Random random = new Random();

    public SpawnerManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "spawners.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
        startTask();
    }

    public Spawner getSpawner(String id) {
        return spawners.get(id);
    }

    public Map<String, Spawner> spawners() {
        return spawners;
    }

    public void saveSpawner(Spawner spawner) {
        config.set(spawner.id(), null);
        ConfigurationSection section = config.createSection(spawner.id());
        section.set("zoneId", spawner.zoneId());
        section.set("maxMobs", spawner.maxMobs());
        section.set("spawnInterval", spawner.spawnInterval());
        section.set("mobs", spawner.mobs());
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (Spawner spawner : spawners.values()) {
            saveSpawner(spawner);
        }
        save();
    }

    private void load() {
        spawners.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Spawner spawner = new Spawner(id);
            spawner.setZoneId(section.getString("zoneId", null));
            spawner.setMaxMobs(section.getInt("maxMobs", 6));
            spawner.setSpawnInterval(section.getInt("spawnInterval", 200));
            ConfigurationSection mobsSection = section.getConfigurationSection("mobs");
            if (mobsSection != null) {
                Map<String, Double> mobs = new HashMap<>();
                for (String mobId : mobsSection.getKeys(false)) {
                    mobs.put(mobId, mobsSection.getDouble(mobId, 1.0));
                }
                spawner.setMobs(mobs);
            }
            spawners.put(id, spawner);
        }
    }

    private void seedDefaults() {
        Spawner spawner = new Spawner("forest_spawner");
        spawner.setZoneId("startzone");
        spawner.setMaxMobs(6);
        spawner.setSpawnInterval(200);
        spawner.setMobs(Map.of("boss_zombie", 1.0));
        spawners.put(spawner.id(), spawner);
        saveAll();
    }

    private void startTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            long now = plugin.getServer().getCurrentTick();
            for (Spawner spawner : spawners.values()) {
                if (spawner.spawnInterval() <= 0) {
                    continue;
                }
                if (now % spawner.spawnInterval() != 0) {
                    continue;
                }
                if (spawner.zoneId() == null) {
                    continue;
                }
                Zone zone = plugin.zoneManager().getZone(spawner.zoneId());
                if (zone == null) {
                    continue;
                }
                if (!hasPlayersInZone(zone)) {
                    continue;
                }
                int current = countMobsInZone(zone);
                if (current >= spawner.maxMobs()) {
                    continue;
                }
                MobDefinition mob = pickMob(spawner);
                if (mob == null) {
                    continue;
                }
                spawnMobInZone(zone, mob);
            }
        }, 40L, 40L);
    }

    private boolean hasPlayersInZone(Zone zone) {
        World world = plugin.getServer().getWorld(zone.world());
        if (world == null) {
            return false;
        }
        return world.getPlayers().stream().anyMatch(player -> zone.contains(player.getLocation()));
    }

    private int countMobsInZone(Zone zone) {
        World world = plugin.getServer().getWorld(zone.world());
        if (world == null) {
            return 0;
        }
        return (int) world.getLivingEntities().stream()
            .filter(entity -> entity.getPersistentDataContainer()
                .has(plugin.customMobListener().mobKey(), PersistentDataType.STRING))
            .filter(entity -> zone.contains(entity.getLocation()))
            .count();
    }

    private MobDefinition pickMob(Spawner spawner) {
        if (spawner.mobs().isEmpty()) {
            return null;
        }
        double total = spawner.mobs().values().stream().mapToDouble(Double::doubleValue).sum();
        double roll = random.nextDouble() * total;
        double current = 0;
        for (Map.Entry<String, Double> entry : spawner.mobs().entrySet()) {
            current += entry.getValue();
            if (roll <= current) {
                return plugin.mobManager().getMob(entry.getKey());
            }
        }
        String fallback = spawner.mobs().keySet().iterator().next();
        return plugin.mobManager().getMob(fallback);
    }

    private void spawnMobInZone(Zone zone, MobDefinition mob) {
        World world = plugin.getServer().getWorld(zone.world());
        if (world == null) {
            return;
        }
        int x = randomBetween(zone.x1(), zone.x2());
        int z = randomBetween(zone.z1(), zone.z2());
        int y = world.getHighestBlockYAt(x, z);
        Location location = new Location(world, x + 0.5, y + 1, z + 0.5);
        var type = org.bukkit.entity.EntityType.valueOf(mob.type().toUpperCase());
        var entity = world.spawnEntity(location, type);
        if (entity instanceof org.bukkit.entity.LivingEntity living) {
            plugin.customMobListener().applyDefinition(living, mob);
        } else {
            entity.remove();
        }
    }

    private int randomBetween(int min, int max) {
        int low = Math.min(min, max);
        int high = Math.max(min, max);
        return low + random.nextInt(Math.max(1, high - low + 1));
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save spawners.yml: " + e.getMessage());
        }
    }
}

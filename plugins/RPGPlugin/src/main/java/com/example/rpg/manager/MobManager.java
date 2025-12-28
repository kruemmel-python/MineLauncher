package com.example.rpg.manager;

import com.example.rpg.model.MobDefinition;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class MobManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, MobDefinition> mobs = new HashMap<>();

    public MobManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "mobs.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public MobDefinition getMob(String id) {
        return mobs.get(id);
    }

    public Map<String, MobDefinition> mobs() {
        return mobs;
    }

    public void saveMob(MobDefinition mob) {
        ConfigurationSection section = config.createSection(mob.id());
        section.set("name", mob.name());
        section.set("type", mob.type());
        section.set("health", mob.health());
        section.set("damage", mob.damage());
        section.set("mainHand", mob.mainHand());
        section.set("helmet", mob.helmet());
        section.set("skills", mob.skills());
        section.set("skillIntervalSeconds", mob.skillIntervalSeconds());
        section.set("xp", mob.xp());
        section.set("lootTable", mob.lootTable());
        section.set("behaviorTree", mob.behaviorTree());
        section.set("boss", mob.boss());
        section.set("debugAi", mob.debugAi());
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (MobDefinition mob : mobs.values()) {
            saveMob(mob);
        }
        save();
    }

    private void load() {
        mobs.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            MobDefinition mob = new MobDefinition(id);
            mob.setName(section.getString("name", id));
            mob.setType(section.getString("type", "ZOMBIE"));
            mob.setHealth(section.getDouble("health", 40));
            mob.setDamage(section.getDouble("damage", 6));
            mob.setMainHand(section.getString("mainHand", null));
            mob.setHelmet(section.getString("helmet", null));
            mob.setSkills(section.getStringList("skills"));
            mob.setSkillIntervalSeconds(section.getInt("skillIntervalSeconds", 8));
            mob.setXp(section.getInt("xp", 50));
            mob.setLootTable(section.getString("lootTable", null));
            mob.setBehaviorTree(section.getString("behaviorTree", null));
            mob.setBoss(section.getBoolean("boss", false));
            mob.setDebugAi(section.getBoolean("debugAi", false));
            mobs.put(id, mob);
        }
    }

    private void seedDefaults() {
        MobDefinition zombie = new MobDefinition("boss_zombie");
        zombie.setName("§cSeuchenbringer");
        zombie.setType("ZOMBIE");
        zombie.setHealth(60);
        zombie.setDamage(8);
        zombie.setMainHand("IRON_SWORD");
        zombie.setHelmet("IRON_HELMET");
        zombie.setSkills(List.of("ember_shot", "whirlwind"));
        zombie.setSkillIntervalSeconds(10);
        zombie.setXp(120);
        zombie.setLootTable("forest_mobs");
        zombie.setBoss(true);
        mobs.put(zombie.id(), zombie);

        MobDefinition skeletonKing = new MobDefinition("skeleton_king");
        skeletonKing.setName("§cSkelettkönig");
        skeletonKing.setType("SKELETON");
        skeletonKing.setHealth(80);
        skeletonKing.setDamage(10);
        skeletonKing.setMainHand("DIAMOND_SWORD");
        skeletonKing.setHelmet("GOLDEN_HELMET");
        skeletonKing.setSkills(List.of("shield_wall", "ember_shot"));
        skeletonKing.setSkillIntervalSeconds(8);
        skeletonKing.setXp(180);
        skeletonKing.setLootTable("forest_mobs");
        skeletonKing.setBehaviorTree("skeleton_king");
        skeletonKing.setBoss(true);
        mobs.put(skeletonKing.id(), skeletonKing);
        saveAll();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save mobs.yml: " + e.getMessage());
        }
    }
}

package com.example.rpg.manager;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.QuestProgress;
import com.example.rpg.model.RPGStat;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerDataManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<UUID, PlayerProfile> profiles = new HashMap<>();

    public PlayerDataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "players.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public PlayerProfile getProfile(UUID uuid) {
        return profiles.computeIfAbsent(uuid, this::loadProfile);
    }

    public PlayerProfile getProfile(Player player) {
        return getProfile(player.getUniqueId());
    }

    private PlayerProfile loadProfile(UUID uuid) {
        String key = uuid.toString();
        PlayerProfile profile = new PlayerProfile(uuid);
        if (config.contains(key)) {
            ConfigurationSection section = config.getConfigurationSection(key);
            profile.setLevel(section.getInt("level", 1));
            profile.setXp(section.getInt("xp", 0));
            profile.setSkillPoints(section.getInt("skillPoints", 0));
            profile.setMana(section.getInt("mana", 100));
            profile.setMaxMana(section.getInt("maxMana", 100));
            profile.setClassId(section.getString("classId", null));
            ConfigurationSection stats = section.getConfigurationSection("stats");
            if (stats != null) {
                for (String statKey : stats.getKeys(false)) {
                    try {
                        RPGStat stat = RPGStat.valueOf(statKey);
                        profile.stats().put(stat, stats.getInt(statKey));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
            ConfigurationSection skills = section.getConfigurationSection("skills");
            if (skills != null) {
                for (String skillKey : skills.getKeys(false)) {
                    profile.learnedSkills().put(skillKey, skills.getInt(skillKey));
                }
            }
            ConfigurationSection quests = section.getConfigurationSection("activeQuests");
            if (quests != null) {
                for (String questId : quests.getKeys(false)) {
                    QuestProgress progress = new QuestProgress(questId);
                    ConfigurationSection progressSection = quests.getConfigurationSection(questId);
                    if (progressSection != null) {
                        ConfigurationSection steps = progressSection.getConfigurationSection("steps");
                        if (steps != null) {
                            for (String stepKey : steps.getKeys(false)) {
                                progress.incrementStep(Integer.parseInt(stepKey), steps.getInt(stepKey));
                            }
                        }
                        progress.setCompleted(progressSection.getBoolean("completed", false));
                    }
                    profile.activeQuests().put(questId, progress);
                }
            }
            profile.completedQuests().addAll(section.getStringList("completedQuests"));
            ConfigurationSection factions = section.getConfigurationSection("factionRep");
            if (factions != null) {
                for (String factionId : factions.getKeys(false)) {
                    profile.factionRep().put(factionId, factions.getInt(factionId));
                }
            }

            ConfigurationSection cooldowns = section.getConfigurationSection("skillCooldowns");
            if (cooldowns != null) {
                for (String skillId : cooldowns.getKeys(false)) {
                    profile.skillCooldowns().put(skillId, cooldowns.getLong(skillId, 0L));
                }
            }
        }
        return profile;
    }

    public void saveProfile(PlayerProfile profile) {
        String key = profile.uuid().toString();
        ConfigurationSection section = config.createSection(key);
        section.set("level", profile.level());
        section.set("xp", profile.xp());
        section.set("skillPoints", profile.skillPoints());
        section.set("mana", profile.mana());
        section.set("maxMana", profile.maxMana());
        section.set("classId", profile.classId());
        ConfigurationSection stats = section.createSection("stats");
        for (Map.Entry<RPGStat, Integer> entry : profile.stats().entrySet()) {
            stats.set(entry.getKey().name(), entry.getValue());
        }
        ConfigurationSection skills = section.createSection("skills");
        for (Map.Entry<String, Integer> entry : profile.learnedSkills().entrySet()) {
            skills.set(entry.getKey(), entry.getValue());
        }
        ConfigurationSection quests = section.createSection("activeQuests");
        for (QuestProgress progress : profile.activeQuests().values()) {
            ConfigurationSection progressSection = quests.createSection(progress.questId());
            ConfigurationSection steps = progressSection.createSection("steps");
            for (Map.Entry<Integer, Integer> entry : progress.stepProgress().entrySet()) {
                steps.set(String.valueOf(entry.getKey()), entry.getValue());
            }
            progressSection.set("completed", progress.completed());
        }
        section.set("completedQuests", profile.completedQuests().stream().toList());
        ConfigurationSection factions = section.createSection("factionRep");
        for (Map.Entry<String, Integer> entry : profile.factionRep().entrySet()) {
            factions.set(entry.getKey(), entry.getValue());
        }

        ConfigurationSection cooldowns = section.createSection("skillCooldowns");
        for (Map.Entry<String, Long> entry : profile.skillCooldowns().entrySet()) {
            cooldowns.set(entry.getKey(), entry.getValue());
        }
        save();
    }

    public void saveAll() {
        for (PlayerProfile profile : profiles.values()) {
            saveProfile(profile);
        }
        save();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save players.yml: " + e.getMessage());
        }
    }
}

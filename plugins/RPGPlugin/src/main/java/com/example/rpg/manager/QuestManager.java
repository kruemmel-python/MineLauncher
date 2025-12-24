package com.example.rpg.manager;

import com.example.rpg.model.Quest;
import com.example.rpg.model.QuestReward;
import com.example.rpg.model.QuestStep;
import com.example.rpg.model.QuestStepType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class QuestManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Quest> quests = new HashMap<>();

    public QuestManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "quests.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public Quest getQuest(String id) {
        return quests.get(id);
    }

    public Map<String, Quest> quests() {
        return quests;
    }

    public void saveQuest(Quest quest) {
        ConfigurationSection section = config.createSection(quest.id());
        section.set("name", quest.name());
        section.set("description", quest.description());
        section.set("repeatable", quest.repeatable());
        section.set("minLevel", quest.minLevel());
        List<Map<String, Object>> steps = new ArrayList<>();
        for (QuestStep step : quest.steps()) {
            Map<String, Object> map = new HashMap<>();
            map.put("type", step.type().name());
            map.put("target", step.target());
            map.put("amount", step.amount());
            steps.add(map);
        }
        section.set("steps", steps);
        QuestReward reward = quest.reward();
        section.set("reward.xp", reward.xp());
        section.set("reward.skillPoints", reward.skillPoints());
        section.set("reward.factionRep", reward.factionRep());
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (Quest quest : quests.values()) {
            saveQuest(quest);
        }
        save();
    }

    private void load() {
        quests.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Quest quest = new Quest(id);
            quest.setName(section.getString("name", id));
            quest.setDescription(section.getString("description", ""));
            quest.setRepeatable(section.getBoolean("repeatable", false));
            quest.setMinLevel(section.getInt("minLevel", 1));
            List<QuestStep> steps = new ArrayList<>();
            for (Map<?, ?> map : section.getMapList("steps")) {
                String typeName = String.valueOf(map.getOrDefault("type", "KILL"));
                String target = String.valueOf(map.getOrDefault("target", "ZOMBIE"));
                int amount = Integer.parseInt(String.valueOf(map.getOrDefault("amount", 1)));
                QuestStepType type = QuestStepType.valueOf(typeName);
                steps.add(new QuestStep(type, target, amount));
            }
            quest.setSteps(steps);
            QuestReward reward = new QuestReward();
            reward.setXp(section.getInt("reward.xp", 50));
            reward.setSkillPoints(section.getInt("reward.skillPoints", 1));
            ConfigurationSection factionRep = section.getConfigurationSection("reward.factionRep");
            if (factionRep != null) {
                Map<String, Integer> rep = new HashMap<>();
                for (String faction : factionRep.getKeys(false)) {
                    rep.put(faction, factionRep.getInt(faction));
                }
                reward.setFactionRep(rep);
            }
            quest.setReward(reward);
            quests.put(id, quest);
        }
    }

    private void seedDefaults() {
        Quest quest = new Quest("starter_hunt");
        quest.setName("Wolfsplage");
        quest.setDescription("Jage 3 Wölfe und kehre zurück.");
        quest.setRepeatable(false);
        quest.setMinLevel(1);
        quest.setSteps(List.of(new QuestStep(QuestStepType.KILL, "WOLF", 3)));
        QuestReward reward = new QuestReward();
        reward.setXp(120);
        reward.setSkillPoints(1);
        quest.setReward(reward);
        quests.put(quest.id(), quest);
        saveAll();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save quests.yml: " + e.getMessage());
        }
    }
}

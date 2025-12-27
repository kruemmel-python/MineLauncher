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
import org.bukkit.configuration.InvalidConfigurationException;
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
        section.set("requiredEvent", quest.requiredEvent());
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

    public void reload() {
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().warning("Failed to reload quests.yml: " + e.getMessage());
        }
        load();
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
            quest.setRequiredEvent(section.getString("requiredEvent", null));
            List<QuestStep> steps = new ArrayList<>();
            for (Map<?, ?> raw : section.getMapList("steps")) {
                Object typeValue = raw.containsKey("type") ? raw.get("type") : "KILL";
                Object targetValue = raw.containsKey("target") ? raw.get("target") : "ZOMBIE";
                Object amountValue = raw.containsKey("amount") ? raw.get("amount") : 1;
                String typeName = String.valueOf(typeValue);
                String target = String.valueOf(targetValue);
                int amount = Integer.parseInt(String.valueOf(amountValue));
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

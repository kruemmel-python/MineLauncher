package com.example.rpg.manager;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.Quest;
import com.example.rpg.model.QuestStep;
import com.example.rpg.model.QuestStepType;
import com.example.rpg.model.WorldEvent;
import com.example.rpg.util.Text;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class WorldEventManager {
    private final RPGPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, WorldEvent> events = new HashMap<>();
    private final Map<String, Boolean> completed = new HashMap<>();

    public WorldEventManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "events.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public Map<String, WorldEvent> events() {
        return events;
    }

    public WorldEvent getEvent(String id) {
        return events.get(id);
    }

    public boolean isCompleted(String id) {
        return completed.getOrDefault(id, false);
    }

    public void startEvent(String id) {
        WorldEvent event = events.get(id);
        if (event == null) {
            return;
        }
        event.setActive(true);
        event.progress().clear();
        event.participants().clear();
        saveEvent(event);
        broadcast("<gold>Welt-Event gestartet:</gold> <white>" + event.name());
    }

    public void stopEvent(String id) {
        WorldEvent event = events.get(id);
        if (event == null) {
            return;
        }
        event.setActive(false);
        saveEvent(event);
        broadcast("<yellow>Welt-Event beendet:</yellow> <white>" + event.name());
    }

    public void trackParticipation(Player player, WorldEvent event) {
        event.participants().add(player.getUniqueId());
    }

    public void increment(WorldEvent event, int stepIndex, int amount) {
        int current = event.progress().getOrDefault(stepIndex, 0);
        int updated = current + amount;
        event.progress().put(stepIndex, updated);
        saveEvent(event);
        checkCompletion(event);
    }

    public void handleKill(Player player, String mobType, String zoneId) {
        for (WorldEvent event : activeEvents(zoneId)) {
            for (int i = 0; i < event.steps().size(); i++) {
                QuestStep step = event.steps().get(i);
                if (step.type() == QuestStepType.KILL && step.target().equalsIgnoreCase(mobType)) {
                    trackParticipation(player, event);
                    increment(event, i, 1);
                }
                if (step.type() == QuestStepType.DEFEND && step.target().equalsIgnoreCase(zoneId)) {
                    trackParticipation(player, event);
                    increment(event, i, 1);
                }
            }
        }
    }

    public void handleExplore(Player player, String zoneId) {
        for (WorldEvent event : activeEvents(zoneId)) {
            for (int i = 0; i < event.steps().size(); i++) {
                QuestStep step = event.steps().get(i);
                if (step.type() == QuestStepType.EXPLORE && step.target().equalsIgnoreCase(zoneId)) {
                    trackParticipation(player, event);
                    increment(event, i, 1);
                }
                if (step.type() == QuestStepType.ESCORT && step.target().equalsIgnoreCase(zoneId)) {
                    trackParticipation(player, event);
                    increment(event, i, 1);
                }
            }
        }
    }

    public void handleCraft(Player player, String material, String zoneId) {
        for (WorldEvent event : activeEvents(zoneId)) {
            for (int i = 0; i < event.steps().size(); i++) {
                QuestStep step = event.steps().get(i);
                if (step.type() == QuestStepType.CRAFT && step.target().equalsIgnoreCase(material)) {
                    trackParticipation(player, event);
                    increment(event, i, 1);
                }
            }
        }
    }

    public void handleCollect(Player player, String material, String zoneId) {
        for (WorldEvent event : activeEvents(zoneId)) {
            for (int i = 0; i < event.steps().size(); i++) {
                QuestStep step = event.steps().get(i);
                if (step.type() == QuestStepType.COLLECT && step.target().equalsIgnoreCase(material)) {
                    trackParticipation(player, event);
                    increment(event, i, 1);
                }
            }
        }
    }

    public void handleUseItem(Player player, String material, String zoneId) {
        for (WorldEvent event : activeEvents(zoneId)) {
            for (int i = 0; i < event.steps().size(); i++) {
                QuestStep step = event.steps().get(i);
                if (step.type() == QuestStepType.USE_ITEM && step.target().equalsIgnoreCase(material)) {
                    trackParticipation(player, event);
                    increment(event, i, 1);
                }
            }
        }
    }

    private List<WorldEvent> activeEvents(String zoneId) {
        List<WorldEvent> active = new ArrayList<>();
        for (WorldEvent event : events.values()) {
            if (!event.active()) {
                continue;
            }
            if (event.zoneId() != null && zoneId != null && event.zoneId().equalsIgnoreCase(zoneId)) {
                active.add(event);
            }
        }
        return active;
    }

    private void checkCompletion(WorldEvent event) {
        for (int i = 0; i < event.steps().size(); i++) {
            QuestStep step = event.steps().get(i);
            int required = step.amount();
            int current = event.progress().getOrDefault(i, 0);
            if (current < required) {
                return;
            }
        }
        completeEvent(event);
    }

    private void completeEvent(WorldEvent event) {
        event.setActive(false);
        completed.put(event.id(), true);
        saveEvent(event);
        saveCompleted();
        broadcast("<green>Welt-Event abgeschlossen:</green> <white>" + event.name());
        for (UUID participant : event.participants()) {
            Player player = plugin.getServer().getPlayer(participant);
            if (player == null) {
                continue;
            }
            PlayerProfile profile = plugin.playerDataManager().getProfile(player);
            profile.addXp(event.rewardXp());
            profile.setGold(profile.gold() + event.rewardGold());
            event.rewardFactionRep().forEach((faction, amount) ->
                profile.factionRep().put(faction, profile.factionRep().getOrDefault(faction, 0) + amount)
            );
            player.sendMessage(Text.mm("<gold>Event-Belohnung:</gold> +" + event.rewardXp() + " XP, +" + event.rewardGold() + " Gold"));
            for (String questId : event.unlockQuests()) {
                Quest quest = plugin.questManager().getQuest(questId);
                if (quest != null) {
                    quest.setRequiredEvent(event.id());
                    plugin.questManager().saveQuest(quest);
                }
            }
        }
        event.participants().clear();
    }

    public void saveEvent(WorldEvent event) {
        ConfigurationSection section = config.createSection(event.id());
        section.set("name", event.name());
        section.set("zoneId", event.zoneId());
        section.set("active", event.active());
        section.set("reward.xp", event.rewardXp());
        section.set("reward.gold", event.rewardGold());
        section.set("reward.factionRep", event.rewardFactionRep());
        section.set("unlockQuests", event.unlockQuests());
        List<Map<String, Object>> steps = new ArrayList<>();
        for (QuestStep step : event.steps()) {
            Map<String, Object> map = new HashMap<>();
            map.put("type", step.type().name());
            map.put("target", step.target());
            map.put("amount", step.amount());
            steps.add(map);
        }
        section.set("steps", steps);
        section.set("progress", event.progress());
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (WorldEvent event : events.values()) {
            saveEvent(event);
        }
        saveCompleted();
        save();
    }

    private void load() {
        events.clear();
        completed.clear();
        ConfigurationSection completedSection = config.getConfigurationSection("completed");
        if (completedSection != null) {
            for (String id : completedSection.getKeys(false)) {
                completed.put(id, completedSection.getBoolean(id, false));
            }
        }
        for (String id : config.getKeys(false)) {
            if ("completed".equals(id)) {
                continue;
            }
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            WorldEvent event = new WorldEvent(id);
            event.setName(section.getString("name", id));
            event.setZoneId(section.getString("zoneId", null));
            event.setActive(section.getBoolean("active", false));
            event.setRewardXp(section.getInt("reward.xp", 100));
            event.setRewardGold(section.getInt("reward.gold", 50));
            ConfigurationSection repSection = section.getConfigurationSection("reward.factionRep");
            if (repSection != null) {
                for (String faction : repSection.getKeys(false)) {
                    event.rewardFactionRep().put(faction, repSection.getInt(faction));
                }
            }
            event.unlockQuests().addAll(section.getStringList("unlockQuests"));
            for (Map<?, ?> raw : section.getMapList("steps")) {
                String typeName = String.valueOf(raw.getOrDefault("type", "KILL"));
                String target = String.valueOf(raw.getOrDefault("target", "ZOMBIE"));
                int amount = Integer.parseInt(String.valueOf(raw.getOrDefault("amount", 1)));
                event.steps().add(new QuestStep(QuestStepType.valueOf(typeName), target, amount));
            }
            ConfigurationSection progressSection = section.getConfigurationSection("progress");
            if (progressSection != null) {
                for (String key : progressSection.getKeys(false)) {
                    try {
                        int index = Integer.parseInt(key);
                        event.progress().put(index, progressSection.getInt(key));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            events.put(id, event);
        }
    }

    private void saveCompleted() {
        ConfigurationSection section = config.getConfigurationSection("completed");
        if (section == null) {
            section = config.createSection("completed");
        }
        for (Map.Entry<String, Boolean> entry : completed.entrySet()) {
            section.set(entry.getKey(), entry.getValue());
        }
    }

    private void broadcast(String message) {
        plugin.getServer().broadcast(Text.mm(message));
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save events.yml: " + e.getMessage());
        }
    }
}

package com.example.rpg.manager;

import com.example.rpg.model.PlayerProfile;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ProfessionManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, ProfessionDefinition> professions = new HashMap<>();

    public ProfessionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "professions.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public int getLevel(PlayerProfile profile, String profession) {
        return profile.professions().getOrDefault(profession + "_level", 1);
    }

    public void setLevel(PlayerProfile profile, String profession, int level) {
        profile.professions().put(profession + "_level", Math.max(1, level));
    }

    public Map<String, Integer> professions(PlayerProfile profile) {
        return profile.professions();
    }

    public int addXp(PlayerProfile profile, String profession, int xp, Player player) {
        int currentXp = profile.professions().getOrDefault(profession + "_xp", 0);
        int newXp = currentXp + Math.max(0, xp);
        profile.professions().put(profession + "_xp", newXp);
        int level = profile.professions().getOrDefault(profession + "_level", 1);
        int oldLevel = level;
        int threshold = level * 100;
        while (newXp >= threshold) {
            newXp -= threshold;
            level++;
            threshold = level * 100;
        }
        profile.professions().put(profession + "_level", level);
        profile.professions().put(profession + "_xp", newXp);
        if (player != null && level > oldLevel) {
            fireLevelRewards(profession, level, player);
        }
        return level;
    }

    public int xpForMaterial(String profession, String material) {
        ProfessionDefinition definition = professions.get(profession);
        if (definition == null) {
            return 0;
        }
        return definition.xpSources().getOrDefault(material, 0);
    }

    public int requiredLevelForCraft(String profession, String resultMaterial) {
        ProfessionDefinition definition = professions.get(profession);
        if (definition == null) {
            return 0;
        }
        return definition.craftRequirements().getOrDefault(resultMaterial, 0);
    }

    public Map<String, ProfessionDefinition> definitions() {
        return professions;
    }

    private void fireLevelRewards(String profession, int level, Player player) {
        ProfessionDefinition definition = professions.get(profession);
        if (definition == null) {
            return;
        }
        List<String> commands = definition.levelRewards().get(level);
        if (commands == null) {
            return;
        }
        for (String command : commands) {
            String resolved = command.replace("{player}", player.getName());
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), resolved);
        }
        player.sendMessage(com.example.rpg.util.Text.mm("<gold>Beruf " + definition.displayName()
            + " Level " + level + " erreicht!"));
    }

    private void load() {
        professions.clear();
        ConfigurationSection root = config.getConfigurationSection("professions");
        if (root == null) {
            return;
        }
        for (String id : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            ProfessionDefinition definition = new ProfessionDefinition(id);
            definition.setDisplayName(section.getString("display", id));
            ConfigurationSection xpSources = section.getConfigurationSection("xpSources");
            if (xpSources != null) {
                for (String material : xpSources.getKeys(false)) {
                    definition.xpSources().put(material, xpSources.getInt(material, 0));
                }
            }
            ConfigurationSection craftReq = section.getConfigurationSection("craftRequirements");
            if (craftReq != null) {
                for (String material : craftReq.getKeys(false)) {
                    definition.craftRequirements().put(material, craftReq.getInt(material, 0));
                }
            }
            ConfigurationSection rewards = section.getConfigurationSection("levelRewards");
            if (rewards != null) {
                for (String levelKey : rewards.getKeys(false)) {
                    try {
                        int lvl = Integer.parseInt(levelKey);
                        definition.levelRewards().put(lvl, rewards.getStringList(levelKey));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            professions.put(id, definition);
        }
    }

    private void seedDefaults() {
        config.set("professions.mining.display", "Bergbau");
        config.set("professions.mining.xpSources.COAL_ORE", 5);
        config.set("professions.mining.xpSources.IRON_ORE", 8);
        config.set("professions.mining.xpSources.DIAMOND_ORE", 15);
        config.set("professions.mining.levelRewards.5", List.of("give {player} iron_pickaxe 1"));

        config.set("professions.herbalism.display", "Kräuterkunde");
        config.set("professions.herbalism.xpSources.WHEAT", 4);
        config.set("professions.herbalism.xpSources.CARROTS", 4);
        config.set("professions.herbalism.xpSources.NETHER_WART", 8);
        config.set("professions.herbalism.levelRewards.5", List.of("give {player} golden_apple 1"));

        config.set("professions.blacksmithing.display", "Schmiedekunst");
        config.set("professions.blacksmithing.xpSources.IRON_SWORD", 10);
        config.set("professions.blacksmithing.xpSources.DIAMOND_SWORD", 20);
        config.set("professions.blacksmithing.craftRequirements.IRON_SWORD", 3);
        config.set("professions.blacksmithing.craftRequirements.DIAMOND_SWORD", 6);
        config.set("professions.blacksmithing.levelRewards.5", List.of("give {player} anvil 1"));
        save();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save professions.yml: " + e.getMessage());
        }
    }

    public static class ProfessionDefinition {
        private final String id;
        private String displayName;
        private final Map<String, Integer> xpSources = new HashMap<>();
        private final Map<String, Integer> craftRequirements = new HashMap<>();
        private final Map<Integer, List<String>> levelRewards = new HashMap<>();

        public ProfessionDefinition(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }

        public String displayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public Map<String, Integer> xpSources() {
            return xpSources;
        }

        public Map<String, Integer> craftRequirements() {
            return craftRequirements;
        }

        public Map<Integer, List<String>> levelRewards() {
            return levelRewards;
        }
    }
}

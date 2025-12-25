package com.example.rpg.manager;

import com.example.rpg.model.ClassDefinition;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ClassManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, ClassDefinition> classes = new HashMap<>();

    public ClassManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "classes.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public ClassDefinition getClass(String id) {
        return classes.get(id);
    }

    public Map<String, ClassDefinition> classes() {
        return classes;
    }

    public void saveClass(ClassDefinition definition) {
        ConfigurationSection section = config.createSection(definition.id());
        section.set("name", definition.name());
        section.set("startSkills", definition.startSkills());
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (ClassDefinition definition : classes.values()) {
            saveClass(definition);
        }
        save();
    }

    private void load() {
        classes.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            ClassDefinition definition = new ClassDefinition(id);
            definition.setName(section.getString("name", id));
            definition.setStartSkills(section.getStringList("startSkills"));
            classes.put(id, definition);
        }
    }

    private void seedDefaults() {
        ClassDefinition warrior = new ClassDefinition("warrior");
        warrior.setName("Krieger");
        warrior.setStartSkills(List.of("taunt"));

        ClassDefinition ranger = new ClassDefinition("ranger");
        ranger.setName("Ranger");
        ranger.setStartSkills(List.of("dash"));

        ClassDefinition mage = new ClassDefinition("mage");
        mage.setName("Magier");
        mage.setStartSkills(List.of("heal"));

        classes.put(warrior.id(), warrior);
        classes.put(ranger.id(), ranger);
        classes.put(mage.id(), mage);
        saveAll();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save classes.yml: " + e.getMessage());
        }
    }
}

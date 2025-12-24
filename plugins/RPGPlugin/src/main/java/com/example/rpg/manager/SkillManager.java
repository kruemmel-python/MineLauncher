package com.example.rpg.manager;

import com.example.rpg.model.Skill;
import com.example.rpg.model.SkillType;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class SkillManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Skill> skills = new HashMap<>();

    public SkillManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "skills.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public Skill getSkill(String id) {
        return skills.get(id);
    }

    public Map<String, Skill> skills() {
        return skills;
    }

    public void saveSkill(Skill skill) {
        ConfigurationSection section = config.createSection(skill.id());
        section.set("name", skill.name());
        section.set("type", skill.type().name());
        section.set("cooldown", skill.cooldown());
        section.set("manaCost", skill.manaCost());
        section.set("effect", skill.effect());
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (Skill skill : skills.values()) {
            saveSkill(skill);
        }
        save();
    }

    private void load() {
        skills.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Skill skill = new Skill(id);
            skill.setName(section.getString("name", id));
            skill.setType(SkillType.valueOf(section.getString("type", "ACTIVE")));
            skill.setCooldown(section.getInt("cooldown", 10));
            skill.setManaCost(section.getInt("manaCost", 20));
            skill.setEffect(section.getString("effect", "dash"));
            skills.put(id, skill);
        }
    }

    private void seedDefaults() {
        Skill dash = new Skill("dash");
        dash.setName("Dash");
        dash.setType(SkillType.ACTIVE);
        dash.setCooldown(10);
        dash.setManaCost(15);
        dash.setEffect("dash");

        Skill heal = new Skill("heal");
        heal.setName("Heal");
        heal.setType(SkillType.ACTIVE);
        heal.setCooldown(20);
        heal.setManaCost(25);
        heal.setEffect("heal");

        Skill taunt = new Skill("taunt");
        taunt.setName("Taunt");
        taunt.setType(SkillType.ACTIVE);
        taunt.setCooldown(30);
        taunt.setManaCost(20);
        taunt.setEffect("taunt");

        skills.put(dash.id(), dash);
        skills.put(heal.id(), heal);
        skills.put(taunt.id(), taunt);
        saveAll();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save skills.yml: " + e.getMessage());
        }
    }
}

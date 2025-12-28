package com.example.rpg.manager;

import com.example.rpg.model.Skill;
import com.example.rpg.model.SkillCategory;
import com.example.rpg.model.SkillType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class SkillManager {
    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration config;
    private final Map<String, Skill> skills = new HashMap<>();

    public SkillManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "skills.yml");
        if (!file.exists()) {
            plugin.saveResource("skills.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
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
        section.set("category", skill.category().name());
        section.set("cooldown", skill.cooldown());
        section.set("manaCost", skill.manaCost());
        section.set("class", skill.classId());
        section.set("minLevel", skill.minLevel());
        section.set("maxRank", skill.maxRank());
        section.set("scaling", skill.scaling());
        section.set("tags", skill.tags());
        section.set("effects", serializeEffects(skill.effects()));
        section.set("parent", skill.requiredSkill());
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
            skill.setCategory(SkillCategory.valueOf(section.getString("category", "ATTACK")));
            skill.setCooldown(section.getInt("cooldown", 10));
            skill.setManaCost(section.getInt("manaCost", 20));
            skill.setClassId(section.getString("class", null));
            skill.setMinLevel(section.getInt("minLevel", 1));
            skill.setMaxRank(section.getInt("maxRank", 1));
            skill.setScaling(new HashMap<>(section.getConfigurationSection("scaling") != null
                ? section.getConfigurationSection("scaling").getValues(true)
                : Map.of()));
            skill.setTags(section.getStringList("tags"));
            String parent = section.getString("parent", null);
            if (parent == null) {
                parent = section.getString("requiredSkill", null);
            }
            skill.setRequiredSkill(parent);
            skill.setEffects(loadEffects(section));
            skills.put(id, skill);
        }
    }

    private List<Map<String, Object>> serializeEffects(List<com.example.rpg.skill.SkillEffectConfig> effects) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (com.example.rpg.skill.SkillEffectConfig config : effects) {
            Map<String, Object> map = new HashMap<>();
            map.put("type", config.type().name());
            map.put("params", config.params());
            list.add(map);
        }
        return list;
    }

    private List<com.example.rpg.skill.SkillEffectConfig> loadEffects(ConfigurationSection section) {
        List<com.example.rpg.skill.SkillEffectConfig> effects = new ArrayList<>();
        for (Map<?, ?> raw : section.getMapList("effects")) {
            Object typeValue = raw.containsKey("type") ? raw.get("type") : "HEAL";
            com.example.rpg.skill.SkillEffectType type = com.example.rpg.skill.SkillEffectType.valueOf(String.valueOf(typeValue));
            Map<String, Object> params = new HashMap<>();
            Object paramsValue = raw.get("params");
            if (paramsValue instanceof Map<?, ?> paramMap) {
                for (Map.Entry<?, ?> entry : paramMap.entrySet()) {
                    params.put(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
            effects.add(new com.example.rpg.skill.SkillEffectConfig(type, params));
        }
        if (effects.isEmpty() && section.contains("effect")) {
            String legacy = section.getString("effect", "");
            effects.add(mapLegacyEffect(legacy));
        }
        return effects;
    }

    private com.example.rpg.skill.SkillEffectConfig mapLegacyEffect(String legacy) {
        if (legacy == null) {
            return new com.example.rpg.skill.SkillEffectConfig(com.example.rpg.skill.SkillEffectType.HEAL, Map.of("amount", 4));
        }
        return switch (legacy) {
            case "heal_small" -> effectConfig("HEAL", Map.of("amount", 4));
            case "heal_medium" -> effectConfig("HEAL", Map.of("amount", 8));
            case "heal_large" -> effectConfig("HEAL", Map.of("amount", 12));
            case "fireball" -> effectConfig("PROJECTILE", Map.of("type", "SMALL_FIREBALL"));
            case "frostbolt" -> effectConfig("PROJECTILE", Map.of("type", "SNOWBALL"));
            case "arcane_blast" -> effectConfig("DAMAGE", Map.of("amount", 6, "radius", 5, "maxTargets", 10));
            case "power_strike" -> effectConfig("DAMAGE", Map.of("amount", 8, "radius", 3, "maxTargets", 1));
            case "whirlwind" -> effectConfig("DAMAGE", Map.of("amount", 5, "radius", 4, "maxTargets", 10));
            case "execute" -> effectConfig("DAMAGE", Map.of("amount", 12, "radius", 3, "maxTargets", 1));
            case "dash" -> effectConfig("VELOCITY", Map.of("forward", 1.2, "up", 0.3, "add", false));
            case "taunt" -> effectConfig("AGGRO", Map.of("radius", 8));
            default -> effectConfig("HEAL", Map.of("amount", 4));
        };
    }

    private com.example.rpg.skill.SkillEffectConfig effectConfig(String type, Map<String, Object> params) {
        return new com.example.rpg.skill.SkillEffectConfig(
            com.example.rpg.skill.SkillEffectType.valueOf(type),
            params
        );
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save skills.yml: " + e.getMessage());
        }
    }
}

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
        section.set("category", skill.category().name());
        section.set("cooldown", skill.cooldown());
        section.set("manaCost", skill.manaCost());
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
            String parent = section.getString("parent", null);
            if (parent == null) {
                parent = section.getString("requiredSkill", null);
            }
            skill.setRequiredSkill(parent);
            skill.setEffects(loadEffects(section));
            skills.put(id, skill);
        }
    }

    private void seedDefaults() {
        Skill healPulse = new Skill("heal_pulse");
        healPulse.setName("Heilpuls");
        healPulse.setType(SkillType.ACTIVE);
        healPulse.setCategory(SkillCategory.HEALING);
        healPulse.setCooldown(20);
        healPulse.setManaCost(20);
        healPulse.setEffects(List.of(effectConfig("HEAL", Map.of("amount", 4))));

        Skill greaterHeal = new Skill("greater_heal");
        greaterHeal.setName("Große Heilung");
        greaterHeal.setType(SkillType.ACTIVE);
        greaterHeal.setCategory(SkillCategory.HEALING);
        greaterHeal.setCooldown(30);
        greaterHeal.setManaCost(35);
        greaterHeal.setEffects(List.of(effectConfig("HEAL", Map.of("amount", 8))));
        greaterHeal.setRequiredSkill("heal_pulse");

        Skill divineBlessing = new Skill("divine_blessing");
        divineBlessing.setName("Segen");
        divineBlessing.setType(SkillType.ACTIVE);
        divineBlessing.setCategory(SkillCategory.HEALING);
        divineBlessing.setCooldown(45);
        divineBlessing.setManaCost(45);
        divineBlessing.setEffects(List.of(effectConfig("HEAL", Map.of("amount", 12)),
            effectConfig("SOUND", Map.of("sound", "BLOCK_BEACON_POWER_SELECT", "volume", 1.0, "pitch", 1.0))));
        divineBlessing.setRequiredSkill("greater_heal");

        Skill emberShot = new Skill("ember_shot");
        emberShot.setName("Flammenstoß");
        emberShot.setType(SkillType.ACTIVE);
        emberShot.setCategory(SkillCategory.MAGIC);
        emberShot.setCooldown(12);
        emberShot.setManaCost(18);
        emberShot.setEffects(List.of(effectConfig("PROJECTILE", Map.of("type", "SMALL_FIREBALL")),
            effectConfig("SOUND", Map.of("sound", "ENTITY_BLAZE_SHOOT", "volume", 1.0, "pitch", 1.2))));

        Skill frostBolt = new Skill("frost_bolt");
        frostBolt.setName("Frostbolzen");
        frostBolt.setType(SkillType.ACTIVE);
        frostBolt.setCategory(SkillCategory.MAGIC);
        frostBolt.setCooldown(18);
        frostBolt.setManaCost(25);
        frostBolt.setEffects(List.of(effectConfig("PROJECTILE", Map.of("type", "SNOWBALL")),
            effectConfig("POTION", Map.of("type", "SLOW", "duration", 60, "amplifier", 1, "radius", 6)),
            effectConfig("SOUND", Map.of("sound", "BLOCK_GLASS_BREAK", "volume", 0.8, "pitch", 1.4))));
        frostBolt.setRequiredSkill("ember_shot");

        Skill arcaneBurst = new Skill("arcane_burst");
        arcaneBurst.setName("Arkane Explosion");
        arcaneBurst.setType(SkillType.ACTIVE);
        arcaneBurst.setCategory(SkillCategory.MAGIC);
        arcaneBurst.setCooldown(30);
        arcaneBurst.setManaCost(35);
        arcaneBurst.setEffects(List.of(effectConfig("DAMAGE", Map.of("amount", 6, "radius", 5, "maxTargets", 10)),
            effectConfig("SOUND", Map.of("sound", "ENTITY_ILLUSIONER_CAST_SPELL", "volume", 1.0, "pitch", 1.2))));
        arcaneBurst.setRequiredSkill("frost_bolt");

        Skill powerStrike = new Skill("power_strike");
        powerStrike.setName("Machtstoß");
        powerStrike.setType(SkillType.ACTIVE);
        powerStrike.setCategory(SkillCategory.ATTACK);
        powerStrike.setCooldown(8);
        powerStrike.setManaCost(10);
        powerStrike.setEffects(List.of(effectConfig("DAMAGE", Map.of("amount", 8, "radius", 3, "maxTargets", 1)),
            effectConfig("SOUND", Map.of("sound", "ENTITY_PLAYER_ATTACK_STRONG", "volume", 1.0, "pitch", 1.0))));

        Skill whirlwind = new Skill("whirlwind");
        whirlwind.setName("Wirbelwind");
        whirlwind.setType(SkillType.ACTIVE);
        whirlwind.setCategory(SkillCategory.ATTACK);
        whirlwind.setCooldown(20);
        whirlwind.setManaCost(20);
        whirlwind.setEffects(List.of(effectConfig("DAMAGE", Map.of("amount", 5, "radius", 4, "maxTargets", 10)),
            effectConfig("SOUND", Map.of("sound", "ENTITY_PLAYER_ATTACK_SWEEP", "volume", 1.0, "pitch", 0.8))));
        whirlwind.setRequiredSkill("power_strike");

        Skill execute = new Skill("execute");
        execute.setName("Hinrichtung");
        execute.setType(SkillType.ACTIVE);
        execute.setCategory(SkillCategory.ATTACK);
        execute.setCooldown(35);
        execute.setManaCost(30);
        execute.setEffects(List.of(effectConfig("DAMAGE", Map.of("amount", 12, "radius", 3, "maxTargets", 1)),
            effectConfig("SOUND", Map.of("sound", "ENTITY_WITHER_SKELETON_HURT", "volume", 1.0, "pitch", 0.9))));
        execute.setRequiredSkill("whirlwind");

        Skill shieldWall = new Skill("shield_wall");
        shieldWall.setName("Schildwall");
        shieldWall.setType(SkillType.ACTIVE);
        shieldWall.setCategory(SkillCategory.DEFENSE);
        shieldWall.setCooldown(25);
        shieldWall.setManaCost(15);
        shieldWall.setEffects(List.of(effectConfig("POTION", Map.of("type", "DAMAGE_RESISTANCE", "duration", 120, "amplifier", 0)),
            effectConfig("SOUND", Map.of("sound", "ITEM_SHIELD_BLOCK", "volume", 1.0, "pitch", 1.0))));

        Skill fortify = new Skill("fortify");
        fortify.setName("Bollwerk");
        fortify.setType(SkillType.ACTIVE);
        fortify.setCategory(SkillCategory.DEFENSE);
        fortify.setCooldown(35);
        fortify.setManaCost(25);
        fortify.setEffects(List.of(effectConfig("POTION", Map.of("type", "DAMAGE_RESISTANCE", "duration", 200, "amplifier", 1)),
            effectConfig("POTION", Map.of("type", "ABSORPTION", "duration", 200, "amplifier", 1)),
            effectConfig("SOUND", Map.of("sound", "BLOCK_ANVIL_USE", "volume", 0.7, "pitch", 1.0))));
        fortify.setRequiredSkill("shield_wall");

        Skill deflect = new Skill("deflect");
        deflect.setName("Abwehrhaltung");
        deflect.setType(SkillType.ACTIVE);
        deflect.setCategory(SkillCategory.DEFENSE);
        deflect.setCooldown(45);
        deflect.setManaCost(30);
        deflect.setEffects(List.of(effectConfig("POTION", Map.of("type", "FIRE_RESISTANCE", "duration", 200, "amplifier", 0)),
            effectConfig("POTION", Map.of("type", "DAMAGE_RESISTANCE", "duration", 200, "amplifier", 0)),
            effectConfig("SOUND", Map.of("sound", "ITEM_SHIELD_BLOCK", "volume", 1.0, "pitch", 1.2))));
        deflect.setRequiredSkill("fortify");

        Skill miningFocus = new Skill("mining_focus");
        miningFocus.setName("Bergbau-Fokus");
        miningFocus.setType(SkillType.ACTIVE);
        miningFocus.setCategory(SkillCategory.PROFESSION);
        miningFocus.setCooldown(60);
        miningFocus.setManaCost(15);
        miningFocus.setEffects(List.of(effectConfig("POTION", Map.of("type", "FAST_DIGGING", "duration", 300, "amplifier", 1)),
            effectConfig("XP", Map.of("amount", 10)),
            effectConfig("SOUND", Map.of("sound", "BLOCK_STONE_HIT", "volume", 0.8, "pitch", 1.0))));

        Skill craftingInsight = new Skill("crafting_insight");
        craftingInsight.setName("Handwerkskunst");
        craftingInsight.setType(SkillType.ACTIVE);
        craftingInsight.setCategory(SkillCategory.PROFESSION);
        craftingInsight.setCooldown(60);
        craftingInsight.setManaCost(20);
        craftingInsight.setEffects(List.of(effectConfig("POTION", Map.of("type", "LUCK", "duration", 300, "amplifier", 1)),
            effectConfig("XP", Map.of("amount", 10)),
            effectConfig("SOUND", Map.of("sound", "BLOCK_ANVIL_PLACE", "volume", 0.8, "pitch", 1.1))));
        craftingInsight.setRequiredSkill("mining_focus");

        Skill alchemyMastery = new Skill("alchemy_mastery");
        alchemyMastery.setName("Alchemie-Meister");
        alchemyMastery.setType(SkillType.ACTIVE);
        alchemyMastery.setCategory(SkillCategory.PROFESSION);
        alchemyMastery.setCooldown(90);
        alchemyMastery.setManaCost(30);
        alchemyMastery.setEffects(List.of(effectConfig("POTION", Map.of("type", "REGENERATION", "duration", 120, "amplifier", 0)),
            effectConfig("XP", Map.of("amount", 15)),
            effectConfig("SOUND", Map.of("sound", "BLOCK_BREWING_STAND_BREW", "volume", 0.8, "pitch", 1.0))));
        alchemyMastery.setRequiredSkill("crafting_insight");

        skills.put(healPulse.id(), healPulse);
        skills.put(greaterHeal.id(), greaterHeal);
        skills.put(divineBlessing.id(), divineBlessing);
        skills.put(emberShot.id(), emberShot);
        skills.put(frostBolt.id(), frostBolt);
        skills.put(arcaneBurst.id(), arcaneBurst);
        skills.put(powerStrike.id(), powerStrike);
        skills.put(whirlwind.id(), whirlwind);
        skills.put(execute.id(), execute);
        skills.put(shieldWall.id(), shieldWall);
        skills.put(fortify.id(), fortify);
        skills.put(deflect.id(), deflect);
        skills.put(miningFocus.id(), miningFocus);
        skills.put(craftingInsight.id(), craftingInsight);
        skills.put(alchemyMastery.id(), alchemyMastery);
        saveAll();
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

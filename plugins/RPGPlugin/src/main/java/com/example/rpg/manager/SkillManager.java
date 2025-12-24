package com.example.rpg.manager;

import com.example.rpg.model.Skill;
import com.example.rpg.model.SkillCategory;
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
        section.set("category", skill.category().name());
        section.set("cooldown", skill.cooldown());
        section.set("manaCost", skill.manaCost());
        section.set("effect", skill.effect());
        section.set("requiredSkill", skill.requiredSkill());
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
            skill.setEffect(section.getString("effect", "dash"));
            skill.setRequiredSkill(section.getString("requiredSkill", null));
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
        healPulse.setEffect("heal_small");

        Skill greaterHeal = new Skill("greater_heal");
        greaterHeal.setName("Große Heilung");
        greaterHeal.setType(SkillType.ACTIVE);
        greaterHeal.setCategory(SkillCategory.HEALING);
        greaterHeal.setCooldown(30);
        greaterHeal.setManaCost(35);
        greaterHeal.setEffect("heal_medium");
        greaterHeal.setRequiredSkill("heal_pulse");

        Skill divineBlessing = new Skill("divine_blessing");
        divineBlessing.setName("Segen");
        divineBlessing.setType(SkillType.ACTIVE);
        divineBlessing.setCategory(SkillCategory.HEALING);
        divineBlessing.setCooldown(45);
        divineBlessing.setManaCost(45);
        divineBlessing.setEffect("heal_large");
        divineBlessing.setRequiredSkill("greater_heal");

        Skill emberShot = new Skill("ember_shot");
        emberShot.setName("Flammenstoß");
        emberShot.setType(SkillType.ACTIVE);
        emberShot.setCategory(SkillCategory.MAGIC);
        emberShot.setCooldown(12);
        emberShot.setManaCost(18);
        emberShot.setEffect("fireball");

        Skill frostBolt = new Skill("frost_bolt");
        frostBolt.setName("Frostbolzen");
        frostBolt.setType(SkillType.ACTIVE);
        frostBolt.setCategory(SkillCategory.MAGIC);
        frostBolt.setCooldown(18);
        frostBolt.setManaCost(25);
        frostBolt.setEffect("frostbolt");
        frostBolt.setRequiredSkill("ember_shot");

        Skill arcaneBurst = new Skill("arcane_burst");
        arcaneBurst.setName("Arkane Explosion");
        arcaneBurst.setType(SkillType.ACTIVE);
        arcaneBurst.setCategory(SkillCategory.MAGIC);
        arcaneBurst.setCooldown(30);
        arcaneBurst.setManaCost(35);
        arcaneBurst.setEffect("arcane_blast");
        arcaneBurst.setRequiredSkill("frost_bolt");

        Skill powerStrike = new Skill("power_strike");
        powerStrike.setName("Machtstoß");
        powerStrike.setType(SkillType.ACTIVE);
        powerStrike.setCategory(SkillCategory.ATTACK);
        powerStrike.setCooldown(8);
        powerStrike.setManaCost(10);
        powerStrike.setEffect("power_strike");

        Skill whirlwind = new Skill("whirlwind");
        whirlwind.setName("Wirbelwind");
        whirlwind.setType(SkillType.ACTIVE);
        whirlwind.setCategory(SkillCategory.ATTACK);
        whirlwind.setCooldown(20);
        whirlwind.setManaCost(20);
        whirlwind.setEffect("whirlwind");
        whirlwind.setRequiredSkill("power_strike");

        Skill execute = new Skill("execute");
        execute.setName("Hinrichtung");
        execute.setType(SkillType.ACTIVE);
        execute.setCategory(SkillCategory.ATTACK);
        execute.setCooldown(35);
        execute.setManaCost(30);
        execute.setEffect("execute");
        execute.setRequiredSkill("whirlwind");

        Skill shieldWall = new Skill("shield_wall");
        shieldWall.setName("Schildwall");
        shieldWall.setType(SkillType.ACTIVE);
        shieldWall.setCategory(SkillCategory.DEFENSE);
        shieldWall.setCooldown(25);
        shieldWall.setManaCost(15);
        shieldWall.setEffect("shield_wall");

        Skill fortify = new Skill("fortify");
        fortify.setName("Bollwerk");
        fortify.setType(SkillType.ACTIVE);
        fortify.setCategory(SkillCategory.DEFENSE);
        fortify.setCooldown(35);
        fortify.setManaCost(25);
        fortify.setEffect("fortify");
        fortify.setRequiredSkill("shield_wall");

        Skill deflect = new Skill("deflect");
        deflect.setName("Abwehrhaltung");
        deflect.setType(SkillType.ACTIVE);
        deflect.setCategory(SkillCategory.DEFENSE);
        deflect.setCooldown(45);
        deflect.setManaCost(30);
        deflect.setEffect("deflect");
        deflect.setRequiredSkill("fortify");

        Skill miningFocus = new Skill("mining_focus");
        miningFocus.setName("Bergbau-Fokus");
        miningFocus.setType(SkillType.ACTIVE);
        miningFocus.setCategory(SkillCategory.PROFESSION);
        miningFocus.setCooldown(60);
        miningFocus.setManaCost(15);
        miningFocus.setEffect("mining_boost");

        Skill craftingInsight = new Skill("crafting_insight");
        craftingInsight.setName("Handwerkskunst");
        craftingInsight.setType(SkillType.ACTIVE);
        craftingInsight.setCategory(SkillCategory.PROFESSION);
        craftingInsight.setCooldown(60);
        craftingInsight.setManaCost(20);
        craftingInsight.setEffect("crafting_boost");
        craftingInsight.setRequiredSkill("mining_focus");

        Skill alchemyMastery = new Skill("alchemy_mastery");
        alchemyMastery.setName("Alchemie-Meister");
        alchemyMastery.setType(SkillType.ACTIVE);
        alchemyMastery.setCategory(SkillCategory.PROFESSION);
        alchemyMastery.setCooldown(90);
        alchemyMastery.setManaCost(30);
        alchemyMastery.setEffect("alchemy_boost");
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

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save skills.yml: " + e.getMessage());
        }
    }
}

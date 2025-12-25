package com.example.rpg.model;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class PlayerProfile {
    private final UUID uuid;
    private int level = 1;
    private int xp = 0;
    private int skillPoints = 0;
    private int mana = 100;
    private int maxMana = 100;
    private String classId;
    private final Map<RPGStat, Integer> stats = new EnumMap<>(RPGStat.class);
    private final Map<String, Integer> learnedSkills = new HashMap<>();
    private final Map<String, QuestProgress> activeQuests = new HashMap<>();
    private final Set<String> completedQuests = new HashSet<>();
    private final Map<String, Integer> factionRep = new HashMap<>();
    /**
     * Skill-Cooldowns persistent: skillId -> lastUseMillis
     */
    private final Map<String, Long> skillCooldowns = new HashMap<>();
    private final Map<Integer, String> skillBindings = new HashMap<>();
    private int gold = 0;

    public PlayerProfile(UUID uuid) {
        this.uuid = uuid;
        for (RPGStat stat : RPGStat.values()) {
            stats.put(stat, 5);
        }
    }

    public UUID uuid() {
        return uuid;
    }

    public int level() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int xp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int skillPoints() {
        return skillPoints;
    }

    public void setSkillPoints(int skillPoints) {
        this.skillPoints = skillPoints;
    }

    public int mana() {
        return mana;
    }

    public void setMana(int mana) {
        this.mana = mana;
    }

    public int maxMana() {
        return maxMana;
    }

    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;
    }

    public String classId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public Map<RPGStat, Integer> stats() {
        return stats;
    }

    public Map<String, Integer> learnedSkills() {
        return learnedSkills;
    }

    public Map<String, QuestProgress> activeQuests() {
        return activeQuests;
    }

    public Set<String> completedQuests() {
        return completedQuests;
    }

    public Map<String, Integer> factionRep() {
        return factionRep;
    }

    public Map<String, Long> skillCooldowns() {
        return skillCooldowns;
    }

    public Map<Integer, String> skillBindings() {
        return skillBindings;
    }

    public int gold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = Math.max(0, gold);
    }

    public void addXp(int amount) {
        xp += amount;
        while (xp >= xpNeeded()) {
            xp -= xpNeeded();
            level++;
            skillPoints += 2;
        }
    }

    public int xpNeeded() {
        return 100 + (level - 1) * 50;
    }

    public void applyAttributes(Player player) {
        int strength = stats.getOrDefault(RPGStat.STRENGTH, 5);
        int dex = stats.getOrDefault(RPGStat.DEXTERITY, 5);
        int con = stats.getOrDefault(RPGStat.CONSTITUTION, 5);
        int intel = stats.getOrDefault(RPGStat.INTELLIGENCE, 5);

        if (player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(1.0 + strength * 0.2);
        }
        if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0 + con * 0.8);
        }
        if (player.getAttribute(Attribute.GENERIC_ATTACK_SPEED) != null) {
            player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(4.0 + dex * 0.05);
        }
        if (player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1 + dex * 0.002);
        }
        maxMana = 100 + intel * 5;
        mana = Math.min(mana, maxMana);
    }
}

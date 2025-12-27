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
    private final Map<String, Integer> professions = new HashMap<>();
    private String guildId;
    private int elo = 1000;
    private String dungeonRole = "DPS";
    private String homeWorld;
    private double homeX;
    private double homeY;
    private double homeZ;
    private final Map<String, Integer> housingUpgrades = new HashMap<>();
    private String lastSkillId;
    private long lastSkillTime;
    private final Set<String> cosmetics = new HashSet<>();
    private String title;

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

    public Map<String, Integer> professions() {
        return professions;
    }

    public String guildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public int elo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = Math.max(0, elo);
    }

    public String dungeonRole() {
        return dungeonRole;
    }

    public void setDungeonRole(String dungeonRole) {
        this.dungeonRole = dungeonRole;
    }

    public String homeWorld() {
        return homeWorld;
    }

    public void setHome(String world, double x, double y, double z) {
        this.homeWorld = world;
        this.homeX = x;
        this.homeY = y;
        this.homeZ = z;
    }

    public double homeX() {
        return homeX;
    }

    public double homeY() {
        return homeY;
    }

    public double homeZ() {
        return homeZ;
    }

    public Map<String, Integer> housingUpgrades() {
        return housingUpgrades;
    }

    public String lastSkillId() {
        return lastSkillId;
    }

    public void setLastSkill(String skillId, long time) {
        this.lastSkillId = skillId;
        this.lastSkillTime = time;
    }

    public long lastSkillTime() {
        return lastSkillTime;
    }

    public Set<String> cosmetics() {
        return cosmetics;
    }

    public String title() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public void applyAttributes(Player player, com.example.rpg.manager.ItemStatManager itemStatManager,
                                com.example.rpg.manager.ClassManager classManager) {
        Map<RPGStat, Integer> totalStats = totalStats(player, itemStatManager, classManager);
        int strength = totalStats.getOrDefault(RPGStat.STRENGTH, 5);
        int dex = totalStats.getOrDefault(RPGStat.DEXTERITY, 5);
        int con = totalStats.getOrDefault(RPGStat.CONSTITUTION, 5);
        int intel = totalStats.getOrDefault(RPGStat.INTELLIGENCE, 5);

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

    public Map<RPGStat, Integer> totalStats(Player player, com.example.rpg.manager.ItemStatManager itemStatManager,
                                            com.example.rpg.manager.ClassManager classManager) {
        Map<RPGStat, Integer> totals = new java.util.EnumMap<>(RPGStat.class);
        for (RPGStat stat : RPGStat.values()) {
            totals.put(stat, stats.getOrDefault(stat, 5));
        }
        Map<RPGStat, Integer> gear = itemStatManager.collectStatBonuses(player);
        for (Map.Entry<RPGStat, Integer> entry : gear.entrySet()) {
            totals.put(entry.getKey(), totals.getOrDefault(entry.getKey(), 0) + entry.getValue());
        }
        Map<RPGStat, Integer> classBonus = classManager.classBonuses(classId);
        for (Map.Entry<RPGStat, Integer> entry : classBonus.entrySet()) {
            totals.put(entry.getKey(), totals.getOrDefault(entry.getKey(), 0) + entry.getValue());
        }
        return totals;
    }
}

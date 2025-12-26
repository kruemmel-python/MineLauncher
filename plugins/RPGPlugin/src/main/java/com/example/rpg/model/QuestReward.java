package com.example.rpg.model;

import java.util.HashMap;
import java.util.Map;

public class QuestReward {
    private int xp;
    private int skillPoints;
    private Map<String, Integer> factionRep = new HashMap<>();

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

    public Map<String, Integer> factionRep() {
        return factionRep;
    }

    public void setFactionRep(Map<String, Integer> factionRep) {
        this.factionRep = factionRep;
    }
}

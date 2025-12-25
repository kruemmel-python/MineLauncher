package com.example.rpg.manager;

import com.example.rpg.model.PlayerProfile;
import java.util.Map;

public class ProfessionManager {
    public int getLevel(PlayerProfile profile, String profession) {
        return profile.professions().getOrDefault(profession + "_level", 1);
    }

    public void setLevel(PlayerProfile profile, String profession, int level) {
        profile.professions().put(profession + "_level", Math.max(1, level));
    }

    public Map<String, Integer> professions(PlayerProfile profile) {
        return profile.professions();
    }

    public void addXp(PlayerProfile profile, String profession, int xp) {
        int currentXp = profile.professions().getOrDefault(profession + "_xp", 0);
        int newXp = currentXp + Math.max(0, xp);
        profile.professions().put(profession + "_xp", newXp);
        int level = profile.professions().getOrDefault(profession + "_level", 1);
        int threshold = level * 100;
        while (newXp >= threshold) {
            newXp -= threshold;
            level++;
            threshold = level * 100;
        }
        profile.professions().put(profession + "_level", level);
        profile.professions().put(profession + "_xp", newXp);
    }
}

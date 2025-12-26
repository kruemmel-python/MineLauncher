package com.example.rpg.manager;

import com.example.rpg.model.PlayerProfile;

public class SkillHotbarManager {
    private final PlayerDataManager playerDataManager;

    public SkillHotbarManager(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    public void bindSkill(PlayerProfile profile, int slot, String skillId) {
        profile.skillBindings().put(slot, skillId);
        playerDataManager.saveProfile(profile);
    }

    public String getBinding(PlayerProfile profile, int slot) {
        return profile.skillBindings().get(slot);
    }
}

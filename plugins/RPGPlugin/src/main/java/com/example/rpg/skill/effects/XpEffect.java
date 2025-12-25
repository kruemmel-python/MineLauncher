package com.example.rpg.skill.effects;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.skill.SkillEffect;
import java.util.Map;
import org.bukkit.entity.Player;

public class XpEffect implements SkillEffect {
    @Override
    public void apply(Player player, PlayerProfile profile, Map<String, Object> params) {
        int amount = parseInt(params.getOrDefault("amount", 0));
        if (amount > 0) {
            profile.addXp(amount);
        }
    }

    private int parseInt(Object raw) {
        try {
            return Integer.parseInt(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

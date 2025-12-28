package com.example.rpg.skill.effects;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.RPGStat;
import com.example.rpg.skill.SkillEffect;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class HealEffect implements SkillEffect {
    @Override
    public void apply(Player player, PlayerProfile profile, Map<String, Object> params) {
        double amount = parseDouble(params.getOrDefault("amount", 4));
        Map<RPGStat, Integer> totals = profile.cachedTotals();
        if (totals == null || totals.isEmpty()) {
            totals = new EnumMap<>(profile.stats());
        }
        int intelligence = totals.getOrDefault(RPGStat.INTELLIGENCE, 5);
        amount += intelligence * 0.2;
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null
            ? player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()
            : 20.0;
        double newHealth = Math.min(maxHealth, player.getHealth() + amount);
        player.setHealth(newHealth);
    }

    private double parseDouble(Object raw) {
        try {
            return Double.parseDouble(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}

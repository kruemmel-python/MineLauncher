package com.example.rpg.skill.effects;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.skill.SkillEffect;
import java.util.Map;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

public class AggroEffect implements SkillEffect {
    @Override
    public void apply(Player player, PlayerProfile profile, Map<String, Object> params) {
        double radius = parseDouble(params.getOrDefault("radius", 8));
        player.getNearbyEntities(radius, radius, radius).stream()
            .filter(entity -> entity instanceof Mob)
            .map(entity -> (Mob) entity)
            .forEach(mob -> mob.setTarget(player));
    }

    private double parseDouble(Object raw) {
        try {
            return Double.parseDouble(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 8.0;
        }
    }
}

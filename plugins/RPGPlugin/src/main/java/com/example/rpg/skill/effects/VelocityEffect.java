package com.example.rpg.skill.effects;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.skill.SkillEffect;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class VelocityEffect implements SkillEffect {
    @Override
    public void apply(Player player, PlayerProfile profile, Map<String, Object> params) {
        double forward = parseDouble(params.getOrDefault("forward", 1.2));
        double up = parseDouble(params.getOrDefault("up", 0.3));
        boolean add = parseBoolean(params.getOrDefault("add", false));
        Vector direction = player.getLocation().getDirection().multiply(forward);
        direction.setY(up);
        if (add) {
            player.setVelocity(player.getVelocity().add(direction));
        } else {
            player.setVelocity(direction);
        }
    }

    private double parseDouble(Object raw) {
        try {
            return Double.parseDouble(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private boolean parseBoolean(Object raw) {
        return Boolean.parseBoolean(String.valueOf(raw));
    }
}

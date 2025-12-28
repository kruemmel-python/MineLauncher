package com.example.rpg.skill.effects;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.skill.SkillEffect;
import java.util.Comparator;
import java.util.Map;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class DamageEffect implements SkillEffect {
    @Override
    public void apply(Player player, PlayerProfile profile, Map<String, Object> params) {
        double amount = parseDouble(params.getOrDefault("amount", 4));
        double radius = parseDouble(params.getOrDefault("radius", 0));
        int maxTargets = parseInt(params.getOrDefault("maxTargets", 1));
        if (radius > 0) {
            player.getNearbyEntities(radius, radius, radius).stream()
                .filter(entity -> entity instanceof LivingEntity && !(entity instanceof Player))
                .sorted(Comparator.comparingDouble(entity -> entity.getLocation().distanceSquared(player.getLocation())))
                .limit(Math.max(1, maxTargets))
                .map(entity -> (LivingEntity) entity)
                .forEach(target -> target.damage(amount, player));
            return;
        }

        Entity target = player.getNearbyEntities(3, 2, 3).stream()
            .filter(entity -> entity instanceof LivingEntity && !(entity instanceof Player))
            .min(Comparator.comparingDouble(entity -> entity.getLocation().distanceSquared(player.getLocation())))
            .orElse(null);
        if (target instanceof LivingEntity living) {
            living.damage(amount, player);
        }
    }

    private double parseDouble(Object raw) {
        try {
            return Double.parseDouble(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private int parseInt(Object raw) {
        try {
            return Integer.parseInt(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}

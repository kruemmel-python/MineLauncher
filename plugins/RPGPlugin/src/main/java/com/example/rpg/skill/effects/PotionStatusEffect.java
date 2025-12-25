package com.example.rpg.skill.effects;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.skill.SkillEffect;
import java.util.Map;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionStatusEffect implements SkillEffect {
    @Override
    public void apply(Player player, PlayerProfile profile, Map<String, Object> params) {
        String typeName = String.valueOf(params.getOrDefault("type", "SPEED")).toUpperCase();
        int duration = parseInt(params.getOrDefault("duration", 100));
        int amplifier = parseInt(params.getOrDefault("amplifier", 0));
        double radius = parseDouble(params.getOrDefault("radius", 0));
        PotionEffectType type = PotionEffectType.getByName(typeName);
        if (type == null) {
            return;
        }
        PotionEffect effect = new PotionEffect(type, duration, amplifier);
        if (radius > 0) {
            player.getNearbyEntities(radius, radius, radius).stream()
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .forEach(target -> target.addPotionEffect(effect));
            return;
        }
        player.addPotionEffect(effect);
    }

    private int parseInt(Object raw) {
        try {
            return Integer.parseInt(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseDouble(Object raw) {
        try {
            return Double.parseDouble(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}

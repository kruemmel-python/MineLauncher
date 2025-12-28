package com.example.rpg.skill.effects;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.skill.SkillEffect;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class ParticleEffect implements SkillEffect {
    @Override
    public void apply(Player player, PlayerProfile profile, Map<String, Object> params) {
        String typeName = String.valueOf(params.getOrDefault("type", "SPELL")).toUpperCase();
        int count = parseInt(params.getOrDefault("count", 10));
        double speed = parseDouble(params.getOrDefault("speed", 0.01));
        String dataName = params.containsKey("data") ? String.valueOf(params.get("data")) : null;
        Particle particle;
        try {
            particle = Particle.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            particle = Particle.SPELL;
        }
        if (requiresBlockData(particle)) {
            Material material = resolveMaterial(dataName);
            if (material == null || !material.isBlock()) {
                particle = Particle.SPELL;
                player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1.0, 0), count, 0.3, 0.6, 0.3, speed);
                return;
            }
            player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1.0, 0), count, 0.3, 0.6, 0.3,
                speed, material.createBlockData());
            return;
        }
        player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1.0, 0), count, 0.3, 0.6, 0.3, speed);
    }

    private int parseInt(Object raw) {
        try {
            return Integer.parseInt(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 10;
        }
    }

    private double parseDouble(Object raw) {
        try {
            return Double.parseDouble(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 0.01;
        }
    }

    private boolean requiresBlockData(Particle particle) {
        return particle == Particle.BLOCK_CRACK || particle == Particle.BLOCK_DUST || particle == Particle.FALLING_DUST;
    }

    private Material resolveMaterial(String dataName) {
        if (dataName == null || dataName.isBlank()) {
            return null;
        }
        return Material.matchMaterial(dataName.trim().toUpperCase());
    }
}

package com.example.rpg.skill.effects;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.skill.SkillEffect;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;

public class ProjectileEffect implements SkillEffect {
    @Override
    public void apply(Player player, PlayerProfile profile, Map<String, Object> params) {
        String type = String.valueOf(params.getOrDefault("type", "SNOWBALL")).toUpperCase();
        switch (type) {
            case "SMALL_FIREBALL" -> player.launchProjectile(SmallFireball.class);
            case "SNOWBALL" -> player.launchProjectile(Snowball.class);
            default -> player.launchProjectile(Snowball.class);
        }
    }
}

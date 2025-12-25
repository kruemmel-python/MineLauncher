package com.example.rpg.skill.effects;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.skill.SkillEffect;
import java.util.Map;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundEffect implements SkillEffect {
    @Override
    public void apply(Player player, PlayerProfile profile, Map<String, Object> params) {
        String soundName = String.valueOf(params.getOrDefault("sound", "ENTITY_PLAYER_LEVELUP")).toUpperCase();
        float volume = parseFloat(params.getOrDefault("volume", 1.0));
        float pitch = parseFloat(params.getOrDefault("pitch", 1.0));
        Sound sound;
        try {
            sound = Sound.valueOf(soundName);
        } catch (IllegalArgumentException e) {
            sound = Sound.ENTITY_PLAYER_LEVELUP;
        }
        player.getWorld().playSound(player.getLocation(), sound, volume, pitch);
    }

    private float parseFloat(Object raw) {
        try {
            return Float.parseFloat(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 1.0f;
        }
    }
}

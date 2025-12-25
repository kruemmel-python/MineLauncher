package com.example.rpg.skill;

import java.util.EnumMap;
import java.util.Map;

public class SkillEffectRegistry {
    private final Map<SkillEffectType, SkillEffect> effects = new EnumMap<>(SkillEffectType.class);

    public SkillEffectRegistry register(SkillEffectType type, SkillEffect effect) {
        effects.put(type, effect);
        return this;
    }

    public void apply(SkillEffectConfig config, org.bukkit.entity.Player player,
                      com.example.rpg.model.PlayerProfile profile) {
        SkillEffect effect = effects.get(config.type());
        if (effect == null) {
            return;
        }
        effect.apply(player, profile, config.params());
    }
}

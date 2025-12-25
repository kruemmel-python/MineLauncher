package com.example.rpg.skill;

import com.example.rpg.model.PlayerProfile;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public interface SkillEffect {
    void apply(Player player, PlayerProfile profile, Map<String, Object> params);

    default List<Component> describe(Map<String, Object> params) {
        return List.of();
    }
}

package com.example.rpg.behavior;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.MobDefinition;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class BehaviorContext {
    private final RPGPlugin plugin;
    private final LivingEntity mob;
    private final MobDefinition definition;
    private Player target;
    private final Map<String, Long> cooldowns = new HashMap<>();

    public BehaviorContext(RPGPlugin plugin, LivingEntity mob, MobDefinition definition) {
        this.plugin = plugin;
        this.mob = mob;
        this.definition = definition;
    }

    public RPGPlugin plugin() {
        return plugin;
    }

    public LivingEntity mob() {
        return mob;
    }

    public MobDefinition definition() {
        return definition;
    }

    public Player target() {
        return target;
    }

    public void setTarget(Player target) {
        this.target = target;
    }

    public Map<String, Long> cooldowns() {
        return cooldowns;
    }

    public UUID mobId() {
        return mob.getUniqueId();
    }
}

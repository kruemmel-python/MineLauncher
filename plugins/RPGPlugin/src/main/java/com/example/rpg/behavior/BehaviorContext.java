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
    private final Map<String, Object> state = new HashMap<>();

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

    public <T> T getState(String key, Class<T> type) {
        Object value = state.get(key);
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }

    public void putState(String key, Object value) {
        if (value == null) {
            state.remove(key);
        } else {
            state.put(key, value);
        }
    }

    public void removeState(String key) {
        state.remove(key);
    }

    public long getStateLong(String key, long fallback) {
        Object value = state.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return fallback;
    }

    public void putStateLong(String key, long value) {
        state.put(key, value);
    }
}

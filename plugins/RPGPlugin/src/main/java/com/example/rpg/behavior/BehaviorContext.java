package com.example.rpg.behavior;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.MobDefinition;
import java.util.Collections;
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

    /**
     * Returns a read-only view of the blackboard state.
     *
     * @return blackboard state
     */
    public Map<String, Object> state() {
        return Collections.unmodifiableMap(state);
    }

    /**
     * Returns a typed value from the blackboard.
     *
     * @param key key to look up
     * @param type expected type
     * @param <T> type
     * @return value or null when missing or mismatched
     */
    public <T> T getState(String key, Class<T> type) {
        Object value = state.get(key);
        if (value == null || !type.isInstance(value)) {
            return null;
        }
        return type.cast(value);
    }

    /**
     * Stores a value in the blackboard.
     *
     * @param key key to store
     * @param value value to store
     */
    public void putState(String key, Object value) {
        state.put(key, value);
    }

    /**
     * Removes a value from the blackboard.
     *
     * @param key key to remove
     */
    public void removeState(String key) {
        state.remove(key);
    }

    /**
     * Returns a long value from the blackboard.
     *
     * @param key key to look up
     * @param defaultValue default value when missing or invalid
     * @return stored long or default
     */
    public long getStateLong(String key, long defaultValue) {
        Object value = state.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return defaultValue;
    }

    /**
     * Stores a long value in the blackboard.
     *
     * @param key key to store
     * @param value value to store
     */
    public void putStateLong(String key, long value) {
        state.put(key, value);
    }
}

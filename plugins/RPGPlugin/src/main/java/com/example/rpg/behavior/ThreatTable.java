package com.example.rpg.behavior;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import org.bukkit.entity.Player;

/**
 * Stores threat values for a mob and provides target selection helpers.
 */
public class ThreatTable {
    private final Map<UUID, Double> threat = new HashMap<>();

    /**
     * Adds threat to the given player.
     *
     * @param player player
     * @param amount threat amount
     */
    public void addThreat(Player player, double amount) {
        if (player == null) {
            return;
        }
        threat.merge(player.getUniqueId(), amount, Double::sum);
    }

    /**
     * Sets threat for the given player.
     *
     * @param player player
     * @param value threat value
     */
    public void setThreat(Player player, double value) {
        if (player == null) {
            return;
        }
        threat.put(player.getUniqueId(), value);
    }

    /**
     * Returns threat for the given player.
     *
     * @param player player
     * @return threat value
     */
    public double getThreat(Player player) {
        if (player == null) {
            return 0;
        }
        return threat.getOrDefault(player.getUniqueId(), 0.0);
    }

    /**
     * Applies a decay factor to all threat values.
     *
     * @param factorPerSecond multiplier to apply (e.g. 0.9)
     */
    public void decay(double factorPerSecond) {
        if (factorPerSecond >= 1.0) {
            return;
        }
        threat.replaceAll((uuid, value) -> value * factorPerSecond);
    }

    /**
     * Returns the top threat target satisfying the predicate.
     *
     * @param valid predicate for valid targets
     * @return top threat player or null
     */
    public Player getTopThreatTarget(Predicate<Player> valid) {
        return threat.entrySet().stream()
            .map(entry -> entry.getKey())
            .map(uuid -> {
                Player player = org.bukkit.Bukkit.getPlayer(uuid);
                if (player == null) {
                    return null;
                }
                return player;
            })
            .filter(player -> player != null && (valid == null || valid.test(player)))
            .max(Comparator.comparingDouble(player -> getThreat(player)))
            .orElse(null);
    }

    /**
     * Removes a player from the table.
     *
     * @param player player to remove
     */
    public void remove(Player player) {
        if (player == null) {
            return;
        }
        threat.remove(player.getUniqueId());
    }

    /**
     * Clears the threat table.
     */
    public void clear() {
        threat.clear();
    }

    /**
     * Returns whether the table is empty.
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return threat.isEmpty();
    }
}

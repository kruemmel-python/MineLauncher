package com.example.rpg.manager;

import com.example.rpg.db.PlayerDao;
import com.example.rpg.model.PlayerProfile;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerDataManager {
    private final JavaPlugin plugin;
    private final PlayerDao playerDao;
    private final Map<UUID, PlayerProfile> profiles = new HashMap<>();

    public PlayerDataManager(JavaPlugin plugin, PlayerDao playerDao) {
        this.plugin = plugin;
        this.playerDao = playerDao;
    }

    public PlayerProfile getProfile(UUID uuid) {
        return profiles.computeIfAbsent(uuid, PlayerProfile::new);
    }

    public PlayerProfile getProfile(Player player) {
        return getProfile(player.getUniqueId());
    }

    public Map<UUID, PlayerProfile> profiles() {
        return profiles;
    }

    public CompletableFuture<PlayerProfile> loadProfileAsync(UUID uuid) {
        return playerDao.loadPlayer(uuid).exceptionally(error -> {
            plugin.getLogger().warning("Failed to load player " + uuid + ": " + error.getMessage());
            return null;
        }).thenApply(profile -> {
            PlayerProfile resolved = profile != null ? profile : new PlayerProfile(uuid);
            profiles.put(uuid, resolved);
            return resolved;
        });
    }

    public void saveProfile(PlayerProfile profile) {
        playerDao.savePlayer(profile).exceptionally(error -> {
            plugin.getLogger().warning("Failed to save player " + profile.uuid() + ": " + error.getMessage());
            return null;
        });
    }

    public void saveAll() {
        for (PlayerProfile profile : profiles.values()) {
            saveProfile(profile);
        }
    }
}

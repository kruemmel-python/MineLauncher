package com.example.rpg.db;

import com.example.rpg.model.PlayerProfile;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerDao {
    CompletableFuture<Void> savePlayer(PlayerProfile profile);
    CompletableFuture<PlayerProfile> loadPlayer(UUID uuid);
}

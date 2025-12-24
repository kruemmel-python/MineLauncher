package de.yourname.rpg.core;

import de.yourname.rpg.storage.StorageService;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataService {
    private final StorageService storageService;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();

    public PlayerDataService(StorageService storageService) {
        this.storageService = storageService;
    }

    public PlayerData getOrCreate(UUID uuid) {
        return cache.computeIfAbsent(uuid, id -> storageService.loadPlayerData(id).orElseGet(() -> new PlayerData(id)));
    }

    public void save(PlayerData data) {
        storageService.savePlayerData(data);
    }

    public void saveAll() {
        cache.values().forEach(storageService::savePlayerData);
    }

    public void unload(UUID uuid) {
        PlayerData data = cache.remove(uuid);
        if (data != null) {
            storageService.savePlayerData(data);
        }
    }
}

package de.yourname.rpg.core;

import java.util.UUID;

public class XPService {
    private final PlayerDataService playerDataService;

    public XPService(PlayerDataService playerDataService) {
        this.playerDataService = playerDataService;
    }

    public void addXp(UUID uuid, int amount) {
        PlayerData data = playerDataService.getOrCreate(uuid);
        data.setXp(data.getXp() + amount);
    }
}

package de.yourname.rpg.core;

import java.util.UUID;

public class FlagService {
    private final PlayerDataService playerDataService;

    public FlagService(PlayerDataService playerDataService) {
        this.playerDataService = playerDataService;
    }

    public boolean hasFlag(UUID uuid, String flag) {
        return playerDataService.getOrCreate(uuid).getFlags().contains(flag);
    }

    public void setFlag(UUID uuid, String flag, boolean value) {
        if (value) {
            playerDataService.getOrCreate(uuid).getFlags().add(flag);
        } else {
            playerDataService.getOrCreate(uuid).getFlags().remove(flag);
        }
    }
}

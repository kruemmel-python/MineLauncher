package de.yourname.rpg.util;

import de.yourname.rpg.core.PlayerDataService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLifecycleListener implements Listener {
    private final PlayerDataService playerDataService;

    public PlayerLifecycleListener(PlayerDataService playerDataService) {
        this.playerDataService = playerDataService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        playerDataService.getOrCreate(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        playerDataService.unload(event.getPlayer().getUniqueId());
    }
}

package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class ArenaListener implements Listener {
    private final RPGPlugin plugin;

    public ArenaListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        plugin.arenaManager().handleDeath(player);
    }
}

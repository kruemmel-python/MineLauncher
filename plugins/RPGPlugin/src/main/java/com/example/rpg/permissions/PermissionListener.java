package com.example.rpg.permissions;

import com.example.rpg.RPGPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PermissionListener implements Listener {
    private final RPGPlugin plugin;

    public PermissionListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.permissionService().applyAttachments(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.permissionService().removeAttachment(event.getPlayer().getUniqueId());
    }
}

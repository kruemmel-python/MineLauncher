package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class ItemStatListener implements Listener {
    private final RPGPlugin plugin;

    public ItemStatListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, () -> plugin.itemStatManager().updateSetBonus(player));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.itemStatManager().updateSetBonus(event.getPlayer());
    }
}

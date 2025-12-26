package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.util.Text;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class BuildingPlacementListener implements Listener {
    private final RPGPlugin plugin;

    public BuildingPlacementListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlace(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!event.getPlayer().hasPermission("rpg.admin")) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }
        Location target = event.getClickedBlock().getLocation().add(0, 1, 0);
        if (plugin.buildingManager().handlePlacement(event.getPlayer(), target)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Text.mm("<gray>Platziere Gebäude..."));
        }
    }
}

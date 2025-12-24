package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.util.Text;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
    private final RPGPlugin plugin;

    public PlayerListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        profile.applyAttributes(player);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (plugin.promptManager().handle(player, event.getMessage())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWandUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("rpg.editor")) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.STICK) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        if (!meta.getPersistentDataContainer().has(plugin.wandKey(), PersistentDataType.BYTE)) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            player.sendMessage(Text.mm("<green>Pos1 gesetzt: " + event.getClickedBlock().getLocation().toVector()));
            player.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "pos1"),
                org.bukkit.persistence.PersistentDataType.STRING,
                serializeLocation(event.getClickedBlock().getLocation())
            );
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            player.sendMessage(Text.mm("<green>Pos2 gesetzt: " + event.getClickedBlock().getLocation().toVector()));
            player.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "pos2"),
                org.bukkit.persistence.PersistentDataType.STRING,
                serializeLocation(event.getClickedBlock().getLocation())
            );
        }
    }

    private String serializeLocation(org.bukkit.Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }
}

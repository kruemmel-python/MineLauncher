package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class SkillHotbarListener implements Listener {
    private final RPGPlugin plugin;

    public SkillHotbarListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        var player = event.getPlayer();
        int slot = player.getInventory().getHeldItemSlot() + 1;
        var profile = plugin.playerDataManager().getProfile(player);
        String skillId = plugin.skillHotbarManager().getBinding(profile, slot);
        if (skillId == null || skillId.isBlank()) {
            return;
        }
        plugin.useSkill(player, skillId);
    }
}

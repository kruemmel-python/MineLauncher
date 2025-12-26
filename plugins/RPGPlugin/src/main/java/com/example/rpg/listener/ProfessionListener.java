package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.util.Text;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class ProfessionListener implements Listener {
    private final RPGPlugin plugin;

    public ProfessionListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Material material = event.getBlock().getType();
        String materialKey = material.name();
        PlayerProfile profile = plugin.playerDataManager().getProfile(event.getPlayer());
        int miningXp = plugin.professionManager().xpForMaterial("mining", materialKey);
        int herbalismXp = plugin.professionManager().xpForMaterial("herbalism", materialKey);
        if (miningXp > 0) {
            plugin.professionManager().addXp(profile, "mining", miningXp, event.getPlayer());
        }
        if (herbalismXp > 0) {
            plugin.professionManager().addXp(profile, "herbalism", herbalismXp, event.getPlayer());
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof org.bukkit.entity.Player player)) {
            return;
        }
        ItemStack result = event.getRecipe().getResult();
        if (result == null || result.getType().isAir()) {
            return;
        }
        String materialKey = result.getType().name();
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        int required = plugin.professionManager().requiredLevelForCraft("blacksmithing", materialKey);
        if (required > 0 && plugin.professionManager().getLevel(profile, "blacksmithing") < required) {
            event.setCancelled(true);
            player.sendMessage(Text.mm("<red>Benötigtes Schmiede-Level: " + required));
            return;
        }
        int xp = plugin.professionManager().xpForMaterial("blacksmithing", materialKey);
        if (xp > 0) {
            plugin.professionManager().addXp(profile, "blacksmithing", xp, player);
        }
    }
}

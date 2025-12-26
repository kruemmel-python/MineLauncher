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
        var node = plugin.resourceNodeManager().nodeAt(event.getBlock().getLocation());
        if (node != null) {
            long now = System.currentTimeMillis();
            if (node.nextAvailableAt() > now) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Text.mm("<red>Dieser Knoten regeneriert gerade."));
                return;
            }
            Material nodeMaterial = Material.matchMaterial(node.material());
            if (nodeMaterial != null && nodeMaterial != material) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Text.mm("<red>Falscher Knoten-Typ."));
                return;
            }
            node.setNextAvailableAt(now + (node.respawnSeconds() * 1000L));
            plugin.resourceNodeManager().saveNode(node);
            event.setDropItems(false);
            event.getBlock().setType(Material.AIR);
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(material));
            PlayerProfile profile = plugin.playerDataManager().getProfile(event.getPlayer());
            plugin.professionManager().addXp(profile, node.profession(), node.xp(), event.getPlayer());
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (event.getBlock().getType() == Material.AIR) {
                    event.getBlock().setType(material);
                }
            }, node.respawnSeconds() * 20L);
        }
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
        plugin.guildManager().guildFor(player.getUniqueId()).ifPresent(guild -> {
            var hall = plugin.guildManager().hallLocation(guild);
            if (hall != null && hall.getWorld().equals(player.getWorld())
                && player.getLocation().distanceSquared(hall) <= 20 * 20) {
                int bonusLevel = guild.hallUpgrades().getOrDefault("craft", 0);
                if (bonusLevel > 0) {
                    int bonusXp = bonusLevel * 2;
                    plugin.professionManager().addXp(profile, "blacksmithing", bonusXp, player);
                    player.sendMessage(Text.mm("<gold>Gildenhalle-Bonus: +" + bonusXp + " XP"));
                }
            }
        });
    }
}

package de.yourname.rpg.util;

import de.yourname.rpg.core.PluginContext;
import de.yourname.rpg.zone.Zone;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class GameplayListener implements Listener {
    private final PluginContext context;

    public GameplayListener(PluginContext context) {
        this.context = context;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }
        context.getZoneService().getZoneAt(event.getEntity().getLocation()).ifPresent(zone -> handleZoneKill(killer, zone));
        context.getQuestService().notifyKill(killer.getUniqueId(), event.getEntity().getType().name());
    }

    private void handleZoneKill(Player player, Zone zone) {
        int baseXp = 10;
        int gainedXp = (int) Math.round(baseXp * zone.getXpMultiplier());
        context.getXpService().addXp(player.getUniqueId(), gainedXp);
        if (zone.getLootTableId() != null) {
            List<String> drops = context.getLootService().roll(zone.getLootTableId());
            if (!drops.isEmpty()) {
                player.sendMessage("§aLoot erhalten: " + String.join(", ", drops));
            }
        }
    }
}

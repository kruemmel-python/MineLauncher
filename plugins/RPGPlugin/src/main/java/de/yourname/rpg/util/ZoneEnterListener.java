package de.yourname.rpg.util;

import de.yourname.rpg.quest.QuestService;
import de.yourname.rpg.zone.Zone;
import de.yourname.rpg.zone.ZoneService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class ZoneEnterListener implements Listener {
    private final ZoneService zoneService;
    private final QuestService questService;
    private final Map<UUID, String> currentZones = new HashMap<>();

    public ZoneEnterListener(ZoneService zoneService, QuestService questService) {
        this.zoneService = zoneService;
        this.questService = questService;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null || (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ())) {
            return;
        }
        Optional<Zone> zone = zoneService.getZoneAt(to);
        String zoneId = zone.map(Zone::getId).orElse(null);
        UUID playerId = event.getPlayer().getUniqueId();
        String previous = currentZones.get(playerId);
        if (zoneId != null && !zoneId.equals(previous)) {
            currentZones.put(playerId, zoneId);
            questService.notifyGoto(playerId, zoneId);
        } else if (zoneId == null && previous != null) {
            currentZones.remove(playerId);
        }
    }
}

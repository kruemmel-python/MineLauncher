package de.yourname.rpg.zone;

import de.yourname.rpg.core.Registry;
import de.yourname.rpg.storage.StorageService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bukkit.Location;

public class ZoneService {
    private final Registry<Zone> registry = new Registry<>();
    private final List<ZoneTrigger> triggers = new ArrayList<>();
    private final StorageService storageService;

    public ZoneService(StorageService storageService) {
        this.storageService = storageService;
    }

    public void load() {
        storageService.loadZones().forEach(zone -> registry.register(zone.getId(), zone));
    }

    public void save() {
        storageService.saveZones(registry.all().stream().toList());
    }

    public Optional<Zone> getZone(String id) {
        return registry.get(id);
    }

    public Optional<Zone> getZoneAt(Location location) {
        return registry.all().stream().filter(zone -> matches(zone, location)).findFirst();
    }

    public void registerTrigger(ZoneTrigger trigger) {
        triggers.add(trigger);
    }

    private boolean matches(Zone zone, Location location) {
        if (!location.getWorld().getName().equals(zone.getWorld())) {
            return false;
        }
        ZonePosition pos1 = zone.getPos1();
        ZonePosition pos2 = zone.getPos2();
        int minX = Math.min(pos1.getX(), pos2.getX());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }
}

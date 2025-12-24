package de.yourname.rpg.zone;

import java.util.UUID;

public interface ZoneTrigger {
    void onEnter(UUID playerId, Zone zone);
    void onExit(UUID playerId, Zone zone);
}

package de.yourname.rpg.quest;

import de.yourname.rpg.core.PlayerData;

public class GotoStep implements QuestStep {
    private final String zoneId;

    public GotoStep(String zoneId) {
        this.zoneId = zoneId;
    }

    @Override
    public String getDescription() {
        return "Betritt Zone " + zoneId;
    }

    @Override
    public boolean isComplete(PlayerData data, QuestProgress progress) {
        return progress.getCount("goto:" + zoneId) >= 1;
    }

    public String getZoneId() {
        return zoneId;
    }
}

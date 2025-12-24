package de.yourname.rpg.quest;

import de.yourname.rpg.core.PlayerData;

public class TalkStep implements QuestStep {
    private final String npcId;

    public TalkStep(String npcId) {
        this.npcId = npcId;
    }

    @Override
    public String getDescription() {
        return "Sprich mit " + npcId;
    }

    @Override
    public boolean isComplete(PlayerData data, QuestProgress progress) {
        return progress.getCount("talk:" + npcId) >= 1;
    }

    public String getNpcId() {
        return npcId;
    }
}

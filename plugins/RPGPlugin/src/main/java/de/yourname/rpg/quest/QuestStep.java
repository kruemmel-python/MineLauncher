package de.yourname.rpg.quest;

import de.yourname.rpg.core.PlayerData;

public interface QuestStep {
    String getDescription();

    boolean isComplete(PlayerData data, QuestProgress progress);
}

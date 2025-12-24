package de.yourname.rpg.quest;

import de.yourname.rpg.core.PlayerData;

public class QuestEvaluator {
    public boolean evaluate(PlayerData data, Quest quest, QuestState state) {
        for (QuestStep step : quest.getSteps()) {
            if (!step.isComplete(data, state.getProgress())) {
                return false;
            }
        }
        return true;
    }
}

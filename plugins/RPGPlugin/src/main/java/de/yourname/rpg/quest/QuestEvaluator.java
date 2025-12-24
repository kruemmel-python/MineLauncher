package de.yourname.rpg.quest;

public class QuestEvaluator {
    public boolean evaluate(Quest quest, QuestState state) {
        for (QuestStepData step : quest.getSteps()) {
            if (!isStepComplete(step, state.getProgress())) {
                return false;
            }
        }
        return true;
    }

    private boolean isStepComplete(QuestStepData step, QuestProgress progress) {
        String key = switch (step.getType()) {
            case KILL -> "kill:" + step.getTarget();
            case COLLECT -> "collect:" + step.getTarget();
            case TALK -> "talk:" + step.getTarget();
            case GOTO -> "goto:" + step.getTarget();
        };
        return progress.getCount(key) >= step.getAmount();
    }
}

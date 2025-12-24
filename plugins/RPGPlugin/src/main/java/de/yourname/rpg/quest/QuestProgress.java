package de.yourname.rpg.quest;

import java.util.HashMap;
import java.util.Map;

public class QuestProgress {
    private final Map<String, Integer> stepCounts = new HashMap<>();

    public int getCount(String key) {
        return stepCounts.getOrDefault(key, 0);
    }

    public void setCount(String key, int value) {
        stepCounts.put(key, value);
    }

    public Map<String, Integer> getStepCounts() {
        return stepCounts;
    }
}

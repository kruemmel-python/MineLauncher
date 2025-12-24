package de.yourname.rpg.quest;

import de.yourname.rpg.core.PlayerData;

public class CollectStep implements QuestStep {
    private final String itemId;
    private final int amount;

    public CollectStep(String itemId, int amount) {
        this.itemId = itemId;
        this.amount = amount;
    }

    @Override
    public String getDescription() {
        return "Sammle " + amount + "x " + itemId;
    }

    @Override
    public boolean isComplete(PlayerData data, QuestProgress progress) {
        return progress.getCount("collect:" + itemId) >= amount;
    }

    public String getItemId() {
        return itemId;
    }

    public int getAmount() {
        return amount;
    }
}

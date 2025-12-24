package de.yourname.rpg.quest;

import de.yourname.rpg.core.PlayerData;

public class KillStep implements QuestStep {
    private final String mobType;
    private final int amount;

    public KillStep(String mobType, int amount) {
        this.mobType = mobType;
        this.amount = amount;
    }

    @Override
    public String getDescription() {
        return "Besiege " + amount + "x " + mobType;
    }

    @Override
    public boolean isComplete(PlayerData data, QuestProgress progress) {
        return progress.getCount("kill:" + mobType) >= amount;
    }

    public String getMobType() {
        return mobType;
    }

    public int getAmount() {
        return amount;
    }
}

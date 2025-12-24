package de.yourname.rpg.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LootRoller {
    private final Random random = new Random();

    public List<String> roll(LootTable table) {
        List<String> rewards = new ArrayList<>();
        for (LootTableEntry entry : table.getEntries()) {
            if (random.nextDouble() <= entry.getChance()) {
                rewards.add(entry.getItemId());
            }
        }
        return rewards;
    }
}

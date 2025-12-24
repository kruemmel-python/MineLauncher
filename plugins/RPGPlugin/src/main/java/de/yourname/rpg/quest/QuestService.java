package de.yourname.rpg.quest;

import de.yourname.rpg.core.PlayerData;
import de.yourname.rpg.core.PlayerDataService;
import de.yourname.rpg.core.Registry;
import de.yourname.rpg.storage.StorageService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class QuestService {
    private final Registry<Quest> questRegistry = new Registry<>();
    private final Map<UUID, Map<String, QuestState>> playerStates = new HashMap<>();
    private final PlayerDataService playerDataService;
    private final StorageService storageService;
    private final QuestEvaluator evaluator = new QuestEvaluator();

    public QuestService(PlayerDataService playerDataService, StorageService storageService) {
        this.playerDataService = playerDataService;
        this.storageService = storageService;
    }

    public void load() {
        List<Quest> quests = storageService.loadQuests();
        quests.forEach(quest -> questRegistry.register(quest.getId(), quest));
    }

    public void save() {
        storageService.saveQuests(questRegistry.all().stream().toList());
    }

    public Optional<Quest> getQuest(String id) {
        return questRegistry.get(id);
    }

    public void startQuest(UUID uuid, String questId) {
        playerStates.computeIfAbsent(uuid, key -> new HashMap<>())
                .putIfAbsent(questId, new QuestState(questId));
    }

    public void notifyKill(UUID uuid, String mobType) {
        updateProgress(uuid, "kill:" + mobType);
    }

    public void notifyCollect(UUID uuid, String itemId) {
        updateProgress(uuid, "collect:" + itemId);
    }

    public void notifyTalk(UUID uuid, String npcId) {
        updateProgress(uuid, "talk:" + npcId);
    }

    public void notifyGoto(UUID uuid, String zoneId) {
        updateProgress(uuid, "goto:" + zoneId);
    }

    private void updateProgress(UUID uuid, String key) {
        Map<String, QuestState> quests = playerStates.get(uuid);
        if (quests == null) {
            return;
        }
        quests.values().forEach(state -> {
            QuestProgress progress = state.getProgress();
            progress.setCount(key, progress.getCount(key) + 1);
            questRegistry.get(state.getQuestId()).ifPresent(quest -> {
                if (!state.isCompleted() && evaluator.evaluate(playerDataService.getOrCreate(uuid), quest, state)) {
                    state.setCompleted(true);
                    PlayerData data = playerDataService.getOrCreate(uuid);
                    data.getQuestStates().put(quest.getId(), "completed");
                }
            });
        });
    }
}

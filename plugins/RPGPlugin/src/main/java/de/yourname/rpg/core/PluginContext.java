package de.yourname.rpg.core;

import de.yourname.rpg.loot.LootService;
import de.yourname.rpg.npc.NpcService;
import de.yourname.rpg.quest.QuestService;
import de.yourname.rpg.skill.SkillService;
import de.yourname.rpg.storage.StorageService;
import de.yourname.rpg.zone.ZoneService;

public class PluginContext {
    private final StorageService storageService;
    private final PlayerDataService playerDataService;
    private final FlagService flagService;
    private final QuestService questService;
    private final ZoneService zoneService;
    private final NpcService npcService;
    private final LootService lootService;
    private final SkillService skillService;
    private final XPService xpService;

    public PluginContext(StorageService storageService,
                         PlayerDataService playerDataService,
                         FlagService flagService,
                         QuestService questService,
                         ZoneService zoneService,
                         NpcService npcService,
                         LootService lootService,
                         SkillService skillService,
                         XPService xpService) {
        this.storageService = storageService;
        this.playerDataService = playerDataService;
        this.flagService = flagService;
        this.questService = questService;
        this.zoneService = zoneService;
        this.npcService = npcService;
        this.lootService = lootService;
        this.skillService = skillService;
        this.xpService = xpService;
    }

    public StorageService getStorageService() {
        return storageService;
    }

    public PlayerDataService getPlayerDataService() {
        return playerDataService;
    }

    public FlagService getFlagService() {
        return flagService;
    }

    public QuestService getQuestService() {
        return questService;
    }

    public ZoneService getZoneService() {
        return zoneService;
    }

    public NpcService getNpcService() {
        return npcService;
    }

    public LootService getLootService() {
        return lootService;
    }

    public SkillService getSkillService() {
        return skillService;
    }

    public XPService getXpService() {
        return xpService;
    }
}

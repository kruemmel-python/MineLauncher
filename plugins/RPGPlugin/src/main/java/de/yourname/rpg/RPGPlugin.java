package de.yourname.rpg;

import de.yourname.rpg.command.RpgAdminCommand;
import de.yourname.rpg.command.RpgCommand;
import de.yourname.rpg.core.FlagService;
import de.yourname.rpg.core.PlayerDataService;
import de.yourname.rpg.core.PluginContext;
import de.yourname.rpg.core.XPService;
import de.yourname.rpg.gui.RpgMenu;
import de.yourname.rpg.loot.LootService;
import de.yourname.rpg.npc.NpcInteractionHandler;
import de.yourname.rpg.npc.NpcService;
import de.yourname.rpg.quest.QuestService;
import de.yourname.rpg.skill.SkillService;
import de.yourname.rpg.storage.StorageService;
import de.yourname.rpg.util.GameplayListener;
import de.yourname.rpg.util.PdcKeys;
import de.yourname.rpg.util.PlayerLifecycleListener;
import de.yourname.rpg.util.ZoneEnterListener;
import de.yourname.rpg.zone.EditorWandItem;
import de.yourname.rpg.zone.EditorWandListener;
import de.yourname.rpg.zone.ZoneService;
import org.bukkit.plugin.java.JavaPlugin;

public class RPGPlugin extends JavaPlugin {
    private PluginContext context;

    @Override
    public void onEnable() {
        StorageService storageService = new StorageService(getDataFolder().toPath());
        storageService.ensureFolders();

        PlayerDataService playerDataService = new PlayerDataService(storageService);
        FlagService flagService = new FlagService(playerDataService);
        QuestService questService = new QuestService(playerDataService, storageService);
        ZoneService zoneService = new ZoneService(storageService);
        NpcService npcService = new NpcService(storageService);
        LootService lootService = new LootService(storageService);
        SkillService skillService = new SkillService(storageService);
        XPService xpService = new XPService(playerDataService);

        context = new PluginContext(storageService, playerDataService, flagService, questService,
                zoneService, npcService, lootService, skillService, xpService);

        questService.load();
        zoneService.load();
        npcService.load();
        lootService.load();
        skillService.load();

        PdcKeys keys = new PdcKeys(this);
        RpgMenu menu = new RpgMenu();
        RpgCommand rpgCommand = new RpgCommand(menu, context);
        EditorWandItem wandItem = new EditorWandItem(keys);
        RpgAdminCommand adminCommand = new RpgAdminCommand(wandItem, context, keys);

        getCommand("rpg").setExecutor(rpgCommand);
        getCommand("rpgadmin").setExecutor(adminCommand);

        getServer().getPluginManager().registerEvents(new PlayerLifecycleListener(playerDataService), this);
        getServer().getPluginManager().registerEvents(new GameplayListener(context), this);
        getServer().getPluginManager().registerEvents(new NpcInteractionHandler(npcService, questService, keys), this);
        getServer().getPluginManager().registerEvents(new EditorWandListener(adminCommand, keys), this);
        getServer().getPluginManager().registerEvents(new ZoneEnterListener(zoneService, questService), this);
    }

    @Override
    public void onDisable() {
        if (context == null) {
            return;
        }
        context.getPlayerDataService().saveAll();
        context.getQuestService().save();
        context.getZoneService().save();
        context.getNpcService().save();
        context.getLootService().save();
        context.getSkillService().save();
    }
}

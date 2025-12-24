package com.example.rpg;

import com.example.rpg.command.PartyCommand;
import com.example.rpg.command.RPGAdminCommand;
import com.example.rpg.command.RPGCommand;
import com.example.rpg.gui.GuiManager;
import com.example.rpg.listener.CombatListener;
import com.example.rpg.listener.GuiListener;
import com.example.rpg.listener.NpcListener;
import com.example.rpg.listener.NpcProtectionListener;
import com.example.rpg.listener.PlayerListener;
import com.example.rpg.listener.SkillHotbarListener;
import com.example.rpg.listener.ZoneListener;
import com.example.rpg.manager.ClassManager;
import com.example.rpg.manager.FactionManager;
import com.example.rpg.manager.LootManager;
import com.example.rpg.manager.NpcManager;
import com.example.rpg.manager.PartyManager;
import com.example.rpg.manager.PlayerDataManager;
import com.example.rpg.manager.QuestManager;
import com.example.rpg.manager.SkillHotbarManager;
import com.example.rpg.manager.SkillManager;
import com.example.rpg.manager.ZoneManager;
import com.example.rpg.util.ItemGenerator;
import com.example.rpg.util.AuditLog;
import com.example.rpg.util.PromptManager;
import com.example.rpg.skill.SkillEffectRegistry;
import com.example.rpg.skill.SkillEffectType;
import com.example.rpg.skill.effects.DamageEffect;
import com.example.rpg.skill.effects.HealEffect;
import com.example.rpg.skill.effects.PotionStatusEffect;
import com.example.rpg.skill.effects.ProjectileEffect;
import com.example.rpg.skill.effects.SoundEffect;
import com.example.rpg.skill.effects.XpEffect;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class RPGPlugin extends JavaPlugin {
    private PlayerDataManager playerDataManager;
    private QuestManager questManager;
    private ZoneManager zoneManager;
    private NpcManager npcManager;
    private LootManager lootManager;
    private SkillManager skillManager;
    private SkillHotbarManager skillHotbarManager;
    private ClassManager classManager;
    private FactionManager factionManager;
    private PartyManager partyManager;
    private GuiManager guiManager;
    private PromptManager promptManager;
    private ItemGenerator itemGenerator;
    private SkillEffectRegistry skillEffects;
    private final Set<UUID> debugPlayers = new HashSet<>();
    private NamespacedKey questKey;
    private NamespacedKey skillKey;
    private NamespacedKey wandKey;
    private AuditLog auditLog;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        playerDataManager = new PlayerDataManager(this);
        questManager = new QuestManager(this);
        zoneManager = new ZoneManager(this);
        npcManager = new NpcManager(this);
        lootManager = new LootManager(this);
        skillManager = new SkillManager(this);
        skillHotbarManager = new SkillHotbarManager(playerDataManager);
        classManager = new ClassManager(this);
        factionManager = new FactionManager(this);
        partyManager = new PartyManager();
        promptManager = new PromptManager();
        itemGenerator = new ItemGenerator(this);
        questKey = new NamespacedKey(this, "quest_id");
        skillKey = new NamespacedKey(this, "skill_id");
        wandKey = new NamespacedKey(this, "editor_wand");
        skillEffects = new SkillEffectRegistry()
            .register(SkillEffectType.HEAL, new HealEffect())
            .register(SkillEffectType.DAMAGE, new DamageEffect())
            .register(SkillEffectType.PROJECTILE, new ProjectileEffect())
            .register(SkillEffectType.POTION, new PotionStatusEffect())
            .register(SkillEffectType.SOUND, new SoundEffect())
            .register(SkillEffectType.XP, new XpEffect());
        guiManager = new GuiManager(playerDataManager, questManager, skillManager, classManager, factionManager, questKey, skillKey);
        auditLog = new AuditLog(this);

        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CombatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ZoneListener(this), this);
        Bukkit.getPluginManager().registerEvents(new NpcListener(this), this);
        Bukkit.getPluginManager().registerEvents(new NpcProtectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GuiListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SkillHotbarListener(this), this);

        getCommand("rpg").setExecutor(new RPGCommand(this));
        getCommand("rpgadmin").setExecutor(new RPGAdminCommand(this));
        getCommand("party").setExecutor(new PartyCommand(this));

        npcManager.spawnAll();
        startDebugTask();
        startManaRegenTask();
    }

    @Override
    public void onDisable() {
        playerDataManager.saveAll();
        questManager.saveAll();
        zoneManager.saveAll();
        npcManager.saveAll();
        lootManager.saveAll();
        skillManager.saveAll();
        classManager.saveAll();
        factionManager.saveAll();
    }

    public PlayerDataManager playerDataManager() {
        return playerDataManager;
    }

    public QuestManager questManager() {
        return questManager;
    }

    public ZoneManager zoneManager() {
        return zoneManager;
    }

    public NpcManager npcManager() {
        return npcManager;
    }

    public LootManager lootManager() {
        return lootManager;
    }

    public SkillManager skillManager() {
        return skillManager;
    }

    public SkillHotbarManager skillHotbarManager() {
        return skillHotbarManager;
    }

    public ClassManager classManager() {
        return classManager;
    }

    public FactionManager factionManager() {
        return factionManager;
    }

    public PartyManager partyManager() {
        return partyManager;
    }

    public GuiManager guiManager() {
        return guiManager;
    }

    public PromptManager promptManager() {
        return promptManager;
    }

    public ItemGenerator itemGenerator() {
        return itemGenerator;
    }

    public SkillEffectRegistry skillEffects() {
        return skillEffects;
    }

    public AuditLog auditLog() {
        return auditLog;
    }

    public NamespacedKey questKey() {
        return questKey;
    }

    public NamespacedKey skillKey() {
        return skillKey;
    }

    public NamespacedKey wandKey() {
        return wandKey;
    }

    public boolean useSkill(Player player, String skillId) {
        var skill = skillManager.getSkill(skillId);
        if (skill == null) {
            player.sendMessage("§cUnbekannter Skill.");
            return false;
        }
        var profile = playerDataManager.getProfile(player);
        if (!profile.learnedSkills().containsKey(skillId)) {
            player.sendMessage("§cSkill nicht gelernt.");
            return false;
        }
        if (skill.type() == com.example.rpg.model.SkillType.PASSIVE) {
            player.sendMessage("§ePassiver Skill ist aktiv.");
            return false;
        }
        long now = System.currentTimeMillis();
        long last = profile.skillCooldowns().getOrDefault(skillId, 0L);
        if (now - last < skill.cooldown() * 1000L) {
            long remaining = (skill.cooldown() * 1000L - (now - last)) / 1000L;
            player.sendMessage("§cCooldown: " + remaining + "s");
            return false;
        }
        if (profile.mana() < skill.manaCost()) {
            player.sendMessage("§cNicht genug Mana.");
            return false;
        }
        profile.setMana(profile.mana() - skill.manaCost());
        for (var effect : skill.effects()) {
            skillEffects.apply(effect, player, profile);
        }
        profile.skillCooldowns().put(skillId, now);
        player.sendMessage("§aSkill benutzt: " + skill.name());
        return true;
    }

    public boolean toggleDebug(UUID uuid) {
        if (debugPlayers.contains(uuid)) {
            debugPlayers.remove(uuid);
            return false;
        }
        debugPlayers.add(uuid);
        return true;
    }

    private void startDebugTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (UUID uuid : debugPlayers) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) {
                    continue;
                }
                var zone = zoneManager.getZoneAt(player.getLocation());
                String zoneName = zone != null ? zone.name() : "Keine Zone";
                player.sendActionBar("§7Zone: §f" + zoneName + " §7Quest: §f" + playerDataManager.getProfile(player).activeQuests().size());
            }
        }, 20L, 40L);
    }

    private void startManaRegenTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                var profile = playerDataManager.getProfile(player);
                profile.setMana(Math.min(profile.maxMana(), profile.mana() + 5));
            }
        }, 20L, 40L);
    }

    public boolean completeQuestIfReady(Player player, com.example.rpg.model.Quest quest, com.example.rpg.model.QuestProgress progress) {
        if (progress.completed()) {
            return false;
        }
        boolean done = true;
        for (int i = 0; i < quest.steps().size(); i++) {
            int required = quest.steps().get(i).amount();
            if (progress.getStepProgress(i) < required) {
                done = false;
                break;
            }
        }
        if (!done) {
            return false;
        }
        progress.setCompleted(true);
        var profile = playerDataManager.getProfile(player);
        profile.completedQuests().add(quest.id());
        profile.activeQuests().remove(quest.id());
        profile.addXp(quest.reward().xp());
        profile.setSkillPoints(profile.skillPoints() + quest.reward().skillPoints());
        quest.reward().factionRep().forEach((id, amount) ->
            profile.factionRep().put(id, profile.factionRep().getOrDefault(id, 0) + amount)
        );
        player.sendMessage("§aQuest abgeschlossen: " + quest.name());
        return true;
    }
}

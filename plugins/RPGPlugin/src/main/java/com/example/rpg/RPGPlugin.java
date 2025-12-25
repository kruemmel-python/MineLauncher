package com.example.rpg;

import com.example.rpg.command.PartyCommand;
import com.example.rpg.command.RPGAdminCommand;
import com.example.rpg.command.RPGCommand;
import com.example.rpg.command.AuctionCommand;
import com.example.rpg.command.DungeonCommand;
import com.example.rpg.command.GuildCommand;
import com.example.rpg.command.PvpCommand;
import com.example.rpg.command.TradeCommand;
import com.example.rpg.db.DatabaseService;
import com.example.rpg.db.PlayerDao;
import com.example.rpg.db.SqlPlayerDao;
import com.example.rpg.gui.GuiManager;
import com.example.rpg.gui.SkillTreeGui;
import com.example.rpg.listener.ArenaListener;
import com.example.rpg.listener.CombatListener;
import com.example.rpg.listener.CustomMobListener;
import com.example.rpg.listener.DamageIndicatorListener;
import com.example.rpg.listener.GuiListener;
import com.example.rpg.listener.ItemStatListener;
import com.example.rpg.listener.NpcListener;
import com.example.rpg.listener.NpcProtectionListener;
import com.example.rpg.listener.PlayerListener;
import com.example.rpg.listener.ProfessionListener;
import com.example.rpg.listener.SkillHotbarListener;
import com.example.rpg.listener.ZoneListener;
import com.example.rpg.manager.ArenaManager;
import com.example.rpg.manager.BehaviorTreeManager;
import com.example.rpg.manager.ClassManager;
import com.example.rpg.manager.DungeonManager;
import com.example.rpg.manager.GuildManager;
import com.example.rpg.manager.FactionManager;
import com.example.rpg.manager.ItemStatManager;
import com.example.rpg.manager.LootManager;
import com.example.rpg.manager.MobManager;
import com.example.rpg.manager.NpcManager;
import com.example.rpg.manager.PartyManager;
import com.example.rpg.manager.PlayerDataManager;
import com.example.rpg.manager.SkillTreeManager;
import com.example.rpg.manager.QuestManager;
import com.example.rpg.manager.AuctionHouseManager;
import com.example.rpg.manager.ShopManager;
import com.example.rpg.manager.SkillHotbarManager;
import com.example.rpg.manager.SkillManager;
import com.example.rpg.manager.SpawnerManager;
import com.example.rpg.manager.ZoneManager;
import com.example.rpg.manager.TradeManager;
import com.example.rpg.manager.ProfessionManager;
import com.example.rpg.util.ItemGenerator;
import com.example.rpg.util.AuditLog;
import com.example.rpg.util.PromptManager;
import com.example.rpg.skill.SkillEffectRegistry;
import com.example.rpg.skill.SkillEffectType;
import com.example.rpg.skill.effects.DamageEffect;
import com.example.rpg.skill.effects.HealEffect;
import com.example.rpg.skill.effects.ParticleEffect;
import com.example.rpg.skill.effects.PotionStatusEffect;
import com.example.rpg.skill.effects.ProjectileEffect;
import com.example.rpg.skill.effects.SoundEffect;
import com.example.rpg.skill.effects.VelocityEffect;
import com.example.rpg.skill.effects.AggroEffect;
import com.example.rpg.skill.effects.XpEffect;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class RPGPlugin extends JavaPlugin {
    private DatabaseService databaseService;
    private PlayerDataManager playerDataManager;
    private QuestManager questManager;
    private ZoneManager zoneManager;
    private NpcManager npcManager;
    private LootManager lootManager;
    private MobManager mobManager;
    private SkillManager skillManager;
    private SkillHotbarManager skillHotbarManager;
    private ClassManager classManager;
    private SpawnerManager spawnerManager;
    private ShopManager shopManager;
    private AuctionHouseManager auctionHouseManager;
    private TradeManager tradeManager;
    private ProfessionManager professionManager;
    private DungeonManager dungeonManager;
    private ArenaManager arenaManager;
    private BehaviorTreeManager behaviorTreeManager;
    private GuildManager guildManager;
    private FactionManager factionManager;
    private PartyManager partyManager;
    private GuiManager guiManager;
    private SkillTreeGui skillTreeGui;
    private SkillTreeManager skillTreeManager;
    private ItemStatManager itemStatManager;
    private PromptManager promptManager;
    private ItemGenerator itemGenerator;
    private SkillEffectRegistry skillEffects;
    private final Set<UUID> debugPlayers = new HashSet<>();
    private CustomMobListener customMobListener;
    private final java.util.Map<UUID, Long> actionBarErrorUntil = new java.util.HashMap<>();
    private final java.util.Map<UUID, String> actionBarErrorMessage = new java.util.HashMap<>();
    private NamespacedKey questKey;
    private NamespacedKey skillKey;
    private NamespacedKey wandKey;
    private AuditLog auditLog;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        databaseService = new DatabaseService(this);
        databaseService.initTables();
        PlayerDao playerDao = new SqlPlayerDao(databaseService);
        playerDataManager = new PlayerDataManager(this, playerDao);
        questManager = new QuestManager(this);
        zoneManager = new ZoneManager(this);
        npcManager = new NpcManager(this);
        lootManager = new LootManager(this);
        mobManager = new MobManager(this);
        behaviorTreeManager = new BehaviorTreeManager(this);
        skillManager = new SkillManager(this);
        skillHotbarManager = new SkillHotbarManager(playerDataManager);
        classManager = new ClassManager(this);
        factionManager = new FactionManager(this);
        spawnerManager = new SpawnerManager(this);
        shopManager = new ShopManager(this);
        auctionHouseManager = new AuctionHouseManager(this);
        tradeManager = new TradeManager();
        professionManager = new ProfessionManager(this);
        dungeonManager = new DungeonManager(this);
        arenaManager = new ArenaManager(this);
        guildManager = new GuildManager(this);
        partyManager = new PartyManager();
        promptManager = new PromptManager();
        itemStatManager = new ItemStatManager(this);
        itemGenerator = new ItemGenerator(this, itemStatManager);
        questKey = new NamespacedKey(this, "quest_id");
        skillKey = new NamespacedKey(this, "skill_id");
        wandKey = new NamespacedKey(this, "editor_wand");
        skillEffects = new SkillEffectRegistry()
            .register(SkillEffectType.HEAL, new HealEffect())
            .register(SkillEffectType.DAMAGE, new DamageEffect())
            .register(SkillEffectType.PROJECTILE, new ProjectileEffect())
            .register(SkillEffectType.POTION, new PotionStatusEffect())
            .register(SkillEffectType.SOUND, new SoundEffect())
            .register(SkillEffectType.XP, new XpEffect())
            .register(SkillEffectType.PARTICLE, new ParticleEffect())
            .register(SkillEffectType.VELOCITY, new VelocityEffect())
            .register(SkillEffectType.AGGRO, new AggroEffect());
        guiManager = new GuiManager(playerDataManager, questManager, skillManager, classManager, factionManager, questKey, skillKey);
        skillTreeManager = new SkillTreeManager(skillManager);
        skillTreeGui = new SkillTreeGui(this);
        auditLog = new AuditLog(this);

        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CombatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ZoneListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DamageIndicatorListener(this), this);
        Bukkit.getPluginManager().registerEvents(new NpcListener(this), this);
        Bukkit.getPluginManager().registerEvents(new NpcProtectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GuiListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SkillHotbarListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ProfessionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ItemStatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ArenaListener(this), this);
        customMobListener = new CustomMobListener(this);
        Bukkit.getPluginManager().registerEvents(customMobListener, this);

        getCommand("rpg").setExecutor(new RPGCommand(this));
        getCommand("rpgadmin").setExecutor(new RPGAdminCommand(this));
        getCommand("party").setExecutor(new PartyCommand(this));
        getCommand("p").setExecutor(new PartyCommand(this));
        getCommand("auction").setExecutor(new AuctionCommand(this));
        getCommand("trade").setExecutor(new TradeCommand(this));
        getCommand("dungeon").setExecutor(new DungeonCommand(this));
        getCommand("guild").setExecutor(new GuildCommand(this));
        getCommand("g").setExecutor(new GuildCommand(this));
        getCommand("pvp").setExecutor(new PvpCommand(this));

        npcManager.spawnAll();
        startDebugTask();
        startManaRegenTask();
        startHudTask();
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }
        if (questManager != null) {
            questManager.saveAll();
        }
        if (zoneManager != null) {
            zoneManager.saveAll();
        }
        if (npcManager != null) {
            npcManager.saveAll();
        }
        if (lootManager != null) {
            lootManager.saveAll();
        }
        if (mobManager != null) {
            mobManager.saveAll();
        }
        if (skillManager != null) {
            skillManager.saveAll();
        }
        if (classManager != null) {
            classManager.saveAll();
        }
        if (factionManager != null) {
            factionManager.saveAll();
        }
        if (spawnerManager != null) {
            spawnerManager.saveAll();
        }
        if (shopManager != null) {
            shopManager.saveAll();
        }
        if (auctionHouseManager != null) {
            auctionHouseManager.listings().values().forEach(auctionHouseManager::saveListing);
        }
        if (guildManager != null) {
            guildManager.saveAll();
        }
        if (dungeonManager != null) {
            getLogger().info("Cleaning up dungeon worlds...");
            dungeonManager.shutdown();
        }
        if (databaseService != null) {
            databaseService.shutdown();
        }
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

    public MobManager mobManager() {
        return mobManager;
    }

    public BehaviorTreeManager behaviorTreeManager() {
        return behaviorTreeManager;
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

    public SpawnerManager spawnerManager() {
        return spawnerManager;
    }

    public ShopManager shopManager() {
        return shopManager;
    }

    public AuctionHouseManager auctionHouseManager() {
        return auctionHouseManager;
    }

    public TradeManager tradeManager() {
        return tradeManager;
    }

    public ProfessionManager professionManager() {
        return professionManager;
    }

    public DungeonManager dungeonManager() {
        return dungeonManager;
    }

    public ArenaManager arenaManager() {
        return arenaManager;
    }

    public GuildManager guildManager() {
        return guildManager;
    }

    public SkillTreeManager skillTreeManager() {
        return skillTreeManager;
    }

    public SkillTreeGui skillTreeGui() {
        return skillTreeGui;
    }

    public ItemStatManager itemStatManager() {
        return itemStatManager;
    }

    public CustomMobListener customMobListener() {
        return customMobListener;
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
            notifySkillError(player, "Unbekannter Skill");
            return false;
        }
        var profile = playerDataManager.getProfile(player);
        if (!profile.learnedSkills().containsKey(skillId)) {
            notifySkillError(player, "Skill nicht gelernt");
            return false;
        }
        if (skill.type() == com.example.rpg.model.SkillType.PASSIVE) {
            notifySkillError(player, "Passiver Skill ist aktiv");
            return false;
        }
        long now = System.currentTimeMillis();
        long last = profile.skillCooldowns().getOrDefault(skillId, 0L);
        if (now - last < skill.cooldown() * 1000L) {
            long remaining = (skill.cooldown() * 1000L - (now - last)) / 1000L;
            notifySkillError(player, "Cooldown: " + remaining + "s");
            return false;
        }
        if (profile.mana() < skill.manaCost()) {
            notifySkillError(player, "Nicht genug Mana");
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

    public boolean useMobSkill(org.bukkit.entity.LivingEntity caster, Player target, String skillId) {
        var skill = skillManager.getSkill(skillId);
        if (skill == null) {
            return false;
        }
        for (var effect : skill.effects()) {
            switch (effect.type()) {
                case DAMAGE -> {
                    double amount = parseDouble(effect.params().getOrDefault("amount", 4));
                    target.damage(amount, caster);
                }
                case PROJECTILE -> {
                    String type = String.valueOf(effect.params().getOrDefault("type", "SNOWBALL")).toUpperCase();
                    if ("SMALL_FIREBALL".equals(type)) {
                        caster.launchProjectile(org.bukkit.entity.SmallFireball.class);
                    } else {
                        caster.launchProjectile(org.bukkit.entity.Snowball.class);
                    }
                }
                case POTION -> {
                    String type = String.valueOf(effect.params().getOrDefault("type", "SLOW")).toUpperCase();
                    int duration = (int) parseDouble(effect.params().getOrDefault("duration", 60));
                    int amplifier = (int) parseDouble(effect.params().getOrDefault("amplifier", 0));
                    var potion = org.bukkit.potion.PotionEffectType.getByName(type);
                    if (potion != null) {
                        target.addPotionEffect(new org.bukkit.potion.PotionEffect(potion, duration, amplifier));
                    }
                }
                case PARTICLE -> {
                    String particleName = String.valueOf(effect.params().getOrDefault("type", "SMOKE")).toUpperCase();
                    int count = (int) parseDouble(effect.params().getOrDefault("count", 10));
                    double speed = parseDouble(effect.params().getOrDefault("speed", 0.01));
                    org.bukkit.Particle particle;
                    try {
                        particle = org.bukkit.Particle.valueOf(particleName);
                    } catch (IllegalArgumentException e) {
                        particle = org.bukkit.Particle.SMOKE_NORMAL;
                    }
                    caster.getWorld().spawnParticle(particle, caster.getLocation().add(0, 1.0, 0), count, 0.3, 0.6, 0.3, speed);
                }
                case SOUND -> {
                    String soundName = String.valueOf(effect.params().getOrDefault("sound", "ENTITY_ZOMBIE_HURT")).toUpperCase();
                    float volume = (float) parseDouble(effect.params().getOrDefault("volume", 1.0));
                    float pitch = (float) parseDouble(effect.params().getOrDefault("pitch", 1.0));
                    org.bukkit.Sound sound;
                    try {
                        sound = org.bukkit.Sound.valueOf(soundName);
                    } catch (IllegalArgumentException e) {
                        sound = org.bukkit.Sound.ENTITY_ZOMBIE_HURT;
                    }
                    caster.getWorld().playSound(caster.getLocation(), sound, volume, pitch);
                }
                default -> {
                }
            }
        }
        return true;
    }

    private double parseDouble(Object raw) {
        try {
            return Double.parseDouble(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public void notifySkillError(Player player, String message) {
        actionBarErrorUntil.put(player.getUniqueId(), System.currentTimeMillis() + 2000L);
        actionBarErrorMessage.put(player.getUniqueId(), message);
        player.sendActionBar("§c" + message);
    }

    private void startHudTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                var profile = playerDataManager.getProfile(player);
                String health = String.format("❤ Leben: %.0f/%.0f", player.getHealth(),
                    player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null
                        ? player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()
                        : 20.0);
                String mana = "🔵 Mana: " + profile.mana() + "/" + profile.maxMana();
                String gold = "💰 " + profile.gold();
                Long until = actionBarErrorUntil.get(player.getUniqueId());
                if (until != null && until > System.currentTimeMillis()) {
                    String msg = actionBarErrorMessage.getOrDefault(player.getUniqueId(), "Fehler");
                    player.sendActionBar("§c" + msg);
                } else {
                    player.sendActionBar("§f" + health + " §7| §f" + mana + " §7| §6" + gold);
                }

                int slot = player.getInventory().getHeldItemSlot() + 1;
                String skillId = skillHotbarManager.getBinding(profile, slot);
                if (skillId != null) {
                    var skill = skillManager.getSkill(skillId);
                    if (skill != null && skill.cooldown() > 0) {
                        long last = profile.skillCooldowns().getOrDefault(skillId, 0L);
                        long remaining = skill.cooldown() * 1000L - (System.currentTimeMillis() - last);
                        if (remaining > 0) {
                            float progress = Math.max(0f, Math.min(1f, remaining / (skill.cooldown() * 1000f)));
                            player.setExp(progress);
                            player.setLevel((int) Math.ceil(remaining / 1000f));
                        } else {
                            player.setExp(0f);
                            player.setLevel(profile.level());
                        }
                    }
                }
            }
        }, 10L, 10L);
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

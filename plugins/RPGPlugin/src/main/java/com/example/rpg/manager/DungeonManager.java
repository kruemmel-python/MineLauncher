package com.example.rpg.manager;

import com.example.rpg.RPGPlugin;
import com.example.rpg.dungeon.DungeonGenerator;
import com.example.rpg.dungeon.DungeonInstance;
import com.example.rpg.util.Text;
import com.example.rpg.util.WorldUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class DungeonManager {
    private final RPGPlugin plugin;
    private final FileConfiguration config;
    private Location entrance;
    private Location exit;
    private final Map<UUID, Location> returnLocations = new HashMap<>();
    private final DungeonGenerator generator;
    private final Map<UUID, DungeonInstance> activeInstances = new HashMap<>();
    private final List<DungeonInstance> allInstances = new ArrayList<>();
    private final Map<String, DungeonInstance> instanceByWorld = new HashMap<>();
    private final Map<String, java.util.List<UUID>> queueByTheme = new HashMap<>();
    private final Map<UUID, String> queuedTheme = new HashMap<>();

    public DungeonManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.generator = new DungeonGenerator(plugin);
        load();
    }

    public Location getEntrance() {
        return entrance;
    }

    public void enterDungeon(org.bukkit.entity.Player player) {
        returnLocations.put(player.getUniqueId(), player.getLocation());
        if (entrance != null) {
            player.teleport(entrance);
        }
    }

    public void leaveDungeon(org.bukkit.entity.Player player) {
        Location back = returnLocations.remove(player.getUniqueId());
        activeInstances.remove(player.getUniqueId());
        if (back != null) {
            player.teleport(back);
            return;
        }
        if (exit != null) {
            player.teleport(exit);
            return;
        }
        if (!plugin.getServer().getWorlds().isEmpty()) {
            player.teleport(plugin.getServer().getWorlds().get(0).getSpawnLocation());
        }
    }

    public void generateDungeon(Player player, String theme, List<Player> party) {
        if (!party.contains(player)) {
            party.add(player);
        }
        java.util.Set<UUID> participants = new java.util.HashSet<>();
        for (Player member : party) {
            participants.add(member.getUniqueId());
        }
        double scale = computeScale(party);
        java.util.function.Consumer<DungeonInstance> onGenerated = instance -> {
            allInstances.add(instance);
            instanceByWorld.put(instance.world().getName(), instance);
            for (Player member : party) {
                returnLocations.put(member.getUniqueId(), member.getLocation());
                activeInstances.put(member.getUniqueId(), instance);
                if (instance.start() != null) {
                    member.teleport(instance.start());
                    member.sendMessage(Text.mm("<green>Dungeon generiert: " + theme));
                }
            }
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> closeDungeon(instance), 20L * 60L * 15L);
        };

        if ("wfc".equalsIgnoreCase(theme)) {
            generator.generateWfc(theme, party, participants, scale, onGenerated);
            return;
        }
        DungeonInstance instance = generator.generate(theme, party, participants, scale);
        onGenerated.accept(instance);
    }

    public void closeDungeon(DungeonInstance instance) {
        if (!allInstances.contains(instance)) {
            return;
        }
        WorldUtils.unloadAndDeleteWorld(instance.world(), exit != null ? exit : entrance);
        allInstances.remove(instance);
        activeInstances.values().removeIf(active -> active.equals(instance));
        instanceByWorld.remove(instance.world().getName());
    }

    public void shutdown() {
        for (DungeonInstance instance : new ArrayList<>(allInstances)) {
            WorldUtils.unloadAndDeleteWorld(instance.world(), exit);
        }
        allInstances.clear();
        activeInstances.clear();
        instanceByWorld.clear();
    }

    public void joinQueue(Player player, String theme) {
        if (!hasFactionAccess(player, theme)) {
            player.sendMessage(Text.mm("<red>Dein Ruf reicht für diesen Dungeon nicht aus."));
            return;
        }
        if (queuedTheme.containsKey(player.getUniqueId()) || activeInstances.containsKey(player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Du bist bereits in einer Queue oder Instanz."));
            return;
        }
        queueByTheme.computeIfAbsent(theme, ignored -> new ArrayList<>()).add(player.getUniqueId());
        queuedTheme.put(player.getUniqueId(), theme);
        player.sendMessage(Text.mm("<green>Dungeon-Queue beigetreten: " + theme));
        tryMatch(theme);
    }

    public void leaveQueue(Player player) {
        String theme = queuedTheme.remove(player.getUniqueId());
        if (theme == null) {
            return;
        }
        queueByTheme.getOrDefault(theme, new ArrayList<>()).remove(player.getUniqueId());
        player.sendMessage(Text.mm("<yellow>Dungeon-Queue verlassen."));
    }

    public DungeonInstance instanceForWorld(org.bukkit.World world) {
        return instanceByWorld.get(world.getName());
    }

    public void markDeath(Player player) {
        DungeonInstance instance = activeInstances.get(player.getUniqueId());
        if (instance != null) {
            instance.setNoDeath(false);
        }
    }

    public void completeDungeon(DungeonInstance instance) {
        if (instance == null) {
            return;
        }
        for (UUID participant : instance.participants()) {
            Player player = plugin.getServer().getPlayer(participant);
            if (player == null) {
                continue;
            }
            var profile = plugin.playerDataManager().getProfile(player);
            int xp = (int) Math.round(120 * instance.scale());
            int gold = (int) Math.round(80 * instance.scale());
            if (instance.noDeath()) {
                xp += 50;
                gold += 40;
            }
            profile.addXp(xp);
            profile.setGold(profile.gold() + gold);
            player.sendMessage(Text.mm("<gold>Dungeon abgeschlossen!</gold> +" + xp + " XP, +" + gold + " Gold"));
            if (instance.noDeath()) {
                player.sendMessage(Text.mm("<green>No-Death Bonus erhalten!"));
            }
        }
        closeDungeon(instance);
    }

    public boolean hasFactionAccess(Player player, String theme) {
        String factionId = config.getString("dungeon.requireFaction." + theme, null);
        if (factionId == null) {
            return true;
        }
        var profile = plugin.playerDataManager().getProfile(player);
        int rep = profile.factionRep().getOrDefault(factionId, 0);
        var rank = plugin.factionManager().getRank(factionId, rep);
        return rank != null && rank.dungeonAccess();
    }

    private void load() {
        String world = config.getString("dungeon.entrance.world", null);
        if (world != null && plugin.getServer().getWorld(world) != null) {
            entrance = new Location(plugin.getServer().getWorld(world),
                config.getDouble("dungeon.entrance.x"),
                config.getDouble("dungeon.entrance.y"),
                config.getDouble("dungeon.entrance.z"));
        }
        String exitWorld = config.getString("dungeon.exit.world", null);
        if (exitWorld != null && plugin.getServer().getWorld(exitWorld) != null) {
            exit = new Location(plugin.getServer().getWorld(exitWorld),
                config.getDouble("dungeon.exit.x"),
                config.getDouble("dungeon.exit.y"),
                config.getDouble("dungeon.exit.z"));
        }
    }

    private void tryMatch(String theme) {
        java.util.List<UUID> queue = queueByTheme.getOrDefault(theme, new ArrayList<>());
        if (queue.size() < 4) {
            return;
        }
        UUID tank = null;
        UUID heal = null;
        java.util.List<UUID> dps = new ArrayList<>();
        for (UUID uuid : new ArrayList<>(queue)) {
            var profile = plugin.playerDataManager().getProfile(uuid);
            String role = profile.dungeonRole() != null ? profile.dungeonRole().toUpperCase() : "DPS";
            switch (role) {
                case "TANK" -> {
                    if (tank == null) {
                        tank = uuid;
                    }
                }
                case "HEAL" -> {
                    if (heal == null) {
                        heal = uuid;
                    }
                }
                default -> dps.add(uuid);
            }
        }
        if (tank == null || heal == null || dps.size() < 2) {
            return;
        }
        java.util.List<Player> party = new ArrayList<>();
        addIfOnline(party, tank);
        addIfOnline(party, heal);
        addIfOnline(party, dps.get(0));
        addIfOnline(party, dps.get(1));
        queue.remove(tank);
        queue.remove(heal);
        queue.remove(dps.get(0));
        queue.remove(dps.get(1));
        queuedTheme.remove(tank);
        queuedTheme.remove(heal);
        queuedTheme.remove(dps.get(0));
        queuedTheme.remove(dps.get(1));
        for (Player member : party) {
            member.sendMessage(Text.mm("<green>Dungeon-Gruppe gefunden!"));
        }
        if (!party.isEmpty()) {
            generateDungeon(party.get(0), theme, party);
        }
    }

    private void addIfOnline(List<Player> party, UUID uuid) {
        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null) {
            party.add(player);
        }
    }

    private double computeScale(List<Player> party) {
        if (party.isEmpty()) {
            return 1.0;
        }
        double avg = party.stream()
            .mapToInt(player -> plugin.playerDataManager().getProfile(player).level())
            .average()
            .orElse(1.0);
        return Math.max(1.0, 1.0 + ((avg - 1) * 0.05));
    }
}

package com.example.rpg.manager;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.Arena;
import com.example.rpg.model.ArenaStatus;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.util.EloCalculator;
import com.example.rpg.util.Text;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class ArenaManager {
    private final RPGPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Arena> arenas = new HashMap<>();
    private final Map<UUID, Arena> arenaByPlayer = new HashMap<>();
    private final Queue<UUID> queue = new ArrayDeque<>();

    public ArenaManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "arenas.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public void joinQueue(Player player) {
        if (arenaByPlayer.containsKey(player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Du bist bereits in einer Arena."));
            return;
        }
        if (queue.contains(player.getUniqueId())) {
            player.sendMessage(Text.mm("<yellow>Du bist bereits in der Warteschlange."));
            return;
        }
        queue.add(player.getUniqueId());
        player.sendMessage(Text.mm("<green>Du bist der PvP-Warteschlange beigetreten."));
        tryStartMatch();
    }

    public void removeFromQueue(Player player) {
        queue.remove(player.getUniqueId());
    }

    public Optional<Arena> arenaFor(Player player) {
        return Optional.ofNullable(arenaByPlayer.get(player.getUniqueId()));
    }

    public void handleDeath(Player loser) {
        Arena arena = arenaByPlayer.get(loser.getUniqueId());
        if (arena == null) {
            return;
        }
        Player winner = plugin.getServer().getPlayer(other(arena, loser.getUniqueId()));
        endMatch(arena, winner, loser);
    }

    public List<PlayerProfile> topPlayers(int limit) {
        List<PlayerProfile> profiles = new ArrayList<>(plugin.playerDataManager().profiles().values());
        profiles.sort(Comparator.comparingInt(PlayerProfile::elo).reversed());
        return profiles.subList(0, Math.min(limit, profiles.size()));
    }

    private void tryStartMatch() {
        if (queue.size() < 2) {
            return;
        }
        Arena arena = arenas.values().stream()
            .filter(a -> a.status() == ArenaStatus.WAITING)
            .findFirst()
            .orElse(null);
        if (arena == null) {
            return;
        }
        UUID playerOne = queue.poll();
        UUID playerTwo = queue.poll();
        Player p1 = plugin.getServer().getPlayer(playerOne);
        Player p2 = plugin.getServer().getPlayer(playerTwo);
        if (p1 == null || p2 == null) {
            if (p1 != null) {
                queue.add(p1.getUniqueId());
            }
            if (p2 != null) {
                queue.add(p2.getUniqueId());
            }
            return;
        }
        arena.setPlayerOne(playerOne);
        arena.setPlayerTwo(playerTwo);
        arena.setStatus(ArenaStatus.FIGHTING);
        arenaByPlayer.put(playerOne, arena);
        arenaByPlayer.put(playerTwo, arena);
        teleportPlayers(arena, p1, p2);
        p1.sendMessage(Text.mm("<gold>PvP-Kampf gestartet!"));
        p2.sendMessage(Text.mm("<gold>PvP-Kampf gestartet!"));
    }

    private void teleportPlayers(Arena arena, Player p1, Player p2) {
        World world = plugin.getServer().getWorld(arena.world());
        if (world == null) {
            return;
        }
        p1.teleport(new Location(world, arena.spawn1x() + 0.5, arena.spawn1y(), arena.spawn1z() + 0.5));
        p2.teleport(new Location(world, arena.spawn2x() + 0.5, arena.spawn2y(), arena.spawn2z() + 0.5));
    }

    private void endMatch(Arena arena, Player winner, Player loser) {
        arena.setStatus(ArenaStatus.ENDING);
        if (winner != null && loser != null) {
            PlayerProfile winnerProfile = plugin.playerDataManager().getProfile(winner);
            PlayerProfile loserProfile = plugin.playerDataManager().getProfile(loser);
            int winnerNew = EloCalculator.calculateNewRating(winnerProfile.elo(), loserProfile.elo(), 1.0, 32);
            int loserNew = EloCalculator.calculateNewRating(loserProfile.elo(), winnerProfile.elo(), 0.0, 32);
            winnerProfile.setElo(winnerNew);
            loserProfile.setElo(loserNew);
            winner.sendMessage(Text.mm("<green>Du hast gewonnen! Neuer ELO: " + winnerNew));
            loser.sendMessage(Text.mm("<red>Du hast verloren! Neuer ELO: " + loserNew));
            plugin.playerDataManager().saveProfile(winnerProfile);
            plugin.playerDataManager().saveProfile(loserProfile);
        }
        arenaByPlayer.remove(arena.playerOne());
        arenaByPlayer.remove(arena.playerTwo());
        arena.setPlayerOne(null);
        arena.setPlayerTwo(null);
        arena.setStatus(ArenaStatus.WAITING);
    }

    private UUID other(Arena arena, UUID player) {
        if (arena.playerOne() != null && arena.playerOne().equals(player)) {
            return arena.playerTwo();
        }
        return arena.playerOne();
    }

    private void load() {
        arenas.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Arena arena = new Arena(id);
            arena.setWorld(section.getString("world", "world"));
            arena.setX1(section.getInt("pos1.x"));
            arena.setY1(section.getInt("pos1.y"));
            arena.setZ1(section.getInt("pos1.z"));
            arena.setX2(section.getInt("pos2.x"));
            arena.setY2(section.getInt("pos2.y"));
            arena.setZ2(section.getInt("pos2.z"));
            arena.setSpawn1x(section.getInt("spawn1.x"));
            arena.setSpawn1y(section.getInt("spawn1.y"));
            arena.setSpawn1z(section.getInt("spawn1.z"));
            arena.setSpawn2x(section.getInt("spawn2.x"));
            arena.setSpawn2y(section.getInt("spawn2.y"));
            arena.setSpawn2z(section.getInt("spawn2.z"));
            arenas.put(id, arena);
        }
    }

    private void seedDefaults() {
        config.set("arena1.world", "world");
        config.set("arena1.pos1.x", -10);
        config.set("arena1.pos1.y", 60);
        config.set("arena1.pos1.z", -10);
        config.set("arena1.pos2.x", 10);
        config.set("arena1.pos2.y", 70);
        config.set("arena1.pos2.z", 10);
        config.set("arena1.spawn1.x", -5);
        config.set("arena1.spawn1.y", 65);
        config.set("arena1.spawn1.z", 0);
        config.set("arena1.spawn2.x", 5);
        config.set("arena1.spawn2.y", 65);
        config.set("arena1.spawn2.z", 0);
        save();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save arenas.yml: " + e.getMessage());
        }
    }
}

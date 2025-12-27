package com.example.rpg.manager;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.PvpSeason;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.util.Text;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import org.bukkit.configuration.file.YamlConfiguration;

public class PvpSeasonManager {
    private final RPGPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private PvpSeason season;

    public PvpSeasonManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "pvp_season.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public PvpSeason currentSeason() {
        return season;
    }

    public void startSeason(String id, String name, long endTimestamp) {
        PvpSeason newSeason = new PvpSeason(id);
        newSeason.setName(name);
        newSeason.setEndTimestamp(endTimestamp);
        this.season = newSeason;
        resetRatings();
        save();
        plugin.getServer().broadcast(Text.mm("<gold>PvP-Saison gestartet:</gold> " + name));
    }

    public void endSeason() {
        if (season == null) {
            return;
        }
        awardRewards();
        save();
        plugin.getServer().broadcast(Text.mm("<yellow>PvP-Saison beendet:</yellow> " + season.name()));
        season = null;
    }

    public void checkSeasonEnd() {
        if (season == null) {
            return;
        }
        if (!season.isActive()) {
            endSeason();
        }
    }

    private void resetRatings() {
        for (PlayerProfile profile : plugin.playerDataManager().profiles().values()) {
            profile.setElo(1000);
        }
    }

    private void awardRewards() {
        List<PlayerProfile> top = plugin.playerDataManager().profiles().values().stream()
            .sorted(Comparator.comparingInt(PlayerProfile::elo).reversed())
            .limit(3)
            .toList();
        if (top.isEmpty()) {
            return;
        }
        award(top.get(0), "Champion", "pvp_title_champion");
        if (top.size() > 1) {
            award(top.get(1), "Gladiator", "pvp_title_gladiator");
        }
        if (top.size() > 2) {
            award(top.get(2), "Contender", "pvp_title_contender");
        }
    }

    private void award(PlayerProfile profile, String title, String cosmeticId) {
        profile.setTitle(title);
        profile.cosmetics().add(cosmeticId);
        var player = plugin.getServer().getPlayer(profile.uuid());
        if (player != null) {
            player.sendMessage(Text.mm("<gold>PvP-Belohnung:</gold> Titel " + title));
        }
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        String id = config.getString("id", null);
        if (id == null) {
            return;
        }
        season = new PvpSeason(id);
        season.setName(config.getString("name", id));
        season.setEndTimestamp(config.getLong("endTimestamp", 0));
    }

    private void save() {
        if (season == null) {
            config.set("id", null);
            config.set("name", null);
            config.set("endTimestamp", null);
        } else {
            config.set("id", season.id());
            config.set("name", season.name());
            config.set("endTimestamp", season.endTimestamp());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save pvp_season.yml: " + e.getMessage());
        }
    }
}

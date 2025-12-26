package com.example.rpg.manager;

import com.example.rpg.model.Faction;
import com.example.rpg.model.FactionRank;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class FactionManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Faction> factions = new HashMap<>();

    public FactionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "factions.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public Faction getFaction(String id) {
        return factions.get(id);
    }

    public Map<String, Faction> factions() {
        return factions;
    }

    public FactionRank getRank(String factionId, int rep) {
        Faction faction = factions.get(factionId);
        if (faction == null) {
            return null;
        }
        return faction.rankForRep(rep);
    }

    public void saveFaction(Faction faction) {
        ConfigurationSection section = config.createSection(faction.id());
        section.set("name", faction.name());
        section.set("ranks", serializeRanks(faction));
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (Faction faction : factions.values()) {
            saveFaction(faction);
        }
        save();
    }

    private void load() {
        factions.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Faction faction = new Faction(id);
            faction.setName(section.getString("name", id));
            faction.setRanks(loadRanks(section));
            factions.put(id, faction);
        }
    }

    private void seedDefaults() {
        Faction faction = new Faction("adventurers");
        faction.setName("Abenteurergilde");
        faction.setRanks(new ArrayList<>());
        FactionRank neutral = new FactionRank("neutral");
        neutral.setName("Neutral");
        neutral.setMinRep(0);
        neutral.setShopDiscount(0.0);
        neutral.setDungeonAccess(false);
        FactionRank friendly = new FactionRank("friendly");
        friendly.setName("Freundlich");
        friendly.setMinRep(250);
        friendly.setShopDiscount(0.1);
        friendly.setDungeonAccess(false);
        FactionRank revered = new FactionRank("revered");
        revered.setName("Ehrfürchtig");
        revered.setMinRep(750);
        revered.setShopDiscount(0.2);
        revered.setDungeonAccess(true);
        faction.ranks().add(neutral);
        faction.ranks().add(friendly);
        faction.ranks().add(revered);
        factions.put(faction.id(), faction);
        saveAll();
    }

    private java.util.List<Map<String, Object>> serializeRanks(Faction faction) {
        java.util.List<Map<String, Object>> ranks = new ArrayList<>();
        for (FactionRank rank : faction.ranks()) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", rank.id());
            data.put("name", rank.name());
            data.put("minRep", rank.minRep());
            data.put("shopDiscount", rank.shopDiscount());
            data.put("dungeonAccess", rank.dungeonAccess());
            ranks.add(data);
        }
        return ranks;
    }

    private java.util.List<FactionRank> loadRanks(ConfigurationSection section) {
        java.util.List<FactionRank> ranks = new ArrayList<>();
        for (Map<?, ?> raw : section.getMapList("ranks")) {
            Object idValue = raw.getOrDefault("id", "rank");
            Object nameValue = raw.getOrDefault("name", String.valueOf(idValue));
            Object minValue = raw.getOrDefault("minRep", 0);
            Object discountValue = raw.getOrDefault("shopDiscount", 0.0);
            Object accessValue = raw.getOrDefault("dungeonAccess", false);
            FactionRank rank = new FactionRank(String.valueOf(idValue));
            rank.setName(String.valueOf(nameValue));
            rank.setMinRep(Integer.parseInt(String.valueOf(minValue)));
            rank.setShopDiscount(Double.parseDouble(String.valueOf(discountValue)));
            rank.setDungeonAccess(Boolean.parseBoolean(String.valueOf(accessValue)));
            ranks.add(rank);
        }
        return ranks;
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save factions.yml: " + e.getMessage());
        }
    }
}

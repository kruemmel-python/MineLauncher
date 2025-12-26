package com.example.rpg.db;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.QuestProgress;
import com.example.rpg.model.RPGStat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SqlPlayerDao implements PlayerDao {
    private final DatabaseService databaseService;
    private final Gson gson = new Gson();
    private final Type mapStringInt = new TypeToken<Map<String, Integer>>() {}.getType();
    private final Type mapStringLong = new TypeToken<Map<String, Long>>() {}.getType();
    private final Type mapIntString = new TypeToken<Map<Integer, String>>() {}.getType();
    private final Type listString = new TypeToken<List<String>>() {}.getType();
    private final Type mapStringObject = new TypeToken<Map<String, Object>>() {}.getType();

    public SqlPlayerDao(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public CompletableFuture<Void> savePlayer(PlayerProfile profile) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = """
                INSERT INTO rpg_players (uuid, level, xp, skill_points, mana, max_mana, class_id, gold, guild_id, elo,
                    dungeon_role, home_world, home_x, home_y, home_z,
                    professions, stats, learned_skills, active_quests, completed_quests, faction_rep, skill_cooldowns, skill_bindings,
                    housing_upgrades, cosmetics, title)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb, ?, ?)
                ON CONFLICT (uuid) DO UPDATE SET
                    level = EXCLUDED.level,
                    xp = EXCLUDED.xp,
                    skill_points = EXCLUDED.skill_points,
                    mana = EXCLUDED.mana,
                    max_mana = EXCLUDED.max_mana,
                    class_id = EXCLUDED.class_id,
                    gold = EXCLUDED.gold,
                    guild_id = EXCLUDED.guild_id,
                    elo = EXCLUDED.elo,
                    dungeon_role = EXCLUDED.dungeon_role,
                    home_world = EXCLUDED.home_world,
                    home_x = EXCLUDED.home_x,
                    home_y = EXCLUDED.home_y,
                    home_z = EXCLUDED.home_z,
                    professions = EXCLUDED.professions,
                    stats = EXCLUDED.stats,
                    learned_skills = EXCLUDED.learned_skills,
                    active_quests = EXCLUDED.active_quests,
                    completed_quests = EXCLUDED.completed_quests,
                    faction_rep = EXCLUDED.faction_rep,
                    skill_cooldowns = EXCLUDED.skill_cooldowns,
                    skill_bindings = EXCLUDED.skill_bindings,
                    housing_upgrades = EXCLUDED.housing_upgrades,
                    cosmetics = EXCLUDED.cosmetics,
                    title = EXCLUDED.title
                """;
            try (Connection connection = databaseService.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setObject(1, profile.uuid());
                statement.setInt(2, profile.level());
                statement.setInt(3, profile.xp());
                statement.setInt(4, profile.skillPoints());
                statement.setInt(5, profile.mana());
                statement.setInt(6, profile.maxMana());
                statement.setString(7, profile.classId());
                statement.setInt(8, profile.gold());
                statement.setString(9, profile.guildId());
                statement.setInt(10, profile.elo());
                statement.setString(11, profile.dungeonRole());
                statement.setString(12, profile.homeWorld());
                statement.setObject(13, profile.homeWorld() != null ? profile.homeX() : null);
                statement.setObject(14, profile.homeWorld() != null ? profile.homeY() : null);
                statement.setObject(15, profile.homeWorld() != null ? profile.homeZ() : null);
                statement.setString(16, gson.toJson(profile.professions()));
                statement.setString(17, gson.toJson(statsToMap(profile.stats())));
                statement.setString(18, gson.toJson(profile.learnedSkills()));
                statement.setString(19, gson.toJson(questsToMap(profile.activeQuests())));
                statement.setString(20, gson.toJson(profile.completedQuests().stream().toList()));
                statement.setString(21, gson.toJson(profile.factionRep()));
                statement.setString(22, gson.toJson(profile.skillCooldowns()));
                statement.setString(23, gson.toJson(profile.skillBindings()));
                statement.setString(24, gson.toJson(profile.housingUpgrades()));
                statement.setString(25, gson.toJson(profile.cosmetics().stream().toList()));
                statement.setString(26, profile.title());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        }, databaseService.executor());
    }

    @Override
    public CompletableFuture<PlayerProfile> loadPlayer(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM rpg_players WHERE uuid = ?";
            try (Connection connection = databaseService.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setObject(1, uuid);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        return null;
                    }
                    PlayerProfile profile = new PlayerProfile(uuid);
                    profile.setLevel(resultSet.getInt("level"));
                    profile.setXp(resultSet.getInt("xp"));
                    profile.setSkillPoints(resultSet.getInt("skill_points"));
                    profile.setMana(resultSet.getInt("mana"));
                    profile.setMaxMana(resultSet.getInt("max_mana"));
                    profile.setClassId(resultSet.getString("class_id"));
                    profile.setGold(resultSet.getInt("gold"));
                    profile.setGuildId(resultSet.getString("guild_id"));
                    profile.setElo(resultSet.getInt("elo"));
                    String role = resultSet.getString("dungeon_role");
                    profile.setDungeonRole(role != null ? role : "DPS");
                    String homeWorld = resultSet.getString("home_world");
                    if (homeWorld != null) {
                        profile.setHome(homeWorld,
                            resultSet.getDouble("home_x"),
                            resultSet.getDouble("home_y"),
                            resultSet.getDouble("home_z"));
                    }
                    applyMap(resultSet.getString("professions"), profile.professions(), mapStringInt);
                    Map<String, Integer> stats = fromJson(resultSet.getString("stats"), mapStringInt);
                    if (stats != null) {
                        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                            try {
                                profile.stats().put(RPGStat.valueOf(entry.getKey()), entry.getValue());
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    }
                    applyMap(resultSet.getString("learned_skills"), profile.learnedSkills(), mapStringInt);
                    loadQuests(resultSet.getString("active_quests"), profile);
                    Set<String> completed = fromJson(resultSet.getString("completed_quests"),
                        new TypeToken<Set<String>>() {}.getType());
                    if (completed != null) {
                        profile.completedQuests().addAll(completed);
                    }
                    applyMap(resultSet.getString("faction_rep"), profile.factionRep(), mapStringInt);
                    applyMap(resultSet.getString("skill_cooldowns"), profile.skillCooldowns(), mapStringLong);
                    Map<Integer, String> bindings = fromJson(resultSet.getString("skill_bindings"), mapIntString);
                    if (bindings != null) {
                        profile.skillBindings().putAll(bindings);
                    }
                    applyMap(resultSet.getString("housing_upgrades"), profile.housingUpgrades(), mapStringInt);
                    List<String> cosmetics = fromJson(resultSet.getString("cosmetics"), listString);
                    if (cosmetics != null) {
                        profile.cosmetics().addAll(cosmetics);
                    }
                    profile.setTitle(resultSet.getString("title"));
                    return profile;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, databaseService.executor());
    }

    private Map<String, Integer> statsToMap(Map<RPGStat, Integer> stats) {
        Map<String, Integer> mapped = new HashMap<>();
        for (Map.Entry<RPGStat, Integer> entry : stats.entrySet()) {
            mapped.put(entry.getKey().name(), entry.getValue());
        }
        return mapped;
    }

    private Map<String, Object> questsToMap(Map<String, QuestProgress> quests) {
        Map<String, Object> data = new HashMap<>();
        for (QuestProgress progress : quests.values()) {
            Map<String, Object> quest = new HashMap<>();
            Map<String, Integer> steps = new HashMap<>();
            for (Map.Entry<Integer, Integer> entry : progress.stepProgress().entrySet()) {
                steps.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            quest.put("steps", steps);
            quest.put("completed", progress.completed());
            data.put(progress.questId(), quest);
        }
        return data;
    }

    private void loadQuests(String json, PlayerProfile profile) {
        Map<String, Object> data = fromJson(json, mapStringObject);
        if (data == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            QuestProgress progress = new QuestProgress(entry.getKey());
            if (entry.getValue() instanceof Map<?, ?> map) {
                Object stepsObj = map.get("steps");
                if (stepsObj instanceof Map<?, ?> steps) {
                    for (Map.Entry<?, ?> stepEntry : steps.entrySet()) {
                        try {
                            int step = Integer.parseInt(String.valueOf(stepEntry.getKey()));
                            int value = Integer.parseInt(String.valueOf(stepEntry.getValue()));
                            progress.incrementStep(step, value);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                Object completed = map.get("completed");
                if (completed instanceof Boolean done) {
                    progress.setCompleted(done);
                }
            }
            profile.activeQuests().put(entry.getKey(), progress);
        }
    }

    private <T> void applyMap(String json, Map<String, T> target, Type type) {
        Map<String, T> data = fromJson(json, type);
        if (data != null) {
            target.putAll(data);
        }
    }

    private <T> T fromJson(String json, Type type) {
        if (json == null || json.isBlank()) {
            return null;
        }
        return gson.fromJson(json, type);
    }
}

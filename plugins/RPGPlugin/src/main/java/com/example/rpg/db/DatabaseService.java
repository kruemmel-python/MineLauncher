package com.example.rpg.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class DatabaseService {
    private final JavaPlugin plugin;
    private final HikariDataSource dataSource;
    private final ExecutorService executor;

    public DatabaseService(JavaPlugin plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        String host = config.getString("database.host", "localhost");
        int port = config.getInt("database.port", 5432);
        String database = config.getString("database.name", "rpg");
        String username = config.getString("database.user", "rpg");
        String password = config.getString("database.password", "password");
        int poolSize = config.getInt("database.poolSize", 10);

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + database);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setMaximumPoolSize(poolSize);
        hikariConfig.setPoolName("MineLauncherRPG");
        hikariConfig.setAutoCommit(true);
        this.dataSource = new HikariDataSource(hikariConfig);
        this.executor = Executors.newFixedThreadPool(Math.max(2, poolSize));
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public ExecutorService executor() {
        return executor;
    }

    public void initTables() {
        String playersTable = """
            CREATE TABLE IF NOT EXISTS rpg_players (
                uuid UUID PRIMARY KEY,
                level INT,
                xp INT,
                skill_points INT,
                mana INT,
                max_mana INT,
                class_id TEXT,
                gold INT,
                guild_id TEXT,
                elo INT,
                professions JSONB,
                stats JSONB,
                learned_skills JSONB,
                active_quests JSONB,
                completed_quests JSONB,
                faction_rep JSONB,
                skill_cooldowns JSONB,
                skill_bindings JSONB
            )
            """;
        String skillsTable = """
            CREATE TABLE IF NOT EXISTS rpg_skills (
                player_uuid UUID PRIMARY KEY,
                data JSONB
            )
            """;
        String questsTable = """
            CREATE TABLE IF NOT EXISTS rpg_quests (
                player_uuid UUID PRIMARY KEY,
                data JSONB
            )
            """;
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(playersTable);
            statement.execute(skillsTable);
            statement.execute(questsTable);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to init database tables: " + e.getMessage());
        }
    }

    public void shutdown() {
        executor.shutdown();
        dataSource.close();
    }
}

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
    private HikariDataSource dataSource;
    private final ExecutorService executor;
    private final String jdbcUrl;
    private final String databaseName;
    private final String username;
    private final String password;

    public DatabaseService(JavaPlugin plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        String host = config.getString("database.host", "localhost");
        int port = config.getInt("database.port", 5432);
        String database = config.getString("database.name", "rpg");
        this.databaseName = database;
        this.username = config.getString("database.user", "rpg");
        this.password = config.getString("database.password", "minecraft");
        int poolSize = config.getInt("database.poolSize", 10);

        this.jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("PostgreSQL JDBC driver not found.");
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setDriverClassName("org.postgresql.Driver");
        hikariConfig.setUsername(this.username);
        hikariConfig.setPassword(this.password);
        hikariConfig.setMaximumPoolSize(poolSize);
        hikariConfig.setPoolName("MineLauncherRPG");
        hikariConfig.setAutoCommit(true);
        this.dataSource = createDataSource(hikariConfig);
        this.executor = Executors.newFixedThreadPool(Math.max(2, poolSize));
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public ExecutorService executor() {
        return executor;
    }

    public void initTables() {
        if (dataSource == null) {
            plugin.getLogger().severe("Database not available. Skipping table initialization.");
            return;
        }
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
                dungeon_role TEXT,
                home_world TEXT,
                home_x DOUBLE PRECISION,
                home_y DOUBLE PRECISION,
                home_z DOUBLE PRECISION,
                professions JSONB,
                stats JSONB,
                learned_skills JSONB,
                active_quests JSONB,
                completed_quests JSONB,
                faction_rep JSONB,
                skill_cooldowns JSONB,
                skill_bindings JSONB,
                housing_upgrades JSONB,
                cosmetics JSONB,
                title TEXT
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
        String rolesTable = """
            CREATE TABLE IF NOT EXISTS rpg_roles (
                role_key TEXT PRIMARY KEY,
                display_name TEXT NOT NULL,
                parents JSONB NOT NULL DEFAULT '[]',
                nodes JSONB NOT NULL DEFAULT '{}'
            )
            """;
        String playerRolesTable = """
            CREATE TABLE IF NOT EXISTS rpg_player_roles (
                player_uuid UUID PRIMARY KEY,
                primary_role TEXT,
                extra_roles JSONB NOT NULL DEFAULT '[]'
            )
            """;
        String auditTable = """
            CREATE TABLE IF NOT EXISTS rpg_audit_log (
                id BIGSERIAL PRIMARY KEY,
                ts TIMESTAMPTZ NOT NULL DEFAULT now(),
                actor_uuid UUID,
                actor_name TEXT,
                action TEXT NOT NULL,
                target TEXT NOT NULL,
                before JSONB,
                after JSONB
            )
            """;
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(playersTable);
            statement.execute(skillsTable);
            statement.execute(questsTable);
            statement.execute(rolesTable);
            statement.execute(playerRolesTable);
            statement.execute(auditTable);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to init database tables: " + e.getMessage());
        }
    }

    public void shutdown() {
        executor.shutdown();
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private HikariDataSource createDataSource(HikariConfig hikariConfig) {
        try {
            return new HikariDataSource(hikariConfig);
        } catch (RuntimeException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("existiert nicht")) {
                plugin.getLogger().warning("Database not found. Attempting to create '" + databaseName + "'.");
                if (createDatabase()) {
                    return new HikariDataSource(hikariConfig);
                }
            }
            plugin.getLogger().severe("Failed to initialize database: " + ex.getMessage());
            return null;
        }
    }

    private boolean createDatabase() {
        String adminUrl = jdbcUrl.replace("/" + databaseName, "/postgres");
        try (Connection connection = java.sql.DriverManager.getConnection(adminUrl, username, password);
             java.sql.PreparedStatement exists = connection.prepareStatement(
                 "SELECT 1 FROM pg_database WHERE datname = ?")) {
            exists.setString(1, databaseName);
            try (java.sql.ResultSet resultSet = exists.executeQuery()) {
                if (resultSet.next()) {
                    return true;
                }
            }
            try (java.sql.Statement statement = connection.createStatement()) {
                statement.execute("CREATE DATABASE \"" + databaseName + "\"");
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create database: " + e.getMessage());
            return false;
        }
    }
}

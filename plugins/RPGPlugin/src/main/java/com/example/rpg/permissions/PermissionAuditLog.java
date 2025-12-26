package com.example.rpg.permissions;

import com.example.rpg.db.DatabaseService;
import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PermissionAuditLog {
    private final DatabaseService database;
    private final Gson gson = new Gson();

    public PermissionAuditLog(DatabaseService database) {
        this.database = database;
    }

    public void log(UUID actorUuid, String actorName, String action, String target, Object before, Object after) {
        String sql = "INSERT INTO rpg_audit_log (actor_uuid, actor_name, action, target, before, after) VALUES (?, ?, ?, ?, ?::jsonb, ?::jsonb)";
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, actorUuid);
            stmt.setString(2, actorName);
            stmt.setString(3, action);
            stmt.setString(4, target);
            stmt.setString(5, before != null ? gson.toJson(before) : null);
            stmt.setString(6, after != null ? gson.toJson(after) : null);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to write audit log: " + e.getMessage(), e);
        }
    }

    public List<String> recent(int limit) {
        String sql = "SELECT ts, actor_name, action, target FROM rpg_audit_log ORDER BY ts DESC LIMIT ?";
        List<String> entries = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String ts = rs.getString("ts");
                    String actor = rs.getString("actor_name");
                    String action = rs.getString("action");
                    String target = rs.getString("target");
                    entries.add(ts + " | " + actor + " | " + action + " | " + target);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to read audit log: " + e.getMessage(), e);
        }
        return entries;
    }
}

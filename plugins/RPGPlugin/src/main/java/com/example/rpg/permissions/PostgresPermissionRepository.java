package com.example.rpg.permissions;

import com.example.rpg.db.DatabaseService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class PostgresPermissionRepository implements PermissionRepository {
    private final DatabaseService database;
    private final Gson gson = new Gson();
    private final Type setType = new TypeToken<Set<String>>() {}.getType();
    private final Type mapType = new TypeToken<Map<String, PermissionDecision>>() {}.getType();

    public PostgresPermissionRepository(DatabaseService database) {
        this.database = database;
    }

    @Override
    public List<Role> loadAllRoles() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT role_key, display_name, parents, nodes FROM rpg_roles";
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String key = rs.getString("role_key");
                String displayName = rs.getString("display_name");
                Role role = new Role(key, displayName);
                String parentsJson = rs.getString("parents");
                String nodesJson = rs.getString("nodes");
                if (parentsJson != null && !parentsJson.isBlank()) {
                    Set<String> parents = gson.fromJson(parentsJson, setType);
                    if (parents != null) {
                        role.parents().addAll(parents);
                    }
                }
                if (nodesJson != null && !nodesJson.isBlank()) {
                    Map<String, PermissionDecision> nodes = gson.fromJson(nodesJson, mapType);
                    if (nodes != null) {
                        role.nodes().putAll(nodes);
                    }
                }
                roles.add(role);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load roles: " + e.getMessage(), e);
        }
        return roles;
    }

    @Override
    public void saveRole(Role role) {
        String sql = "INSERT INTO rpg_roles (role_key, display_name, parents, nodes) VALUES (?, ?, ?::jsonb, ?::jsonb) "
            + "ON CONFLICT (role_key) DO UPDATE SET display_name = EXCLUDED.display_name, parents = EXCLUDED.parents, nodes = EXCLUDED.nodes";
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, role.key());
            stmt.setString(2, role.displayName());
            stmt.setString(3, gson.toJson(role.parents()));
            stmt.setString(4, gson.toJson(role.nodes()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save role: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteRole(String roleKey) {
        String sql = "DELETE FROM rpg_roles WHERE role_key = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, roleKey);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete role: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<PlayerRoles> loadPlayerRoles(UUID playerId) {
        String sql = "SELECT primary_role, extra_roles FROM rpg_player_roles WHERE player_uuid = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, playerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    PlayerRoles roles = new PlayerRoles(playerId);
                    roles.setPrimaryRole(rs.getString("primary_role"));
                    String extraJson = rs.getString("extra_roles");
                    if (extraJson != null && !extraJson.isBlank()) {
                        Set<String> extra = gson.fromJson(extraJson, setType);
                        if (extra != null) {
                            roles.extraRoles().addAll(extra);
                        }
                    }
                    return Optional.of(roles);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load player roles: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public void savePlayerRoles(PlayerRoles playerRoles) {
        String sql = "INSERT INTO rpg_player_roles (player_uuid, primary_role, extra_roles) VALUES (?, ?, ?::jsonb) "
            + "ON CONFLICT (player_uuid) DO UPDATE SET primary_role = EXCLUDED.primary_role, extra_roles = EXCLUDED.extra_roles";
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, playerRoles.playerId());
            stmt.setString(2, playerRoles.primaryRole());
            stmt.setString(3, gson.toJson(playerRoles.extraRoles()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save player roles: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PlayerRoles> listPlayerRoles() {
        List<PlayerRoles> list = new ArrayList<>();
        String sql = "SELECT player_uuid, primary_role, extra_roles FROM rpg_player_roles";
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                UUID uuid = rs.getObject("player_uuid", java.util.UUID.class);
                PlayerRoles roles = new PlayerRoles(uuid);
                roles.setPrimaryRole(rs.getString("primary_role"));
                String extraJson = rs.getString("extra_roles");
                if (extraJson != null && !extraJson.isBlank()) {
                    Set<String> extra = gson.fromJson(extraJson, setType);
                    if (extra != null) {
                        roles.extraRoles().addAll(extra);
                    }
                }
                list.add(roles);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list player roles: " + e.getMessage(), e);
        }
        return list;
    }
}

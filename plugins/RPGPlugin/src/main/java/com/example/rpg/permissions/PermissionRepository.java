package com.example.rpg.permissions;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository {
    List<Role> loadAllRoles();
    void saveRole(Role role);
    void deleteRole(String roleKey);
    Optional<PlayerRoles> loadPlayerRoles(UUID playerId);
    void savePlayerRoles(PlayerRoles playerRoles);
    List<PlayerRoles> listPlayerRoles();
}

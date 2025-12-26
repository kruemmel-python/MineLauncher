package com.example.rpg.permissions;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerRoles {
    private final UUID playerId;
    private String primaryRole;
    private final Set<String> extraRoles = new HashSet<>();

    public PlayerRoles(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID playerId() {
        return playerId;
    }

    public String primaryRole() {
        return primaryRole;
    }

    public void setPrimaryRole(String primaryRole) {
        this.primaryRole = primaryRole;
    }

    public Set<String> extraRoles() {
        return extraRoles;
    }
}

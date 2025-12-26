package com.example.rpg.permissions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Role {
    private final String key;
    private String displayName;
    private final Set<String> parents = new HashSet<>();
    private final Map<String, PermissionDecision> nodes = new HashMap<>();

    public Role(String key, String displayName) {
        this.key = key;
        this.displayName = displayName;
    }

    public String key() {
        return key;
    }

    public String displayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Set<String> parents() {
        return parents;
    }

    public Map<String, PermissionDecision> nodes() {
        return nodes;
    }
}

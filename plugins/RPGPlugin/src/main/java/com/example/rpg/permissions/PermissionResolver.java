package com.example.rpg.permissions;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class PermissionResolver {
    private final Map<String, Role> roles;
    private final PermissionDecision defaultDecision;

    public PermissionResolver(Map<String, Role> roles, PermissionDecision defaultDecision) {
        this.roles = roles;
        this.defaultDecision = defaultDecision;
    }

    public PermissionExplanation explain(PlayerRoles playerRoles, String node) {
        List<String> trace = new ArrayList<>();
        if (playerRoles == null) {
            return finalizeDecision(defaultDecision, null, null, trace);
        }
        Set<String> roleKeys = collectRoleKeys(playerRoles);
        DecisionResult result = resolveForRoles(roleKeys, node, trace);
        return finalizeDecision(result.decision(), result.roleKey(), result.node(), trace);
    }

    public boolean resolve(PlayerRoles playerRoles, String node) {
        return explain(playerRoles, node).allowed();
    }

    private PermissionExplanation finalizeDecision(PermissionDecision decision, String roleKey, String node, List<String> trace) {
        boolean allowed = decision == PermissionDecision.ALLOW;
        return new PermissionExplanation(allowed, roleKey, node, decision, trace);
    }

    private Set<String> collectRoleKeys(PlayerRoles playerRoles) {
        Set<String> keys = new HashSet<>();
        if (playerRoles.primaryRole() != null) {
            keys.add(playerRoles.primaryRole());
        }
        keys.addAll(playerRoles.extraRoles());
        Set<String> resolved = new HashSet<>();
        Queue<String> queue = new ArrayDeque<>(keys);
        while (!queue.isEmpty()) {
            String key = queue.poll();
            if (!resolved.add(key)) {
                continue;
            }
            Role role = roles.get(key);
            if (role == null) {
                continue;
            }
            for (String parent : role.parents()) {
                if (!resolved.contains(parent)) {
                    queue.add(parent);
                }
            }
        }
        return resolved;
    }

    private DecisionResult resolveForRoles(Set<String> roleKeys, String node, List<String> trace) {
        PermissionDecision finalDecision = PermissionDecision.INHERIT;
        String winningRole = null;
        String winningNode = null;
        for (String roleKey : roleKeys) {
            Role role = roles.get(roleKey);
            if (role == null) {
                continue;
            }
            for (Map.Entry<String, PermissionDecision> entry : role.nodes().entrySet()) {
                String nodeKey = entry.getKey();
                PermissionDecision decision = entry.getValue();
                if (!matches(nodeKey, node)) {
                    continue;
                }
                trace.add(roleKey + " -> " + nodeKey + " = " + decision);
                if (decision == PermissionDecision.DENY) {
                    return new DecisionResult(PermissionDecision.DENY, roleKey, nodeKey);
                }
                if (decision == PermissionDecision.ALLOW && finalDecision != PermissionDecision.ALLOW) {
                    finalDecision = PermissionDecision.ALLOW;
                    winningRole = roleKey;
                    winningNode = nodeKey;
                }
            }
        }
        if (finalDecision == PermissionDecision.INHERIT) {
            finalDecision = defaultDecision;
        }
        return new DecisionResult(finalDecision, winningRole, winningNode);
    }

    private boolean matches(String rule, String node) {
        if (rule == null) {
            return false;
        }
        if (rule.equalsIgnoreCase(node)) {
            return true;
        }
        if (rule.endsWith(".*")) {
            String prefix = rule.substring(0, rule.length() - 2).toLowerCase();
            return node.toLowerCase().startsWith(prefix);
        }
        return false;
    }

    private record DecisionResult(PermissionDecision decision, String roleKey, String node) {
    }
}

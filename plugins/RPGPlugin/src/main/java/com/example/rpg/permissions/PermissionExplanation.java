package com.example.rpg.permissions;

import java.util.ArrayList;
import java.util.List;

public class PermissionExplanation {
    private final boolean allowed;
    private final String winningRole;
    private final String winningNode;
    private final PermissionDecision winningDecision;
    private final List<String> trace;

    public PermissionExplanation(boolean allowed, String winningRole, String winningNode, PermissionDecision winningDecision,
                                 List<String> trace) {
        this.allowed = allowed;
        this.winningRole = winningRole;
        this.winningNode = winningNode;
        this.winningDecision = winningDecision;
        this.trace = trace != null ? trace : new ArrayList<>();
    }

    public boolean allowed() {
        return allowed;
    }

    public String winningRole() {
        return winningRole;
    }

    public String winningNode() {
        return winningNode;
    }

    public PermissionDecision winningDecision() {
        return winningDecision;
    }

    public List<String> trace() {
        return trace;
    }
}

package com.example.rpg.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Party {
    private final UUID leader;
    private final Set<UUID> members = new HashSet<>();

    public Party(UUID leader) {
        this.leader = leader;
        members.add(leader);
    }

    public UUID leader() {
        return leader;
    }

    public Set<UUID> members() {
        return members;
    }

    public void addMember(UUID uuid) {
        members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }
}

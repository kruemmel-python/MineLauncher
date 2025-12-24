package com.example.rpg.manager;

import com.example.rpg.model.Party;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PartyManager {
    private final Map<UUID, Party> partiesByMember = new HashMap<>();

    public Party createParty(UUID leader) {
        Party party = new Party(leader);
        partiesByMember.put(leader, party);
        return party;
    }

    public Optional<Party> getParty(UUID member) {
        return Optional.ofNullable(partiesByMember.get(member));
    }

    public void addMember(Party party, UUID member) {
        party.addMember(member);
        partiesByMember.put(member, party);
    }

    public void removeMember(UUID member) {
        Party party = partiesByMember.remove(member);
        if (party != null) {
            party.removeMember(member);
            if (party.leader().equals(member)) {
                party.members().forEach(partiesByMember::remove);
            } else if (party.members().isEmpty()) {
                partiesByMember.remove(member);
            }
        }
    }
}

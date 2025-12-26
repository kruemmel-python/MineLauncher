package com.example.rpg.manager;

import com.example.rpg.model.TradeRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TradeManager {
    private final Map<UUID, TradeRequest> requests = new HashMap<>();

    public void requestTrade(UUID requester, UUID target) {
        TradeRequest request = new TradeRequest(requester, target);
        requests.put(requester, request);
        requests.put(target, request);
    }

    public TradeRequest getRequest(UUID player) {
        return requests.get(player);
    }

    public void clear(UUID player) {
        TradeRequest request = requests.remove(player);
        if (request != null) {
            requests.remove(request.requester());
            requests.remove(request.target());
        }
    }
}

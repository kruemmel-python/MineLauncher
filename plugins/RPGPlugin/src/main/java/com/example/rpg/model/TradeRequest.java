package com.example.rpg.model;

import java.util.UUID;

public class TradeRequest {
    private final UUID requester;
    private final UUID target;
    private int goldOffer;
    private int goldRequest;
    private boolean requesterReady;
    private boolean targetReady;

    public TradeRequest(UUID requester, UUID target) {
        this.requester = requester;
        this.target = target;
    }

    public UUID requester() {
        return requester;
    }

    public UUID target() {
        return target;
    }

    public int goldOffer() {
        return goldOffer;
    }

    public void setGoldOffer(int goldOffer) {
        this.goldOffer = goldOffer;
    }

    public int goldRequest() {
        return goldRequest;
    }

    public void setGoldRequest(int goldRequest) {
        this.goldRequest = goldRequest;
    }

    public boolean requesterReady() {
        return requesterReady;
    }

    public void setRequesterReady(boolean requesterReady) {
        this.requesterReady = requesterReady;
    }

    public boolean targetReady() {
        return targetReady;
    }

    public void setTargetReady(boolean targetReady) {
        this.targetReady = targetReady;
    }
}

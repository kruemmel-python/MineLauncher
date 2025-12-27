package com.example.rpg.model;

import java.util.UUID;

public class CraftingOrder {
    private final String id;
    private UUID requester;
    private String material;
    private int amount;
    private int rewardGold;

    public CraftingOrder(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public UUID requester() {
        return requester;
    }

    public void setRequester(UUID requester) {
        this.requester = requester;
    }

    public String material() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public int amount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int rewardGold() {
        return rewardGold;
    }

    public void setRewardGold(int rewardGold) {
        this.rewardGold = rewardGold;
    }
}

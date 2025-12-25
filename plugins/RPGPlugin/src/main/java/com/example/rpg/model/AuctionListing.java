package com.example.rpg.model;

import java.util.UUID;

public class AuctionListing {
    private final String id;
    private UUID seller;
    private String itemData;
    private int price;

    public AuctionListing(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public UUID seller() {
        return seller;
    }

    public void setSeller(UUID seller) {
        this.seller = seller;
    }

    public String itemData() {
        return itemData;
    }

    public void setItemData(String itemData) {
        this.itemData = itemData;
    }

    public int price() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}

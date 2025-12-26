package com.example.rpg.model;

public class FactionRank {
    private final String id;
    private String name;
    private int minRep;
    private double shopDiscount;
    private boolean dungeonAccess;

    public FactionRank(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int minRep() {
        return minRep;
    }

    public void setMinRep(int minRep) {
        this.minRep = minRep;
    }

    public double shopDiscount() {
        return shopDiscount;
    }

    public void setShopDiscount(double shopDiscount) {
        this.shopDiscount = shopDiscount;
    }

    public boolean dungeonAccess() {
        return dungeonAccess;
    }

    public void setDungeonAccess(boolean dungeonAccess) {
        this.dungeonAccess = dungeonAccess;
    }
}

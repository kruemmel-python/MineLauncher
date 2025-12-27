package com.example.rpg.model;

public class Faction {
    private final String id;
    private String name;
    private java.util.List<FactionRank> ranks = new java.util.ArrayList<>();

    public Faction(String id) {
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

    public java.util.List<FactionRank> ranks() {
        return ranks;
    }

    public void setRanks(java.util.List<FactionRank> ranks) {
        this.ranks = ranks;
    }

    public FactionRank rankForRep(int rep) {
        FactionRank result = null;
        for (FactionRank rank : ranks) {
            if (rep >= rank.minRep()) {
                if (result == null || rank.minRep() > result.minRep()) {
                    result = rank;
                }
            }
        }
        return result;
    }
}

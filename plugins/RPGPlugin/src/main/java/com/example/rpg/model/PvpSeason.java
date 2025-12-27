package com.example.rpg.model;

import java.time.Instant;

public class PvpSeason {
    private final String id;
    private String name;
    private long endTimestamp;

    public PvpSeason(String id) {
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

    public long endTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public boolean isActive() {
        return endTimestamp == 0 || Instant.now().toEpochMilli() < endTimestamp;
    }
}

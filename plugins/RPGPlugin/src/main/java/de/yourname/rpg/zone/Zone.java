package de.yourname.rpg.zone;

public class Zone {
    private String id;
    private String world;
    private ZonePosition pos1;
    private ZonePosition pos2;
    private int levelMin;
    private int levelMax;
    private String lootTableId;
    private double xpMultiplier;

    public Zone() {
    }

    public Zone(String id, String world, ZonePosition pos1, ZonePosition pos2) {
        this.id = id;
        this.world = world;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.levelMin = 1;
        this.levelMax = 60;
        this.xpMultiplier = 1.0;
    }

    public String getId() {
        return id;
    }

    public String getWorld() {
        return world;
    }

    public ZonePosition getPos1() {
        return pos1;
    }

    public ZonePosition getPos2() {
        return pos2;
    }

    public int getLevelMin() {
        return levelMin;
    }

    public int getLevelMax() {
        return levelMax;
    }

    public String getLootTableId() {
        return lootTableId;
    }

    public double getXpMultiplier() {
        return xpMultiplier;
    }

    public void setLootTableId(String lootTableId) {
        this.lootTableId = lootTableId;
    }

    public void setXpMultiplier(double xpMultiplier) {
        this.xpMultiplier = xpMultiplier;
    }
}

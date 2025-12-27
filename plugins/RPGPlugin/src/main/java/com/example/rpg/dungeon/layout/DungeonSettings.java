package com.example.rpg.dungeon.layout;

import java.util.List;
import org.bukkit.Material;

public class DungeonSettings {
    private final int roomCount;
    private final int roomMinSizeX;
    private final int roomMinSizeZ;
    private final int roomMaxSizeX;
    private final int roomMaxSizeZ;
    private final int corridorWidth;
    private final Material wallBlock;
    private final Material floorBlock;
    private final Material doorBlock;
    private final Material lightBlock;
    private final boolean waterEnabled;
    private final double canalChance;
    private final double floodRoomChance;
    private final boolean mobsEnabled;
    private final int mobsMin;
    private final int mobsMax;
    private final double eliteChance;
    private final boolean bossEnabled;
    private final boolean lootEnabled;
    private final int lootMin;
    private final int lootMax;
    private final String lootTable;
    private final boolean debugEnabled;
    private final boolean jigsawEnabled;
    private final boolean wfcRoomFillEnabled;
    private final String wfcRoomTheme;

    public DungeonSettings(int roomCount,
                           List<Integer> roomMinSize,
                           List<Integer> roomMaxSize,
                           int corridorWidth,
                           Material wallBlock,
                           Material floorBlock,
                           Material doorBlock,
                           Material lightBlock,
                           boolean waterEnabled,
                           double canalChance,
                           double floodRoomChance,
                           boolean mobsEnabled,
                           int mobsMin,
                           int mobsMax,
                           double eliteChance,
                           boolean bossEnabled,
                           boolean lootEnabled,
                           int lootMin,
                           int lootMax,
                           String lootTable,
                           boolean debugEnabled,
                           boolean jigsawEnabled,
                           boolean wfcRoomFillEnabled,
                           String wfcRoomTheme) {
        this.roomCount = roomCount;
        this.roomMinSizeX = roomMinSize.get(0);
        this.roomMinSizeZ = roomMinSize.get(1);
        this.roomMaxSizeX = roomMaxSize.get(0);
        this.roomMaxSizeZ = roomMaxSize.get(1);
        this.corridorWidth = corridorWidth;
        this.wallBlock = wallBlock;
        this.floorBlock = floorBlock;
        this.doorBlock = doorBlock;
        this.lightBlock = lightBlock;
        this.waterEnabled = waterEnabled;
        this.canalChance = canalChance;
        this.floodRoomChance = floodRoomChance;
        this.mobsEnabled = mobsEnabled;
        this.mobsMin = mobsMin;
        this.mobsMax = mobsMax;
        this.eliteChance = eliteChance;
        this.bossEnabled = bossEnabled;
        this.lootEnabled = lootEnabled;
        this.lootMin = lootMin;
        this.lootMax = lootMax;
        this.lootTable = lootTable;
        this.debugEnabled = debugEnabled;
        this.jigsawEnabled = jigsawEnabled;
        this.wfcRoomFillEnabled = wfcRoomFillEnabled;
        this.wfcRoomTheme = wfcRoomTheme;
    }

    public int roomCount() {
        return roomCount;
    }

    public int roomMinSizeX() {
        return roomMinSizeX;
    }

    public int roomMinSizeZ() {
        return roomMinSizeZ;
    }

    public int roomMaxSizeX() {
        return roomMaxSizeX;
    }

    public int roomMaxSizeZ() {
        return roomMaxSizeZ;
    }

    public int corridorWidth() {
        return corridorWidth;
    }

    public Material wallBlock() {
        return wallBlock;
    }

    public Material floorBlock() {
        return floorBlock;
    }

    public Material doorBlock() {
        return doorBlock;
    }

    public Material lightBlock() {
        return lightBlock;
    }

    public boolean waterEnabled() {
        return waterEnabled;
    }

    public double canalChance() {
        return canalChance;
    }

    public double floodRoomChance() {
        return floodRoomChance;
    }

    public boolean mobsEnabled() {
        return mobsEnabled;
    }

    public int mobsMin() {
        return mobsMin;
    }

    public int mobsMax() {
        return mobsMax;
    }

    public double eliteChance() {
        return eliteChance;
    }

    public boolean bossEnabled() {
        return bossEnabled;
    }

    public boolean lootEnabled() {
        return lootEnabled;
    }

    public int lootMin() {
        return lootMin;
    }

    public int lootMax() {
        return lootMax;
    }

    public String lootTable() {
        return lootTable;
    }

    public boolean debugEnabled() {
        return debugEnabled;
    }

    public boolean jigsawEnabled() {
        return jigsawEnabled;
    }

    public boolean wfcRoomFillEnabled() {
        return wfcRoomFillEnabled;
    }

    public String wfcRoomTheme() {
        return wfcRoomTheme;
    }
}

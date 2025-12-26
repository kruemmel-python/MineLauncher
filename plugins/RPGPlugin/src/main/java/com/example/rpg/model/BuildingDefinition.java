package com.example.rpg.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuildingDefinition {
    private final String id;
    private String name;
    private BuildingCategory category = BuildingCategory.RESIDENTIAL;
    private String schematic;
    private String floorSchematic;
    private int minFloors = 1;
    private int maxFloors = 1;
    private int floorHeight = 5;
    private String basementSchematic;
    private int basementDepth = 0;
    private boolean includeAir = false;
    private int offsetX;
    private int offsetY;
    private int offsetZ;
    private final List<FurnitureDefinition> furniture = new ArrayList<>();

    public BuildingDefinition(String id) {
        this.id = id;
        this.name = id;
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

    public BuildingCategory category() {
        return category;
    }

    public void setCategory(BuildingCategory category) {
        this.category = category;
    }

    public String schematic() {
        return schematic;
    }

    public void setSchematic(String schematic) {
        this.schematic = schematic;
    }

    public String floorSchematic() {
        return floorSchematic;
    }

    public void setFloorSchematic(String floorSchematic) {
        this.floorSchematic = floorSchematic;
    }

    public int minFloors() {
        return minFloors;
    }

    public void setMinFloors(int minFloors) {
        this.minFloors = minFloors;
    }

    public int maxFloors() {
        return maxFloors;
    }

    public void setMaxFloors(int maxFloors) {
        this.maxFloors = maxFloors;
    }

    public int floorHeight() {
        return floorHeight;
    }

    public void setFloorHeight(int floorHeight) {
        this.floorHeight = floorHeight;
    }

    public String basementSchematic() {
        return basementSchematic;
    }

    public void setBasementSchematic(String basementSchematic) {
        this.basementSchematic = basementSchematic;
    }

    public int basementDepth() {
        return basementDepth;
    }

    public void setBasementDepth(int basementDepth) {
        this.basementDepth = basementDepth;
    }

    public boolean includeAir() {
        return includeAir;
    }

    public void setIncludeAir(boolean includeAir) {
        this.includeAir = includeAir;
    }

    public int offsetX() {
        return offsetX;
    }

    public int offsetY() {
        return offsetY;
    }

    public int offsetZ() {
        return offsetZ;
    }

    public void setOffset(int x, int y, int z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
    }

    public List<FurnitureDefinition> furniture() {
        return Collections.unmodifiableList(furniture);
    }

    public void addFurniture(FurnitureDefinition furnitureDefinition) {
        furniture.add(furnitureDefinition);
    }
}

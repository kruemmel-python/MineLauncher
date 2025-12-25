package com.example.rpg.model;

import java.util.HashMap;
import java.util.Map;

public class ShopDefinition {
    private final String id;
    private String title;
    private Map<Integer, ShopItem> items = new HashMap<>();

    public ShopDefinition(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Map<Integer, ShopItem> items() {
        return items;
    }

    public void setItems(Map<Integer, ShopItem> items) {
        this.items = items;
    }
}

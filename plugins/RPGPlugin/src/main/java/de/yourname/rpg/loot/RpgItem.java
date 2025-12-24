package de.yourname.rpg.loot;

public class RpgItem {
    private String id;
    private String material;
    private ItemStats stats;

    public RpgItem() {
    }

    public RpgItem(String id, String material, ItemStats stats) {
        this.id = id;
        this.material = material;
        this.stats = stats;
    }

    public String getId() {
        return id;
    }

    public String getMaterial() {
        return material;
    }

    public ItemStats getStats() {
        return stats;
    }
}

package com.example.worldcreator;

import org.bukkit.Material;

public enum WorldTypeOption {
    VOID("Leere Welt", Material.GLASS),
    WATER("Wasserwelt", Material.WATER_BUCKET),
    SKY_ISLANDS("Sky Inseln", Material.ELYTRA),
    SKY_REALMS("Sky Regionen", Material.END_STONE),
    MYCELIA("Mycelia", Material.MYCELIUM),
    JUNGLE("Dschungel", Material.JUNGLE_LOG),
    DESERT("Wüste", Material.SAND);

    private final String displayName;
    private final Material icon;

    WorldTypeOption(String displayName, Material icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }
}

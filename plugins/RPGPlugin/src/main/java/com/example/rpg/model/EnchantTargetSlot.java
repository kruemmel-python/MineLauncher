package com.example.rpg.model;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum EnchantTargetSlot {
    HAND,
    OFF_HAND,
    ARMOR_HEAD,
    ARMOR_CHEST,
    ARMOR_LEGS,
    ARMOR_FEET,
    SHIELD;

    public boolean matches(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        Material material = item.getType();
        String name = material.name();
        return switch (this) {
            case HAND -> true;
            case OFF_HAND -> true;
            case SHIELD -> material == Material.SHIELD;
            case ARMOR_HEAD -> name.endsWith("HELMET") || name.endsWith("HEAD") || name.endsWith("SKULL");
            case ARMOR_CHEST -> name.endsWith("CHESTPLATE");
            case ARMOR_LEGS -> name.endsWith("LEGGINGS");
            case ARMOR_FEET -> name.endsWith("BOOTS");
        };
    }
}

package com.example.rpg.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Robuste Identifikation von GUIs:
 * Nicht über Inventory-Titel (anfällig für Farbe/Locale),
 * sondern über InventoryHolder-Typen.
 */
public final class GuiHolders {
    private GuiHolders() {}

    public static final class PlayerMenuHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class AdminMenuHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class QuestListHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class SkillListHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }
}

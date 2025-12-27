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

    public static final class ZoneEditorHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class NpcEditorHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class QuestEditorHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class LootEditorHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class SkillAdminHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class ClassAdminHolder implements InventoryHolder {
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

    public static final class SkillTreeHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class BuildingCategoryHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class BuildingListHolder implements InventoryHolder {
        private final String category;

        public BuildingListHolder(String category) {
            this.category = category;
        }

        public String category() {
            return category;
        }

        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class BehaviorTreeEditorHolder implements InventoryHolder {
        private final String treeName;

        public BehaviorTreeEditorHolder(String treeName) {
            this.treeName = treeName;
        }

        public String treeName() {
            return treeName;
        }

        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class ShopHolder implements InventoryHolder {
        private final String shopId;

        public ShopHolder(String shopId) {
            this.shopId = shopId;
        }

        public String shopId() {
            return shopId;
        }

        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class SchematicMoveHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class PermissionsMainHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class RoleListHolder implements InventoryHolder {
        private final int page;

        public RoleListHolder(int page) {
            this.page = page;
        }

        public int page() {
            return page;
        }

        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class RoleDetailHolder implements InventoryHolder {
        private final String roleKey;

        public RoleDetailHolder(String roleKey) {
            this.roleKey = roleKey;
        }

        public String roleKey() {
            return roleKey;
        }

        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class RoleNodesHolder implements InventoryHolder {
        private final String roleKey;
        private final int page;

        public RoleNodesHolder(String roleKey, int page) {
            this.roleKey = roleKey;
            this.page = page;
        }

        public String roleKey() {
            return roleKey;
        }

        public int page() {
            return page;
        }

        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class RoleParentsHolder implements InventoryHolder {
        private final String roleKey;

        public RoleParentsHolder(String roleKey) {
            this.roleKey = roleKey;
        }

        public String roleKey() {
            return roleKey;
        }

        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class PlayerListHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class PlayerRoleHolder implements InventoryHolder {
        private final java.util.UUID targetId;

        public PlayerRoleHolder(java.util.UUID targetId) {
            this.targetId = targetId;
        }

        public java.util.UUID targetId() {
            return targetId;
        }

        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class PermissionAuditHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class EnchantingHolder implements InventoryHolder {
        private final String recipeId;

        public EnchantingHolder(String recipeId) {
            this.recipeId = recipeId;
        }

        public String recipeId() {
            return recipeId;
        }

        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }
}

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

    public static final class DungeonAdminHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class ZoneEditorHolder implements InventoryHolder {
        private final int page;

        public ZoneEditorHolder(int page) {
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

    public static final class NpcEditorHolder implements InventoryHolder {
        private final int page;

        public NpcEditorHolder(int page) {
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

    public static final class QuestEditorHolder implements InventoryHolder {
        private final int page;

        public QuestEditorHolder(int page) {
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

    public static final class LootEditorHolder implements InventoryHolder {
        private final int page;

        public LootEditorHolder(int page) {
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

    public static final class SkillAdminHolder implements InventoryHolder {
        private final int page;

        public SkillAdminHolder(int page) {
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

    public static final class ClassAdminHolder implements InventoryHolder {
        private final int page;

        public ClassAdminHolder(int page) {
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

    public static final class QuestListHolder implements InventoryHolder {
        private final int page;

        public QuestListHolder(int page) {
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

    public static final class QuestLogHolder implements InventoryHolder {
        private final int page;

        public QuestLogHolder(int page) {
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

    public static final class QuestDetailHolder implements InventoryHolder {
        private final String questId;
        private final boolean active;
        private final int page;

        public QuestDetailHolder(String questId, boolean active, int page) {
            this.questId = questId;
            this.active = active;
            this.page = page;
        }

        public String questId() {
            return questId;
        }

        public boolean active() {
            return active;
        }

        public int page() {
            return page;
        }

        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class SkillListHolder implements InventoryHolder {
        private final int page;

        public SkillListHolder(int page) {
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
        private final int page;

        public BuildingListHolder(String category, int page) {
            this.category = category;
            this.page = page;
        }

        public String category() {
            return category;
        }

        public int page() {
            return page;
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
        private final int page;

        public PlayerListHolder(int page) {
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
        private final int page;

        public PermissionAuditHolder(int page) {
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

    public static final class EnchantingHolder implements InventoryHolder {
        private final String recipeId;
        private final int page;

        public EnchantingHolder(String recipeId, int page) {
            this.recipeId = recipeId;
            this.page = page;
        }

        public String recipeId() {
            return recipeId;
        }

        public int page() {
            return page;
        }

        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }
}

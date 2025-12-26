package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.gui.GuiHolders;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.Quest;
import com.example.rpg.model.QuestProgress;
import com.example.rpg.model.Skill;
import com.example.rpg.util.Text;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.inventory.Inventory;
import org.bukkit.Sound;
import org.bukkit.Material;

public class GuiListener implements Listener {
    private final RPGPlugin plugin;

    public GuiListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack current = event.getCurrentItem();
        if (current == null) {
            return;
        }
        var holder = event.getInventory().getHolder();
        if (holder instanceof GuiHolders.PlayerMenuHolder) {
            event.setCancelled(true);
            switch (event.getSlot()) {
                case 12 -> plugin.guiManager().openSkillList(player);
                case 14 -> plugin.guiManager().openQuestList(player);
                default -> {
                }
            }
            return;
        }
        if (holder instanceof GuiHolders.AdminMenuHolder) {
            event.setCancelled(true);
            if (event.getSlot() == 15) {
                boolean enabled = plugin.toggleDebug(player.getUniqueId());
                player.sendMessage(Text.mm(enabled ? "<green>Debug aktiviert." : "<red>Debug deaktiviert."));
            } else if (event.getSlot() == 16) {
                plugin.guiManager().openBuildingCategories(player);
            } else if (event.getSlot() == 17) {
                plugin.guiManager().openPermissionsMain(player);
            }
            return;
        }
        if (holder instanceof GuiHolders.BuildingCategoryHolder) {
            event.setCancelled(true);
            String category = resolveBuildingCategory(current);
            if (category == null) {
                return;
            }
            if ("SINGLE".equalsIgnoreCase(category)) {
                player.closeInventory();
                plugin.promptManager().prompt(player, Text.mm("<yellow>Schematic-Dateiname eingeben (z.B. haus.schem):"), input -> {
                    plugin.buildingManager().beginSingleSchematicPlacement(player, input, com.example.rpg.schematic.Transform.Rotation.NONE);
                });
                return;
            }
            plugin.guiManager().openBuildingList(player, com.example.rpg.model.BuildingCategory.fromString(category));
            return;
        }
        if (holder instanceof GuiHolders.BuildingListHolder) {
            event.setCancelled(true);
            String buildingId = resolveBuilding(current);
            if (buildingId == null) {
                return;
            }
            plugin.buildingManager().beginPlacement(player, buildingId, com.example.rpg.schematic.Transform.Rotation.NONE);
            player.closeInventory();
            return;
        }
        if (holder instanceof GuiHolders.QuestListHolder) {
            event.setCancelled(true);
            Quest quest = resolveQuest(current);
            if (quest == null) {
                return;
            }
            PlayerProfile profile = plugin.playerDataManager().getProfile(player);
            if (profile.level() < quest.minLevel()) {
                player.sendMessage(Text.mm("<red>Du brauchst Level " + quest.minLevel() + "."));
                return;
            }
            if (profile.activeQuests().containsKey(quest.id())) {
                player.sendMessage(Text.mm("<yellow>Quest bereits aktiv."));
                return;
            }
            if (profile.completedQuests().contains(quest.id()) && !quest.repeatable()) {
                player.sendMessage(Text.mm("<red>Quest bereits abgeschlossen."));
                return;
            }
            profile.activeQuests().put(quest.id(), new QuestProgress(quest.id()));
            player.sendMessage(Text.mm("<green>Quest angenommen: " + quest.name()));
            return;
        }
        if (holder instanceof GuiHolders.SkillListHolder) {
            event.setCancelled(true);
            Skill skill = resolveSkill(current);
            if (skill == null) {
                return;
            }
            PlayerProfile profile = plugin.playerDataManager().getProfile(player);
            if (profile.skillPoints() <= 0) {
                player.sendMessage(Text.mm("<red>Keine Skillpunkte."));
                return;
            }
            if (skill.requiredSkill() != null && !profile.learnedSkills().containsKey(skill.requiredSkill())) {
                player.sendMessage(Text.mm("<red>Du musst zuerst " + skill.requiredSkill() + " lernen."));
                return;
            }
            profile.learnedSkills().put(skill.id(), profile.learnedSkills().getOrDefault(skill.id(), 0) + 1);
            profile.setSkillPoints(profile.skillPoints() - 1);
            player.sendMessage(Text.mm("<green>Skill gelernt: " + skill.name()));
            plugin.guiManager().openSkillList(player);
            return;
        }
        if (holder instanceof GuiHolders.SkillTreeHolder) {
            event.setCancelled(true);
            Skill skill = resolveSkill(current);
            if (skill == null) {
                return;
            }
            PlayerProfile profile = plugin.playerDataManager().getProfile(player);
            if (profile.skillPoints() <= 0) {
                player.sendMessage(Text.mm("<red>Keine Skillpunkte."));
                return;
            }
            if (skill.requiredSkill() != null && !profile.learnedSkills().containsKey(skill.requiredSkill())) {
                player.sendMessage(Text.mm("<red>Du musst zuerst " + skill.requiredSkill() + " lernen."));
                return;
            }
            if (profile.learnedSkills().containsKey(skill.id())) {
                player.sendMessage(Text.mm("<yellow>Bereits gelernt."));
                return;
            }
            profile.learnedSkills().put(skill.id(), 1);
            profile.setSkillPoints(profile.skillPoints() - 1);
            player.sendMessage(Text.mm("<green>Skill gelernt: " + skill.name()));
            plugin.skillTreeGui().open(player);
            return;
        }
        if (holder instanceof GuiHolders.ShopHolder shopHolder) {
            event.setCancelled(true);
            handleShopClick(player, event.getInventory(), event.getSlot(), current, shopHolder, event.isRightClick());
        }
        if (holder instanceof GuiHolders.SchematicMoveHolder) {
            event.setCancelled(true);
            switch (event.getSlot()) {
                case 11 -> plugin.buildingManager().moveLastPlacement(player, -1, 0, 0);
                case 15 -> plugin.buildingManager().moveLastPlacement(player, 1, 0, 0);
                case 13 -> plugin.buildingManager().moveLastPlacement(player, 0, 1, 0);
                case 22 -> plugin.buildingManager().moveLastPlacement(player, 0, -1, 0);
                case 26 -> player.closeInventory();
                default -> {
                }
            }
            return;
        }
        if (holder instanceof GuiHolders.PermissionsMainHolder) {
            event.setCancelled(true);
            switch (event.getSlot()) {
                case 11 -> plugin.guiManager().openRoleList(player, 0);
                case 13 -> plugin.guiManager().openPlayerList(player);
                case 15 -> plugin.guiManager().openAuditLog(player);
                default -> {
                }
            }
            return;
        }
        if (holder instanceof GuiHolders.RoleListHolder roleListHolder) {
            event.setCancelled(true);
            if (event.getSlot() == 45) {
                plugin.guiManager().openPermissionsMain(player);
                return;
            }
            if (event.getSlot() == 53) {
                plugin.promptManager().prompt(player, Text.mm("<yellow>Rolle erstellen: <key> <displayName>"), input -> {
                    String[] parts = input.split("\\s+", 2);
                    if (parts.length < 2) {
                        player.sendMessage(Text.mm("<red>Format: <key> <displayName>"));
                        return;
                    }
                    plugin.permissionService().createRole(player, parts[0], parts[1]);
                    plugin.guiManager().openRoleList(player, roleListHolder.page());
                });
                return;
            }
            String roleKey = resolveRoleKey(current);
            if (roleKey != null) {
                plugin.guiManager().openRoleDetails(player, roleKey);
            }
            return;
        }
        if (holder instanceof GuiHolders.RoleDetailHolder roleDetailHolder) {
            event.setCancelled(true);
            String roleKey = roleDetailHolder.roleKey();
            switch (event.getSlot()) {
                case 11 -> plugin.guiManager().openRoleParents(player, roleKey);
                case 13 -> plugin.guiManager().openRoleNodes(player, roleKey, 0);
                case 15 -> plugin.promptManager().prompt(player, Text.mm("<yellow>Neuer Display-Name für " + roleKey + ":"), input -> {
                    if (input.isBlank()) {
                        player.sendMessage(Text.mm("<red>Display-Name darf nicht leer sein."));
                        return;
                    }
                    plugin.permissionService().renameRole(player, roleKey, input.trim());
                    plugin.guiManager().openRoleDetails(player, roleKey);
                });
                case 26 -> {
                    plugin.permissionService().deleteRole(player, roleKey);
                    plugin.guiManager().openRoleList(player, 0);
                }
                default -> {
                }
            }
            return;
        }
        if (holder instanceof GuiHolders.RoleParentsHolder roleParentsHolder) {
            event.setCancelled(true);
            if (event.getSlot() == 45) {
                plugin.guiManager().openRoleDetails(player, roleParentsHolder.roleKey());
                return;
            }
            String parentKey = resolveNodeKey(current);
            if (parentKey == null) {
                return;
            }
            String roleKey = roleParentsHolder.roleKey();
            var role = plugin.permissionService().roles().get(roleKey);
            if (role == null) {
                return;
            }
            if (role.parents().contains(parentKey)) {
                plugin.permissionService().removeParent(player, roleKey, parentKey);
            } else {
                plugin.permissionService().addParent(player, roleKey, parentKey);
            }
            plugin.guiManager().openRoleParents(player, roleKey);
            return;
        }
        if (holder instanceof GuiHolders.RoleNodesHolder roleNodesHolder) {
            event.setCancelled(true);
            if (event.getSlot() == 45) {
                plugin.guiManager().openRoleDetails(player, roleNodesHolder.roleKey());
                return;
            }
            if (event.getSlot() == 53) {
                plugin.promptManager().prompt(player, Text.mm("<yellow>Node setzen: <node> <allow|deny|inherit>"), input -> {
                    String[] parts = input.split("\\s+", 2);
                    if (parts.length < 2) {
                        player.sendMessage(Text.mm("<red>Format: <node> <allow|deny|inherit>"));
                        return;
                    }
                    var decision = parseDecision(parts[1]);
                    plugin.permissionService().setRoleNode(player, roleNodesHolder.roleKey(), parts[0], decision);
                    plugin.guiManager().openRoleNodes(player, roleNodesHolder.roleKey(), roleNodesHolder.page());
                });
                return;
            }
            String node = resolveNodeKey(current);
            if (node == null) {
                return;
            }
            var role = plugin.permissionService().roles().get(roleNodesHolder.roleKey());
            if (role == null) {
                return;
            }
            var currentDecision = role.nodes().getOrDefault(node, com.example.rpg.permissions.PermissionDecision.INHERIT);
            var nextDecision = switch (currentDecision) {
                case INHERIT -> com.example.rpg.permissions.PermissionDecision.ALLOW;
                case ALLOW -> com.example.rpg.permissions.PermissionDecision.DENY;
                case DENY -> com.example.rpg.permissions.PermissionDecision.INHERIT;
            };
            plugin.permissionService().setRoleNode(player, roleNodesHolder.roleKey(), node, nextDecision);
            plugin.guiManager().openRoleNodes(player, roleNodesHolder.roleKey(), roleNodesHolder.page());
            return;
        }
        if (holder instanceof GuiHolders.PlayerListHolder) {
            event.setCancelled(true);
            if (event.getSlot() == 45) {
                plugin.guiManager().openPermissionsMain(player);
                return;
            }
            if (event.getSlot() == 53) {
                plugin.promptManager().prompt(player, Text.mm("<yellow>Spielername eingeben:"), input -> {
                    var target = plugin.getServer().getOfflinePlayer(input);
                    if (target == null) {
                        player.sendMessage(Text.mm("<red>Spieler nicht gefunden."));
                        return;
                    }
                    plugin.guiManager().openPlayerRoles(player, target.getUniqueId());
                });
                return;
            }
            UUID targetId = resolvePlayerId(current);
            if (targetId != null) {
                plugin.guiManager().openPlayerRoles(player, targetId);
            }
            return;
        }
        if (holder instanceof GuiHolders.PlayerRoleHolder playerRoleHolder) {
            event.setCancelled(true);
            UUID targetId = playerRoleHolder.targetId();
            switch (event.getSlot()) {
                case 12 -> plugin.promptManager().prompt(player, Text.mm("<yellow>Primary Rolle setzen:"), input -> {
                    plugin.permissionService().assignPrimary(player, targetId, input.trim());
                    plugin.guiManager().openPlayerRoles(player, targetId);
                });
                case 14 -> plugin.promptManager().prompt(player, Text.mm("<yellow>Rolle hinzufügen:"), input -> {
                    plugin.permissionService().addRole(player, targetId, input.trim());
                    plugin.guiManager().openPlayerRoles(player, targetId);
                });
                case 16 -> plugin.promptManager().prompt(player, Text.mm("<yellow>Rolle entfernen:"), input -> {
                    plugin.permissionService().removeRole(player, targetId, input.trim());
                    plugin.guiManager().openPlayerRoles(player, targetId);
                });
                case 22 -> plugin.promptManager().prompt(player, Text.mm("<yellow>Node prüfen:"), input -> {
                    var explain = plugin.permissionService().explain(targetId, input.trim());
                    player.sendMessage(Text.mm("<yellow>Ergebnis: " + (explain.allowed() ? "ALLOW" : "DENY")));
                    if (explain.winningRole() != null) {
                        player.sendMessage(Text.mm("<gray>Role: " + explain.winningRole() + " Node: " + explain.winningNode()));
                    }
                });
                default -> {
                }
            }
            return;
        }
        if (holder instanceof GuiHolders.PermissionAuditHolder) {
            event.setCancelled(true);
            if (event.getSlot() == 45) {
                plugin.guiManager().openPermissionsMain(player);
            }
            return;
        }
        if (holder instanceof GuiHolders.EnchantingHolder enchantingHolder) {
            event.setCancelled(true);
            if (event.getSlot() == 26) {
                player.closeInventory();
                return;
            }
            if (event.getSlot() == 22) {
                String recipeId = enchantingHolder.recipeId();
                if (recipeId == null) {
                    player.sendMessage(Text.mm("<red>Kein Rezept ausgewählt."));
                    return;
                }
                plugin.enchantManager().applyRecipe(player, recipeId);
                plugin.guiManager().openEnchanting(player, recipeId);
                return;
            }
            String recipeId = resolveEnchantRecipeId(current);
            if (recipeId != null) {
                plugin.guiManager().openEnchanting(player, recipeId);
            }
        }
    }

    private Quest resolveQuest(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        String questId = meta.getPersistentDataContainer().get(plugin.questKey(), PersistentDataType.STRING);
        if (questId == null) {
            return null;
        }
        return plugin.questManager().getQuest(questId);
    }

    private Skill resolveSkill(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        String skillId = meta.getPersistentDataContainer().get(plugin.skillKey(), PersistentDataType.STRING);
        if (skillId == null) {
            return null;
        }
        return plugin.skillManager().getSkill(skillId);
    }

    private String resolveBuilding(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(plugin.buildingKey(), PersistentDataType.STRING);
    }

    private String resolveBuildingCategory(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(plugin.buildingCategoryKey(), PersistentDataType.STRING);
    }

    private String resolveRoleKey(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(plugin.permRoleKey(), PersistentDataType.STRING);
    }

    private String resolveNodeKey(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(plugin.permNodeKey(), PersistentDataType.STRING);
    }

    private UUID resolvePlayerId(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        String value = meta.getPersistentDataContainer().get(plugin.permPlayerKey(), PersistentDataType.STRING);
        if (value == null) {
            return null;
        }
        return UUID.fromString(value);
    }

    private String resolveEnchantRecipeId(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(plugin.enchantRecipeKey(), PersistentDataType.STRING);
    }

    private com.example.rpg.permissions.PermissionDecision parseDecision(String value) {
        return switch (value.toLowerCase()) {
            case "allow" -> com.example.rpg.permissions.PermissionDecision.ALLOW;
            case "deny" -> com.example.rpg.permissions.PermissionDecision.DENY;
            default -> com.example.rpg.permissions.PermissionDecision.INHERIT;
        };
    }

    private void handleShopClick(Player player, Inventory inventory, int slot, ItemStack clicked,
                                 GuiHolders.ShopHolder holder, boolean rightClick) {
        var shop = plugin.shopManager().getShop(holder.shopId());
        if (shop == null) {
            player.sendMessage(Text.mm("<red>Shop nicht gefunden."));
            return;
        }
        var shopItem = shop.items().get(slot);
        if (shopItem == null) {
            return;
        }
        var profile = plugin.playerDataManager().getProfile(player);
        Material material = Material.matchMaterial(shopItem.material());
        if (material == null) {
            player.sendMessage(Text.mm("<red>Item ungültig."));
            return;
        }
        if (rightClick) {
            int sellPrice = shopItem.sellPrice();
            if (sellPrice <= 0) {
                player.sendMessage(Text.mm("<red>Dieses Item kann nicht verkauft werden."));
                return;
            }
            if (!player.getInventory().contains(material)) {
                player.sendMessage(Text.mm("<red>Du hast dieses Item nicht."));
                return;
            }
            removeOne(player.getInventory(), material);
            profile.setGold(profile.gold() + sellPrice);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
            player.sendMessage(Text.mm("<green>Verkauft für <gold>" + sellPrice + "</gold> Gold."));
        } else {
            int buyPrice = shopItem.buyPrice();
            if (buyPrice <= 0) {
                player.sendMessage(Text.mm("<red>Dieses Item kann nicht gekauft werden."));
                return;
            }
            if (profile.gold() < buyPrice) {
                player.sendMessage(Text.mm("<red>Nicht genug Gold."));
                return;
            }
            profile.setGold(profile.gold() - buyPrice);
            player.getInventory().addItem(new ItemStack(material));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
            player.sendMessage(Text.mm("<green>Gekauft für <gold>" + buyPrice + "</gold> Gold."));
        }
        player.updateInventory();
        plugin.playerDataManager().saveProfile(profile);
    }

    private void removeOne(Inventory inventory, Material material) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack == null || stack.getType() != material) {
                continue;
            }
            if (stack.getAmount() > 1) {
                stack.setAmount(stack.getAmount() - 1);
            } else {
                inventory.setItem(i, null);
            }
            return;
        }
    }
}

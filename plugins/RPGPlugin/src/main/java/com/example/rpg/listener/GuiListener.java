package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.gui.GuiHolders;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.Quest;
import com.example.rpg.model.QuestProgress;
import com.example.rpg.model.Skill;
import com.example.rpg.util.Text;
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

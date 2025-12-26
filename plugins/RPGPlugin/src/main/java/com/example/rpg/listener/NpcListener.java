package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.Npc;
import com.example.rpg.model.NpcRole;
import com.example.rpg.model.DialogueNode;
import com.example.rpg.model.DialogueOption;
import com.example.rpg.model.FactionRank;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.Rarity;
import com.example.rpg.model.ShopDefinition;
import com.example.rpg.model.ShopItem;
import com.example.rpg.util.Text;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;

public class NpcListener implements Listener {
    private final RPGPlugin plugin;
    private final Random random = new Random();

    public NpcListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (!entity.getPersistentDataContainer().has(plugin.npcManager().npcKey(), PersistentDataType.STRING)) {
            return;
        }
        String npcId = entity.getPersistentDataContainer().get(plugin.npcManager().npcKey(), PersistentDataType.STRING);
        if (npcId == null) {
            return;
        }
        Npc npc = plugin.npcManager().getNpc(npcId);
        if (npc == null) {
            return;
        }
        Player player = event.getPlayer();
        if (!npc.dialogueNodes().isEmpty()) {
            openDialogue(player, npc, "start");
        } else if (!npc.dialog().isEmpty()) {
            player.sendMessage(Text.mm("<gold>" + npc.name() + ":"));
            for (String line : npc.dialog()) {
                player.sendMessage(Text.mm("<gray>" + line));
            }
        }
        if (npc.factionId() != null && npc.requiredRankId() != null) {
            PlayerProfile profile = plugin.playerDataManager().getProfile(player);
            int rep = profile.factionRep().getOrDefault(npc.factionId(), 0);
            FactionRank rank = plugin.factionManager().getRank(npc.factionId(), rep);
            if (rank == null || !rank.id().equalsIgnoreCase(npc.requiredRankId())) {
                player.sendMessage(Text.mm("<red>Dein Ruf reicht nicht aus."));
                return;
            }
        }
        if (npc.role() == NpcRole.QUESTGIVER && npc.questLink() != null) {
            player.sendMessage(Text.mm("<yellow>Quest verfügbar: <white>" + npc.questLink()));
            plugin.guiManager().openQuestList(player);
        }
        if (npc.role() == NpcRole.VENDOR) {
            if (npc.shopId() != null) {
                openStaticShop(player, npc);
            } else {
                ShopDefinition shop = buildMixedVendorShop(npc, player);
                applyFactionDiscount(npc, player, shop);
                plugin.guiManager().openShop(player, shop);
            }
            return;
        }
        if (npc.role() == NpcRole.WEAPON_VENDOR
            || npc.role() == NpcRole.ARMOR_VENDOR
            || npc.role() == NpcRole.ITEM_VENDOR
            || npc.role() == NpcRole.RESOURCE_VENDOR) {
            ShopDefinition shop = buildVendorShop(npc, player);
            applyFactionDiscount(npc, player, shop);
            plugin.guiManager().openShop(player, shop);
        }
    }

    private void openStaticShop(Player player, Npc npc) {
        if (npc.shopId() == null) {
            return;
        }
        var shop = plugin.shopManager().getShop(npc.shopId());
        if (shop == null) {
            player.sendMessage(Text.mm("<red>Shop nicht gefunden."));
            return;
        }
        plugin.guiManager().openShop(player, shop);
    }

    private ShopDefinition buildVendorShop(Npc npc, Player player) {
        String title = switch (npc.role()) {
            case WEAPON_VENDOR -> "Waffenhändler";
            case ARMOR_VENDOR -> "Rüstungshändler";
            case ITEM_VENDOR -> "Gegenstandshändler";
            case RESOURCE_VENDOR -> "Rohstoffhändler";
            default -> "Händler";
        };
        ShopDefinition shop = new ShopDefinition("npc_" + npc.id());
        shop.setTitle(title);
        Map<Integer, ShopItem> items = new java.util.HashMap<>();
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        int level = Math.max(1, profile.level());
        List<Material> materials = switch (npc.role()) {
            case WEAPON_VENDOR -> List.of(
                Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD,
                Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.BOW
            );
            case ARMOR_VENDOR -> List.of(
                Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS,
                Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS,
                Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS
            );
            case ITEM_VENDOR -> List.of(
                Material.BREAD, Material.COOKED_BEEF, Material.COOKED_CHICKEN, Material.GOLDEN_APPLE,
                Material.POTION, Material.ARROW
            );
            case RESOURCE_VENDOR -> List.of(
                Material.IRON_NUGGET, Material.GOLD_NUGGET, Material.IRON_INGOT,
                Material.GOLD_INGOT, Material.DIAMOND, Material.EMERALD
            );
            default -> List.of(Material.BREAD);
        };
        int slot = 0;
        for (int i = 0; i < Math.min(materials.size(), 9); i++) {
            Material material = materials.get(random.nextInt(materials.size()));
            ShopItem item = new ShopItem();
            item.setSlot(slot++);
            item.setMaterial(material.name());
            if (npc.role() == NpcRole.RESOURCE_VENDOR) {
                item.setBuyPrice(40 + random.nextInt(60));
                item.setSellPrice(10 + random.nextInt(20));
                item.setRpgItem(false);
            } else {
                Rarity rarity = rollRarity();
                int base = 60 + (level * 15);
                int buyPrice = (int) Math.max(20, base * (1 + rarity.weight()));
                int sellPrice = Math.max(10, buyPrice / 4);
                item.setBuyPrice(buyPrice);
                item.setSellPrice(sellPrice);
                item.setRpgItem(true);
                item.setRarity(rarity.name());
                item.setMinLevel(level);
            }
            items.put(item.slot(), item);
        }
        shop.setItems(items);
        plugin.shopManager().registerShop(shop);
        return shop;
    }

    private ShopDefinition buildMixedVendorShop(Npc npc, Player player) {
        ShopDefinition shop = new ShopDefinition("npc_" + npc.id());
        shop.setTitle("Gemischtwaren");
        Map<Integer, ShopItem> items = new java.util.HashMap<>();
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        int level = Math.max(1, profile.level());
        List<Material> rpgMaterials = List.of(
            Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD,
            Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS,
            Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE, Material.BOW
        );
        List<Material> normalMaterials = List.of(
            Material.BREAD, Material.COOKED_BEEF, Material.COOKED_CHICKEN,
            Material.IRON_NUGGET, Material.GOLD_NUGGET, Material.ARROW
        );
        int slot = 0;
        for (int i = 0; i < 6; i++) {
            Material material = rpgMaterials.get(random.nextInt(rpgMaterials.size()));
            ShopItem item = new ShopItem();
            item.setSlot(slot++);
            item.setMaterial(material.name());
            Rarity rarity = rollRarity();
            int base = 60 + (level * 12);
            int buyPrice = (int) Math.max(20, base * (1 + rarity.weight()));
            item.setBuyPrice(buyPrice);
            item.setSellPrice(Math.max(10, buyPrice / 4));
            item.setRpgItem(true);
            item.setRarity(rarity.name());
            item.setMinLevel(level);
            items.put(item.slot(), item);
        }
        for (int i = 0; i < 3; i++) {
            Material material = normalMaterials.get(random.nextInt(normalMaterials.size()));
            ShopItem item = new ShopItem();
            item.setSlot(slot++);
            item.setMaterial(material.name());
            item.setBuyPrice(20 + random.nextInt(30));
            item.setSellPrice(5 + random.nextInt(10));
            item.setRpgItem(false);
            items.put(item.slot(), item);
        }
        shop.setItems(items);
        plugin.shopManager().registerShop(shop);
        return shop;
    }

    private Rarity rollRarity() {
        double roll = random.nextDouble();
        double total = 0.0;
        for (Rarity rarity : Rarity.values()) {
            total += rarity.weight();
            if (roll <= total) {
                return rarity;
            }
        }
        return Rarity.COMMON;
    }

    private void applyFactionDiscount(Npc npc, Player player, ShopDefinition shop) {
        if (npc.factionId() == null) {
            return;
        }
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        int rep = profile.factionRep().getOrDefault(npc.factionId(), 0);
        FactionRank rank = plugin.factionManager().getRank(npc.factionId(), rep);
        if (rank == null || rank.shopDiscount() <= 0) {
            return;
        }
        for (ShopItem item : shop.items().values()) {
            int buy = item.buyPrice();
            if (buy <= 0) {
                continue;
            }
            int discounted = (int) Math.max(1, Math.round(buy * (1 - rank.shopDiscount())));
            item.setBuyPrice(discounted);
        }
    }

    private void openDialogue(Player player, Npc npc, String nodeId) {
        DialogueNode node = npc.dialogueNodes().get(nodeId);
        if (node == null) {
            return;
        }
        player.sendMessage(Text.mm("<gold>" + npc.name() + ": <white>" + node.text()));
        if (node.options().isEmpty()) {
            return;
        }
        java.util.List<DialogueOption> available = new java.util.ArrayList<>();
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        for (DialogueOption option : node.options()) {
            if (option.requiredFactionId() != null) {
                int rep = profile.factionRep().getOrDefault(option.requiredFactionId(), 0);
                if (rep < option.minRep()) {
                    continue;
                }
            }
            if (option.requiredQuestId() != null) {
                boolean completed = profile.completedQuests().contains(option.requiredQuestId());
                boolean active = profile.activeQuests().containsKey(option.requiredQuestId());
                if (option.requireQuestCompleted() && !completed) {
                    continue;
                }
                if (!option.requireQuestCompleted() && !active && !completed) {
                    continue;
                }
            }
            available.add(option);
        }
        if (available.isEmpty()) {
            player.sendMessage(Text.mm("<gray>Keine Optionen verfügbar."));
            return;
        }
        int index = 1;
        for (DialogueOption option : available) {
            player.sendMessage(Text.mm("<yellow>" + index++ + ". <white>" + option.text()));
        }
        plugin.promptManager().prompt(player, Text.mm("<gray>Wähle eine Option (Zahl):"), input -> {
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                player.sendMessage(Text.mm("<red>Ungültige Auswahl."));
                return;
            }
            if (choice < 1 || choice > available.size()) {
                player.sendMessage(Text.mm("<red>Ungültige Auswahl."));
                return;
            }
            DialogueOption selected = available.get(choice - 1);
            if (selected.grantQuestId() != null) {
                var quest = plugin.questManager().getQuest(selected.grantQuestId());
                if (quest != null && !profile.activeQuests().containsKey(quest.id())) {
                    profile.activeQuests().put(quest.id(), new com.example.rpg.model.QuestProgress(quest.id()));
                    player.sendMessage(Text.mm("<green>Quest angenommen: " + quest.name()));
                }
            }
            if (selected.nextId() != null && !"end".equalsIgnoreCase(selected.nextId())) {
                openDialogue(player, npc, selected.nextId());
            }
        });
    }
}

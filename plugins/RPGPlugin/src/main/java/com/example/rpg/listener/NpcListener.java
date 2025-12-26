package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.Npc;
import com.example.rpg.model.NpcRole;
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
        if (!npc.dialog().isEmpty()) {
            player.sendMessage(Text.mm("<gold>" + npc.name() + ":"));
            for (String line : npc.dialog()) {
                player.sendMessage(Text.mm("<gray>" + line));
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
                plugin.guiManager().openShop(player, shop);
            }
            return;
        }
        if (npc.role() == NpcRole.WEAPON_VENDOR
            || npc.role() == NpcRole.ARMOR_VENDOR
            || npc.role() == NpcRole.ITEM_VENDOR
            || npc.role() == NpcRole.RESOURCE_VENDOR) {
            ShopDefinition shop = buildVendorShop(npc, player);
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
}

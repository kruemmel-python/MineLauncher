package com.example.rpg.manager;

import com.example.rpg.model.RPGStat;
import com.example.rpg.util.Text;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.EnumMap;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ItemStatManager {
    private final Random random = new Random();
    private final NamespacedKey strengthKey;
    private final NamespacedKey critKey;
    private final NamespacedKey healthKey;
    private final NamespacedKey setIdKey;
    private final NamespacedKey enchantAffixKey;
    private final Map<RPGStat, NamespacedKey> enchantStatKeys = new EnumMap<>(RPGStat.class);
    private final Map<String, PotionEffectType> setBonuses = Map.of(
        "ember", PotionEffectType.FIRE_RESISTANCE,
        "guardian", PotionEffectType.DAMAGE_RESISTANCE,
        "swift", PotionEffectType.SPEED
    );

    public ItemStatManager(JavaPlugin plugin) {
        this.strengthKey = new NamespacedKey(plugin, "stat_strength");
        this.critKey = new NamespacedKey(plugin, "stat_crit");
        this.healthKey = new NamespacedKey(plugin, "stat_health");
        this.setIdKey = new NamespacedKey(plugin, "set_id");
        this.enchantAffixKey = new NamespacedKey(plugin, "enchant_affixes");
        for (RPGStat stat : RPGStat.values()) {
            enchantStatKeys.put(stat, new NamespacedKey(plugin, "enchant_stat_" + stat.name().toLowerCase()));
        }
    }

    public void applyAffixes(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        String prefix = randomFrom(List.of("Brennendes", "Gefrorenes", "Stählernen", "Mystisches"));
        String suffix = randomFrom(List.of("der Stärke", "der Präzision", "des Lebens"));
        meta.displayName(Component.text(prefix + " " + prettyName(item.getType().name()) + " " + suffix));

        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(strengthKey, PersistentDataType.INTEGER, 1 + random.nextInt(4));
        data.set(critKey, PersistentDataType.DOUBLE, 0.02 + random.nextDouble() * 0.08);
        data.set(healthKey, PersistentDataType.INTEGER, 2 + random.nextInt(6));
        data.set(setIdKey, PersistentDataType.STRING, randomFrom(setBonuses.keySet().stream().toList()));

        updateLore(meta);
        item.setItemMeta(meta);
    }

    public void updateLore(ItemMeta meta) {
        PersistentDataContainer data = meta.getPersistentDataContainer();
        int strength = data.getOrDefault(strengthKey, PersistentDataType.INTEGER, 0);
        double crit = data.getOrDefault(critKey, PersistentDataType.DOUBLE, 0.0);
        int health = data.getOrDefault(healthKey, PersistentDataType.INTEGER, 0);
        String setId = data.get(setIdKey, PersistentDataType.STRING);
        List<Component> lore = new java.util.ArrayList<>();
        lore.add(Text.mm("<gray>Stärke: <white>" + strength));
        lore.add(Text.mm("<gray>Krit-Chance: <white>" + String.format("%.1f%%", crit * 100)));
        lore.add(Text.mm("<gray>Leben: <white>" + health));
        if (setId != null) {
            lore.add(Text.mm("<gold>Set: " + setId + " (4 Teile)"));
        } else {
            lore.add(Text.mm("<gray>Kein Set"));
        }
        for (RPGStat stat : RPGStat.values()) {
            NamespacedKey key = enchantStatKeys.get(stat);
            if (key == null) {
                continue;
            }
            int value = data.getOrDefault(key, PersistentDataType.INTEGER, 0);
            if (value > 0) {
                lore.add(Text.mm("<aqua>Affix " + stat.name() + ": <white>+" + value));
            }
        }
        String affixes = data.get(enchantAffixKey, PersistentDataType.STRING);
        if (affixes != null && !affixes.isBlank()) {
            lore.add(Text.mm("<light_purple>Affixe: <white>" + affixes));
        }
        meta.lore(lore);
    }

    public void updateSetBonus(Player player) {
        Map<String, Integer> counts = new java.util.HashMap<>();
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item == null || item.getItemMeta() == null) {
                continue;
            }
            String setId = item.getItemMeta().getPersistentDataContainer().get(setIdKey, PersistentDataType.STRING);
            if (setId == null) {
                continue;
            }
            counts.put(setId, counts.getOrDefault(setId, 0) + 1);
        }
        for (Map.Entry<String, PotionEffectType> entry : setBonuses.entrySet()) {
            PotionEffectType type = entry.getValue();
            if (type == null) {
                continue;
            }
            if (counts.getOrDefault(entry.getKey(), 0) >= 4) {
                player.addPotionEffect(new PotionEffect(type, 220, 0, true, false));
            } else {
                player.removePotionEffect(type);
            }
        }
    }

    public NamespacedKey strengthKey() {
        return strengthKey;
    }

    public NamespacedKey critKey() {
        return critKey;
    }

    public NamespacedKey healthKey() {
        return healthKey;
    }

    public NamespacedKey setIdKey() {
        return setIdKey;
    }

    public NamespacedKey enchantStatKey(RPGStat stat) {
        return enchantStatKeys.get(stat);
    }

    public NamespacedKey enchantAffixKey() {
        return enchantAffixKey;
    }

    private String randomFrom(List<String> values) {
        return values.get(random.nextInt(values.size()));
    }

    private String prettyName(String material) {
        return material.toLowerCase().replace("_", " ");
    }
}

package com.example.rpg.manager;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.EnchantRecipeType;
import com.example.rpg.model.EnchantTargetSlot;
import com.example.rpg.model.EnchantmentRecipe;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.RPGStat;
import com.example.rpg.skill.SkillEffectConfig;
import com.example.rpg.skill.SkillEffectType;
import com.example.rpg.util.Text;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class EnchantManager {
    private final RPGPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, EnchantmentRecipe> recipes = new HashMap<>();
    private final NamespacedKey rpgItemKey;
    private final NamespacedKey affixKey;
    private final Map<String, RPGStat> affixStatBonuses = Map.of(
        "Praezision", RPGStat.DEXTERITY
    );

    public EnchantManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "enchantments.yml");
        if (!file.exists()) {
            plugin.saveResource("enchantments.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        this.rpgItemKey = new NamespacedKey(plugin, "rpg_item");
        this.affixKey = new NamespacedKey(plugin, "enchant_affixes");
        load();
    }

    public Map<String, EnchantmentRecipe> recipes() {
        return recipes;
    }

    public List<EnchantmentRecipe> availableRecipes(Player player, ItemStack target) {
        if (!isRpgItem(target)) {
            return List.of();
        }
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        List<EnchantmentRecipe> available = new ArrayList<>();
        for (EnchantmentRecipe recipe : recipes.values()) {
            if (profile.level() < recipe.minLevel()) {
                continue;
            }
            if (!recipe.targetSlot().matches(target)) {
                continue;
            }
            available.add(recipe);
        }
        return available;
    }

    public boolean applyRecipe(Player player, String recipeId) {
        EnchantmentRecipe recipe = recipes.get(recipeId);
        if (recipe == null) {
            player.sendMessage(Text.mm("<red>Verzauberung nicht gefunden."));
            return false;
        }
        ItemStack target = getTargetItem(player, recipe.targetSlot());
        if (target == null || !isRpgItem(target)) {
            player.sendMessage(Text.mm("<red>Kein gültiges Ziel-Item."));
            return false;
        }
        if (!recipe.targetSlot().matches(target)) {
            player.sendMessage(Text.mm("<red>Dieses Item passt nicht zum Ziel-Slot."));
            return false;
        }
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        if (profile.level() < recipe.minLevel()) {
            player.sendMessage(Text.mm("<red>Level zu niedrig."));
            return false;
        }
        if (profile.gold() < recipe.costGold()) {
            player.sendMessage(Text.mm("<red>Nicht genug Gold."));
            return false;
        }
        if (!hasCostItem(player, recipe)) {
            player.sendMessage(Text.mm("<red>Fehlende Materialien."));
            return false;
        }
        profile.setGold(profile.gold() - recipe.costGold());
        removeCostItem(player, recipe);
        ItemMeta meta = target.getItemMeta();
        if (meta == null) {
            return false;
        }
        String itemName = resolveItemName(target, meta);
        List<String> changeLines = new ArrayList<>();
        switch (recipe.type()) {
            case STAT_UPGRADE -> {
                int delta = applyStatUpgrade(meta, recipe);
                if (recipe.statToImprove() != null && delta > 0) {
                    changeLines.add("+" + delta + " " + recipe.statToImprove().name());
                }
            }
            case AFFIX -> {
                AffixResult result = applyAffix(meta, recipe);
                if (result.added()) {
                    changeLines.add("Affix: " + result.affixName());
                }
                if (result.statBonus() != null && result.statDelta() > 0) {
                    changeLines.add("+" + result.statDelta() + " " + result.statBonus().name());
                }
            }
        }
        plugin.itemStatManager().updateLore(meta);
        target.setItemMeta(meta);
        setTargetItem(player, recipe.targetSlot(), target);
        applyEffects(player, profile, recipe.effects());
        if (changeLines.isEmpty()) {
            player.sendMessage(Text.mm("<green>Verzauberung angewendet auf <white>" + itemName));
        } else {
            player.sendMessage(Text.mm("<green>Verzauberung angewendet:</green> <white>" + itemName));
            for (String line : changeLines) {
                player.sendMessage(Text.mm("<gray> - " + line));
            }
        }
        return true;
    }

    public NamespacedKey affixKey() {
        return affixKey;
    }

    private void load() {
        recipes.clear();
        for (String key : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            EnchantmentRecipe recipe = new EnchantmentRecipe(key);
            recipe.setType(parseEnum(EnchantRecipeType.class, section.getString("type"))
                .orElse(EnchantRecipeType.STAT_UPGRADE));
            recipe.setTargetSlot(parseEnum(EnchantTargetSlot.class, section.getString("targetSlot"))
                .orElse(EnchantTargetSlot.HAND));
            recipe.setStatToImprove(parseEnum(RPGStat.class, section.getString("statToImprove"))
                .orElse(null));
            recipe.setMinLevel(section.getInt("minLevel", 1));
            recipe.setCostGold(section.getInt("costGold", 0));
            parseCostItem(section.getString("costItem"), recipe);
            recipe.setAffix(section.getString("affix", null));
            parseEffects(section.getMapList("effects"), recipe.effects());
            recipes.put(key, recipe);
        }
    }

    private void parseCostItem(String raw, EnchantmentRecipe recipe) {
        if (raw == null || raw.isBlank() || !raw.contains(":")) {
            return;
        }
        String[] parts = raw.split(":", 2);
        Material material = Material.matchMaterial(parts[0].trim().toUpperCase());
        if (material == null) {
            return;
        }
        recipe.setCostMaterial(material);
        try {
            recipe.setCostAmount(Integer.parseInt(parts[1].trim()));
        } catch (NumberFormatException e) {
            recipe.setCostAmount(1);
        }
    }

    private void parseEffects(List<Map<?, ?>> effectList, List<SkillEffectConfig> target) {
        if (effectList == null || effectList.isEmpty()) {
            return;
        }
        for (Map<?, ?> entry : effectList) {
            Object typeValue = entry.get("type");
            SkillEffectType type = parseEnum(SkillEffectType.class, typeValue != null ? typeValue.toString() : null)
                .orElse(null);
            if (type == null) {
                continue;
            }
            Map<String, Object> params = new HashMap<>();
            Object paramsValue = entry.get("params");
            if (paramsValue instanceof Map<?, ?> paramMap) {
                for (Map.Entry<?, ?> paramEntry : paramMap.entrySet()) {
                    params.put(String.valueOf(paramEntry.getKey()), paramEntry.getValue());
                }
            }
            target.add(new SkillEffectConfig(type, params));
        }
    }

    private boolean isRpgItem(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return false;
        }
        PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
        return data.has(rpgItemKey, PersistentDataType.INTEGER);
    }

    private ItemStack getTargetItem(Player player, EnchantTargetSlot targetSlot) {
        return switch (targetSlot) {
            case HAND -> player.getInventory().getItemInMainHand();
            case OFF_HAND, SHIELD -> player.getInventory().getItemInOffHand();
            case ARMOR_HEAD -> player.getInventory().getHelmet();
            case ARMOR_CHEST -> player.getInventory().getChestplate();
            case ARMOR_LEGS -> player.getInventory().getLeggings();
            case ARMOR_FEET -> player.getInventory().getBoots();
        };
    }

    private void setTargetItem(Player player, EnchantTargetSlot targetSlot, ItemStack item) {
        switch (targetSlot) {
            case HAND -> player.getInventory().setItemInMainHand(item);
            case OFF_HAND, SHIELD -> player.getInventory().setItemInOffHand(item);
            case ARMOR_HEAD -> player.getInventory().setHelmet(item);
            case ARMOR_CHEST -> player.getInventory().setChestplate(item);
            case ARMOR_LEGS -> player.getInventory().setLeggings(item);
            case ARMOR_FEET -> player.getInventory().setBoots(item);
        }
        player.updateInventory();
    }

    private boolean hasCostItem(Player player, EnchantmentRecipe recipe) {
        if (recipe.costMaterial() == null || recipe.costAmount() <= 0) {
            return true;
        }
        int total = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() != recipe.costMaterial()) {
                continue;
            }
            total += item.getAmount();
            if (total >= recipe.costAmount()) {
                return true;
            }
        }
        return false;
    }

    private void removeCostItem(Player player, EnchantmentRecipe recipe) {
        if (recipe.costMaterial() == null || recipe.costAmount() <= 0) {
            return;
        }
        int remaining = recipe.costAmount();
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() != recipe.costMaterial()) {
                continue;
            }
            int remove = Math.min(item.getAmount(), remaining);
            item.setAmount(item.getAmount() - remove);
            remaining -= remove;
            if (item.getAmount() <= 0) {
                contents[i] = null;
            }
            if (remaining <= 0) {
                break;
            }
        }
        player.getInventory().setContents(contents);
    }

    private int applyStatUpgrade(ItemMeta meta, EnchantmentRecipe recipe) {
        if (recipe.statToImprove() == null) {
            return 0;
        }
        NamespacedKey key = plugin.itemStatManager().enchantStatKey(recipe.statToImprove());
        PersistentDataContainer data = meta.getPersistentDataContainer();
        int current = data.getOrDefault(key, PersistentDataType.INTEGER, 0);
        data.set(key, PersistentDataType.INTEGER, current + 1);
        applyBaseStatBonus(meta, recipe.statToImprove(), 1);
        return 1;
    }

    private AffixResult applyAffix(ItemMeta meta, EnchantmentRecipe recipe) {
        if (recipe.affix() == null || recipe.affix().isBlank()) {
            return AffixResult.none();
        }
        PersistentDataContainer data = meta.getPersistentDataContainer();
        String current = data.getOrDefault(affixKey, PersistentDataType.STRING, "");
        List<String> affixes = new ArrayList<>();
        if (!current.isBlank()) {
            affixes.addAll(List.of(current.split(",")));
        }
        boolean added = false;
        if (!affixes.contains(recipe.affix())) {
            affixes.add(recipe.affix());
            added = true;
        }
        data.set(affixKey, PersistentDataType.STRING, String.join(",", affixes));
        RPGStat statBonus = affixStatBonuses.getOrDefault(recipe.affix(), RPGStat.STRENGTH);
        NamespacedKey statKey = plugin.itemStatManager().enchantStatKey(statBonus);
        int currentStat = data.getOrDefault(statKey, PersistentDataType.INTEGER, 0);
        data.set(statKey, PersistentDataType.INTEGER, currentStat + 1);
        applyBaseStatBonus(meta, statBonus, 1);
        return new AffixResult(added, recipe.affix(), statBonus, 1);
    }

    private void applyEffects(Player player, PlayerProfile profile, List<SkillEffectConfig> effects) {
        for (SkillEffectConfig config : effects) {
            plugin.skillEffects().apply(config, player, profile);
        }
    }

    private static <E extends Enum<E>> Optional<E> parseEnum(Class<E> type, String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String key = raw.trim().toUpperCase();
        try {
            return Optional.of(Enum.valueOf(type, key));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private String resolveItemName(ItemStack item, ItemMeta meta) {
        if (meta != null && meta.hasDisplayName()) {
            return meta.getDisplayName();
        }
        return item.getType().name().toLowerCase().replace("_", " ");
    }

    private void applyBaseStatBonus(ItemMeta meta, RPGStat stat, int amount) {
        PersistentDataContainer data = meta.getPersistentDataContainer();
        switch (stat) {
            case STRENGTH -> {
                NamespacedKey key = plugin.itemStatManager().strengthKey();
                int current = data.getOrDefault(key, PersistentDataType.INTEGER, 0);
                data.set(key, PersistentDataType.INTEGER, current + amount);
            }
            case DEXTERITY -> {
                NamespacedKey key = plugin.itemStatManager().critKey();
                double current = data.getOrDefault(key, PersistentDataType.DOUBLE, 0.0);
                data.set(key, PersistentDataType.DOUBLE, current + (amount * 0.01));
            }
            case CONSTITUTION -> {
                NamespacedKey key = plugin.itemStatManager().healthKey();
                int current = data.getOrDefault(key, PersistentDataType.INTEGER, 0);
                data.set(key, PersistentDataType.INTEGER, current + amount);
            }
            case INTELLIGENCE, LUCK -> {
                // No direct base stat stored; keep as enchant stat only.
            }
        }
    }

    private record AffixResult(boolean added, String affixName, RPGStat statBonus, int statDelta) {
        private static AffixResult none() {
            return new AffixResult(false, "", null, 0);
        }
    }
}

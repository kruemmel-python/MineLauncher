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
        switch (recipe.type()) {
            case STAT_UPGRADE -> applyStatUpgrade(meta, recipe);
            case AFFIX -> applyAffix(meta, recipe);
        }
        plugin.itemStatManager().updateLore(meta);
        target.setItemMeta(meta);
        applyEffects(player, profile, recipe.effects());
        player.sendMessage(Text.mm("<green>Verzauberung angewendet."));
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

    private void applyStatUpgrade(ItemMeta meta, EnchantmentRecipe recipe) {
        if (recipe.statToImprove() == null) {
            return;
        }
        NamespacedKey key = plugin.itemStatManager().enchantStatKey(recipe.statToImprove());
        PersistentDataContainer data = meta.getPersistentDataContainer();
        int current = data.getOrDefault(key, PersistentDataType.INTEGER, 0);
        data.set(key, PersistentDataType.INTEGER, current + 1);
    }

    private void applyAffix(ItemMeta meta, EnchantmentRecipe recipe) {
        if (recipe.affix() == null || recipe.affix().isBlank()) {
            return;
        }
        PersistentDataContainer data = meta.getPersistentDataContainer();
        String current = data.getOrDefault(affixKey, PersistentDataType.STRING, "");
        List<String> affixes = new ArrayList<>();
        if (!current.isBlank()) {
            affixes.addAll(List.of(current.split(",")));
        }
        if (!affixes.contains(recipe.affix())) {
            affixes.add(recipe.affix());
        }
        data.set(affixKey, PersistentDataType.STRING, String.join(",", affixes));
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
}

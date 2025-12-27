package com.example.rpg.manager;

import com.example.rpg.model.DialogueNode;
import com.example.rpg.model.DialogueOption;
import com.example.rpg.model.Npc;
import com.example.rpg.model.NpcRole;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class NpcManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Npc> npcs = new HashMap<>();
    private final NamespacedKey npcKey;

    public NpcManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "npcs.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        this.npcKey = new NamespacedKey(plugin, "npc_id");
        load();
    }

    public NamespacedKey npcKey() {
        return npcKey;
    }

    public Map<String, Npc> npcs() {
        return npcs;
    }

    public Npc getNpc(String id) {
        return npcs.get(id);
    }

    public void spawnAll() {
        for (Npc npc : npcs.values()) {
            spawnNpc(npc);
        }
    }

    public void spawnNpc(Npc npc) {
        World world = Bukkit.getWorld(npc.world());
        if (world == null) {
            return;
        }
        Location location = npc.toLocation(world);
        Villager villager = (Villager) world.spawnEntity(location, EntityType.VILLAGER);
        villager.customName(Component.text(npc.name()));
        villager.setCustomNameVisible(true);
        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.setSilent(true);
        villager.getPersistentDataContainer().set(npcKey, PersistentDataType.STRING, npc.id());
        npc.setUuid(villager.getUniqueId());
    }

    public void saveNpc(Npc npc) {
        ConfigurationSection section = config.createSection(npc.id());
        section.set("name", npc.name());
        section.set("role", npc.role().name());
        section.set("world", npc.world());
        section.set("x", npc.x());
        section.set("y", npc.y());
        section.set("z", npc.z());
        section.set("yaw", npc.yaw());
        section.set("pitch", npc.pitch());
        section.set("dialog", npc.dialog());
        section.set("questLink", npc.questLink());
        section.set("shopId", npc.shopId());
        section.set("factionId", npc.factionId());
        section.set("requiredRankId", npc.requiredRankId());
        section.set("dialogueNodes", serializeDialogue(npc));
        section.set("uuid", npc.uuid() != null ? npc.uuid().toString() : null);
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (Npc npc : npcs.values()) {
            saveNpc(npc);
        }
        save();
    }

    private void load() {
        npcs.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Npc npc = new Npc(id);
            npc.setName(section.getString("name", id));
            npc.setRole(NpcRole.valueOf(section.getString("role", "QUESTGIVER")));
            npc.setWorld(section.getString("world", "world"));
            npc.setDialog(section.getStringList("dialog"));
            npc.setQuestLink(section.getString("questLink", null));
            npc.setShopId(section.getString("shopId", null));
            npc.setFactionId(section.getString("factionId", null));
            npc.setRequiredRankId(section.getString("requiredRankId", null));
            loadDialogue(section, npc);
            npc.setUuid(section.contains("uuid") ? UUID.fromString(section.getString("uuid")) : null);
            World world = Bukkit.getWorld(npc.world());
            double x = section.getDouble("x");
            double y = section.getDouble("y");
            double z = section.getDouble("z");
            float yaw = (float) section.getDouble("yaw");
            float pitch = (float) section.getDouble("pitch");
            if (world != null) {
                npc.setLocation(new Location(world, x, y, z, yaw, pitch));
            } else {
                npc.setRawLocation(npc.world(), x, y, z, yaw, pitch);
            }
            npcs.put(id, npc);
        }
    }

    private java.util.List<java.util.Map<String, Object>> serializeDialogue(Npc npc) {
        java.util.List<java.util.Map<String, Object>> nodes = new java.util.ArrayList<>();
        for (DialogueNode node : npc.dialogueNodes().values()) {
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("id", node.id());
            data.put("text", node.text());
            java.util.List<java.util.Map<String, Object>> options = new java.util.ArrayList<>();
            for (DialogueOption option : node.options()) {
                java.util.Map<String, Object> optionData = new java.util.HashMap<>();
                optionData.put("text", option.text());
                optionData.put("nextId", option.nextId());
                optionData.put("requiredFactionId", option.requiredFactionId());
                optionData.put("minRep", option.minRep());
                optionData.put("requiredQuestId", option.requiredQuestId());
                optionData.put("requireQuestCompleted", option.requireQuestCompleted());
                optionData.put("grantQuestId", option.grantQuestId());
                options.add(optionData);
            }
            data.put("options", options);
            nodes.add(data);
        }
        return nodes;
    }

    private void loadDialogue(ConfigurationSection section, Npc npc) {
        npc.dialogueNodes().clear();
        for (java.util.Map<?, ?> raw : section.getMapList("dialogueNodes")) {
            String idValue = mapString(raw, "id", "start");
            DialogueNode node = new DialogueNode(idValue);
            node.setText(mapString(raw, "text", ""));
            Object optionsRaw = raw.get("options");
            if (optionsRaw instanceof java.util.List<?> options) {
                for (Object entry : options) {
                    if (!(entry instanceof java.util.Map<?, ?> optRaw)) {
                        continue;
                    }
                    DialogueOption option = new DialogueOption();
                    option.setText(mapString(optRaw, "text", "Weiter"));
                    option.setNextId(mapString(optRaw, "nextId", "end"));
                    option.setRequiredFactionId(valueOrNull(optRaw.get("requiredFactionId")));
                    option.setMinRep(mapInt(optRaw, "minRep", 0));
                    option.setRequiredQuestId(valueOrNull(optRaw.get("requiredQuestId")));
                    option.setRequireQuestCompleted(mapBool(optRaw, "requireQuestCompleted", false));
                    option.setGrantQuestId(valueOrNull(optRaw.get("grantQuestId")));
                    node.options().add(option);
                }
            }
            npc.dialogueNodes().put(node.id(), node);
        }
    }

    private String mapString(java.util.Map<?, ?> raw, String key, String fallback) {
        Object value = raw.get(key);
        if (value == null) {
            return fallback;
        }
        String resolved = String.valueOf(value);
        return resolved.isBlank() ? fallback : resolved;
    }

    private int mapInt(java.util.Map<?, ?> raw, String key, int fallback) {
        Object value = raw.get(key);
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private boolean mapBool(java.util.Map<?, ?> raw, String key, boolean fallback) {
        Object value = raw.get(key);
        if (value == null) {
            return fallback;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private String valueOrNull(Object raw) {
        if (raw == null) {
            return null;
        }
        String value = String.valueOf(raw);
        return value.isBlank() ? null : value;
    }

    private int parseInt(Object raw) {
        try {
            return Integer.parseInt(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save npcs.yml: " + e.getMessage());
        }
    }
}

package com.example.rpg.manager;

import com.example.rpg.behavior.BehaviorNode;
import com.example.rpg.behavior.CastSkillNode;
import com.example.rpg.behavior.CooldownNode;
import com.example.rpg.behavior.FleeNode;
import com.example.rpg.behavior.HealthBelowNode;
import com.example.rpg.behavior.HealSelfNode;
import com.example.rpg.behavior.InverterNode;
import com.example.rpg.behavior.MeleeAttackNode;
import com.example.rpg.behavior.SelectorNode;
import com.example.rpg.behavior.SequenceNode;
import com.example.rpg.behavior.TargetDistanceAboveNode;
import com.example.rpg.behavior.WalkToTargetNode;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class BehaviorTreeManager {
    private final JavaPlugin plugin;
    private final File folder;
    private final Map<String, BehaviorNode> trees = new HashMap<>();

    public BehaviorTreeManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "behaviors");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        seedSkeletonKing();
        loadAll();
    }

    public BehaviorNode getTree(String name) {
        if (name == null) {
            return defaultTree();
        }
        return trees.getOrDefault(name, defaultTree());
    }

    public void addTemplate(String treeName, Map<String, Object> template) {
        YamlConfiguration config = loadConfig(treeName);
        List<Map<?, ?>> children = new ArrayList<>(config.getMapList("children"));
        children.add(template);
        config.set("type", "selector");
        config.set("children", children);
        saveConfig(treeName, config);
        loadAll();
    }

    public void resetTree(String treeName) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("type", "selector");
        config.set("children", new ArrayList<>());
        saveConfig(treeName, config);
        loadAll();
    }

    private void loadAll() {
        trees.clear();
        File[] files = folder.listFiles((dir, file) -> file.endsWith(".yml"));
        if (files == null) {
            return;
        }
        for (File file : files) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            BehaviorNode root = parseNode(config, "root");
            String key = file.getName().replace(".yml", "");
            if (root != null) {
                trees.put(key, root);
            }
        }
    }

    private BehaviorNode parseNode(ConfigurationSection section, String fallbackId) {
        if (section == null) {
            return null;
        }
        String type = section.getString("type", "selector");
        String id = section.getString("id", fallbackId + "-" + UUID.randomUUID());
        return buildNode(type, id, section);
    }

    private BehaviorNode buildNode(String type, String id, ConfigurationSection section) {
        return switch (type.toLowerCase()) {
            case "selector" -> buildComposite(new SelectorNode(id), section);
            case "sequence" -> buildComposite(new SequenceNode(id), section);
            case "inverter" -> {
                BehaviorNode child = parseChild(section, "child", id);
                yield child != null ? new InverterNode(id, child) : null;
            }
            case "cooldown" -> {
                BehaviorNode child = parseChild(section, "child", id);
                long cooldown = (long) (section.getDouble("cooldownSeconds", 5) * 1000);
                yield child != null ? new CooldownNode(id, child, cooldown) : null;
            }
            case "melee_attack" -> new MeleeAttackNode(id);
            case "cast_skill" -> new CastSkillNode(id, section.getString("skill", "ember_shot"));
            case "flee" -> new FleeNode(id);
            case "heal_self" -> new HealSelfNode(id, section.getDouble("amount", 6));
            case "walk_to_target" -> new WalkToTargetNode(id);
            case "health_below" -> new HealthBelowNode(id, section.getDouble("threshold", 0.2));
            case "target_distance_above" -> new TargetDistanceAboveNode(id, section.getDouble("distance", 10));
            default -> null;
        };
    }

    private BehaviorNode buildComposite(com.example.rpg.behavior.CompositeNode node, ConfigurationSection section) {
        List<Map<?, ?>> children = section.getMapList("children");
        for (int i = 0; i < children.size(); i++) {
            Map<?, ?> data = children.get(i);
            if (!(data.get("type") instanceof String childType)) {
                continue;
            }
            YamlConfiguration childConfig = new YamlConfiguration();
            for (Map.Entry<?, ?> entry : data.entrySet()) {
                childConfig.set(String.valueOf(entry.getKey()), entry.getValue());
            }
            BehaviorNode child = buildNode(childType, node.id() + "-child-" + i, childConfig);
            if (child != null) {
                node.children().add(child);
            }
        }
        return node;
    }

    private BehaviorNode parseChild(ConfigurationSection section, String key, String id) {
        ConfigurationSection childSection = section.getConfigurationSection(key);
        if (childSection != null) {
            return parseNode(childSection, id + "-child");
        }
        return null;
    }

    private BehaviorNode defaultTree() {
        SelectorNode root = new SelectorNode("default-root");
        SequenceNode chase = new SequenceNode("default-chase");
        chase.children().add(new TargetDistanceAboveNode("default-dist", 2));
        chase.children().add(new WalkToTargetNode("default-walk"));
        root.children().add(chase);
        root.children().add(new MeleeAttackNode("default-melee"));
        return root;
    }

    private void seedSkeletonKing() {
        File file = new File(folder, "skeleton_king.yml");
        if (file.exists()) {
            return;
        }
        YamlConfiguration config = new YamlConfiguration();
        config.set("type", "selector");
        List<Map<String, Object>> children = new ArrayList<>();

        Map<String, Object> emergency = new HashMap<>();
        emergency.put("type", "sequence");
        List<Map<String, Object>> emergencyChildren = new ArrayList<>();
        emergencyChildren.add(Map.of("type", "health_below", "threshold", 0.2));
        emergencyChildren.add(Map.of("type", "cast_skill", "skill", "shield_wall"));
        emergencyChildren.add(Map.of("type", "heal_self", "amount", 8));
        emergency.put("children", emergencyChildren);
        children.add(emergency);

        Map<String, Object> ranged = new HashMap<>();
        ranged.put("type", "sequence");
        List<Map<String, Object>> rangedChildren = new ArrayList<>();
        rangedChildren.add(Map.of("type", "target_distance_above", "distance", 10));
        rangedChildren.add(Map.of("type", "cast_skill", "skill", "ember_shot"));
        ranged.put("children", rangedChildren);
        children.add(ranged);

        children.add(Map.of("type", "melee_attack"));
        config.set("children", children);
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to seed skeleton_king.yml: " + e.getMessage());
        }
    }

    private YamlConfiguration loadConfig(String treeName) {
        File file = new File(folder, treeName + ".yml");
        return YamlConfiguration.loadConfiguration(file);
    }

    private void saveConfig(String treeName, YamlConfiguration config) {
        File file = new File(folder, treeName + ".yml");
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save behavior tree " + treeName + ": " + e.getMessage());
        }
    }
}

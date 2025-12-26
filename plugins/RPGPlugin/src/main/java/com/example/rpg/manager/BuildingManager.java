package com.example.rpg.manager;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.BuildingCategory;
import com.example.rpg.model.BuildingDefinition;
import com.example.rpg.model.FurnitureDefinition;
import com.example.rpg.schematic.Schematic;
import com.example.rpg.schematic.SchematicPaster;
import com.example.rpg.schematic.SpongeSchemLoader;
import com.example.rpg.schematic.Transform;
import com.example.rpg.util.Text;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class BuildingManager {
    private final RPGPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, BuildingDefinition> buildings = new HashMap<>();
    private final Map<UUID, PlacementSession> placementSessions = new HashMap<>();
    private final SpongeSchemLoader loader = new SpongeSchemLoader();
    private final Map<String, CompletableFuture<Schematic>> schematicCache = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private final Executor asyncExecutor = CompletableFuture.delayedExecutor(0, java.util.concurrent.TimeUnit.MILLISECONDS);

    public BuildingManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "buildings.yml");
        if (!file.exists()) {
            plugin.saveResource("buildings.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        ensureSchematicsFolder();
        load();
    }

    public Map<String, BuildingDefinition> buildings() {
        return buildings;
    }

    public Map<BuildingCategory, List<BuildingDefinition>> byCategory() {
        Map<BuildingCategory, List<BuildingDefinition>> categorized = new EnumMap<>(BuildingCategory.class);
        for (BuildingCategory category : BuildingCategory.values()) {
            categorized.put(category, new ArrayList<>());
        }
        for (BuildingDefinition definition : buildings.values()) {
            categorized.get(definition.category()).add(definition);
        }
        return categorized;
    }

    public BuildingDefinition getBuilding(String id) {
        return buildings.get(id);
    }

    public void beginPlacement(Player player, String buildingId, Transform.Rotation rotation) {
        BuildingDefinition definition = buildings.get(buildingId);
        if (definition == null) {
            player.sendMessage(Text.mm("<red>Gebäude nicht gefunden."));
            return;
        }
        placementSessions.put(player.getUniqueId(), new PlacementSession(buildingId, rotation));
        player.sendMessage(Text.mm("<green>Platzierungsmodus aktiv. Rechtsklick auf einen Block zum Platzieren."));
    }

    public boolean handlePlacement(Player player, Location target) {
        PlacementSession session = placementSessions.remove(player.getUniqueId());
        if (session == null) {
            return false;
        }
        BuildingDefinition definition = buildings.get(session.buildingId());
        if (definition == null) {
            player.sendMessage(Text.mm("<red>Gebäude nicht gefunden."));
            return true;
        }
        placeBuilding(player, target, definition, session.rotation());
        return true;
    }

    private void placeBuilding(Player player, Location origin, BuildingDefinition definition, Transform.Rotation rotation) {
        String schematicName = definition.schematic();
        if (schematicName == null || schematicName.isBlank()) {
            player.sendMessage(Text.mm("<red>Kein Haupt-Schematic gesetzt."));
            return;
        }
        List<CompletableFuture<Schematic>> futures = new ArrayList<>();
        CompletableFuture<Schematic> baseFuture = loadSchematicAsync(schematicName);
        futures.add(baseFuture);
        CompletableFuture<Schematic> floorFuture = null;
        if (definition.floorSchematic() != null) {
            floorFuture = loadSchematicAsync(definition.floorSchematic());
            futures.add(floorFuture);
        }
        CompletableFuture<Schematic> basementFuture = null;
        if (definition.basementSchematic() != null) {
            basementFuture = loadSchematicAsync(definition.basementSchematic());
            futures.add(basementFuture);
        }
        Map<FurnitureDefinition, CompletableFuture<Schematic>> furnitureFutures = new HashMap<>();
        for (FurnitureDefinition furniture : definition.furniture()) {
            CompletableFuture<Schematic> furnitureFuture = loadSchematicAsync(furniture.schematic());
            furnitureFutures.put(furniture, furnitureFuture);
            futures.add(furnitureFuture);
        }
        final CompletableFuture<Schematic> finalBaseFuture = baseFuture;
        final CompletableFuture<Schematic> finalFloorFuture = floorFuture;
        final CompletableFuture<Schematic> finalBasementFuture = basementFuture;
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .whenCompleteAsync((ignored, throwable) -> plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (throwable != null) {
                    plugin.getLogger().warning("Failed to load schematic: " + throwable.getMessage());
                    player.sendMessage(Text.mm("<red>Gebäude konnte nicht geladen werden."));
                    return;
                }
                try {
                    pasteBuilding(origin, definition, rotation, finalBaseFuture.join(),
                        finalFloorFuture != null ? finalFloorFuture.join() : null,
                        finalBasementFuture != null ? finalBasementFuture.join() : null,
                        furnitureFutures);
                    player.sendMessage(Text.mm("<green>Gebäude platziert: " + definition.name()));
                } catch (Exception ex) {
                    plugin.getLogger().warning("Failed to paste building: " + ex.getMessage());
                    player.sendMessage(Text.mm("<red>Gebäude konnte nicht platziert werden."));
                }
            }), asyncExecutor);
    }

    private void pasteBuilding(Location origin, BuildingDefinition definition, Transform.Rotation rotation, Schematic base,
                               Schematic floor, Schematic basement, Map<FurnitureDefinition, CompletableFuture<Schematic>> furnitureFutures) {
        SchematicPaster paster = new SchematicPaster(plugin);
        int minFloors = Math.max(1, definition.minFloors());
        int maxFloors = Math.max(minFloors, definition.maxFloors());
        int floors = Math.max(1, random.nextInt(maxFloors - minFloors + 1) + minFloors);
        Transform baseTransform = new Transform(rotation, definition.offsetX(), definition.offsetY(), definition.offsetZ());
        paster.pasteInBatches(origin.getWorld(), origin, base, new SchematicPaster.PasteOptions(definition.includeAir(), baseTransform), 5000);
        if (basement != null && definition.basementDepth() > 0) {
            Transform basementTransform = new Transform(rotation, definition.offsetX(), definition.offsetY() - definition.basementDepth(), definition.offsetZ());
            paster.pasteInBatches(origin.getWorld(), origin, basement, new SchematicPaster.PasteOptions(definition.includeAir(), basementTransform), 5000);
        }
        for (int i = 1; i < floors; i++) {
            Schematic floorSchematic = floor != null ? floor : base;
            Transform floorTransform = new Transform(rotation, definition.offsetX(), definition.offsetY() + definition.floorHeight() * i, definition.offsetZ());
            paster.pasteInBatches(origin.getWorld(), origin, floorSchematic, new SchematicPaster.PasteOptions(definition.includeAir(), floorTransform), 5000);
        }
        for (var entry : furnitureFutures.entrySet()) {
            FurnitureDefinition furniture = entry.getKey();
            Schematic furnitureSchematic = entry.getValue().join();
            Transform.Rotation combinedRotation = rotationForDegrees((rotationToDegrees(rotation) + furniture.rotation()) % 360);
            Transform transform = new Transform(combinedRotation,
                definition.offsetX() + furniture.offsetX(),
                definition.offsetY() + furniture.offsetY(),
                definition.offsetZ() + furniture.offsetZ());
            paster.pasteInBatches(origin.getWorld(), origin, furnitureSchematic, new SchematicPaster.PasteOptions(definition.includeAir(), transform), 2000);
        }
    }

    private CompletableFuture<Schematic> loadSchematicAsync(String name) {
        return schematicCache.computeIfAbsent(name, key -> CompletableFuture.supplyAsync(() -> {
            File file = new File(schematicsFolder(), key);
            if (!file.exists()) {
                throw new IllegalStateException("Schematic not found: " + key);
            }
            try {
                return loader.load(file);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }).whenComplete((result, throwable) -> {
            if (throwable != null) {
                schematicCache.remove(key);
            }
        }));
    }

    private File schematicsFolder() {
        String folderName = plugin.getConfig().getString("building.schematicsFolder", "schematics");
        return new File(plugin.getDataFolder(), folderName);
    }

    private void ensureSchematicsFolder() {
        File folder = schematicsFolder();
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("Could not create schematics folder: " + folder.getAbsolutePath());
        }
    }

    private void load() {
        buildings.clear();
        ConfigurationSection root = config.getConfigurationSection("buildings");
        if (root == null) {
            return;
        }
        for (String id : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            BuildingDefinition definition = new BuildingDefinition(id);
            definition.setName(section.getString("name", id));
            definition.setCategory(BuildingCategory.fromString(section.getString("category")));
            definition.setSchematic(section.getString("schematic", null));
            definition.setFloorSchematic(section.getString("floorSchematic", null));
            definition.setMinFloors(section.getInt("minFloors", 1));
            definition.setMaxFloors(section.getInt("maxFloors", definition.minFloors()));
            if (definition.maxFloors() < definition.minFloors()) {
                definition.setMaxFloors(definition.minFloors());
            }
            definition.setFloorHeight(section.getInt("floorHeight", 5));
            definition.setIncludeAir(section.getBoolean("includeAir", false));
            ConfigurationSection offset = section.getConfigurationSection("offset");
            if (offset != null) {
                definition.setOffset(offset.getInt("x", 0), offset.getInt("y", 0), offset.getInt("z", 0));
            }
            ConfigurationSection basement = section.getConfigurationSection("basement");
            if (basement != null) {
                definition.setBasementSchematic(basement.getString("schematic", null));
                definition.setBasementDepth(basement.getInt("depth", 0));
            }
            List<Map<?, ?>> furnitureList = section.getMapList("furniture");
            for (Map<?, ?> entry : furnitureList) {
                Object schematicValue = entry.get("schematic");
                String furnitureSchematic = schematicValue != null ? String.valueOf(schematicValue) : "";
                int offsetX = parseInt(entry.get("x"), 0);
                int offsetY = parseInt(entry.get("y"), 0);
                int offsetZ = parseInt(entry.get("z"), 0);
                int rotation = parseInt(entry.get("rotation"), 0);
                if (!furnitureSchematic.isBlank()) {
                    definition.addFurniture(new FurnitureDefinition(furnitureSchematic, offsetX, offsetY, offsetZ, rotation));
                }
            }
            buildings.put(id, definition);
        }
    }

    private int parseInt(Object value, int fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private Transform.Rotation rotationForDegrees(int degrees) {
        return switch (degrees) {
            case 90 -> Transform.Rotation.CLOCKWISE_90;
            case 180 -> Transform.Rotation.CLOCKWISE_180;
            case 270 -> Transform.Rotation.CLOCKWISE_270;
            default -> Transform.Rotation.NONE;
        };
    }

    private int rotationToDegrees(Transform.Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> 90;
            case CLOCKWISE_180 -> 180;
            case CLOCKWISE_270 -> 270;
            default -> 0;
        };
    }

    private record PlacementSession(String buildingId, Transform.Rotation rotation) {
    }
}

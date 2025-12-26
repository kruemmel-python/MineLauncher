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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
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
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public class BuildingManager {
    private final RPGPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, BuildingDefinition> buildings = new HashMap<>();
    private final Map<UUID, PlacementSession> placementSessions = new HashMap<>();
    private final SpongeSchemLoader loader = new SpongeSchemLoader();
    private final Map<String, CompletableFuture<Schematic>> schematicCache = new ConcurrentHashMap<>();
    private final Map<UUID, Deque<com.example.rpg.schematic.UndoBuffer>> undoHistory = new HashMap<>();
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
        placementSessions.put(player.getUniqueId(), new PlacementSession(buildingId, null, rotation));
        player.sendMessage(Text.mm("<green>Platzierungsmodus aktiv. Rechtsklick auf einen Block zum Platzieren."));
    }

    public void beginSingleSchematicPlacement(Player player, String schematicName, Transform.Rotation rotation) {
        if (schematicName == null || schematicName.isBlank()) {
            player.sendMessage(Text.mm("<red>Kein Schematic angegeben."));
            return;
        }
        placementSessions.put(player.getUniqueId(), new PlacementSession(null, schematicName.trim(), rotation));
        player.sendMessage(Text.mm("<green>Platzierungsmodus aktiv. Rechtsklick auf einen Block zum Platzieren."));
    }

    public boolean handlePlacement(Player player, Location target) {
        PlacementSession session = placementSessions.remove(player.getUniqueId());
        if (session == null) {
            return false;
        }
        if (session.schematicName() != null) {
            placeSingleSchematic(player, target, session.schematicName(), session.rotation());
            return true;
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
                    com.example.rpg.schematic.UndoBuffer undoBuffer = new com.example.rpg.schematic.UndoBuffer();
                    int floors = resolveFloors(definition);
                    pasteBuilding(origin, definition, rotation, floors, finalBaseFuture.join(),
                        finalFloorFuture != null ? finalFloorFuture.join() : null,
                        finalBasementFuture != null ? finalBasementFuture.join() : null,
                        furnitureFutures,
                        undoBuffer);
                    pushUndo(player.getUniqueId(), undoBuffer);
                    player.sendMessage(Text.mm("<green>Gebäude platziert: " + definition.name()));
                } catch (Exception ex) {
                    plugin.getLogger().warning("Failed to paste building: " + ex.getMessage());
                    player.sendMessage(Text.mm("<red>Gebäude konnte nicht platziert werden."));
                }
            }), asyncExecutor);
    }

    private void placeSingleSchematic(Player player, Location origin, String schematicName, Transform.Rotation rotation) {
        CompletableFuture<Schematic> future = loadSchematicAsync(schematicName);
        future.whenCompleteAsync((schematic, throwable) -> plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (throwable != null) {
                plugin.getLogger().warning("Failed to load schematic: " + throwable.getMessage());
                player.sendMessage(Text.mm("<red>Schematic konnte nicht geladen werden."));
                return;
            }
            try {
                SchematicPaster paster = new SchematicPaster(plugin);
                Transform transform = new Transform(rotation, 0, 0, 0);
                com.example.rpg.schematic.UndoBuffer undoBuffer = new com.example.rpg.schematic.UndoBuffer();
                prepareArea(origin, schematic, transform, 3, undoBuffer);
                paster.pasteInBatches(origin.getWorld(), origin, schematic,
                    new SchematicPaster.PasteOptions(false, transform, undoBuffer), 5000);
                pushUndo(player.getUniqueId(), undoBuffer);
                player.sendMessage(Text.mm("<green>Schematic platziert: " + schematicName));
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to paste schematic: " + ex.getMessage());
                player.sendMessage(Text.mm("<red>Bereinigung oder Platzierung fehlgeschlagen."));
            }
        }), asyncExecutor);
    }

    public void undoLast(Player player) {
        Deque<com.example.rpg.schematic.UndoBuffer> history = undoHistory.get(player.getUniqueId());
        if (history == null || history.isEmpty()) {
            player.sendMessage(Text.mm("<yellow>Kein Gebäude zum Rückgängig machen."));
            return;
        }
        com.example.rpg.schematic.UndoBuffer buffer = history.pop();
        for (var snapshot : buffer.snapshots()) {
            snapshot.location().getBlock().setBlockData(snapshot.data(), false);
        }
        player.sendMessage(Text.mm("<green>Letztes Gebäude rückgängig gemacht."));
    }

    private void pushUndo(UUID playerId, com.example.rpg.schematic.UndoBuffer buffer) {
        undoHistory.computeIfAbsent(playerId, key -> new ArrayDeque<>()).push(buffer);
    }

    private void pasteBuilding(Location origin, BuildingDefinition definition, Transform.Rotation rotation, int floors, Schematic base,
                               Schematic floor, Schematic basement, Map<FurnitureDefinition, CompletableFuture<Schematic>> furnitureFutures,
                               com.example.rpg.schematic.UndoBuffer undoBuffer) {
        SchematicPaster paster = new SchematicPaster(plugin);
        Transform baseTransform = new Transform(rotation, definition.offsetX(), definition.offsetY(), definition.offsetZ());
        prepareArea(origin, base, baseTransform, 3, undoBuffer);
        paster.pasteInBatches(origin.getWorld(), origin, base,
            new SchematicPaster.PasteOptions(definition.includeAir(), baseTransform, undoBuffer), 5000);
        if (basement != null && definition.basementDepth() > 0) {
            Transform basementTransform = new Transform(rotation, definition.offsetX(), definition.offsetY() - definition.basementDepth(), definition.offsetZ());
            prepareArea(origin, basement, basementTransform, 3, undoBuffer);
            paster.pasteInBatches(origin.getWorld(), origin, basement,
                new SchematicPaster.PasteOptions(definition.includeAir(), basementTransform, undoBuffer), 5000);
        }
        for (int i = 1; i < floors; i++) {
            Schematic floorSchematic = floor != null ? floor : base;
            Transform floorTransform = new Transform(rotation, definition.offsetX(), definition.offsetY() + definition.floorHeight() * i, definition.offsetZ());
            prepareArea(origin, floorSchematic, floorTransform, 3, undoBuffer);
            paster.pasteInBatches(origin.getWorld(), origin, floorSchematic,
                new SchematicPaster.PasteOptions(definition.includeAir(), floorTransform, undoBuffer), 5000);
        }
        for (var entry : furnitureFutures.entrySet()) {
            FurnitureDefinition furniture = entry.getKey();
            Schematic furnitureSchematic = entry.getValue().join();
            Transform.Rotation combinedRotation = rotationForDegrees((rotationToDegrees(rotation) + furniture.rotation()) % 360);
            Transform transform = new Transform(combinedRotation,
                definition.offsetX() + furniture.offsetX(),
                definition.offsetY() + furniture.offsetY(),
                definition.offsetZ() + furniture.offsetZ());
            prepareArea(origin, furnitureSchematic, transform, 3, undoBuffer);
            paster.pasteInBatches(origin.getWorld(), origin, furnitureSchematic,
                new SchematicPaster.PasteOptions(definition.includeAir(), transform, undoBuffer), 2000);
        }
    }

    private int resolveFloors(BuildingDefinition definition) {
        int minFloors = Math.max(1, definition.minFloors());
        int maxFloors = Math.max(minFloors, definition.maxFloors());
        return Math.max(1, random.nextInt(maxFloors - minFloors + 1) + minFloors);
    }

    private void prepareArea(Location origin, Schematic schematic, Transform transform, int buffer,
                             com.example.rpg.schematic.UndoBuffer undoBuffer) {
        if (origin.getWorld() == null) {
            throw new IllegalStateException("World not available for placement.");
        }
        Bounds bounds = calculateBounds(schematic, transform, buffer);
        loadChunks(origin, bounds);
        clearEntities(origin, bounds);
        clearBlocks(origin, bounds, undoBuffer);
    }

    private void clearBlocks(Location origin, Bounds bounds, com.example.rpg.schematic.UndoBuffer undoBuffer) {
        for (int x = bounds.minX; x <= bounds.maxX; x++) {
            for (int y = bounds.minY; y <= bounds.maxY; y++) {
                for (int z = bounds.minZ; z <= bounds.maxZ; z++) {
                    var block = origin.getWorld().getBlockAt(origin.getBlockX() + x, origin.getBlockY() + y, origin.getBlockZ() + z);
                    if (block.getType().isAir()) {
                        continue;
                    }
                    if (isSupportedGround(block.getType()) && y == bounds.minY) {
                        continue;
                    }
                    if (undoBuffer != null) {
                        undoBuffer.add(block.getLocation(), block.getBlockData());
                    }
                    block.setType(Material.AIR, false);
                }
            }
        }
    }

    private void clearEntities(Location origin, Bounds bounds) {
        BoundingBox box = BoundingBox.of(
            new org.bukkit.util.Vector(
                origin.getBlockX() + bounds.minX,
                origin.getBlockY() + bounds.minY,
                origin.getBlockZ() + bounds.minZ
            ),
            new org.bukkit.util.Vector(
                origin.getBlockX() + bounds.maxX + 1,
                origin.getBlockY() + bounds.maxY + 1,
                origin.getBlockZ() + bounds.maxZ + 1
            )
        );
        for (var entity : origin.getWorld().getNearbyEntities(box)) {
            if (entity instanceof Player) {
                continue;
            }
            entity.remove();
        }
    }

    private boolean isSupportedGround(Material material) {
        return material == Material.DIRT
            || material == Material.GRASS_BLOCK
            || material == Material.STONE
            || material == Material.COBBLESTONE;
    }

    private void loadChunks(Location origin, Bounds bounds) {
        if (origin.getWorld() == null) {
            return;
        }
        int minX = origin.getBlockX() + bounds.minX;
        int maxX = origin.getBlockX() + bounds.maxX;
        int minZ = origin.getBlockZ() + bounds.minZ;
        int maxZ = origin.getBlockZ() + bounds.maxZ;
        for (int x = minX >> 4; x <= maxX >> 4; x++) {
            for (int z = minZ >> 4; z <= maxZ >> 4; z++) {
                if (!origin.getWorld().isChunkLoaded(x, z)) {
                    origin.getWorld().getChunkAt(x, z);
                }
            }
        }
    }

    private Bounds calculateBounds(Schematic schematic, Transform transform, int buffer) {
        int width = schematic.width();
        int height = schematic.height();
        int length = schematic.length();
        int[][] corners = new int[][]{
            transform.apply(0, 0, 0, width, length),
            transform.apply(width - 1, 0, 0, width, length),
            transform.apply(0, 0, length - 1, width, length),
            transform.apply(width - 1, 0, length - 1, width, length),
            transform.apply(0, height - 1, 0, width, length),
            transform.apply(width - 1, height - 1, 0, width, length),
            transform.apply(0, height - 1, length - 1, width, length),
            transform.apply(width - 1, height - 1, length - 1, width, length)
        };
        int minX = corners[0][0];
        int maxX = corners[0][0];
        int minY = corners[0][1];
        int maxY = corners[0][1];
        int minZ = corners[0][2];
        int maxZ = corners[0][2];
        for (int[] corner : corners) {
            minX = Math.min(minX, corner[0]);
            maxX = Math.max(maxX, corner[0]);
            minY = Math.min(minY, corner[1]);
            maxY = Math.max(maxY, corner[1]);
            minZ = Math.min(minZ, corner[2]);
            maxZ = Math.max(maxZ, corner[2]);
        }
        return new Bounds(minX - buffer, maxX + buffer, minY - buffer, maxY + buffer, minZ - buffer, maxZ + buffer);
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

    private record PlacementSession(String buildingId, String schematicName, Transform.Rotation rotation) {
    }

    private record Bounds(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
    }
}

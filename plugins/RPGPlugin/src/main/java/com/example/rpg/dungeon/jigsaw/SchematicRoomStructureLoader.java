package com.example.rpg.dungeon.jigsaw;

import com.example.rpg.dungeon.layout.RoomType;
import com.example.rpg.schematic.Schematic;
import com.example.rpg.schematic.SpongeSchemLoader;
import com.example.rpg.schematic.NbtIO;
import com.example.rpg.schematic.nbt.NbtCompound;
import com.example.rpg.schematic.nbt.NbtList;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.plugin.java.JavaPlugin;

public class SchematicRoomStructureLoader implements RoomStructureLoader {
    private final SpongeSchemLoader schemLoader = new SpongeSchemLoader();

    @Override
    public List<RoomTemplate> loadRooms(JavaPlugin plugin, String theme) {
        String baseFolder = plugin.getConfig().getString("dungeon.jigsaw.roomFolder", "dungeon_rooms");
        File root = new File(plugin.getDataFolder(), baseFolder);
        File themeFolder = new File(root, theme);
        File fallbackFolder = new File(root, "default");
        List<RoomTemplate> rooms = new ArrayList<>();
        rooms.addAll(loadFromFolder(themeFolder));
        if (rooms.isEmpty() && !themeFolder.equals(fallbackFolder)) {
            rooms.addAll(loadFromFolder(fallbackFolder));
        }
        return rooms;
    }

    private List<RoomTemplate> loadFromFolder(File folder) {
        List<RoomTemplate> rooms = new ArrayList<>();
        if (!folder.exists() || !folder.isDirectory()) {
            return rooms;
        }
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".schem"));
        if (files == null) {
            return rooms;
        }
        for (File file : files) {
            try {
                RoomTemplate template = loadTemplate(file);
                if (template != null) {
                    rooms.add(template);
                }
            } catch (IOException ignored) {
            }
        }
        return rooms;
    }

    private RoomTemplate loadTemplate(File file) throws IOException {
        String name = file.getName().substring(0, file.getName().length() - 6);
        Schematic schematic = schemLoader.load(file);
        RoomType type = deriveType(name);
        NbtCompound root = NbtIO.read(file);
        List<RoomSocketTemplate> sockets = extractSockets(root);
        return new RoomTemplate(name, type, schematic, sockets);
    }

    private RoomType deriveType(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.contains("start")) {
            return RoomType.START;
        }
        if (lower.contains("boss")) {
            return RoomType.BOSS;
        }
        if (lower.contains("exit")) {
            return RoomType.EXIT;
        }
        if (lower.contains("loot") || lower.contains("treasure")) {
            return RoomType.LOOT;
        }
        if (lower.contains("elite")) {
            return RoomType.ELITE;
        }
        return RoomType.COMBAT;
    }

    private List<RoomSocketTemplate> extractSockets(NbtCompound root) {
        List<RoomSocketTemplate> sockets = new ArrayList<>();
        NbtList blockEntities = root.getList("BlockEntities");
        if (blockEntities == null) {
            return sockets;
        }
        for (NbtCompound entity : blockEntities.compounds()) {
            String id = entity.getString("id", "");
            if (!id.contains("jigsaw")) {
                continue;
            }
            int[] pos = entity.getIntArray("Pos");
            if (pos == null || pos.length < 3) {
                continue;
            }
            String name = entity.getString("name", "socket");
            sockets.add(new RoomSocketTemplate(name, pos[0], pos[1], pos[2]));
        }
        return sockets;
    }
}

package com.example.rpg.dungeon.jigsaw;

import com.example.rpg.dungeon.layout.RoomType;
import com.example.rpg.schematic.Schematic;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;

public interface RoomStructureLoader {
    List<RoomTemplate> loadRooms(JavaPlugin plugin, String theme);
}

package com.example.rpg.schematic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

public class UndoBuffer {
    public record BlockSnapshot(Location location, BlockData data) {
    }

    private final List<BlockSnapshot> snapshots = new ArrayList<>();

    public void add(Location location, BlockData data) {
        snapshots.add(new BlockSnapshot(location, data));
    }

    public List<BlockSnapshot> snapshots() {
        return Collections.unmodifiableList(snapshots);
    }
}

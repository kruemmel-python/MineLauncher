package com.example.rpg.schematic;

import com.example.rpg.schematic.nbt.NbtCompound;
import java.util.List;

public record Schematic(int width, int height, int length, BlockPalette palette, int[] blocks,
                        List<NbtCompound> blockEntities, List<NbtCompound> entities) {
}

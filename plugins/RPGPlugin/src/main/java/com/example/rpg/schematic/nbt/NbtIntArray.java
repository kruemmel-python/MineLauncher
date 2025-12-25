package com.example.rpg.schematic.nbt;

public record NbtIntArray(int[] value) implements NbtTag {
    @Override
    public byte typeId() {
        return NbtType.INT_ARRAY;
    }
}

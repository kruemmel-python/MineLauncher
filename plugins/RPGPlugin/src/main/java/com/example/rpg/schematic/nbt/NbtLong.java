package com.example.rpg.schematic.nbt;

public record NbtLong(long value) implements NbtTag {
    @Override
    public byte typeId() {
        return NbtType.LONG;
    }
}

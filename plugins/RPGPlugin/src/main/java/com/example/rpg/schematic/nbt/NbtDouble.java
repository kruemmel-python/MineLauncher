package com.example.rpg.schematic.nbt;

public record NbtDouble(double value) implements NbtTag {
    @Override
    public byte typeId() {
        return NbtType.DOUBLE;
    }
}

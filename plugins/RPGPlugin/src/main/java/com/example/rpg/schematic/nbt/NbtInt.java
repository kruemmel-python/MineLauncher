package com.example.rpg.schematic.nbt;

public record NbtInt(int value) implements NbtTag {
    @Override
    public byte typeId() {
        return NbtType.INT;
    }
}

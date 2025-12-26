package com.example.rpg.schematic.nbt;

public record NbtFloat(float value) implements NbtTag {
    @Override
    public byte typeId() {
        return NbtType.FLOAT;
    }
}

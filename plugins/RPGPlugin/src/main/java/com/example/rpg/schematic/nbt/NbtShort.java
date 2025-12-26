package com.example.rpg.schematic.nbt;

public record NbtShort(short value) implements NbtTag {
    @Override
    public byte typeId() {
        return NbtType.SHORT;
    }
}

package com.example.rpg.schematic.nbt;

public record NbtString(String value) implements NbtTag {
    @Override
    public byte typeId() {
        return NbtType.STRING;
    }
}

package com.example.rpg.schematic.nbt;

public record NbtByteArray(byte[] value) implements NbtTag {
    @Override
    public byte typeId() {
        return NbtType.BYTE_ARRAY;
    }
}

package com.example.rpg.schematic.nbt;

public record NbtByte(byte value) implements NbtTag {
    @Override
    public byte typeId() {
        return NbtType.BYTE;
    }
}

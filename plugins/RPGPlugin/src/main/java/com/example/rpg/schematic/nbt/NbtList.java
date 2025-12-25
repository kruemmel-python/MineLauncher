package com.example.rpg.schematic.nbt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NbtList implements NbtTag {
    private final byte elementType;
    private final List<NbtTag> values;

    public NbtList(byte elementType, List<NbtTag> values) {
        this.elementType = elementType;
        this.values = new ArrayList<>(values);
    }

    @Override
    public byte typeId() {
        return NbtType.LIST;
    }

    public byte elementType() {
        return elementType;
    }

    public List<NbtTag> values() {
        return Collections.unmodifiableList(values);
    }

    public int size() {
        return values.size();
    }

    public NbtTag get(int index) {
        return values.get(index);
    }

    public List<NbtCompound> compounds() {
        List<NbtCompound> result = new ArrayList<>();
        for (NbtTag tag : values) {
            if (tag instanceof NbtCompound compound) {
                result.add(compound);
            }
        }
        return result;
    }

    public List<String> strings() {
        List<String> result = new ArrayList<>();
        for (NbtTag tag : values) {
            if (tag instanceof NbtString str) {
                result.add(str.value());
            }
        }
        return result;
    }

    public List<Double> doubles() {
        List<Double> result = new ArrayList<>();
        for (NbtTag tag : values) {
            if (tag instanceof NbtDouble dbl) {
                result.add(dbl.value());
            } else if (tag instanceof NbtFloat fl) {
                result.add((double) fl.value());
            } else if (tag instanceof NbtInt i) {
                result.add((double) i.value());
            }
        }
        return result;
    }
}

package com.example.rpg.schematic.nbt;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NbtCompound implements NbtTag {
    private final Map<String, NbtTag> values = new HashMap<>();

    @Override
    public byte typeId() {
        return NbtType.COMPOUND;
    }

    public void put(String name, NbtTag tag) {
        values.put(name, tag);
    }

    public Map<String, NbtTag> values() {
        return Collections.unmodifiableMap(values);
    }

    public NbtTag get(String name) {
        return values.get(name);
    }

    public String getString(String name, String fallback) {
        NbtTag tag = values.get(name);
        if (tag instanceof NbtString str) {
            return str.value();
        }
        return fallback;
    }

    public int getInt(String name, int fallback) {
        Number number = getNumber(name);
        return number != null ? number.intValue() : fallback;
    }

    public long getLong(String name, long fallback) {
        Number number = getNumber(name);
        return number != null ? number.longValue() : fallback;
    }

    public double getDouble(String name, double fallback) {
        Number number = getNumber(name);
        return number != null ? number.doubleValue() : fallback;
    }

    public Number getNumber(String name) {
        NbtTag tag = values.get(name);
        if (tag instanceof NbtByte b) {
            return b.value();
        }
        if (tag instanceof NbtShort s) {
            return s.value();
        }
        if (tag instanceof NbtInt i) {
            return i.value();
        }
        if (tag instanceof NbtLong l) {
            return l.value();
        }
        if (tag instanceof NbtFloat f) {
            return f.value();
        }
        if (tag instanceof NbtDouble d) {
            return d.value();
        }
        return null;
    }

    public NbtCompound getCompound(String name) {
        NbtTag tag = values.get(name);
        if (tag instanceof NbtCompound compound) {
            return compound;
        }
        return null;
    }

    public NbtList getList(String name) {
        NbtTag tag = values.get(name);
        if (tag instanceof NbtList list) {
            return list;
        }
        return null;
    }

    public byte[] getByteArray(String name) {
        NbtTag tag = values.get(name);
        if (tag instanceof NbtByteArray array) {
            return array.value();
        }
        return null;
    }

    public int[] getIntArray(String name) {
        NbtTag tag = values.get(name);
        if (tag instanceof NbtIntArray array) {
            return array.value();
        }
        return null;
    }

    public long[] getLongArray(String name) {
        NbtTag tag = values.get(name);
        if (tag instanceof NbtLongArray array) {
            return array.value();
        }
        return null;
    }
}

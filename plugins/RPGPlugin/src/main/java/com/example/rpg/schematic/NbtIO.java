package com.example.rpg.schematic;

import com.example.rpg.schematic.nbt.*;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public final class NbtIO {
    private NbtIO() {
    }

    public static NbtCompound read(File file) throws IOException {
        try (InputStream input = openInputStream(file);
             DataInputStream data = new DataInputStream(input)) {
            byte type = data.readByte();
            if (type != NbtType.COMPOUND) {
                throw new IOException("Root tag is not a compound");
            }
            data.readUTF();
            return readCompound(data);
        }
    }

    private static InputStream openInputStream(File file) throws IOException {
        InputStream input = new BufferedInputStream(new FileInputStream(file));
        PushbackInputStream pushback = new PushbackInputStream(input, 2);
        byte[] header = new byte[2];
        int read = pushback.read(header);
        if (read > 0) {
            pushback.unread(header, 0, read);
        }
        if (read == 2 && (header[0] == (byte) 0x1f && header[1] == (byte) 0x8b)) {
            return new GZIPInputStream(pushback);
        }
        return pushback;
    }

    private static NbtTag readTagPayload(DataInputStream data, byte type) throws IOException {
        return switch (type) {
            case NbtType.BYTE -> new NbtByte(data.readByte());
            case NbtType.SHORT -> new NbtShort(data.readShort());
            case NbtType.INT -> new NbtInt(data.readInt());
            case NbtType.LONG -> new NbtLong(data.readLong());
            case NbtType.FLOAT -> new NbtFloat(data.readFloat());
            case NbtType.DOUBLE -> new NbtDouble(data.readDouble());
            case NbtType.STRING -> new NbtString(data.readUTF());
            case NbtType.BYTE_ARRAY -> new NbtByteArray(readByteArray(data));
            case NbtType.INT_ARRAY -> new NbtIntArray(readIntArray(data));
            case NbtType.LONG_ARRAY -> new NbtLongArray(readLongArray(data));
            case NbtType.LIST -> readList(data);
            case NbtType.COMPOUND -> readCompound(data);
            default -> throw new IOException("Unsupported NBT tag type: " + type);
        };
    }

    private static byte[] readByteArray(DataInputStream data) throws IOException {
        int length = data.readInt();
        byte[] values = new byte[length];
        data.readFully(values);
        return values;
    }

    private static int[] readIntArray(DataInputStream data) throws IOException {
        int length = data.readInt();
        int[] values = new int[length];
        for (int i = 0; i < length; i++) {
            values[i] = data.readInt();
        }
        return values;
    }

    private static long[] readLongArray(DataInputStream data) throws IOException {
        int length = data.readInt();
        long[] values = new long[length];
        for (int i = 0; i < length; i++) {
            values[i] = data.readLong();
        }
        return values;
    }

    private static NbtList readList(DataInputStream data) throws IOException {
        byte elementType = data.readByte();
        int length = data.readInt();
        List<NbtTag> values = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            values.add(readTagPayload(data, elementType));
        }
        return new NbtList(elementType, values);
    }

    private static NbtCompound readCompound(DataInputStream data) throws IOException {
        NbtCompound compound = new NbtCompound();
        while (true) {
            byte type = data.readByte();
            if (type == NbtType.END) {
                break;
            }
            String name = data.readUTF();
            compound.put(name, readTagPayload(data, type));
        }
        return compound;
    }
}

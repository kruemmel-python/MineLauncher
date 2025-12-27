package com.example.rpg.schematic;

import com.example.rpg.schematic.nbt.*;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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

    public static void write(File file, NbtCompound root) throws IOException {
        try (OutputStream output = new GZIPOutputStream(java.nio.file.Files.newOutputStream(file.toPath()));
             DataOutputStream data = new DataOutputStream(output)) {
            data.writeByte(NbtType.COMPOUND);
            data.writeUTF("");
            writeCompound(data, root);
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

    private static void writeTagPayload(DataOutputStream data, NbtTag tag) throws IOException {
        switch (tag.typeId()) {
            case NbtType.BYTE -> data.writeByte(((NbtByte) tag).value());
            case NbtType.SHORT -> data.writeShort(((NbtShort) tag).value());
            case NbtType.INT -> data.writeInt(((NbtInt) tag).value());
            case NbtType.LONG -> data.writeLong(((NbtLong) tag).value());
            case NbtType.FLOAT -> data.writeFloat(((NbtFloat) tag).value());
            case NbtType.DOUBLE -> data.writeDouble(((NbtDouble) tag).value());
            case NbtType.STRING -> data.writeUTF(((NbtString) tag).value());
            case NbtType.BYTE_ARRAY -> writeByteArray(data, ((NbtByteArray) tag).value());
            case NbtType.INT_ARRAY -> writeIntArray(data, ((NbtIntArray) tag).value());
            case NbtType.LONG_ARRAY -> writeLongArray(data, ((NbtLongArray) tag).value());
            case NbtType.LIST -> writeList(data, (NbtList) tag);
            case NbtType.COMPOUND -> writeCompound(data, (NbtCompound) tag);
            default -> throw new IOException("Unsupported NBT tag type: " + tag.typeId());
        }
    }

    private static byte[] readByteArray(DataInputStream data) throws IOException {
        int length = data.readInt();
        byte[] values = new byte[length];
        data.readFully(values);
        return values;
    }

    private static void writeByteArray(DataOutputStream data, byte[] values) throws IOException {
        data.writeInt(values.length);
        data.write(values);
    }

    private static int[] readIntArray(DataInputStream data) throws IOException {
        int length = data.readInt();
        int[] values = new int[length];
        for (int i = 0; i < length; i++) {
            values[i] = data.readInt();
        }
        return values;
    }

    private static void writeIntArray(DataOutputStream data, int[] values) throws IOException {
        data.writeInt(values.length);
        for (int value : values) {
            data.writeInt(value);
        }
    }

    private static long[] readLongArray(DataInputStream data) throws IOException {
        int length = data.readInt();
        long[] values = new long[length];
        for (int i = 0; i < length; i++) {
            values[i] = data.readLong();
        }
        return values;
    }

    private static void writeLongArray(DataOutputStream data, long[] values) throws IOException {
        data.writeInt(values.length);
        for (long value : values) {
            data.writeLong(value);
        }
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

    private static void writeList(DataOutputStream data, NbtList list) throws IOException {
        data.writeByte(list.elementType());
        data.writeInt(list.size());
        for (NbtTag tag : list.values()) {
            writeTagPayload(data, tag);
        }
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

    private static void writeCompound(DataOutputStream data, NbtCompound compound) throws IOException {
        for (var entry : compound.values().entrySet()) {
            NbtTag tag = entry.getValue();
            data.writeByte(tag.typeId());
            data.writeUTF(entry.getKey());
            writeTagPayload(data, tag);
        }
        data.writeByte(NbtType.END);
    }
}

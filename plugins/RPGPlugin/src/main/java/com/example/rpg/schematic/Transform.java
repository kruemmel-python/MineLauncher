package com.example.rpg.schematic;

public class Transform {
    public enum Rotation {
        NONE,
        CLOCKWISE_90,
        CLOCKWISE_180,
        CLOCKWISE_270
    }

    private final Rotation rotation;
    private final int offsetX;
    private final int offsetY;
    private final int offsetZ;

    public Transform(Rotation rotation, int offsetX, int offsetY, int offsetZ) {
        this.rotation = rotation;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
    }

    public Rotation rotation() {
        return rotation;
    }

    public int offsetX() {
        return offsetX;
    }

    public int offsetY() {
        return offsetY;
    }

    public int offsetZ() {
        return offsetZ;
    }

    public int[] apply(int x, int y, int z, int width, int length) {
        int rx = x;
        int rz = z;
        switch (rotation) {
            case CLOCKWISE_90 -> {
                rx = length - 1 - z;
                rz = x;
            }
            case CLOCKWISE_180 -> {
                rx = width - 1 - x;
                rz = length - 1 - z;
            }
            case CLOCKWISE_270 -> {
                rx = z;
                rz = width - 1 - x;
            }
            default -> {
            }
        }
        return new int[]{rx + offsetX, y + offsetY, rz + offsetZ};
    }
}

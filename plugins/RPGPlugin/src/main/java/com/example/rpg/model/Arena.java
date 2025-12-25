package com.example.rpg.model;

import java.util.UUID;

public class Arena {
    private final String id;
    private String world;
    private int x1;
    private int y1;
    private int z1;
    private int x2;
    private int y2;
    private int z2;
    private int spawn1x;
    private int spawn1y;
    private int spawn1z;
    private int spawn2x;
    private int spawn2y;
    private int spawn2z;
    private ArenaStatus status = ArenaStatus.WAITING;
    private UUID playerOne;
    private UUID playerTwo;

    public Arena(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String world() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public int x1() {
        return x1;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public int y1() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public int z1() {
        return z1;
    }

    public void setZ1(int z1) {
        this.z1 = z1;
    }

    public int x2() {
        return x2;
    }

    public void setX2(int x2) {
        this.x2 = x2;
    }

    public int y2() {
        return y2;
    }

    public void setY2(int y2) {
        this.y2 = y2;
    }

    public int z2() {
        return z2;
    }

    public void setZ2(int z2) {
        this.z2 = z2;
    }

    public int spawn1x() {
        return spawn1x;
    }

    public void setSpawn1x(int spawn1x) {
        this.spawn1x = spawn1x;
    }

    public int spawn1y() {
        return spawn1y;
    }

    public void setSpawn1y(int spawn1y) {
        this.spawn1y = spawn1y;
    }

    public int spawn1z() {
        return spawn1z;
    }

    public void setSpawn1z(int spawn1z) {
        this.spawn1z = spawn1z;
    }

    public int spawn2x() {
        return spawn2x;
    }

    public void setSpawn2x(int spawn2x) {
        this.spawn2x = spawn2x;
    }

    public int spawn2y() {
        return spawn2y;
    }

    public void setSpawn2y(int spawn2y) {
        this.spawn2y = spawn2y;
    }

    public int spawn2z() {
        return spawn2z;
    }

    public void setSpawn2z(int spawn2z) {
        this.spawn2z = spawn2z;
    }

    public ArenaStatus status() {
        return status;
    }

    public void setStatus(ArenaStatus status) {
        this.status = status;
    }

    public UUID playerOne() {
        return playerOne;
    }

    public void setPlayerOne(UUID playerOne) {
        this.playerOne = playerOne;
    }

    public UUID playerTwo() {
        return playerTwo;
    }

    public void setPlayerTwo(UUID playerTwo) {
        this.playerTwo = playerTwo;
    }
}

package com.github.tartaricacid.touhoulittlemaid.entity.projectile;

import java.util.Random;

public enum DanmakuType {
    PELLET(0.6D), BALL(0.5D), ORBS(0.3D), BIG_BALL(0.5D);

    private final double size;

    DanmakuType(double size) { this.size = size; }
    public double getSize() { return size; }
    public static DanmakuType byOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < values().length ? values()[ordinal] : PELLET;
    }
    public static DanmakuType random(Random random) { return values()[random.nextInt(values().length)]; }
}

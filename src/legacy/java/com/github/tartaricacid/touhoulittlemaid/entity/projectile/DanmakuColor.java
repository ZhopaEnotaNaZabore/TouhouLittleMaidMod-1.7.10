package com.github.tartaricacid.touhoulittlemaid.entity.projectile;

import java.util.Random;

public enum DanmakuColor {
    RED, ORANGE, YELLOW, LIME, LIGHT_GREEN, GREEN, CYAN,
    LIGHT_BLUE, BLUE, PURPLE, MAGENTA, PINK, GRAY;

    public static DanmakuColor byOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < values().length ? values()[ordinal] : RED;
    }
    public static DanmakuColor random(Random random) { return values()[random.nextInt(values().length)]; }
}

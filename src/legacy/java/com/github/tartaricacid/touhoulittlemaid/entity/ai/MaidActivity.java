package com.github.tartaricacid.touhoulittlemaid.entity.ai;

public enum MaidActivity {
    WORK,
    IDLE,
    REST;

    public static MaidActivity byOrdinal(int value) {
        MaidActivity[] values = values();
        return value >= 0 && value < values.length ? values[value] : IDLE;
    }
}

package com.github.tartaricacid.touhoulittlemaid.entity.ai;

public enum MaidSchedule {
    DAY,
    NIGHT,
    ALL;

    public MaidActivity getActivity(long worldTime) {
        int time = (int) (worldTime % 24000L);
        switch (this) {
            case NIGHT:
                if (time < 8000) return MaidActivity.REST;
                if (time < 12000) return MaidActivity.IDLE;
                return MaidActivity.WORK;
            case ALL:
                return MaidActivity.WORK;
            case DAY:
            default:
                if (time < 12000) return MaidActivity.WORK;
                if (time < 16000) return MaidActivity.IDLE;
                return MaidActivity.REST;
        }
    }

    public static MaidSchedule byOrdinal(int value) {
        MaidSchedule[] values = values();
        return value >= 0 && value < values.length ? values[value] : DAY;
    }

    public static MaidSchedule byName(String value) {
        try {
            return valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return DAY;
        }
    }
}

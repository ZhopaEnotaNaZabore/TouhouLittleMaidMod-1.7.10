package com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.animation;

/** Read-only-by-convention animation input; no game or rendering dependencies. */
public final class LegacyAnimationFrame {
    public float limbSwing, limbAmount, age, yaw, pitch, swing, useTicks, swingTicks;
    public float healthRatio = 1, verticalSpeed;
    public float groundSpeed, verticalDisplacement, yawSpeed;
    public float health=20, maxHealth=20, inputVertical, inputHorizontal;
    public final double[] positionDelta=new double[3], position=new double[3];
    public float foodLevel=20, bodyYaw;
    public boolean sneaking, wet;
    public boolean sitting, riding, sleeping, begging, fishing, ranged, useLeft, swingLeft;
    public boolean backpack, helmet, chest, leggings, boots, hurt, dead, sprinting, water, climbing;
    public boolean cancelSwing, actionsDisabled;
    public boolean onGround = true, boat, carried;
    public long uuidSeed;
    public String task = "", joy = "", use = "", mainCategory = "", offCategory = "";
    public String mainId = "", offId = "";
    public int dimension;
    public int swingSequence, useSequence;
    public boolean using() { return !use.isEmpty(); }
    public boolean seated() { return sitting || riding; }
    public boolean joyArms() {
        return "gomoku".equals(joy) || "bookshelf".equals(joy) || "computer".equals(joy)
                || "keyboard".equals(joy) || "picnic".equals(joy);
    }
}

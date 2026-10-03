package com.github.tartaricacid.touhoulittlemaid.entity.animation;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** Transient presentation state. It never equips, consumes, spawns or damages anything. */
public final class MaidActionState {
    private long revision;
    private int useStart, useDuration, swingStart, swingDuration, useSequence, swingSequence;
    private boolean useLeft, swingLeft, ranged;
    private String kind = "";
    private ItemStack displayItem;
    public boolean using(float tick) { return !kind.isEmpty() && tick >= useStart && tick - useStart < useDuration; }
    public boolean swinging(float tick) { return swingDuration > 0 && tick >= swingStart && tick - swingStart < swingDuration; }
    public boolean hasSwingEvent() { return swingDuration > 0; }
    public boolean isRanged() { return ranged; }
    public boolean useLeft() { return useLeft; }
    public boolean swingLeft() { return swingLeft; }
    public String kind(float tick) { return using(tick) ? kind : ""; }
    public float useElapsed(float tick) { return Math.max(0, tick - useStart); }
    /** Vanilla 1.7 bow icon thresholds; -1 is the resting sprite. */
    public int bowPullStage(float tick, boolean left) {
        if (!"bow".equals(kind(tick)) || useLeft != left) return -1;
        float elapsed=useElapsed(tick);
        return elapsed>=18?2:elapsed>13?1:elapsed>0?0:-1;
    }
    public float swingElapsed(float tick) { return Math.max(0, tick - swingStart); }
    public float swingProgress(float tick) { return swinging(tick) ? swingElapsed(tick) / swingDuration : 0; }
    public int useSequence() { return useSequence; }
    public int swingSequence() { return swingSequence; }
    public ItemStack displayItem(float tick) { return using(tick) ? displayItem : null; }
    public void beginUse(ItemStack item, boolean left, String action, int duration, boolean aiming, int tick) {
        if (item == null || duration < 1 || !validKind(action)) return;
        kind = action; useLeft = left; ranged = aiming; useStart = tick; useDuration = Math.min(72000, duration);
        displayItem = item.copy(); displayItem.stackSize = 1;
        useSequence++; revision++;
    }
    public boolean stopUse() {
        if (kind.isEmpty()) return false;
        kind = ""; displayItem = null; ranged = false; useDuration = 0; revision++; return true;
    }
    public void swing(boolean left, int duration, int tick) {
        swingLeft = left; swingStart = tick; swingDuration = Math.max(1, Math.min(60, duration));
        swingSequence++; revision++;
    }
    public boolean expire(int tick) { return !kind.isEmpty() && !using(tick) && stopUse(); }
    public void clear() { stopUse(); swingDuration = 0; revision++; }
    private static boolean validKind(String s) {
        return "eat".equals(s) || "drink".equals(s) || "bow".equals(s) || "gohei".equals(s) || "block".equals(s);
    }
    public NBTTagCompound snapshot(int tick) {
        NBTTagCompound n = new NBTTagCompound();
        n.setLong("Revision", revision); n.setString("Kind", kind(tick));
        n.setBoolean("UseLeft", useLeft); n.setBoolean("SwingLeft", swingLeft); n.setBoolean("Ranged", ranged);
        n.setInteger("UseDuration", useDuration); n.setInteger("UseElapsed", (int) useElapsed(tick));
        n.setInteger("SwingDuration", swingDuration); n.setInteger("SwingElapsed", (int) swingElapsed(tick));
        n.setInteger("UseSequence", useSequence); n.setInteger("SwingSequence", swingSequence);
        if (displayItem != null && using(tick)) n.setTag("Item", displayItem.writeToNBT(new NBTTagCompound()));
        return n;
    }
    public void accept(NBTTagCompound n, int tick) {
        if (n == null || n.getLong("Revision") < revision) return;
        revision = n.getLong("Revision");
        String incoming = n.getString("Kind"); kind = validKind(incoming) ? incoming : "";
        useLeft = n.getBoolean("UseLeft"); swingLeft = n.getBoolean("SwingLeft"); ranged = n.getBoolean("Ranged");
        useDuration = clamp(n.getInteger("UseDuration"), 0, 72000); swingDuration = clamp(n.getInteger("SwingDuration"), 0, 60);
        useStart = tick - clamp(n.getInteger("UseElapsed"), 0, 72000);
        swingStart = tick - clamp(n.getInteger("SwingElapsed"), 0, 72000);
        useSequence = n.getInteger("UseSequence"); swingSequence = n.getInteger("SwingSequence");
        displayItem = n.hasKey("Item", 10) ? ItemStack.loadItemStackFromNBT(n.getCompoundTag("Item")) : null;
        if (displayItem == null) { kind = ""; ranged = false; }
    }
    private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }
}

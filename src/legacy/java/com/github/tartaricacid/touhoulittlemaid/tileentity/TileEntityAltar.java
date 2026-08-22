package com.github.tartaricacid.touhoulittlemaid.tileentity;

import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityPowerPoint;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

import java.util.List;

/** Six offering slots and local Power storage for the 1.7 altar adaptation. */
public final class TileEntityAltar extends TileEntityInventory {
    public static final float MAX_POWER = 5000.0F;
    private float power;
    private int tick;

    public TileEntityAltar() { super(6, "container.tlm.altar"); }
    public float getPower() { return power; }
    public boolean consumePower(float amount) {
        if (amount < 0 || Float.isNaN(amount) || Float.isInfinite(amount) || power < amount) return false;
        power -= amount; markDirty(); return true;
    }

    @Override public void updateEntity() {
        if (worldObj == null || worldObj.isRemote || ++tick % 5 != 0) return;
        @SuppressWarnings("unchecked") List<EntityPowerPoint> points = worldObj.getEntitiesWithinAABB(EntityPowerPoint.class,
                AxisAlignedBB.getBoundingBox(xCoord - 2, yCoord - 1, zCoord - 2, xCoord + 3, yCoord + 3, zCoord + 3));
        for (EntityPowerPoint point : points) {
            if (point.isDead) continue;
            int value = Math.max(1, point.getValue());
            if (power + value <= MAX_POWER) {
                power += value;
                point.setDead();
                markDirty();
            }
        }
    }

    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        power = tag.getFloat("Power");
        if (Float.isNaN(power) || Float.isInfinite(power)) power = 0;
        power = Math.max(0, Math.min(MAX_POWER, power));
    }
    @Override public void writeToNBT(NBTTagCompound tag) { super.writeToNBT(tag); tag.setFloat("Power", power); }
}

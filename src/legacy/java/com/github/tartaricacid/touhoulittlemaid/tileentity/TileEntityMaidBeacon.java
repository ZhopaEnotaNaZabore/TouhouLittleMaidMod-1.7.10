package com.github.tartaricacid.touhoulittlemaid.tileentity;

import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityPowerPoint;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.config.LegacyConfig;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;

import java.util.List;

public final class TileEntityMaidBeacon extends TileEntity {
    private static final int[] EFFECTS = {Potion.moveSpeed.id, Potion.fireResistance.id,
            Potion.damageBoost.id, Potion.resistance.id, Potion.regeneration.id};
    private int potionIndex = -1;
    private float storagePower;
    private boolean overflowDelete;

    @Override public boolean canUpdate() { return true; }
    @Override public void updateEntity() {
        if (worldObj == null || worldObj.isRemote || worldObj.getTotalWorldTime() % 80L != 0L) return;
        applySelectedEffect();
        // Source order is effect first, absorption second: newly collected Power is usable next cycle.
        absorbPower();
    }

    /** Runs the same server-side effect cycle used by the normal beacon tick. */
    public int applySelectedEffect() {
        if (worldObj == null || worldObj.isRemote || getSelectedPotionId() < 0 || storagePower < getEffectCost()) return -1;
        storagePower -= getEffectCost();
        @SuppressWarnings("unchecked")
        List<EntityMaid> maids = worldObj.getEntitiesWithinAABB(EntityMaid.class,
                net.minecraft.util.AxisAlignedBB.getBoundingBox(xCoord - 8, yCoord - 8, zCoord - 8,
                        xCoord + 9, yCoord + 9, zCoord + 9));
        int affected = 0;
        for (EntityMaid maid : maids) {
            if (!maid.isEntityAlive()) continue;
            maid.addPotionEffect(new PotionEffect(getSelectedPotionId(), 100, 1, true));
            affected++;
        }
        markDirty();
        return affected;
    }

    private void absorbPower() {
        @SuppressWarnings("unchecked")
        int range = LegacyConfig.shrineLampMaxRange;
        List<EntityPowerPoint> points = worldObj.getEntitiesWithinAABB(EntityPowerPoint.class,
                net.minecraft.util.AxisAlignedBB.getBoundingBox(xCoord - range, yCoord - range, zCoord - range,
                        xCoord + range + 1, yCoord + range + 1, zCoord + range + 1));
        for (EntityPowerPoint point : points) {
            float add = point.getValue() / 100.0F;
            if (storagePower + add <= getMaxStorage()) {
                storagePower += add; point.setDead(); markDirty();
            } else if (overflowDelete) point.setDead();
        }
    }

    public int getPotionIndex() { return potionIndex; }
    public int getSelectedPotionId() { return potionIndex >= 0 && potionIndex < EFFECTS.length ? EFFECTS[potionIndex] : -1; }
    public void setPotionIndex(int value) { potionIndex = Math.max(-1, Math.min(EFFECTS.length - 1, value)); markDirty(); }
    public float getStoragePower() { return storagePower; }
    public void setStoragePower(float value) {
        storagePower = Float.isNaN(value) || Float.isInfinite(value) ? 0 : Math.max(0, Math.min(getMaxStorage(), value));
        markDirty();
    }
    public boolean isOverflowDelete() { return overflowDelete; }
    public void setOverflowDelete(boolean value) { overflowDelete = value; markDirty(); }
    public float getEffectCost() { return LegacyConfig.shrineLampEffectCost / 900.0F; }
    public float getMaxStorage() { return LegacyConfig.shrineLampMaxStorage; }

    @Override public void markDirty() {
        super.markDirty();
        if (worldObj != null && !worldObj.isRemote) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        potionIndex = Math.max(-1, Math.min(EFFECTS.length - 1, tag.getInteger("PotionIndex")));
        float savedPower = tag.getFloat("StoragePower");
        storagePower = Float.isNaN(savedPower) || Float.isInfinite(savedPower)
                ? 0 : Math.max(0, Math.min(getMaxStorage(), savedPower));
        overflowDelete = tag.getBoolean("OverflowDelete");
    }
    @Override public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag); tag.setInteger("PotionIndex", potionIndex);
        tag.setFloat("StoragePower", storagePower); tag.setBoolean("OverflowDelete", overflowDelete);
    }
    @Override public net.minecraft.network.Packet getDescriptionPacket() { NBTTagCompound tag=new NBTTagCompound();writeToNBT(tag);return new net.minecraft.network.play.server.S35PacketUpdateTileEntity(xCoord,yCoord,zCoord,1,tag); }
    @Override public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.S35PacketUpdateTileEntity packet) { readFromNBT(packet.func_148857_g()); }
}

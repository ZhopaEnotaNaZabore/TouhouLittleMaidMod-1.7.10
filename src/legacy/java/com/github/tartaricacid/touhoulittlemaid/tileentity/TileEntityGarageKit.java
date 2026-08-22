package com.github.tartaricacid.touhoulittlemaid.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public final class TileEntityGarageKit extends TileEntity {
    private int facing;
    private NBTTagCompound extraData = new NBTTagCompound();

    public int getFacing() { return facing; }
    public NBTTagCompound getExtraData() { return (NBTTagCompound) extraData.copy(); }
    public void setData(int facing, NBTTagCompound data) {
        this.facing = facing & 3;
        extraData = data == null ? new NBTTagCompound() : (NBTTagCompound) data.copy();
        markDirty();
        if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }
    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        facing = readFacing(tag);
        extraData = tag.hasKey("ExtraData", 10) ? tag.getCompoundTag("ExtraData") : new NBTTagCompound();
    }
    @Override public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("GarageKitFacing", facing);
        tag.setTag("ExtraData", extraData.copy());
    }
    @Override public net.minecraft.network.Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound(); writeToNBT(tag);
        return new net.minecraft.network.play.server.S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
    }
    @Override public void onDataPacket(net.minecraft.network.NetworkManager net,
                                       net.minecraft.network.play.server.S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.func_148857_g());
    }

    private int readFacing(NBTTagCompound tag) {
        if (tag.hasKey("GarageKitFacing", 8)) {
            String value = tag.getString("GarageKitFacing");
            if ("south".equals(value)) return 0;
            if ("east".equals(value)) return 1;
            if ("west".equals(value)) return 3;
            return 2;
        }
        return tag.getInteger("GarageKitFacing") & 3;
    }
}

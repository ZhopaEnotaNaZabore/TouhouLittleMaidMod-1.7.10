package com.github.tartaricacid.touhoulittlemaid.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public final class TileEntityMaidBed extends TileEntity {
    /** Modern DyeColor id (pink=6), deliberately independent of reversed 1.7 dye damage. */
    private int color = 6;

    public int getColor() { return color; }
    public void setColor(int color) {
        this.color = Math.max(0, Math.min(15, color));
        markDirty();
        if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        NBTTagCompound data=tag.hasKey("ForgeData",10)?tag.getCompoundTag("ForgeData"):tag;
        if(!data.hasKey("BedColor")) color=6;
        else if(data.getBoolean("ModernBedColor")||data!=tag)color=Math.max(0,Math.min(15,data.getInteger("BedColor")));
        else color=15-Math.max(0,Math.min(15,data.getInteger("BedColor")));
    }
    @Override public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("BedColor", color);
        tag.setBoolean("ModernBedColor", true);
    }
    @Override public net.minecraft.network.Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound(); writeToNBT(tag);
        return new net.minecraft.network.play.server.S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
    }
    @Override public void onDataPacket(net.minecraft.network.NetworkManager net,
                                       net.minecraft.network.play.server.S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.func_148857_g());
    }
}

package com.github.tartaricacid.touhoulittlemaid.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public final class TileEntityMaidBed extends TileEntity {
    /** A stale tile or a missing/mismatched foot is not a sleeping destination. */
    public boolean isComplete() {
        if(worldObj==null || !worldObj.blockExists(xCoord,yCoord,zCoord)
                || !(worldObj.getBlock(xCoord,yCoord,zCoord) instanceof com.github.tartaricacid.touhoulittlemaid.block.BlockMaidBed)) return false;
        int meta=worldObj.getBlockMetadata(xCoord,yCoord,zCoord),facing=meta&3;
        if((meta&8)==0)return false;
        int dx=facing==1?-1:facing==3?1:0,dz=facing==0?1:facing==2?-1:0;
        return worldObj.blockExists(xCoord-dx,yCoord,zCoord-dz)
                && worldObj.getBlock(xCoord-dx,yCoord,zCoord-dz)==worldObj.getBlock(xCoord,yCoord,zCoord)
                && worldObj.getBlockMetadata(xCoord-dx,yCoord,zCoord-dz)==facing;
    }

    @Override public net.minecraft.util.AxisAlignedBB getRenderBoundingBox() {
        return net.minecraft.util.AxisAlignedBB.getBoundingBox(xCoord - 2,yCoord + 0,zCoord - 2,xCoord + 2,yCoord + 1,zCoord + 2);
    }

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
        boolean modernNested = !tag.hasKey("BedColor") && tag.hasKey("ForgeData",10);
        NBTTagCompound data=LegacyTileNbt.data(tag);
        if(!data.hasKey("BedColor")) color=6;
        else if(data.getBoolean("ModernBedColor")||modernNested)color=Math.max(0,Math.min(15,data.getInteger("BedColor")));
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

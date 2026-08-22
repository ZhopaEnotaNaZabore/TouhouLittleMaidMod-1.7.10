package com.github.tartaricacid.touhoulittlemaid.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.List;

public final class TileEntityStatue extends TileEntity {
    private int statueSize = 1;
    private boolean coreBlock;
    private int coreX, coreY, coreZ;
    private int facing;
    private final List<int[]> allBlocks = new ArrayList<int[]>();
    private NBTTagCompound extraMaidData;

    public int getStatueSize() { return statueSize; }
    public boolean isCoreBlock() { return coreBlock; }
    public int getFacing() { return facing; }
    public int getCoreX() { return coreX; }
    public int getCoreY() { return coreY; }
    public int getCoreZ() { return coreZ; }
    public List<int[]> getAllBlocks() {
        List<int[]> copy = new ArrayList<int[]>(allBlocks.size());
        for (int[] xyz : allBlocks) copy.add(new int[]{xyz[0], xyz[1], xyz[2]});
        return copy;
    }
    public NBTTagCompound getExtraMaidData() { return extraMaidData == null ? null : (NBTTagCompound) extraMaidData.copy(); }

    public void setForgeData(int size, boolean core, int coreX, int coreY, int coreZ, int facing,
                             List<int[]> positions, NBTTagCompound maidData) {
        statueSize = Math.max(0, Math.min(3, size));
        coreBlock = core;
        this.coreX = coreX; this.coreY = coreY; this.coreZ = coreZ;
        this.facing = facing & 3;
        allBlocks.clear();
        if (positions != null) for (int[] xyz : positions) {
            if (xyz != null && xyz.length >= 3 && allBlocks.size() < 54) {
                allBlocks.add(new int[]{xyz[0], xyz[1], xyz[2]});
            }
        }
        extraMaidData = maidData == null ? null : (NBTTagCompound) maidData.copy();
        markDirty();
        if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        statueSize = Math.max(0, Math.min(3, tag.getInteger("StatueSize")));
        coreBlock = tag.getBoolean("CoreBlock");
        if (tag.hasKey("CoreBlockPos", 10)) {
            NBTTagCompound core = tag.getCompoundTag("CoreBlockPos");
            coreX = core.getInteger("X"); coreY = core.getInteger("Y"); coreZ = core.getInteger("Z");
        } else {
            coreX = tag.getInteger("CoreBlockX"); coreY = tag.getInteger("CoreBlockY"); coreZ = tag.getInteger("CoreBlockZ");
        }
        facing = readFacing(tag, "StatueFacing");
        allBlocks.clear();
        NBTTagList list = tag.getTagList("AllBlocks", 10);
        for (int i = 0; i < list.tagCount() && i < 54; i++) {
            NBTTagCompound pos = list.getCompoundTagAt(i);
            allBlocks.add(new int[]{pos.getInteger("X"), pos.getInteger("Y"), pos.getInteger("Z")});
        }
        extraMaidData = tag.hasKey("ExtraMaidData", 10) ? tag.getCompoundTag("ExtraMaidData") : null;
    }
    @Override public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("StatueSize", statueSize); tag.setBoolean("CoreBlock", coreBlock);
        tag.setInteger("CoreBlockX", coreX); tag.setInteger("CoreBlockY", coreY); tag.setInteger("CoreBlockZ", coreZ);
        tag.setInteger("StatueFacing", facing);
        NBTTagList list = new NBTTagList();
        for (int[] xyz : allBlocks) {
            NBTTagCompound pos = new NBTTagCompound();
            pos.setInteger("X", xyz[0]); pos.setInteger("Y", xyz[1]); pos.setInteger("Z", xyz[2]); list.appendTag(pos);
        }
        tag.setTag("AllBlocks", list);
        if (extraMaidData != null) tag.setTag("ExtraMaidData", extraMaidData.copy());
    }
    @Override public net.minecraft.network.Packet getDescriptionPacket() { NBTTagCompound tag=new NBTTagCompound();writeToNBT(tag);return new net.minecraft.network.play.server.S35PacketUpdateTileEntity(xCoord,yCoord,zCoord,1,tag); }
    @Override public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.S35PacketUpdateTileEntity packet) { readFromNBT(packet.func_148857_g()); }

    private int readFacing(NBTTagCompound tag, String key) {
        if (tag.hasKey(key, 8)) {
            String value = tag.getString(key);
            if ("south".equals(value)) return 0;
            if ("east".equals(value)) return 1;
            if ("west".equals(value)) return 3;
            return 2;
        }
        return tag.getInteger(key) & 3;
    }
}

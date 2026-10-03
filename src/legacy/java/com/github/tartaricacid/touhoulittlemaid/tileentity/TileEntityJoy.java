package com.github.tartaricacid.touhoulittlemaid.tileentity;

import com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit;
import net.minecraft.entity.Entity;
import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

import java.util.UUID;

/** Persistent association between a joy block and its temporary sit mount. */
public class TileEntityJoy extends TileEntity {
    private String sitUuid = "";
    private boolean structureChecked;

    @Override public void markDirty() {
        super.markDirty();
        if (worldObj != null && !worldObj.isRemote) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override public void updateEntity() {
        if (!structureChecked && worldObj != null && !worldObj.isRemote) {
            structureChecked = true;
            Block block = worldObj.getBlock(xCoord, yCoord, zCoord);
            if (block instanceof com.github.tartaricacid.touhoulittlemaid.block.BlockBoardGame)
                ((com.github.tartaricacid.touhoulittlemaid.block.BlockBoardGame) block)
                        .ensureMultiblock(worldObj, xCoord, yCoord, zCoord);
        }
    }

    @Override public AxisAlignedBB getRenderBoundingBox() {
        if (worldObj != null && worldObj.getBlock(xCoord,yCoord,zCoord)
                instanceof com.github.tartaricacid.touhoulittlemaid.block.BlockBoardGame)
            return AxisAlignedBB.getBoundingBox(xCoord-1,yCoord,zCoord-1,xCoord+2,yCoord+2,zCoord+2);
        return AxisAlignedBB.getBoundingBox(xCoord-2,yCoord,zCoord-2,xCoord+2,yCoord+1,zCoord+2);
    }

    public EntitySit getSitEntity() {
        if (worldObj == null || sitUuid.isEmpty()) return null;
        @SuppressWarnings("unchecked")
        java.util.List<Entity> entities = worldObj.loadedEntityList;
        for (Entity entity : entities) {
            if (entity instanceof EntitySit && entity.getUniqueID().toString().equals(sitUuid) && entity.isEntityAlive()) {
                return (EntitySit) entity;
            }
        }
        return null;
    }

    public void setSitEntity(EntitySit sit) {
        sitUuid = sit == null ? "" : sit.getUniqueID().toString();
        markDirty();
    }

    public void removeSitEntity() {
        if (worldObj != null && worldObj.isRemote) return;
        EntitySit sit = getSitEntity();
        if (sit != null) setDeadSafely(sit);
        sitUuid = "";
        markDirty();
    }

    private void setDeadSafely(EntitySit sit) {
        if (sit.riddenByEntity != null) sit.riddenByEntity.mountEntity(null);
        sit.setDead();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        tag = LegacyTileNbt.data(tag);
        if (tag.hasKey("SitId", 8)) sitUuid = normalizeUuid(tag.getString("SitId"));
        else if (tag.hasKey("SitId", 11)) {
            int[] value = tag.getIntArray("SitId");
            if (value.length == 4) {
                long most = ((long) value[0] << 32) | (value[1] & 0xffffffffL);
                long least = ((long) value[2] << 32) | (value[3] & 0xffffffffL);
                sitUuid = new UUID(most, least).toString();
            } else sitUuid = "";
        } else sitUuid = "";
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setString("SitId", sitUuid);
    }

    @Override public Packet getDescriptionPacket() { NBTTagCompound tag=new NBTTagCompound();writeToNBT(tag);return new S35PacketUpdateTileEntity(xCoord,yCoord,zCoord,1,tag); }
    @Override public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) { readFromNBT(packet.func_148857_g()); }

    private static String normalizeUuid(String value) {
        try { return value == null || value.isEmpty() ? "" : UUID.fromString(value).toString(); }
        catch (IllegalArgumentException invalid) { return ""; }
    }
}

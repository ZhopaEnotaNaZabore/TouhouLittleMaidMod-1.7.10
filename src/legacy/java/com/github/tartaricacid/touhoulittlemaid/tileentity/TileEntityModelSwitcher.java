package com.github.tartaricacid.touhoulittlemaid.tileentity;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.List;

public final class TileEntityModelSwitcher extends TileEntity {
    private final List<ModeInfo> infoList = new ArrayList<ModeInfo>();
    private boolean powered;
    private String entityUuid = "";
    private String ownerUuid = "";
    private int index;

    public EntityMaid getBoundMaid() {
        if (worldObj == null || entityUuid.isEmpty()) return null;
        for (Object object : worldObj.loadedEntityList) {
            if (object instanceof EntityMaid && ((Entity) object).getUniqueID().toString().equals(entityUuid)
                    && ((Entity) object).isEntityAlive()) return (EntityMaid) object;
        }
        return null;
    }
    public void bind(EntityMaid maid) { entityUuid = maid == null ? "" : maid.getUniqueID().toString(); ownerUuid=maid==null?"":maid.getOwnerId();changed(); }
    public boolean isOwnedBy(net.minecraft.entity.player.EntityPlayer player){return ownerUuid.isEmpty()||ownerUuid.equals(player.getUniqueID().toString());}
    public boolean hasOwner(){return !ownerUuid.isEmpty();}
    public List<ModeInfo> getInfoList() { return infoList; }
    public int getIndex() { return index; }
    public boolean isPowered() { return powered; }
    public void setPowered(boolean value) { powered = value; }
    public void cycle(int direction) {
        if (infoList.isEmpty()) return;
        index = (index + direction) % infoList.size(); if (index < 0) index += infoList.size();
        applyCurrent(); changed();
    }
    public void capture(EntityMaid maid) {
        bind(maid);
        if (infoList.size() >= 128) infoList.remove(0);
        infoList.add(new ModeInfo(maid.getModelId(), maid.hasCustomNameTag() ? maid.getCustomNameTag() : "", maid.rotationYaw));
        index = infoList.size() - 1; changed();
    }
    public void addMode(String modelId) {
        if (!validModelId(modelId) || infoList.size() >= 128) return;
        infoList.add(new ModeInfo(modelId, "", 0)); changed();
    }
    public void removeMode(int selected) {
        if (selected < 0 || selected >= infoList.size()) return;
        infoList.remove(selected);
        if (infoList.isEmpty()) index = 0; else index = Math.min(index, infoList.size() - 1);
        changed();
    }
    public void applyMode(int selected) {
        if (selected < 0 || selected >= infoList.size()) return;
        index = selected; applyCurrent(); changed();
    }
    public void rotateMode(int selected, float degrees) {
        if (selected < 0 || selected >= infoList.size()) return;
        ModeInfo old = infoList.get(selected);
        float yaw = (old.yaw + degrees) % 360.0F; if (yaw < 0) yaw += 360.0F;
        infoList.set(selected, new ModeInfo(old.modelId, old.name, yaw));
        changed();
    }
    public void renameMode(int selected, String name) {
        if (selected < 0 || selected >= infoList.size()) return;
        if (name == null) name = "";
        if (name.length() > 64) name = name.substring(0, 64);
        ModeInfo old = infoList.get(selected);
        infoList.set(selected, new ModeInfo(old.modelId, name, old.yaw));
        changed();
    }
    public void applyCurrent() {
        EntityMaid maid = getBoundMaid();
        if (maid == null || index < 0 || index >= infoList.size()) return;
        ModeInfo info = infoList.get(index); maid.setModelId(info.modelId);
        if (info.name.isEmpty()) {
            maid.setCustomNameTag("");
            maid.setAlwaysRenderNameTag(false);
        } else {
            maid.setCustomNameTag(info.name);
            maid.setAlwaysRenderNameTag(true);
        }
        maid.setPosition(Math.floor(maid.posX) + 0.5D, maid.posY, Math.floor(maid.posZ) + 0.5D);
        maid.prevRotationYaw = maid.rotationYaw = info.yaw;
        maid.prevRotationYawHead = maid.rotationYawHead = info.yaw;
        maid.prevRenderYawOffset = maid.renderYawOffset = info.yaw;
    }
    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag); readStorage(tag);
    }
    public void readStorage(NBTTagCompound tag) {
        // 1.20 stores custom persistent data below ForgeData; native 1.7 saves it flat.
        NBTTagCompound data = tag.hasKey("ForgeData", 10) ? tag.getCompoundTag("ForgeData") : tag;
        entityUuid = readUuid(data, "entity_uuid");
        ownerUuid = readUuid(data, "owner_uuid");
        index = data.getInteger("list_index");
        infoList.clear(); NBTTagList list = data.getTagList("info_list", 10);
        for (int i = 0; i < list.tagCount() && i < 128; i++) infoList.add(ModeInfo.read(list.getCompoundTagAt(i)));
        if (infoList.isEmpty()) index = 0; else index = Math.max(0, Math.min(index, infoList.size() - 1));
        changed();
    }
    @Override public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag); writeStorage(tag);
    }
    public void writeStorage(NBTTagCompound tag) {
        tag.setString("entity_uuid", entityUuid);tag.setString("owner_uuid",ownerUuid); tag.setInteger("list_index", index);
        NBTTagList list = new NBTTagList(); for (ModeInfo info : infoList) list.appendTag(info.write()); tag.setTag("info_list", list);
    }
    private void changed() { markDirty(); if (worldObj != null && !worldObj.isRemote) worldObj.markBlockForUpdate(xCoord,yCoord,zCoord); }
    private static boolean validModelId(String value) {
        return value != null && value.length() <= 96 && value.matches("[a-z0-9_.-]+:[a-z0-9_./-]+");
    }
    private static String readUuid(NBTTagCompound tag, String key) {
        if (tag.hasKey(key, 8)) return tag.getString(key);
        if (tag.hasKey(key, 11)) {
            int[] value = tag.getIntArray(key);
            if (value.length == 4) {
                long most = ((long) value[0] << 32) | (value[1] & 0xffffffffL);
                long least = ((long) value[2] << 32) | (value[3] & 0xffffffffL);
                return new java.util.UUID(most, least).toString();
            }
        }
        return "";
    }
    @Override public net.minecraft.network.Packet getDescriptionPacket() { NBTTagCompound tag=new NBTTagCompound();writeToNBT(tag);return new net.minecraft.network.play.server.S35PacketUpdateTileEntity(xCoord,yCoord,zCoord,1,tag); }
    @Override public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.S35PacketUpdateTileEntity packet) { readFromNBT(packet.func_148857_g()); }
    public static final class ModeInfo {
        public final String modelId, name; public final float yaw;
        public ModeInfo(String modelId, String name, float yaw) { this.modelId = modelId; this.name = name; this.yaw = yaw; }
        NBTTagCompound write() { NBTTagCompound tag = new NBTTagCompound(); tag.setString("model_id", modelId);
            tag.setString("text", name); tag.setFloat("yaw", yaw); return tag; }
        static ModeInfo read(NBTTagCompound tag) {
            String model = tag.getString("model_id");
            if (!validModelId(model)) model = "touhou_little_maid:hakurei_reimu";
            String text = tag.getString("text");
            if (text.length() > 64) text = text.substring(0, 64);
            float yaw = tag.hasKey("yaw", 99) ? tag.getFloat("yaw") : (tag.getInteger("direction") & 3) * 90.0F;
            if (Float.isNaN(yaw) || Float.isInfinite(yaw)) yaw = 0;
            yaw %= 360.0F; if (yaw < 0) yaw += 360.0F;
            return new ModeInfo(model, text, yaw);
        }
    }
}

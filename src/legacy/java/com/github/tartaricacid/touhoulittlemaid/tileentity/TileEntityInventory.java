package com.github.tartaricacid.touhoulittlemaid.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

public abstract class TileEntityInventory extends TileEntity implements IInventory {
    private final ItemStack[] items;
    private final String inventoryName;

    protected TileEntityInventory(int size, String inventoryName) {
        if (inventoryName == null || inventoryName.length() > 32)
            throw new IllegalArgumentException("1.7.10 inventory name exceeds S2DPacketOpenWindow limit: " + inventoryName);
        items = new ItemStack[size];
        this.inventoryName = inventoryName;
    }

    @Override public int getSizeInventory() { return items.length; }
    @Override public ItemStack getStackInSlot(int slot) { return items[slot]; }
    @Override public ItemStack decrStackSize(int slot, int amount) {
        ItemStack stack = items[slot];
        if (stack == null) return null;
        if (stack.stackSize <= amount) { items[slot] = null; markDirty(); return stack; }
        ItemStack result = stack.splitStack(amount);
        if (stack.stackSize == 0) items[slot] = null;
        markDirty(); return result;
    }
    @Override public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = items[slot];
        if (stack != null) { items[slot] = null; markDirty(); }
        return stack;
    }
    @Override public void setInventorySlotContents(int slot, ItemStack stack) {
        items[slot] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) stack.stackSize = getInventoryStackLimit();
        markDirty();
    }
    @Override public String getInventoryName() { return inventoryName; }
    @Override public boolean hasCustomInventoryName() { return false; }
    @Override public int getInventoryStackLimit() { return 64; }
    @Override public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj != null && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
                && player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
    }
    @Override public void openInventory() { }
    @Override public void closeInventory() { }
    @Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return true; }

    @Override public void markDirty() {
        super.markDirty();
        if (worldObj != null && !worldObj.isRemote)
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        // Description packets contain only occupied slots. Clear the previous
        // client state first or removed items survive locally and TESRs keep
        // rendering them after crafting/extraction.
        java.util.Arrays.fill(items, null);
        NBTTagList list = tag.getTagList("Items", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound item = list.getCompoundTagAt(i);
            com.github.tartaricacid.touhoulittlemaid.entity.passive.LegacyNbtMigration.normalizeItemStack(item);
            int slot = item.getByte("Slot") & 255;
            if (slot < items.length) items[slot] = ItemStack.loadItemStackFromNBT(item);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagList list = new NBTTagList();
        for (int slot = 0; slot < items.length; slot++) {
            if (items[slot] != null) {
                NBTTagCompound item = new NBTTagCompound();
                item.setByte("Slot", (byte) slot);
                items[slot].writeToNBT(item);
                list.appendTag(item);
            }
        }
        tag.setTag("Items", list);
    }
    @Override public Packet getDescriptionPacket() { NBTTagCompound tag=new NBTTagCompound();writeToNBT(tag);return new S35PacketUpdateTileEntity(xCoord,yCoord,zCoord,1,tag); }
    @Override public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) { readFromNBT(packet.func_148857_g()); }
}

package com.github.tartaricacid.touhoulittlemaid.entity.item;

import com.github.tartaricacid.touhoulittlemaid.init.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

/** Owner-protected storage created when a maid dies. */
public final class EntityTombstone extends Entity {
    private static final int WATCHER_MAID_NAME = 10;
    private final InventoryBasic items = new InventoryBasic("TombstoneItems", false, 256);
    private String ownerId = "";

    public EntityTombstone(World world) {
        super(world);
        setSize(0.8F, 1.2F);
    }

    public EntityTombstone(World world, String ownerId, double x, double y, double z) {
        this(world);
        this.ownerId = ownerId == null ? "" : ownerId;
        setPosition(x, y, z);
    }

    @Override
    protected void entityInit() { dataWatcher.addObject(WATCHER_MAID_NAME, ""); }

    public void insertItem(ItemStack source) {
        if (source == null) return;
        ItemStack remaining = source.copy();
        for (int slot = 0; slot < items.getSizeInventory() && remaining.stackSize > 0; slot++) {
            ItemStack stored = items.getStackInSlot(slot);
            if (stored == null) {
                items.setInventorySlotContents(slot, remaining.copy());
                remaining.stackSize = 0;
            } else if (stored.isItemEqual(remaining) && ItemStack.areItemStackTagsEqual(stored, remaining)) {
                int move = Math.min(remaining.stackSize, Math.min(stored.getMaxStackSize(), items.getInventoryStackLimit()) - stored.stackSize);
                if (move > 0) { stored.stackSize += move; remaining.stackSize -= move; }
            }
        }
        if (remaining.stackSize > 0) entityDropItem(remaining, 0.0F);
    }

    @Override
    public boolean interactFirst(EntityPlayer player) {
        ItemStack held = player.getCurrentEquippedItem();
        boolean conversionTool = held != null && held.getItem() == ModItems.OWNER_CONVERSION_TOOL;
        if (!ownerId.equals(player.getUniqueID().toString()) && !conversionTool) {
            if (!worldObj.isRemote) player.addChatMessage(new ChatComponentText("This tombstone belongs to another maid owner."));
            return true;
        }
        if (!worldObj.isRemote) {
            for (int slot = 0; slot < items.getSizeInventory(); slot++) {
                ItemStack stack = items.getStackInSlot(slot);
                if (stack == null) continue;
                items.setInventorySlotContents(slot, null);
                if (!player.inventory.addItemStackToInventory(stack)) entityDropItem(stack, 0.0F);
            }
            player.inventory.markDirty();
            setDead();
        }
        return true;
    }

    @Override
    public boolean canBeCollidedWith() { return !isDead; }
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) { return false; }

    public void setMaidName(String name) { dataWatcher.updateObject(WATCHER_MAID_NAME, name == null ? "" : name); }
    public String getMaidName() { return dataWatcher.getWatchableObjectString(WATCHER_MAID_NAME); }
    public boolean isOwnedBy(EntityPlayer player){return player!=null&&ownerId.equals(player.getUniqueID().toString());}

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        ownerId = tag.getString("OwnerId");
        setMaidName(tag.getString("MaidName"));
        NBTTagList list = tag.getTagList("TombstoneItems", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound item = list.getCompoundTagAt(i);
            int slot = item.getShort("Slot") & 65535;
            if (slot < items.getSizeInventory()) items.setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(item));
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        tag.setString("OwnerId", ownerId);
        tag.setString("MaidName", getMaidName());
        NBTTagList list = new NBTTagList();
        for (int slot = 0; slot < items.getSizeInventory(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (stack != null) {
                NBTTagCompound item = new NBTTagCompound();
                item.setShort("Slot", (short) slot);
                stack.writeToNBT(item);
                list.appendTag(item);
            }
        }
        tag.setTag("TombstoneItems", list);
    }
}

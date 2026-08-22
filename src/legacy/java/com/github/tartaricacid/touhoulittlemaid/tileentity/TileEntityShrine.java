package com.github.tartaricacid.touhoulittlemaid.tileentity;

import com.github.tartaricacid.touhoulittlemaid.init.ModItems;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.LegacyNbtMigration;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public final class TileEntityShrine extends TileEntityInventory {
    public TileEntityShrine() { super(1, "container.tlm.shrine"); }
    @Override public int getInventoryStackLimit() { return 1; }
    @Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return stack != null && stack.getItem() == ModItems.FILM; }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        NBTTagCompound data = tag.hasKey("ForgeData", 10) ? tag.getCompoundTag("ForgeData") : tag;
        if (!data.hasKey("StorageItem", 10)) return;
        NBTTagList items = data.getCompoundTag("StorageItem").getTagList("Items", 10);
        for (int i = 0; i < items.tagCount(); i++) {
            NBTTagCompound item = items.getCompoundTagAt(i);
            if ((item.getByte("Slot") & 255) != 0) continue;
            LegacyNbtMigration.normalizeItemStack(item);
            ItemStack stack = ItemStack.loadItemStackFromNBT(item);
            if (isItemValidForSlot(0, stack)) setInventorySlotContents(0, stack);
            break;
        }
    }
}

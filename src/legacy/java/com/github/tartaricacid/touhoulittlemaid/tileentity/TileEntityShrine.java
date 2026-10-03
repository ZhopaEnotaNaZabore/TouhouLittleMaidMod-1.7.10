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

}

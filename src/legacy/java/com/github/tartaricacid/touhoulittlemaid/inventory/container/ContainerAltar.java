package com.github.tartaricacid.touhoulittlemaid.inventory.container;

import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityAltar;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/** Six real offering slots; ContainerChest truncates a six-slot inventory to zero rows. */
public final class ContainerAltar extends Container {
    private final TileEntityAltar altar;
    public ContainerAltar(InventoryPlayer player, TileEntityAltar altar) {
        this.altar = altar;
        for (int i=0;i<6;i++) addSlotToContainer(new Slot(altar,i,35+i*18,20));
        for (int row=0;row<3;row++) for(int col=0;col<9;col++)
            addSlotToContainer(new Slot(player,col+row*9+9,8+col*18,51+row*18));
        for (int col=0;col<9;col++) addSlotToContainer(new Slot(player,col,8+col*18,109));
    }
    @Override public boolean canInteractWith(EntityPlayer player) { return altar.isUseableByPlayer(player); }
    @Override public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        if (index<0 || index>=inventorySlots.size()) return null;
        Slot slot=(Slot)inventorySlots.get(index);
        if (!slot.getHasStack()) return null;
        ItemStack stack=slot.getStack(), before=stack.copy();
        if (index<6 ? !mergeItemStack(stack,6,inventorySlots.size(),true) : !mergeItemStack(stack,0,6,false)) return null;
        if (stack.stackSize==0) slot.putStack(null); else slot.onSlotChanged();
        if (stack.stackSize==before.stackSize) return null;
        slot.onPickupFromSlot(player,stack);
        return before;
    }
}

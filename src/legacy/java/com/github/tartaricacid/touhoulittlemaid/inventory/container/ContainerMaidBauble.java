package com.github.tartaricacid.touhoulittlemaid.inventory.container;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.item.ItemMaidBauble;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public final class ContainerMaidBauble extends Container {
    private final EntityMaid maid;
    private final int baubleSlots;
    public ContainerMaidBauble(InventoryPlayer player, EntityMaid maid) {
        this.maid = maid;
        int level = maid.getFavorabilityManager().getLevel();
        this.baubleSlots = 30;
        for (int row=0;row<baubleSlots/5;row++) for(int col=0;col<5;col++) addSlotToContainer(new Slot(maid.getMaidBaubleInventory(),col+row*5,152+col*18,45+row*18){
            @Override public boolean isItemValid(ItemStack stack){return getSlotIndex()<ContainerMaidBauble.this.maid.getBaubleCapacity()&&EntityMaid.isBauble(stack);}
        });
        for(int row=0;row<3;row++) for(int col=0;col<9;col++) addSlotToContainer(new Slot(player,col+row*9+9,88+col*18,174+row*18));
        for(int col=0;col<9;col++) addSlotToContainer(new Slot(player,col,88+col*18,232));
    }
    @Override public boolean canInteractWith(EntityPlayer player){return maid.isEntityAlive()&&maid.getOwner()==player&&player.getDistanceSqToEntity(maid)<64;}
    @Override public ItemStack transferStackInSlot(EntityPlayer player,int index){
        Slot slot=(Slot)inventorySlots.get(index);if(slot==null||!slot.getHasStack())return null;ItemStack stack=slot.getStack(),copy=stack.copy();
        if(index<baubleSlots){if(!mergeItemStack(stack,baubleSlots,baubleSlots+36,true))return null;}else{if(!EntityMaid.isBauble(stack)||!mergeItemStack(stack,0,baubleSlots,false))return null;}
        if(stack.stackSize==0)slot.putStack(null);else slot.onSlotChanged();return copy;
    }
}

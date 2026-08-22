package com.github.tartaricacid.touhoulittlemaid.inventory.container;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public final class ContainerMaidTask extends Container{
 private final EntityMaid maid;public ContainerMaidTask(InventoryPlayer p,EntityMaid m){maid=m;
  addSlotToContainer(new Slot(m.getMaidHideInventory(),0,143,45));for(int n=0;n<9;n++)addSlotToContainer(new Slot(m.getMaidTaskInventory(),n,88+n*18,82));
  for(int r=0;r<3;r++)for(int c=0;c<9;c++)addSlotToContainer(new Slot(p,c+r*9+9,88+c*18,174+r*18));for(int c=0;c<9;c++)addSlotToContainer(new Slot(p,c,88+c*18,232));}
 @Override public boolean canInteractWith(EntityPlayer p){return maid.isEntityAlive()&&maid.getOwner()==p&&p.getDistanceSqToEntity(maid)<64;}
 @Override public ItemStack transferStackInSlot(EntityPlayer p,int index){Slot s=(Slot)inventorySlots.get(index);if(s==null||!s.getHasStack())return null;ItemStack a=s.getStack(),copy=a.copy();if(index<10){if(!mergeItemStack(a,10,46,true))return null;}else if(!mergeItemStack(a,1,10,false))return null;if(a.stackSize==0)s.putStack(null);else s.onSlotChanged();return copy;}
}

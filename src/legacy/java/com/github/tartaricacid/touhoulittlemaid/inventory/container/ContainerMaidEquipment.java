package com.github.tartaricacid.touhoulittlemaid.inventory.container;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public final class ContainerMaidEquipment extends Container {
    private final EntityMaid maid;
    public ContainerMaidEquipment(InventoryPlayer player,EntityMaid maid){this.maid=maid;
        addSlotToContainer(new Slot(maid.getMaidEquipmentInventory(),0,87,77));
        addSlotToContainer(new Slot(maid.getMaidEquipmentInventory(),1,121,77));
        final int[] slots={5,4,3,2};
        for(int n=0;n<4;n++){final int slot=slots[n], armorType=n;addSlotToContainer(new Slot(maid.getMaidEquipmentInventory(),slot,94+(n%2)*20,37+(n/2)*20){@Override public boolean isItemValid(ItemStack s){return s!=null&&s.getItem() instanceof ItemArmor&&((ItemArmor)s.getItem()).armorType==armorType;}@Override public int getSlotStackLimit(){return 1;}});}
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlotToContainer(new Slot(player,col+row*9+9,88+col*18,174+row*18));
        for(int col=0;col<9;col++)addSlotToContainer(new Slot(player,col,88+col*18,232));
    }
    @Override public boolean canInteractWith(EntityPlayer p){return maid.isEntityAlive()&&maid.getOwner()==p&&p.getDistanceSqToEntity(maid)<64;}
    @Override public ItemStack transferStackInSlot(EntityPlayer p,int index){Slot s=(Slot)inventorySlots.get(index);if(s==null||!s.getHasStack())return null;ItemStack stack=s.getStack(),copy=stack.copy();if(index<6){if(!mergeItemStack(stack,6,42,true))return null;}else if(stack.getItem() instanceof ItemArmor){int target=2+((ItemArmor)stack.getItem()).armorType;if(!mergeItemStack(stack,target,target+1,false))return null;}else if(!mergeItemStack(stack,0,2,false))return null;if(stack.stackSize==0)s.putStack(null);else s.onSlotChanged();return copy;}
}

package com.github.tartaricacid.touhoulittlemaid.inventory.container;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.inventory.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
public final class ContainerMaidTank extends Container {
    private final EntityMaid maid;
    public int amount,fluidId=-1;
    public ContainerMaidTank(InventoryPlayer player,EntityMaid maid){this.maid=maid;
        addSlotToContainer(new Slot(maid.getTankInventory(),0,44,35){@Override public boolean isItemValid(ItemStack stack){return inventory.isItemValidForSlot(0,stack);}});
        addSlotToContainer(new Slot(maid.getTankInventory(),1,116,35){@Override public boolean isItemValid(ItemStack stack){return inventory.isItemValidForSlot(1,stack);}});
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlotToContainer(new Slot(player,9+row*9+col,8+col*18,84+row*18));
        for(int col=0;col<9;col++)addSlotToContainer(new Slot(player,col,8+col*18,142));
    }
    @Override public boolean canInteractWith(EntityPlayer p){return maid.isEntityAlive() && maid.worldObj==p.worldObj && maid.getOwner()==p
        && maid.getDistanceSqToEntity(p)<64 && "tank_backpack".equals(maid.getBackpackType());}
    @Override public void detectAndSendChanges(){super.detectAndSendChanges();
        int nextAmount=maid.getBackpackFluidAmount();String name=maid.getBackpackFluid();
        int nextId="milk".equals(name)?-2:FluidRegistry.getFluidID(name);
        for(Object obj:crafters){ICrafting listener=(ICrafting)obj;listener.sendProgressBarUpdate(this,0,nextAmount);listener.sendProgressBarUpdate(this,1,nextId);}
        amount=nextAmount;fluidId=nextId;
    }
    @Override public void updateProgressBar(int id,int value){if(id==0)amount=value;if(id==1)fluidId=value;}
    @Override public ItemStack transferStackInSlot(EntityPlayer player,int index){
        if(index<0||index>=inventorySlots.size())return null;
        Slot slot=getSlot(index);if(!slot.getHasStack())return null;ItemStack stack=slot.getStack(),original=stack.copy();
        if(index<2){if(!mergeItemStack(stack,2,38,true))return null;}
        else { // One container per operation, including shift-clicking a stack of empty buckets.
            int target=net.minecraftforge.fluids.FluidContainerRegistry.getFluidForFilledItem(stack)!=null||stack.getItem()==net.minecraft.init.Items.milk_bucket?0:1;
            Slot destination=getSlot(target);if(destination.getHasStack() || !destination.isItemValid(stack))return null;
            ItemStack one=stack.copy();one.stackSize=1;destination.putStack(one);--stack.stackSize;
        }
        if(stack.stackSize<=0)slot.putStack(null);else slot.onSlotChanged();slot.onPickupFromSlot(player,stack);return original;
    }
}

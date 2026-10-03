package com.github.tartaricacid.touhoulittlemaid.inventory;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
public final class MaidTankInventory extends InventoryBasic {
    private final EntityMaid maid;
    public MaidTankInventory(EntityMaid maid){super("container.touhou_little_maid.tank",false,2);this.maid=maid;}
    @Override public int getInventoryStackLimit(){return 1;}
    @Override public boolean isItemValidForSlot(int slot,ItemStack stack){return stack!=null &&
            (stack.getItem()==net.minecraft.init.Items.bucket || stack.getItem()==net.minecraft.init.Items.milk_bucket
            || net.minecraftforge.fluids.FluidContainerRegistry.isContainer(stack));}
    public void tick(){
        if(maid.worldObj.isRemote)return;
        for(int slot=0;slot<2;slot++){
            LegacyTankTransfer.Result result=LegacyTankTransfer.transfer(getStackInSlot(slot),maid.getBackpackFluid(),maid.getBackpackFluidAmount(),slot==0);
            if(result!=null){setInventorySlotContents(slot,result.container);maid.setBackpackFluidState(result.fluid,result.amount);}
        }
    }
    public void clear(){setInventorySlotContents(0,null);setInventorySlotContents(1,null);}
    public NBTTagCompound save(){NBTTagCompound tag=new NBTTagCompound();NBTTagList items=new NBTTagList();
        for(int i=0;i<2;i++){ItemStack stack=getStackInSlot(i);if(stack!=null){NBTTagCompound entry=new NBTTagCompound();entry.setByte("Slot",(byte)i);stack.writeToNBT(entry);items.appendTag(entry);}}
        tag.setTag("Items",items);return tag;
    }
    public void load(NBTTagCompound tag){clear();NBTTagList items=tag.getTagList("Items",10);
        for(int i=0;i<items.tagCount();i++){NBTTagCompound entry=(NBTTagCompound)items.getCompoundTagAt(i).copy();
            com.github.tartaricacid.touhoulittlemaid.entity.passive.LegacyNbtMigration.normalizeItemStack(entry);
            int slot=entry.getByte("Slot")&255;ItemStack stack=ItemStack.loadItemStackFromNBT(entry);
            if(slot<2&&stack!=null&&stack.stackSize>0)setInventorySlotContents(slot,stack);
        }
    }
}

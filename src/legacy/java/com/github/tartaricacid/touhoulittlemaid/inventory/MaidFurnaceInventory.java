package com.github.tartaricacid.touhoulittlemaid.inventory;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;

/** Furnace inventory owned by an entity; never installs a furnace block in the world. */
public final class MaidFurnaceInventory extends TileEntityFurnace {
    private final EntityMaid maid;
    public MaidFurnaceInventory(EntityMaid maid){this.maid=maid;}
    @Override public boolean isUseableByPlayer(EntityPlayer player){
        return maid!=null && maid.isEntityAlive() && "furnace_backpack".equals(maid.getBackpackType())
                && maid.worldObj==player.worldObj && maid.getOwner()==player && maid.getDistanceSqToEntity(player)<64;
    }
    @Override public void markDirty() { }
    @Override public void updateEntity() { } // EntityMaid drives tick; vanilla would replace a world block.
    private ItemStack recipe(){ItemStack in=getStackInSlot(0);return in==null||in.stackSize<=0?null:FurnaceRecipes.smelting().getSmeltingResult(in);}
    private boolean canCook(ItemStack result){
        if(result==null||result.stackSize<=0)return false;
        ItemStack out=getStackInSlot(2);
        int limit=Math.min(getInventoryStackLimit(),result.getMaxStackSize());
        return out==null ? result.stackSize<=limit : out.isItemEqual(result) && ItemStack.areItemStackTagsEqual(out,result)
                && out.stackSize+result.stackSize<=Math.min(limit,out.getMaxStackSize());
    }
    public void tick(){
        if(maid!=null && maid.worldObj.isRemote)return;
        if(furnaceBurnTime>0)--furnaceBurnTime;
        ItemStack fuel=getStackInSlot(1),result=recipe();
        if(furnaceBurnTime>0 || (fuel!=null && fuel.stackSize>0 && result!=null)) {
            if(furnaceBurnTime==0 && canCook(result)) {
                furnaceBurnTime=currentItemBurnTime=getItemBurnTime(fuel);
                if(furnaceBurnTime>0) {
                    ItemStack one=fuel.copy();one.stackSize=1;
                    ItemStack container=fuel.getItem().hasContainerItem(one)?fuel.getItem().getContainerItem(one):null;
                    // Container fuels normally have stack size one. Do not discard a modded stack's remainder.
                    if(container!=null && fuel.stackSize>1){furnaceBurnTime=currentItemBurnTime=0;}
                    else {decrStackSize(1,1);if(container!=null)setInventorySlotContents(1,container);}
                }
            }
            if(furnaceBurnTime>0 && canCook(result)) {
                if(++furnaceCookTime>=200){furnaceCookTime=0;smeltItem();}
            } else furnaceCookTime=0;
        } else furnaceCookTime=Math.max(0,furnaceCookTime-2);
    }
    @Override public void smeltItem(){
        ItemStack result=recipe();if(!canCook(result))return;
        ItemStack out=getStackInSlot(2);
        if(out==null)super.setInventorySlotContents(2,result.copy());else out.stackSize+=result.stackSize;
        decrStackSize(0,1);
        if(maid!=null){float exp=FurnaceRecipes.smelting().func_151398_b(result);int amount=(int)exp;
            if(maid.getRNG().nextFloat()<exp-amount)amount++;
            maid.setMaidExperience(maid.getMaidExperience()+amount);}
    }
    @Override public void setInventorySlotContents(int slot,ItemStack stack){
        ItemStack old=getStackInSlot(slot);
        boolean same=old!=null && stack!=null && old.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(old,stack);
        super.setInventorySlotContents(slot,stack);
        if(slot==0 && !same)furnaceCookTime=0;
    }
    public void clear(){for(int i=0;i<3;i++)super.setInventorySlotContents(i,null);furnaceBurnTime=currentItemBurnTime=furnaceCookTime=0;}
    @Override public void writeToNBT(NBTTagCompound tag){
        net.minecraft.nbt.NBTTagList items=new net.minecraft.nbt.NBTTagList();
        for(int i=0;i<3;i++){ItemStack stack=getStackInSlot(i);if(stack!=null){NBTTagCompound entry=new NBTTagCompound();entry.setByte("Slot",(byte)i);stack.writeToNBT(entry);items.appendTag(entry);}}
        tag.setTag("Items",items);tag.setInteger("BurnTime",furnaceBurnTime);tag.setInteger("CookTime",furnaceCookTime);
        tag.setInteger("BurnDuration",currentItemBurnTime);tag.setInteger("CookTimeTotal",200);
    }
    @Override public void readFromNBT(NBTTagCompound tag){
        NBTTagCompound normalized=(NBTTagCompound)tag.copy();
        net.minecraft.nbt.NBTTagList items=normalized.getTagList("Items",10);
        for(int i=0;i<items.tagCount();i++)com.github.tartaricacid.touhoulittlemaid.entity.passive.LegacyNbtMigration.normalizeItemStack(items.getCompoundTagAt(i));
        clear();super.readFromNBT(normalized);
        furnaceBurnTime=Math.max(0,tag.getInteger("BurnTime"));currentItemBurnTime=Math.max(furnaceBurnTime,tag.getInteger("BurnDuration"));
        furnaceCookTime=Math.max(0,Math.min(199,tag.getInteger("CookTime")));
        for(int i=0;i<3;i++){ItemStack stack=getStackInSlot(i);if(stack!=null&&stack.stackSize<=0)super.setInventorySlotContents(i,null);}
    }
}

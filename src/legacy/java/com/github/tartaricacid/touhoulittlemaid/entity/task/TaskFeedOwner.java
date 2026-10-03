package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public final class TaskFeedOwner implements IMaidTask {
    @Override public String getId() { return TaskManager.FEED_OWNER_ID; }

    @Override public void tick(EntityMaid maid) {
        if (maid.worldObj.isRemote || maid.isSitting() || maid.isMaidSleeping()
                || !maid.isWorkingNow() || !maid.isPeriodicTick(20)) return;
        EntityLivingBase entity=maid.getOwner();
        if (!(entity instanceof EntityPlayer) || !entity.isEntityAlive()
                || entity.worldObj!=maid.worldObj || entity.dimension!=maid.dimension
                || !maid.isPositionWithinRestriction(entity.posX,entity.posY,entity.posZ)) return;
        EntityPlayer owner=(EntityPlayer)entity;
        IInventory[] inventories={maid.getMaidEquipmentInventory(),maid.getMaidTaskInventory(),maid.getMaidInventory()};
        int[] limits={2,inventories[1].getSizeInventory(),maid.getBackpackCapacity()};
        IInventory selected=null; int selectedSlot=-1, best=0;
        for(int n=0;n<inventories.length;n++) for(int i=0;i<Math.min(limits[n],inventories[n].getSizeInventory());i++) {
            int priority=priority(inventories[n].getStackInSlot(i),owner);
            if(priority>best){best=priority;selected=inventories[n];selectedSlot=i;}
        }
        if(selected==null) return;
        if(maid.getDistanceSqToEntity(owner)>6.25D) {
            if(!maid.isRiding()) maid.getNavigator().tryMoveToEntityLiving(owner,.7D);
            return;
        }
        if(feedFrom(maid,owner,selected,selectedSlot)) maid.swingItem();
    }

    private static int priority(ItemStack stack,EntityPlayer owner) {
        if(stack==null || stack.stackSize<=0) return 0;
        if(stack.getItem()==Items.milk_bucket) {
            for(Object value:owner.getActivePotionEffects()) {
                PotionEffect effect=(PotionEffect)value;
                int id=effect.getPotionID();
                Potion potion=id>=0 && id<Potion.potionTypes.length ? Potion.potionTypes[id] : null;
                if(potion!=null && potion.isBadEffect() && effect.getDuration()>60 && effect.isCurativeItem(stack)) return 4;
            }
            return 0;
        }
        if(!(stack.getItem() instanceof ItemFood) || stack.getItem()==Items.rotten_flesh
                || stack.getItem()==Items.spider_eye || stack.getItem()==Items.poisonous_potato
                || stack.getItem()==Items.chicken || (stack.getItem()==Items.fish && stack.getItemDamage()==3)) return 0;
        boolean dying=owner.getHealth()*2<owner.getMaxHealth();
        if(stack.getItem()==Items.golden_apple) return dying ? 3 : 0;
        if(!owner.getFoodStats().needFood()) return dying ? 1 : 0;
        return ((ItemFood)stack.getItem()).func_150905_g(stack)>=20-owner.getFoodStats().getFoodLevel() ? 3 : 2;
    }

    private static boolean feedFrom(EntityMaid maid,EntityPlayer owner,IInventory inventory,int slot) {
        ItemStack source=inventory.getStackInSlot(slot);
        if(priority(source,owner)==0) return false;
        // Invoke vanilla/mod food behaviour on one item, including potion effects and containers.
        ItemStack one=source.copy();one.stackSize=1;
        ItemStack remainder=one.getItem().onEaten(one,maid.worldObj,owner);
        inventory.decrStackSize(slot,1);
        inventory.markDirty();
        if(remainder!=null && remainder.stackSize>0) {
            if(inventory.getStackInSlot(slot)==null) inventory.setInventorySlotContents(slot,remainder);
            else {
                ItemStack left=maid.addToMaidInventory(remainder);
                if(left!=null && left.stackSize>0) maid.entityDropItem(left,0);
            }
        }
        return true;
    }
}

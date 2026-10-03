package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.List;

public final class TaskMilk implements IMaidTask {
    @Override
    public String getId() {
        return TaskManager.MILK_ID;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void tick(EntityMaid maid) {
        if (maid.worldObj.isRemote || !maid.isEntityAlive() || maid.isMaidSleeping() || !maid.isWorkingNow() || maid.isSitting() || !maid.isPeriodicTick(40) || !hasOutputSpace(maid)) return;
        int bucketSlot = maid.findInventorySlot(Items.bucket);
        if (bucketSlot < 0) return;
        List<EntityCow> cows = maid.worldObj.getEntitiesWithinAABB(
                EntityCow.class, maid.boundingBox.expand(10.0D, 4.0D, 10.0D));
        EntityCow target = null;
        double nearest = Double.MAX_VALUE;
        for (EntityCow cow : cows) {
            double distance = maid.getDistanceSqToEntity(cow);
            if (cow.isEntityAlive() && !cow.isChild() && maid.canEntityBeSeen(cow) && distance < nearest
                    && maid.isPositionWithinRestriction(cow.posX, cow.posY, cow.posZ)) {
                target = cow;
                nearest = distance;
            }
        }
        if (target == null) return;
        maid.getLookHelper().setLookPositionWithEntity(target,30,30);
        if (nearest >= 4.0D) {
            maid.getNavigator().tryMoveToEntityLiving(target, 0.65D);
            return;
        }
        ItemStack bucket=maid.takeOneFromSlot(bucketSlot);
        if(bucket==null)return;
        ItemStack remaining = maid.addToMaidInventory(new ItemStack(Items.milk_bucket));
        if (remaining != null) maid.entityDropItem(remaining, 0.0F);
        maid.workSwing(bucket);
        maid.worldObj.playSoundAtEntity(target,"mob.cow.say",.5F,1F);
    }

    private boolean hasOutputSpace(EntityMaid maid) {
        for (int slot = 0; slot < maid.getBackpackCapacity(); slot++)
            if (maid.getMaidInventory().getStackInSlot(slot) == null) return true;
        return false;
    }
}

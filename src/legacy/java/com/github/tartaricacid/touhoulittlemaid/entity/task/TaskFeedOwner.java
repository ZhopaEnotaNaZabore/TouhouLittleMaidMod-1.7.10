package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public final class TaskFeedOwner implements IMaidTask {
    @Override
    public String getId() {
        return TaskManager.FEED_OWNER_ID;
    }

    @Override
    public void tick(EntityMaid maid) {
        if (maid.isSitting() || !maid.isPeriodicTick(20)) return;
        EntityLivingBase ownerEntity = maid.getOwner();
        if (!(ownerEntity instanceof EntityPlayer)) return;
        EntityPlayer owner = (EntityPlayer) ownerEntity;
        int foodSlot = findSafeFood(maid);
        boolean needsMilk = hasLongHarmfulEffect(owner) && maid.findInventorySlot(Items.milk_bucket) >= 0;
        if (!needsMilk && (!owner.getFoodStats().needFood() || foodSlot < 0)) return;

        if (maid.getDistanceSqToEntity(owner) > 6.25D) {
            maid.getNavigator().tryMoveToEntityLiving(owner, 0.7D);
            return;
        }
        if (needsMilk) {
            int milkSlot = maid.findInventorySlot(Items.milk_bucket);
            owner.curePotionEffects(new ItemStack(Items.milk_bucket));
            maid.takeOneFromSlot(milkSlot);
            maid.addToMaidInventory(new ItemStack(Items.bucket));
            maid.swingItem();
            return;
        }

        ItemStack stack = maid.getStackInLogicalSlot(foodSlot);
        ItemFood food = (ItemFood) stack.getItem();
        owner.getFoodStats().addStats(food.func_150905_g(stack), food.func_150906_h(stack));
        maid.takeOneFromSlot(foodSlot);
        maid.swingItem();
        maid.getFavorabilityManager().add(1);
    }

    private boolean hasLongHarmfulEffect(EntityPlayer owner) {
        for (Object value : owner.getActivePotionEffects()) {
            PotionEffect effect = (PotionEffect) value;
            Potion potion = Potion.potionTypes[effect.getPotionID()];
            if (potion != null && potion.isBadEffect() && effect.getDuration() > 60) return true;
        }
        return false;
    }

    private int findSafeFood(EntityMaid maid) {
        for (int slot = 0; slot < maid.getMaidInventory().getSizeInventory(); slot++) {
            ItemStack stack = maid.getMaidInventory().getStackInSlot(slot);
            if (stack == null || !(stack.getItem() instanceof ItemFood)) continue;
            if (stack.getItem() == Items.rotten_flesh || stack.getItem() == Items.spider_eye
                    || stack.getItem() == Items.poisonous_potato || stack.getItem() == Items.chicken) continue;
            return slot;
        }
        return -1;
    }
}

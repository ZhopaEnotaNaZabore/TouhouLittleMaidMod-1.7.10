package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.List;

public final class TaskFeedAnimal implements IMaidTask {
    private static final int MAX_NEARBY_ANIMALS = 20;

    @Override
    public String getId() {
        return TaskManager.FEED_ANIMAL_ID;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void tick(EntityMaid maid) {
        if (maid.isSitting() || !maid.isPeriodicTick(20)) return;
        EntityLivingBase ownerEntity = maid.getOwner();
        if (!(ownerEntity instanceof EntityPlayer)) return;
        List<EntityAnimal> animals = maid.worldObj.getEntitiesWithinAABB(
                EntityAnimal.class, maid.boundingBox.expand(10.0D, 4.0D, 10.0D));
        if (animals.size() >= MAX_NEARBY_ANIMALS - 2) {
            updateCullTarget(maid, animals);
            return;
        }
        maid.setAttackTarget(null);

        EntityAnimal target = null;
        int foodSlot = -1;
        double nearest = Double.MAX_VALUE;
        for (EntityAnimal animal : animals) {
            if (!animal.isEntityAlive() || animal.getGrowingAge() != 0 || animal.isInLove()
                    || !maid.isPositionWithinRestriction(animal.posX, animal.posY, animal.posZ)) continue;
            int slot = findBreedingFood(maid, animal);
            double distance = maid.getDistanceSqToEntity(animal);
            if (slot >= 0 && distance < nearest) {
                target = animal;
                foodSlot = slot;
                nearest = distance;
            }
        }
        if (target == null) return;
        if (nearest > 6.25D) {
            maid.getNavigator().tryMoveToEntityLiving(target, 0.65D);
            return;
        }
        target.func_146082_f((EntityPlayer) ownerEntity);
        maid.takeOneFromSlot(foodSlot);
        maid.swingItem();
    }

    private void updateCullTarget(EntityMaid maid, List<EntityAnimal> animals) {
        if (!maid.canEngageCombat()) {
            maid.setAttackTarget(null);
            return;
        }
        ItemStack held = maid.getMaidEquipmentInventory().getStackInSlot(0);
        if (!LegacyTaskEquipUtil.isAttackWeapon(held)) {
            maid.setAttackTarget(null);
            return;
        }
        EntityAnimal nearestTarget = null;
        double nearest = Double.MAX_VALUE;
        for (EntityAnimal animal : animals) {
            if (!animal.isEntityAlive() || animal.getGrowingAge() != 0 || animal.isInLove()
                    || !maid.isPositionWithinRestriction(animal.posX, animal.posY, animal.posZ)
                    || findBreedingFood(maid, animal) < 0) continue;
            double distance = maid.getDistanceSqToEntity(animal);
            if (distance < nearest) { nearest = distance; nearestTarget = animal; }
        }
        maid.setAttackTarget(nearestTarget);
    }

    @Override
    public void onDeselected(EntityMaid maid) { maid.setAttackTarget(null); }

    private int findBreedingFood(EntityMaid maid, EntityAnimal animal) {
        for (int slot = 0; slot < maid.getMaidInventory().getSizeInventory(); slot++) {
            ItemStack stack = maid.getMaidInventory().getStackInSlot(slot);
            if (stack != null && animal.isBreedingItem(stack)) return slot;
        }
        return -1;
    }
}

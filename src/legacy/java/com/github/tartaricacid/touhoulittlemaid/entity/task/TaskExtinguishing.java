package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.ModItems;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityExtinguishingAgent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Blocks;

import java.util.List;

public final class TaskExtinguishing implements IMaidTask {
    @Override
    public String getId() {
        return TaskManager.EXTINGUISHING_ID;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void tick(EntityMaid maid) {
        if (maid.worldObj.isRemote || maid.isMaidSleeping() || !maid.isWorkingNow() || maid.isSitting() || !ensureMainhandExtinguisher(maid) || maid.ticksExisted % 12 != 0) return;

        if (maid.isBurning()) {
            extinguish(maid, maid);
            return;
        }
        EntityLivingBase owner = maid.getOwner();
        if (owner != null && owner.isEntityAlive() && maid.canEntityBeSeen(owner) && owner.isBurning()
                && maid.isPositionWithinRestriction(owner.posX, owner.posY, owner.posZ)) {
            if (maid.getDistanceSqToEntity(owner) > 4.0D) {
                maid.getNavigator().tryMoveToEntityLiving(owner, 0.65D);
            } else {
                extinguish(maid, owner);
            }
            return;
        }

        List<EntityTameable> pets = maid.worldObj.getEntitiesWithinAABB(
                EntityTameable.class, maid.boundingBox.expand(2.0D, 1.0D, 2.0D));
        for (EntityTameable pet : pets) {
            if (owner != null && pet.isEntityAlive() && maid.canEntityBeSeen(pet) && pet.isBurning() && pet.getOwner() == owner
                    && maid.isPositionWithinRestriction(pet.posX, pet.posY, pet.posZ)) {
                extinguish(maid, pet);
                return;
            }
        }

        FireTarget fire = findNearestFire(maid);
        if (fire == null) return;
        if (maid.getDistanceSq(fire.x + 0.5D, fire.y, fire.z + 0.5D) > 6.25D) {
            maid.getNavigator().tryMoveToXYZ(fire.x + 0.5D, fire.y, fire.z + 0.5D, 0.65D);
        } else {
            maid.getLookHelper().setLookPosition(fire.x+.5D,fire.y+.5D,fire.z+.5D,30,30);
            if (maid.worldObj.spawnEntityInWorld(new EntityExtinguishingAgent(
                    maid.worldObj, fire.x + 0.5D, fire.y + 0.5D, fire.z + 0.5D))) damageExtinguisher(maid);
        }
    }

    @Override
    public void onSelected(EntityMaid maid) {
        ensureMainhandExtinguisher(maid);
    }

    private boolean ensureMainhandExtinguisher(EntityMaid maid) {
        ItemStack held = maid.getMaidEquipmentInventory().getStackInSlot(0);
        if (held != null && held.getItem() == ModItems.EXTINGUISHER) return true;
        int sourceSlot = maid.findInventorySlot(ModItems.EXTINGUISHER);
        if (sourceSlot < 0) return false;
        ItemStack extinguisher = maid.takeOneFromSlot(sourceSlot);
        if (extinguisher == null) return false;
        maid.getMaidEquipmentInventory().setInventorySlotContents(0, extinguisher);
        if (held != null) {
            ItemStack remaining = maid.addToMaidInventory(held);
            if (remaining != null) maid.entityDropItem(remaining, 0.0F);
        }
        maid.getMaidEquipmentInventory().markDirty();
        return true;
    }

    private void extinguish(EntityMaid maid, EntityLivingBase target) {
        if (target != maid) maid.getLookHelper().setLookPositionWithEntity(target,30,30);
        if (maid.worldObj.spawnEntityInWorld(new EntityExtinguishingAgent(
                maid.worldObj, target.posX, target.posY + 0.5D, target.posZ))) damageExtinguisher(maid);
    }

    private void damageExtinguisher(EntityMaid maid) {
        ItemStack extinguisher = maid.getMaidEquipmentInventory().getStackInSlot(0);
        if (extinguisher == null) return;
        extinguisher.damageItem(1, maid);
        if (extinguisher.stackSize <= 0)
            maid.getMaidEquipmentInventory().setInventorySlotContents(0, null);
        maid.getMaidEquipmentInventory().markDirty();
        maid.swingItem();
    }

    private FireTarget findNearestFire(EntityMaid maid) {
        int centerX = (int) Math.floor(maid.posX);
        int centerY = (int) Math.floor(maid.posY);
        int centerZ = (int) Math.floor(maid.posZ);
        FireTarget nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (int x = centerX - 8; x <= centerX + 8; x++) {
            for (int y = centerY - 3; y <= centerY + 3; y++) {
                for (int z = centerZ - 8; z <= centerZ + 8; z++) {
                    if (maid.worldObj.getBlock(x, y, z) != Blocks.fire
                            || !maid.isPositionWithinRestriction(x + 0.5D, y, z + 0.5D)) continue;
                    double distance = maid.getDistanceSq(x + 0.5D, y, z + 0.5D);
                    if (distance < nearestDistance) {
                        nearest = new FireTarget(x, y, z);
                        nearestDistance = distance;
                    }
                }
            }
        }
        return nearest;
    }

    private static final class FireTarget {
        private final int x, y, z;
        private FireTarget(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }
    }
}

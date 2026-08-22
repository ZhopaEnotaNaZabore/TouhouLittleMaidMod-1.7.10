package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.List;

public final class TaskShears implements IMaidTask {
    @Override
    public String getId() {
        return TaskManager.SHEARS_ID;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void tick(EntityMaid maid) {
        if (maid.isSitting() || !ensureMainhandShears(maid) || !maid.isPeriodicTick(20)) return;
        List<EntitySheep> sheep = maid.worldObj.getEntitiesWithinAABB(
                EntitySheep.class, maid.boundingBox.expand(10.0D, 4.0D, 10.0D));
        EntitySheep target = null;
        double nearest = Double.MAX_VALUE;
        for (EntitySheep candidate : sheep) {
            double distance = maid.getDistanceSqToEntity(candidate);
            if (!candidate.getSheared() && !candidate.isChild() && distance < nearest
                    && maid.isPositionWithinRestriction(candidate.posX, candidate.posY, candidate.posZ)) {
                target = candidate;
                nearest = distance;
            }
        }
        if (target == null) return;
        if (nearest > 6.25D) {
            maid.getNavigator().tryMoveToEntityLiving(target, 0.65D);
            return;
        }

        target.setSheared(true);
        int count = 1 + maid.getRNG().nextInt(3);
        for (int i = 0; i < count; i++) {
            ItemStack remaining = maid.addToMaidInventory(new ItemStack(Blocks.wool, 1, target.getFleeceColor()));
            if (remaining != null) maid.entityDropItem(remaining, 0.0F);
        }
        maid.worldObj.playSoundAtEntity(target, "mob.sheep.shear", 1.0F, 1.0F);
        ItemStack shears = maid.getMaidEquipmentInventory().getStackInSlot(0);
        shears.damageItem(1, maid);
        if (shears.stackSize <= 0) maid.getMaidEquipmentInventory().setInventorySlotContents(0, null);
        maid.getMaidEquipmentInventory().markDirty();
        maid.swingItem();
    }

    @Override
    public void onSelected(EntityMaid maid) {
        ensureMainhandShears(maid);
    }

    private boolean ensureMainhandShears(EntityMaid maid) {
        ItemStack held = maid.getMaidEquipmentInventory().getStackInSlot(0);
        if (held != null && held.getItem() == Items.shears) return true;
        int sourceSlot = maid.findInventorySlot(Items.shears);
        if (sourceSlot < 0) return false;
        ItemStack shears = maid.takeOneFromSlot(sourceSlot);
        if (shears == null) return false;
        maid.getMaidEquipmentInventory().setInventorySlotContents(0, shears);
        if (held != null) {
            ItemStack remaining = maid.addToMaidInventory(held);
            if (remaining != null) maid.entityDropItem(remaining, 0.0F);
        }
        maid.getMaidEquipmentInventory().markDirty();
        return true;
    }
}

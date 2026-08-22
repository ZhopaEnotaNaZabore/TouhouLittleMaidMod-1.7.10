package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.compat.LegacyHoneyCompat;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

/** 1.7 bee-less compatibility: tends nearby flowers and periodically produces honey. */
public final class TaskHoney implements IMaidTask {
    @Override public String getId() { return TaskManager.HONEY_ID; }

    @Override
    public void tick(EntityMaid maid) {
        if (maid.isSitting() || !maid.isPeriodicTick(600)) return;
        int centerX = (int) Math.floor(maid.posX);
        int centerY = (int) Math.floor(maid.posY);
        int centerZ = (int) Math.floor(maid.posZ);
        for (int dx = -6; dx <= 6; dx++) for (int dy = -2; dy <= 2; dy++) for (int dz = -6; dz <= 6; dz++) {
            int x = centerX + dx, y = centerY + dy, z = centerZ + dz;
            if (!maid.isPositionWithinRestriction(x + 0.5D, y, z + 0.5D)) continue;
            Block block = maid.worldObj.getBlock(x, y, z);
            if (block == Blocks.red_flower || block == Blocks.yellow_flower) {
                ItemStack left = maid.addToMaidInventory(LegacyHoneyCompat.createOutput());
                if (left != null) maid.entityDropItem(left, 0);
                maid.swingItem();
                return;
            }
        }
    }
}

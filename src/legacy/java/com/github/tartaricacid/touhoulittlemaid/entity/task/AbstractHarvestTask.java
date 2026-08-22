package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

abstract class AbstractHarvestTask implements IMaidTask {
    @Override
    public final void tick(EntityMaid maid) {
        if (maid.isSitting() || !maid.isPeriodicTick(20)) return;
        Target target = findNearest(maid);
        if (target == null) return;
        if (maid.getDistanceSq(target.x + 0.5D, target.y, target.z + 0.5D) > closeDistanceSq()) {
            maid.getNavigator().tryMoveToXYZ(target.x + 0.5D, target.y, target.z + 0.5D, 0.65D);
        } else {
            harvest(maid, target);
        }
    }

    protected double closeDistanceSq() {
        return 6.25D;
    }

    protected abstract boolean shouldHarvest(EntityMaid maid, int x, int y, int z, Block block, int metadata);

    protected void harvest(EntityMaid maid, Target target) {
        ArrayList<ItemStack> drops = target.block.getDrops(
                maid.worldObj, target.x, target.y, target.z, target.metadata, 0);
        maid.worldObj.setBlockToAir(target.x, target.y, target.z);
        playHarvestEffect(maid, target);
        insertDrops(maid, drops);
    }

    protected final void playHarvestEffect(EntityMaid maid, Target target) {
        maid.worldObj.playAuxSFX(2001, target.x, target.y, target.z,
                Block.getIdFromBlock(target.block) + (target.metadata << 12));
        maid.swingItem();
    }

    protected final void insertDrops(EntityMaid maid, ArrayList<ItemStack> drops) {
        for (ItemStack drop : drops) {
            ItemStack remaining = maid.addToMaidInventory(drop);
            if (remaining != null && remaining.stackSize > 0) maid.entityDropItem(remaining, 0.0F);
        }
    }

    private Target findNearest(EntityMaid maid) {
        int centerX = (int) Math.floor(maid.posX);
        int centerY = (int) Math.floor(maid.posY);
        int centerZ = (int) Math.floor(maid.posZ);
        Target nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (int x = centerX - 8; x <= centerX + 8; x++) {
            for (int y = centerY - 3; y <= centerY + 3; y++) {
                for (int z = centerZ - 8; z <= centerZ + 8; z++) {
                    if (!maid.isPositionWithinRestriction(x + 0.5D, y, z + 0.5D)) continue;
                    Block block = maid.worldObj.getBlock(x, y, z);
                    int metadata = maid.worldObj.getBlockMetadata(x, y, z);
                    if (!shouldHarvest(maid, x, y, z, block, metadata)) continue;
                    double distance = maid.getDistanceSq(x + 0.5D, y, z + 0.5D);
                    if (distance < nearestDistance) {
                        nearest = new Target(x, y, z, block, metadata);
                        nearestDistance = distance;
                    }
                }
            }
        }
        return nearest;
    }

    protected static final class Target {
        protected final int x;
        protected final int y;
        protected final int z;
        protected final Block block;
        protected final int metadata;

        private Target(int x, int y, int z, Block block, int metadata) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.block = block;
            this.metadata = metadata;
        }
    }
}

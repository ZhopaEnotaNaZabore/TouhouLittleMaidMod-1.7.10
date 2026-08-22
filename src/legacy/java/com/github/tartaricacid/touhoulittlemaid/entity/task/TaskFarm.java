package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

/** Harvests mature vanilla crops and immediately replants them. */
public final class TaskFarm implements IMaidTask {
    private static final int SEARCH_RANGE = 8;

    @Override
    public String getId() {
        return TaskManager.FARM_ID;
    }

    @Override
    public void tick(EntityMaid maid) {
        if (maid.isSitting() || !maid.isPeriodicTick(20)) return;
        CropTarget target = findNearestCrop(maid);
        if (target == null) return;

        double distance = maid.getDistanceSq(target.x + 0.5D, target.y, target.z + 0.5D);
        if (distance > 6.25D) {
            maid.getNavigator().tryMoveToXYZ(target.x + 0.5D, target.y, target.z + 0.5D, 0.65D);
            return;
        }
        harvestAndReplant(maid, target);
    }

    private CropTarget findNearestCrop(EntityMaid maid) {
        int centerX = (int) Math.floor(maid.posX);
        int centerY = (int) Math.floor(maid.posY);
        int centerZ = (int) Math.floor(maid.posZ);
        CropTarget nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (int x = centerX - SEARCH_RANGE; x <= centerX + SEARCH_RANGE; x++) {
            for (int y = centerY - 3; y <= centerY + 3; y++) {
                for (int z = centerZ - SEARCH_RANGE; z <= centerZ + SEARCH_RANGE; z++) {
                    if (!maid.isPositionWithinRestriction(x + 0.5D, y, z + 0.5D)) continue;
                    Block block = maid.worldObj.getBlock(x, y, z);
                    int metadata = maid.worldObj.getBlockMetadata(x, y, z);
                    if (!isMature(block, metadata)) continue;
                    double distance = maid.getDistanceSq(x + 0.5D, y, z + 0.5D);
                    if (distance < nearestDistance) {
                        nearest = new CropTarget(x, y, z, block, metadata);
                        nearestDistance = distance;
                    }
                }
            }
        }
        return nearest;
    }

    private boolean isMature(Block block, int metadata) {
        if (block == Blocks.nether_wart) return metadata >= 3;
        return block instanceof BlockCrops && metadata >= 7;
    }

    @SuppressWarnings("unchecked")
    private void harvestAndReplant(EntityMaid maid, CropTarget target) {
        if (!isMature(maid.worldObj.getBlock(target.x, target.y, target.z),
                maid.worldObj.getBlockMetadata(target.x, target.y, target.z))) return;

        ArrayList<ItemStack> drops = target.block.getDrops(
                maid.worldObj, target.x, target.y, target.z, target.metadata, 0);
        consumeReplantSeed(drops, getSeed(target.block));
        maid.worldObj.setBlockMetadataWithNotify(target.x, target.y, target.z, 0, 3);
        maid.worldObj.playAuxSFX(2001, target.x, target.y, target.z,
                Block.getIdFromBlock(target.block) + (target.metadata << 12));
        maid.swingItem();

        for (ItemStack drop : drops) {
            ItemStack remaining = maid.addToMaidInventory(drop);
            if (remaining != null && remaining.stackSize > 0) maid.entityDropItem(remaining, 0.0F);
        }
    }

    private Item getSeed(Block block) {
        if (block == Blocks.wheat) return Items.wheat_seeds;
        if (block == Blocks.carrots) return Items.carrot;
        if (block == Blocks.potatoes) return Items.potato;
        if (block == Blocks.nether_wart) return Items.nether_wart;
        return null;
    }

    private void consumeReplantSeed(ArrayList<ItemStack> drops, Item seed) {
        if (seed == null) return;
        for (int index = 0; index < drops.size(); index++) {
            ItemStack stack = drops.get(index);
            if (stack.getItem() == seed) {
                if (--stack.stackSize <= 0) drops.remove(index);
                return;
            }
        }
    }

    private static final class CropTarget {
        private final int x;
        private final int y;
        private final int z;
        private final Block block;
        private final int metadata;

        private CropTarget(int x, int y, int z, Block block, int metadata) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.block = block;
            this.metadata = metadata;
        }
    }
}

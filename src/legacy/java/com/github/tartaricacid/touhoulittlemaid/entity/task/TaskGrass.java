package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public final class TaskGrass extends AbstractHarvestTask {
    @Override
    public String getId() {
        return TaskManager.GRASS_ID;
    }

    @Override
    protected boolean shouldHarvest(EntityMaid maid, int x, int y, int z, Block block, int metadata) {
        return block instanceof BlockTallGrass || block instanceof BlockFlower
                || block instanceof BlockDoublePlant && (metadata & 8) == 0;
    }

    @Override
    protected void harvest(EntityMaid maid, Target target) {
        if (!(target.block instanceof BlockDoublePlant)) {
            super.harvest(maid, target);
            return;
        }
        ArrayList<ItemStack> drops = target.block.getDrops(
                maid.worldObj, target.x, target.y, target.z, target.metadata, 0);
        // Remove the lower half first: removing the top notifies and drops the lower half.
        if (!maid.worldObj.setBlockToAir(target.x, target.y, target.z)) return;
        if (maid.worldObj.getBlock(target.x, target.y + 1, target.z) == target.block)
            maid.worldObj.setBlockToAir(target.x, target.y + 1, target.z);
        playHarvestEffect(maid, target);
        insertDrops(maid, drops);
    }
}

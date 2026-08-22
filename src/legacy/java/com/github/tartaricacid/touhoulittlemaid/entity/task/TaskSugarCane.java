package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public final class TaskSugarCane extends AbstractHarvestTask {
    @Override
    public String getId() {
        return TaskManager.SUGAR_CANE_ID;
    }

    @Override
    protected boolean shouldHarvest(EntityMaid maid, int x, int y, int z, Block block, int metadata) {
        return block == Blocks.reeds && maid.worldObj.getBlock(x, y - 1, z) == Blocks.reeds
                && maid.worldObj.getBlock(x, y - 2, z) != Blocks.reeds;
    }

    @Override
    protected double closeDistanceSq() {
        return 4.0D;
    }
}

package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public final class TaskCocoa extends AbstractHarvestTask {
    @Override
    public String getId() {
        return TaskManager.COCOA_ID;
    }

    @Override
    protected boolean shouldHarvest(EntityMaid maid, int x, int y, int z, Block block, int metadata) {
        return block == Blocks.cocoa && ((metadata & 12) >> 2) >= 2;
    }

    @Override
    protected void harvest(EntityMaid maid, Target target) {
        ArrayList<ItemStack> drops = target.block.getDrops(
                maid.worldObj, target.x, target.y, target.z, target.metadata, 0);
        consumeCocoaBean(drops);
        maid.worldObj.setBlockMetadataWithNotify(target.x, target.y, target.z, target.metadata & 3, 3);
        playHarvestEffect(maid, target);
        insertDrops(maid, drops);
    }

    private void consumeCocoaBean(ArrayList<ItemStack> drops) {
        for (int index = 0; index < drops.size(); index++) {
            ItemStack stack = drops.get(index);
            if (stack.getItem() == Items.dye && stack.getItemDamage() == 3) {
                if (--stack.stackSize <= 0) drops.remove(index);
                return;
            }
        }
    }
}

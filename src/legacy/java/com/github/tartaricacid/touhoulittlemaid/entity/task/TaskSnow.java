package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSpade;

import java.util.ArrayList;

public final class TaskSnow extends AbstractHarvestTask {
    @Override
    public String getId() {
        return TaskManager.SNOW_ID;
    }

    @Override
    protected boolean shouldHarvest(EntityMaid maid, int x, int y, int z, Block block, int metadata) {
        return block == Blocks.snow_layer;
    }

    @Override
    protected void harvest(EntityMaid maid, Target target) {
        ItemStack held = maid.getMaidEquipmentInventory().getStackInSlot(0);
        boolean hasShovel = held != null && held.getItem() instanceof ItemSpade;
        ArrayList<ItemStack> drops = hasShovel
                ? target.block.getDrops(maid.worldObj, target.x, target.y, target.z, target.metadata, 0)
                : new ArrayList<ItemStack>();
        maid.worldObj.setBlockToAir(target.x, target.y, target.z);
        playHarvestEffect(maid, target);
        insertDrops(maid, drops);
        if (hasShovel) {
            held.damageItem(1, maid);
            if (held.stackSize <= 0) maid.getMaidEquipmentInventory().setInventorySlotContents(0, null);
            maid.getMaidEquipmentInventory().markDirty();
        }
    }
}

package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public final class TaskMelon extends AbstractHarvestTask {
    @Override
    public String getId() {
        return TaskManager.MELON_ID;
    }

    @Override
    protected boolean shouldHarvest(EntityMaid maid, int x, int y, int z, Block block, int metadata) {
        if (block == Blocks.melon_block) return hasAdjacent(maid, x, y, z, Blocks.melon_stem);
        if (block == Blocks.pumpkin) return hasAdjacent(maid, x, y, z, Blocks.pumpkin_stem);
        return false;
    }

    @Override
    protected double closeDistanceSq() {
        return 2.25D;
    }

    @Override
    protected void harvest(EntityMaid maid, Target target) {
        ItemStack held = maid.getMaidEquipmentInventory().getStackInSlot(0);
        boolean silkMelon = target.block == Blocks.melon_block && held != null
                && EnchantmentHelper.getEnchantmentLevel(Enchantment.silkTouch.effectId, held) > 0;
        if (!silkMelon) {
            super.harvest(maid, target);
            return;
        }
        ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        drops.add(new ItemStack(Blocks.melon_block));
        maid.worldObj.setBlockToAir(target.x, target.y, target.z);
        playHarvestEffect(maid, target);
        insertDrops(maid, drops);
        held.damageItem(1, maid);
        if (held.stackSize <= 0) maid.getMaidEquipmentInventory().setInventorySlotContents(0, null);
        maid.getMaidEquipmentInventory().markDirty();
    }

    private boolean hasAdjacent(EntityMaid maid, int x, int y, int z, Block stem) {
        return maid.worldObj.getBlock(x + 1, y, z) == stem || maid.worldObj.getBlock(x - 1, y, z) == stem
                || maid.worldObj.getBlock(x, y, z + 1) == stem || maid.worldObj.getBlock(x, y, z - 1) == stem;
    }
}

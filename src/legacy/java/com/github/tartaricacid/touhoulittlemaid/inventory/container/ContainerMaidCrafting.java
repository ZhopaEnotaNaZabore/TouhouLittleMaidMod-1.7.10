package com.github.tartaricacid.touhoulittlemaid.inventory.container;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerWorkbench;

/** Uses vanilla crafting/recipe/remainder handling, but anchors access to the maid. */
public final class ContainerMaidCrafting extends ContainerWorkbench {
    private final EntityMaid maid;
    public ContainerMaidCrafting(InventoryPlayer inventory, EntityMaid maid) {
        super(inventory, maid.worldObj, (int)maid.posX, (int)maid.posY, (int)maid.posZ);
        this.maid = maid;
    }
    @Override public boolean canInteractWith(EntityPlayer player) {
        return maid.isEntityAlive() && maid.getOwner() == player && maid.getDistanceSqToEntity(player) < 64
                && "crafting_table_backpack".equals(maid.getBackpackType());
    }
}

package com.github.tartaricacid.touhoulittlemaid.inventory.container;

import com.github.tartaricacid.touhoulittlemaid.init.ModBlocks;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityModelSwitcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

public final class ContainerModelSwitcher extends Container {
    private final TileEntityModelSwitcher switcher;
    public ContainerModelSwitcher(TileEntityModelSwitcher switcher) { this.switcher = switcher; }
    public TileEntityModelSwitcher getSwitcher() { return switcher; }
    @Override public boolean canInteractWith(EntityPlayer player) {
        return switcher.getWorldObj() != null
                && switcher.getWorldObj().getBlock(switcher.xCoord, switcher.yCoord, switcher.zCoord) == ModBlocks.MODEL_SWITCHER
                && switcher.isOwnedBy(player)
                && player.getDistanceSq(switcher.xCoord + .5D, switcher.yCoord + .5D, switcher.zCoord + .5D) < 64.0D;
    }
    @Override public ItemStack transferStackInSlot(EntityPlayer player, int index) { return null; }
}

package com.github.tartaricacid.touhoulittlemaid.inventory.container;

import com.github.tartaricacid.touhoulittlemaid.init.ModBlocks;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityMaidBeacon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

public final class ContainerMaidBeacon extends Container {
    private final TileEntityMaidBeacon beacon;
    public ContainerMaidBeacon(TileEntityMaidBeacon beacon){this.beacon=beacon;}
    public TileEntityMaidBeacon getBeacon(){return beacon;}
    @Override public boolean canInteractWith(EntityPlayer player){return beacon.getWorldObj()!=null
            &&beacon.getWorldObj().getBlock(beacon.xCoord,beacon.yCoord,beacon.zCoord)==ModBlocks.MAID_BEACON
            &&player.getDistanceSq(beacon.xCoord+.5D,beacon.yCoord+.5D,beacon.zCoord+.5D)<64;}
    @Override public ItemStack transferStackInSlot(EntityPlayer player,int slot){return null;}
}

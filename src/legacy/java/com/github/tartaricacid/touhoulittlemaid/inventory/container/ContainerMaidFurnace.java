package com.github.tartaricacid.touhoulittlemaid.inventory.container;
import com.github.tartaricacid.touhoulittlemaid.inventory.MaidFurnaceInventory;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.Slot;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
public final class ContainerMaidFurnace extends ContainerFurnace {
    public ContainerMaidFurnace(InventoryPlayer player,MaidFurnaceInventory furnace){
        super(player,furnace);
        // SRC awards smelting experience to the maid, not again when the player removes output.
        Slot output=new Slot(furnace,2,116,35){@Override public boolean isItemValid(ItemStack stack){return false;}};
        output.slotNumber=2;inventorySlots.set(2,output);
    }
}

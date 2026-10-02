package com.github.tartaricacid.touhoulittlemaid.test;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.touhoulittlemaid.init.ModItems;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.ContainerMaidBauble;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.ContainerMaidCrafting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** World-backed probes; never spawned, no AI ticks and no player inventory writes. */
public final class MaidUiVerification {
    private MaidUiVerification() { }
    public static void run(EntityPlayer player) {
        EntityMaid maid = new EntityMaid(player.worldObj);
        maid.setTamed(true);
        maid.func_152115_b(player.getUniqueID().toString());
        maid.setPosition(player.posX, player.posY, player.posZ);
        int[] points = {0,32,63,64,128,191,192,288,383,384};
        double[] percent = {0,.5,63.0/64,0,.5,127.0/128,0,.5,191.0/192,0};
        ContainerMaidBauble baubles = new ContainerMaidBauble(player.inventory, maid);
        int slotCount = baubles.inventorySlots.size();
        for (int i=0;i<points.length;i++) {
            maid.setFavorability(points[i]);
            check(Math.abs(maid.getFavorabilityManager().getLevelPercent()-percent[i])<0.000001,"favorability " + points[i]);
            check(baubles.inventorySlots.size()==slotCount,"unstable slot IDs");
            check(baubles.getSlot(10).isItemValid(new ItemStack(ModItems.MUTE_BAUBLE))==(points[i]>=192),"bauble gate");
            check(baubles.getSlot(20).isItemValid(new ItemStack(ModItems.WIRELESS_IO))==(points[i]>=384),"wireless gate");
        }
        maid.setFavorability(0);
        maid.getMaidBaubleInventory().setInventorySlotContents(20,new ItemStack(ModItems.MUTE_BAUBLE));
        check(!maid.isMuted(),"locked bauble is active");
        check(baubles.getSlot(20).canTakeStack(player),"legacy locked item cannot be recovered");
        EntityMaid synced = new EntityMaid(player.worldObj) {
            @Override public String getBackpackType() { return "maid_backpack_big"; }
        };
        check(synced.getBackpackCapacity()==36,"capacity bypasses synced accessor");
        maid.setTaskId(TaskManager.FARM_ID);
        NBTTagCompound saved = new NBTTagCompound();
        maid.writeToNBT(saved);
        saved.setString(EntityMaid.MAID_BACKPACK_TYPE,"crafting_table_backpack");
        EntityMaid restored = new EntityMaid(player.worldObj);
        restored.readFromNBT(saved);
        check(TaskManager.getByIndex(restored.getDataWatcher().getWatchableObjectInt(26)).getId().equals(TaskManager.FARM_ID),"loaded task watcher");
        ContainerMaidCrafting crafting = new ContainerMaidCrafting(player.inventory,restored);
        check(crafting.canInteractWith(player),"portable workbench needs a block");
        restored.setPosition(player.posX+9,player.posY,player.posZ);
        check(!crafting.canInteractWith(player),"portable workbench distance");
    }
    private static void check(boolean condition,String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}

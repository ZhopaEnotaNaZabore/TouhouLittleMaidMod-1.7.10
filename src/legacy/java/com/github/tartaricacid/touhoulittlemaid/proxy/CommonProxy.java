package com.github.tartaricacid.touhoulittlemaid.proxy;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.ContainerMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.ContainerMaidBauble;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.ContainerMaidEquipment;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.ContainerMaidTask;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.ContainerModelSwitcher;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityModelSwitcher;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityMaidBeacon;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.ContainerMaidBeacon;

public class CommonProxy implements IGuiHandler {
    public static final int MAID_CRAFTING_GUI_ID = 6;
    public static final int MAID_GUI_ID = 0;
    public static final int MAID_BAUBLE_GUI_ID = 1;
    public static final int MAID_EQUIPMENT_GUI_ID = 2;
    public static final int MAID_TASK_GUI_ID = 3;
    public static final int MODEL_SWITCHER_GUI_ID = 4;
    public static final int MAID_BEACON_GUI_ID = 5;
    public void preInit(FMLPreInitializationEvent event) {
    }

    public void init(FMLInitializationEvent event) {
    }

    public void postInit(FMLPostInitializationEvent event) {
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == MODEL_SWITCHER_GUI_ID && world.getTileEntity(x,y,z) instanceof TileEntityModelSwitcher) {
            TileEntityModelSwitcher switcher=(TileEntityModelSwitcher)world.getTileEntity(x,y,z);
            if(switcher.isOwnedBy(player))return new ContainerModelSwitcher(switcher);
        }
        if(id==MAID_BEACON_GUI_ID&&world.getTileEntity(x,y,z) instanceof TileEntityMaidBeacon)return new ContainerMaidBeacon((TileEntityMaidBeacon)world.getTileEntity(x,y,z));
        Entity entity = world.getEntityByID(x);
        if (id == MAID_CRAFTING_GUI_ID && entity instanceof EntityMaid && ((EntityMaid)entity).getOwner() == player && entity.getDistanceSqToEntity(player) < 64) return new com.github.tartaricacid.touhoulittlemaid.inventory.container.ContainerMaidCrafting(player.inventory,(EntityMaid)entity);
        if (id == MAID_GUI_ID && entity instanceof EntityMaid && ((EntityMaid) entity).getOwner() == player) {
            return new ContainerMaid(player.inventory, (EntityMaid) entity);
        }
        if (id == MAID_BAUBLE_GUI_ID && entity instanceof EntityMaid && ((EntityMaid) entity).getOwner() == player) return new ContainerMaidBauble(player.inventory,(EntityMaid)entity);
        if (id == MAID_EQUIPMENT_GUI_ID && entity instanceof EntityMaid && ((EntityMaid) entity).getOwner() == player) return new ContainerMaidEquipment(player.inventory,(EntityMaid)entity);
        if (id == MAID_TASK_GUI_ID && entity instanceof EntityMaid && ((EntityMaid) entity).getOwner() == player) return new ContainerMaidTask(player.inventory,(EntityMaid)entity);
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }
}

package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityBox;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public final class ItemSpawnBox extends Item {
    public ItemSpawnBox() {
        setUnlocalizedName(TouhouLittleMaid.MOD_ID + ".spawn_box");
        setTextureName(TouhouLittleMaid.MOD_ID + ":spawn_box");
        setMaxStackSize(16);
        setCreativeTab(CreativeTabs.tabMisc);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
                             int side, float hitX, float hitY, float hitZ) {
        if (side == 0) return false;
        if (side == 1) y++; else if (side == 2) z--; else if (side == 3) z++;
        else if (side == 4) x--; else if (side == 5) x++;
        if (!world.isAirBlock(x, y, z)) return false;
        if (!world.isRemote) {
            EntityBox box=new EntityBox(world,x+.5D,y,z+.5D);
            world.spawnEntityInWorld(box);
            com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid maid=
                    new com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid(world);
            maid.onSpawnWithEgg(null);
            maid.setPosition(x+.5D,y,z+.5D);
            world.spawnEntityInWorld(maid);
            maid.mountEntity(box);
        }
        if (!player.capabilities.isCreativeMode) --stack.stackSize;
        return true;
    }
}

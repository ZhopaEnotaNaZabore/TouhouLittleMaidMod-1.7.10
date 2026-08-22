package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityBroom;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public final class ItemBroom extends Item {
    public ItemBroom() {
        setUnlocalizedName(TouhouLittleMaid.MOD_ID + ".broom");
        setTextureName(TouhouLittleMaid.MOD_ID + ":broom");
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.tabTransport);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
                             int side, float hitX, float hitY, float hitZ) {
        if (side == 0) return false;
        if (side == 1) y++; else if (side == 2) z--; else if (side == 3) z++;
        else if (side == 4) x--; else if (side == 5) x++;
        if (!world.isAirBlock(x, y, z)) return false;
        if (!world.isRemote) {
            EntityBroom broom = new EntityBroom(world, x + 0.5D, y + 0.25D, z + 0.5D);
            broom.setOwner(player);
            broom.rotationYaw = player.rotationYaw;
            world.spawnEntityInWorld(broom);
            world.playSoundAtEntity(broom, "dig.cloth", 0.75F, 0.8F);
        }
        if (!player.capabilities.isCreativeMode) --stack.stackSize;
        return true;
    }
}

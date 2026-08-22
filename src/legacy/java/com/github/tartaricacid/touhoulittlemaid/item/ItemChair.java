package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityChair;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public final class ItemChair extends Item {
    public ItemChair() {
        setUnlocalizedName(TouhouLittleMaid.MOD_ID + ".chair");
        setTextureName(TouhouLittleMaid.MOD_ID + ":chair_show");
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.tabDecorations);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
                             int side, float hitX, float hitY, float hitZ) {
        if (side == 0) return false;
        if (side == 1) y++; else if (side == 2) z--; else if (side == 3) z++;
        else if (side == 4) x--; else if (side == 5) x++;
        if (!player.canPlayerEdit(x, y, z, side, stack) || !world.isAirBlock(x, y, z)) return false;
        if (!world.isRemote) {
            float yaw = MathHelper.floor_double((player.rotationYaw * 8.0F / 360.0F) + 0.5D) * 45.0F;
            EntityChair chair = new EntityChair(world, x + 0.5D, y, z + 0.5D, yaw);
            chair.setOwner(player);
            if (stack.hasTagCompound()) chair.applyItemData(stack.getTagCompound());
            world.spawnEntityInWorld(chair);
            world.playSoundAtEntity(chair, "dig.cloth", 0.75F, 0.8F);
        }
        if (!player.capabilities.isCreativeMode) --stack.stackSize;
        return true;
    }
}

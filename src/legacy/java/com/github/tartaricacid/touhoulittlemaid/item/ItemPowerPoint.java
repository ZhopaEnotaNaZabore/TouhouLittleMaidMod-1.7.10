package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.projectile.EntityThrowPowerPoint;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Item form of Power. Right-clicking converts one item into a collectible orb. */
public final class ItemPowerPoint extends Item {
    public ItemPowerPoint() {
        setUnlocalizedName(TouhouLittleMaid.MOD_ID + ".power_point");
        setTextureName(TouhouLittleMaid.MOD_ID + ":power_point");
        setMaxStackSize(64);
        setCreativeTab(CreativeTabs.tabMisc);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        world.playSoundAtEntity(player, "random.bow", 0.5F, 0.8F + itemRand.nextFloat() * 0.4F);
        if (!world.isRemote) {
            world.spawnEntityInWorld(new EntityThrowPowerPoint(world, player));
        }
        if (!player.capabilities.isCreativeMode) {
            --stack.stackSize;
        }
        return stack;
    }
}

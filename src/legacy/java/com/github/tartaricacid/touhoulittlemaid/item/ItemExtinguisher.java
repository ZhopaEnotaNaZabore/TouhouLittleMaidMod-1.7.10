package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

public final class ItemExtinguisher extends Item {
    public ItemExtinguisher() {
        setUnlocalizedName(TouhouLittleMaid.MOD_ID + ".extinguisher");
        setTextureName(TouhouLittleMaid.MOD_ID + ":extinguisher");
        setCreativeTab(CreativeTabs.tabTools);
        setMaxStackSize(1);
        setMaxDamage(128);
        setNoRepair();
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote) {
            player.addChatMessage(new ChatComponentTranslation(
                    "message.touhou_little_maid.extinguisher.player_cannot_use"));
        }
        return stack;
    }
}

package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public final class ItemTrumpet extends Item {
    public ItemTrumpet() { setUnlocalizedName(TouhouLittleMaid.MOD_ID + ".trumpet"); setTextureName(TouhouLittleMaid.MOD_ID + ":trumpet"); setMaxStackSize(1); setCreativeTab(CreativeTabs.tabTools); }
    @Override public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            @SuppressWarnings("unchecked") java.util.List<Entity> entities = world.loadedEntityList;
            int offset = 0; for (Entity entity : entities) if (entity instanceof EntityMaid && ((EntityMaid) entity).getOwner() == player) {
                EntityMaid maid = (EntityMaid) entity; maid.setHomeMode(false); maid.setMaidSitting(false);
                maid.setPositionAndUpdate(player.posX + (offset++ % 3) - 1, player.posY, player.posZ + (offset % 3) - 1);
            }
            world.playSoundAtEntity(player, "note.harp", 2, 0.6F);
        }
        return stack;
    }
}

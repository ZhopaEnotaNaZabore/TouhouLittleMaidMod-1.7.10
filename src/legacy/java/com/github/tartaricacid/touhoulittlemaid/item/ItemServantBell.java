package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public final class ItemServantBell extends Item {
    public ItemServantBell() { setUnlocalizedName(TouhouLittleMaid.MOD_ID + ".servant_bell"); setTextureName(TouhouLittleMaid.MOD_ID + ":servant_bell"); setMaxStackSize(1); setCreativeTab(CreativeTabs.tabTools); }
    @Override public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target) {
        if (!(target instanceof EntityMaid) || ((EntityMaid) target).getOwner() != player) return false;
        NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound(); tag.setString("ServantBellUuid", target.getUniqueID().toString()); tag.setString("ServantBellTip", target.getCommandSenderName()); stack.setTagCompound(tag); return true;
    }
    @Override public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            String id = stack.hasTagCompound() ? stack.getTagCompound().getString("ServantBellUuid") : ""; EntityMaid found = null;
            @SuppressWarnings("unchecked") java.util.List<Entity> entities = world.loadedEntityList;
            for (Entity entity : entities) if (entity instanceof EntityMaid && entity.getUniqueID().toString().equals(id) && ((EntityMaid) entity).getOwner() == player) { found = (EntityMaid) entity; break; }
            if (found != null) { found.setHomeMode(false); found.setMaidSitting(false); found.safeTeleportNear(player); }
            else player.addChatMessage(new net.minecraft.util.ChatComponentTranslation(id.isEmpty() ? "message.touhou_little_maid.bell_unbound" : "message.touhou_little_maid.bell_unloaded"));
            world.playSoundAtEntity(player, "random.orb", 1, 0.7F);
        }
        return stack;
    }
    @Override public boolean hasEffect(ItemStack stack, int pass) { return stack.hasTagCompound(); }
}

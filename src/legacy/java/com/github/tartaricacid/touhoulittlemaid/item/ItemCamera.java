package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** Converts an owned maid to a persistent photo, matching the modern transport flow. */
public final class ItemCamera extends Item {
    public static final String MAID_INFO = "MaidInfo";
    public ItemCamera() { setUnlocalizedName(TouhouLittleMaid.MOD_ID + ".camera"); setTextureName(TouhouLittleMaid.MOD_ID + ":camera"); setMaxStackSize(1); setMaxDamage(50); setCreativeTab(CreativeTabs.tabTools); }
    @Override public boolean itemInteractionForEntity(ItemStack camera, EntityPlayer player, EntityLivingBase target) {
        if (!(target instanceof EntityMaid)) return false; EntityMaid maid = (EntityMaid) target;
        if (!maid.isTamed() || maid.getOwner() != player) return false;
        if (!player.worldObj.isRemote) {
            NBTTagCompound data = new NBTTagCompound(); maid.writeToNBT(data); data.setString("id", TouhouLittleMaid.MOD_ID + ":maid");
            ItemStack photo = new ItemStack(ModItems.PHOTO); NBTTagCompound root = new NBTTagCompound(); root.setTag(MAID_INFO, data); photo.setTagCompound(root);
            if (!player.inventory.addItemStackToInventory(photo)) player.entityDropItem(photo, 0.0F);
            maid.setDead(); camera.damageItem(1, player); player.worldObj.playSoundAtEntity(player, "random.click", 1, 1.5F);
        }
        return true;
    }
}

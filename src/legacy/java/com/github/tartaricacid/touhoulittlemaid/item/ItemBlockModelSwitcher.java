package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityModelSwitcher;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;

/** Persistent ItemBlock counterpart of the source ItemModelSwitcher. */
public final class ItemBlockModelSwitcher extends ItemBlock {
    public static final String STORAGE = "StorageData";

    public ItemBlockModelSwitcher(Block block) { super(block); setMaxStackSize(1); }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target) {
        if (!(target instanceof EntityMaid)) return false;
        EntityMaid maid = (EntityMaid) target;
        if (maid.getOwner() != player) return false;
        if (!player.worldObj.isRemote) {
            NBTTagCompound root = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
            NBTTagCompound data = root.hasKey(STORAGE, 10)
                    ? root.getCompoundTag(STORAGE) : new NBTTagCompound();
            NBTTagCompound fields = data.hasKey("ForgeData", 10) ? data.getCompoundTag("ForgeData") : data;
            fields.setString("entity_uuid", maid.getUniqueID().toString());
            fields.setString("owner_uuid", player.getUniqueID().toString());
            if (fields != data) data.setTag("ForgeData", fields);
            root.setTag(STORAGE, data); stack.setTagCompound(root);
            player.addChatMessage(new ChatComponentText("Model Switcher bound to maid"));
        }
        return true;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override public void addInformation(ItemStack stack, EntityPlayer player, java.util.List lines, boolean advanced) {
        NBTTagCompound root = stack.getTagCompound();
        if (root == null || !root.hasKey(STORAGE, 10)) {
            lines.add("\u00a74Not bound to a maid");
            return;
        }
        NBTTagCompound data = root.getCompoundTag(STORAGE);
        if (data.hasKey("ForgeData", 10)) data = data.getCompoundTag("ForgeData");
        boolean bound = data.hasKey("entity_uuid", 8) || data.hasKey("entity_uuid", 11);
        lines.add(bound ? "\u00a77Bound to maid" : "\u00a74Not bound to a maid");
    }
}

package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import com.github.tartaricacid.touhoulittlemaid.world.MaidWorldIndex;
import net.minecraft.util.ChatComponentText;

public final class ItemPhoto extends Item {
    public ItemPhoto() { setUnlocalizedName(TouhouLittleMaid.MOD_ID + ".photo"); setTextureName(TouhouLittleMaid.MOD_ID + ":photo"); setMaxStackSize(1); setCreativeTab(CreativeTabs.tabMisc); }
    @Override public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hx, float hy, float hz) {
        if (side != 1 || stack.getTagCompound() == null || !stack.getTagCompound().hasKey(ItemCamera.MAID_INFO, 10)) return false;
        if (!world.isRemote) {
            net.minecraft.nbt.NBTTagCompound data=stack.getTagCompound().getCompoundTag(ItemCamera.MAID_INFO);if(!MaidWorldIndex.canRestore(data)){player.addChatMessage(new net.minecraft.util.ChatComponentTranslation("message.touhou_little_maid.already_loaded"));return true;}
            EntityMaid maid = new EntityMaid(world); maid.readFromNBT(data);
            maid.setPosition(x + hx, y + 1, z + hz); maid.setHealth(maid.getMaxHealth()); world.spawnEntityInWorld(maid);
            if (!player.capabilities.isCreativeMode) --stack.stackSize; world.playSoundAtEntity(maid, "random.pop", 1, 1);
        }
        return true;
    }
    @Override public boolean hasEffect(ItemStack stack, int pass) { return stack.hasTagCompound(); }
}

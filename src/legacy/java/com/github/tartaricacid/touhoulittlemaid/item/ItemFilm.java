package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import com.github.tartaricacid.touhoulittlemaid.world.MaidWorldIndex;
import com.github.tartaricacid.touhoulittlemaid.init.ModAchievements;

/** Serialized maid identity used by tombstones and the shrine resurrection flow. */
public final class ItemFilm extends Item {
    private static final String MAID_INFO = "MaidInfo";

    public ItemFilm() {
        setUnlocalizedName(TouhouLittleMaid.MOD_ID + ".film");
        setTextureName(TouhouLittleMaid.MOD_ID + ":film");
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.tabMisc);
    }

    public static ItemStack maidToFilm(EntityMaid maid) {
        ItemStack film = new ItemStack(com.github.tartaricacid.touhoulittlemaid.init.ModItems.FILM);
        NBTTagCompound root = new NBTTagCompound();
        NBTTagCompound data = new NBTTagCompound();
        maid.writeToNBT(data);
        data.removeTag(EntityMaid.MAID_INVENTORY_TAG);
        data.removeTag(EntityMaid.EXPERIENCE_TAG);
        data.removeTag(EntityMaid.MAID_BAUBLE_INVENTORY_TAG);
        data.removeTag(EntityMaid.MAID_EQUIPMENT_INVENTORY_TAG);
        data.removeTag(EntityMaid.MAID_HIDE_INVENTORY_TAG);
        data.removeTag(EntityMaid.MAID_TASK_INVENTORY_TAG);
        data.removeTag(EntityMaid.MAID_BACKPACK_TYPE);
        data.removeTag("MaidFurnace");
        data.removeTag("MaidTankItems");
        data.removeTag("MaidBackpackFluid");
        data.removeTag("MaidBackpackFluidAmount");
        data.removeTag("MaidBackpackData");
        data.removeTag("Pos"); data.removeTag("Motion"); data.removeTag("Rotation");
        data.removeTag("Health"); data.removeTag("HurtTime"); data.removeTag("DeathTime");
        data.setString("id", TouhouLittleMaid.MOD_ID + ":maid");
        root.setTag(MAID_INFO, data);
        film.setTagCompound(root);
        return film;
    }

    public static boolean hasMaidData(ItemStack film) {
        return film != null && film.getItem() instanceof ItemFilm && film.hasTagCompound()
                && film.getTagCompound().hasKey(MAID_INFO, 10);
    }

    public static boolean filmToMaid(ItemStack film, World world, int x, int y, int z, EntityPlayer player) {
        if (!hasMaidData(film)) return false;
        if (!world.isRemote) {
            if(!canRestore(film)){player.addChatMessage(new net.minecraft.util.ChatComponentTranslation("message.touhou_little_maid.already_loaded"));return false;}
            EntityMaid maid = new EntityMaid(world);
            maid.readFromNBT(film.getTagCompound().getCompoundTag(MAID_INFO));
            maid.setPosition(x + 0.5D, y, z + 0.5D);
            maid.setHealth(maid.getMaxHealth());
            if (!world.spawnEntityInWorld(maid)) return false;
            world.playSoundAtEntity(maid, "random.levelup", 1.0F, 1.0F);
            player.triggerAchievement(ModAchievements.RESURRECT);
            --film.stackSize;
        }
        return true;
    }
    public static boolean canRestore(ItemStack film){return hasMaidData(film)&&MaidWorldIndex.canRestore(film.getTagCompound().getCompoundTag(MAID_INFO));}

    @Override
    public boolean hasEffect(ItemStack stack, int pass) { return hasMaidData(stack); }
}

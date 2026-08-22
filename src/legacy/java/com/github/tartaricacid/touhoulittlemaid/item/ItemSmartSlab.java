package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.ModItems;
import com.github.tartaricacid.touhoulittlemaid.world.MaidWorldIndex;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

/** Three-state, owner-safe maid carrier using the stable full maid NBT schema. */
public final class ItemSmartSlab extends Item {
    public enum Type { INIT, EMPTY, HAS_MAID }
    private static final String MAID_INFO = "MaidInfo";
    private final Type type;

    public ItemSmartSlab(String name, Type type) {
        this.type = type;
        setUnlocalizedName(TouhouLittleMaid.MOD_ID + ".smart_slab");
        setTextureName(TouhouLittleMaid.MOD_ID + ":" + name);
        setMaxStackSize(1);
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target) {
        if (type != Type.EMPTY || !(target instanceof EntityMaid)) return false;
        EntityMaid maid = (EntityMaid) target;
        if (maid.getOwner() != player || maid.isDead) return true;
        if (!player.worldObj.isRemote) {
            ItemStack stored = new ItemStack(ModItems.SMART_SLAB_HAS_MAID);
            NBTTagCompound root = new NBTTagCompound(), data = new NBTTagCompound();
            maid.writeToNBT(data); root.setTag(MAID_INFO, data); stored.setTagCompound(root);
            player.inventory.setInventorySlotContents(player.inventory.currentItem, stored);
            maid.setDead();
            player.worldObj.playSoundAtEntity(player, "random.splash", 1.0F, 1.0F);
        }
        return true;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
                             int side, float hitX, float hitY, float hitZ) {
        if (side != 1 || type == Type.EMPTY) return false;
        if (!world.isRemote) {
            EntityMaid maid = new EntityMaid(world);
            if (type == Type.INIT) {
                if (!maid.canOwnerAddMaid(player)) {
                    player.addChatMessage(new ChatComponentText("Maid owner limit reached")); return true;
                }
                maid.setTamed(true); maid.func_152115_b(player.getUniqueID().toString());
            } else {
                if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey(MAID_INFO, 10)) return true;
                NBTTagCompound data = stack.getTagCompound().getCompoundTag(MAID_INFO);
                if (!player.getUniqueID().toString().equals(data.getString("OwnerUUID"))
                        && !player.getUniqueID().toString().equals(data.getString("Owner"))) {
                    player.addChatMessage(new ChatComponentText("This slab belongs to another owner")); return true;
                }
                if (!MaidWorldIndex.canRestore(data)) {
                    player.addChatMessage(new ChatComponentText("This maid is already loaded")); return true;
                }
                maid.readFromNBT(data);
            }
            maid.setPosition(x + .5D, y + 1D, z + .5D); world.spawnEntityInWorld(maid);
            player.inventory.setInventorySlotContents(player.inventory.currentItem, new ItemStack(ModItems.SMART_SLAB_EMPTY));
            world.playSoundAtEntity(maid, "random.splash", 1.0F, 1.0F);
        }
        return true;
    }

    @Override public boolean hasEffect(ItemStack stack, int pass) { return type != Type.EMPTY; }
}

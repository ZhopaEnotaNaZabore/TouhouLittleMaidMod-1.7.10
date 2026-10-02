package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public final class ItemEntityIdCopy extends Item {
    public ItemEntityIdCopy(){setUnlocalizedName(TouhouLittleMaid.MOD_ID+".entity_id_copy");setTextureName(TouhouLittleMaid.MOD_ID+":entity_id_copy");setMaxStackSize(1);}
    @Override public boolean itemInteractionForEntity(ItemStack stack,EntityPlayer player,EntityLivingBase target){if(!player.worldObj.isRemote){String id=EntityList.getEntityString(target);NBTTagCompound tag=stack.hasTagCompound()?stack.getTagCompound():new NBTTagCompound();tag.setString("EntityId",id==null?target.getClass().getName():id);stack.setTagCompound(tag);player.addChatMessage(new net.minecraft.util.ChatComponentTranslation("message.touhou_little_maid.entity_id",tag.getString("EntityId")));}return true;}
    @Override public boolean hasEffect(ItemStack stack,int pass){return stack.hasTagCompound()&&stack.getTagCompound().hasKey("EntityId");}
}

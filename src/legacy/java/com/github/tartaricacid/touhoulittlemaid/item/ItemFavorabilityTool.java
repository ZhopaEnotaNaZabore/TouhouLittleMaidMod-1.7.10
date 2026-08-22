package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class ItemFavorabilityTool extends Item {
    public enum Type { ADD, REDUCE, FULL }
    private final Type type;
    public ItemFavorabilityTool(String name, Type type){this.type=type;setUnlocalizedName(TouhouLittleMaid.MOD_ID+"."+name);setTextureName(TouhouLittleMaid.MOD_ID+":"+name);setMaxStackSize(1);}
    @Override public boolean itemInteractionForEntity(ItemStack stack,EntityPlayer player,EntityLivingBase target){if(!(target instanceof EntityMaid)||((EntityMaid)target).getOwner()!=player)return false;if(!player.worldObj.isRemote){EntityMaid maid=(EntityMaid)target;if(type==Type.FULL)maid.getFavorabilityManager().add(384-maid.getFavorability());else maid.getFavorabilityManager().add(type==Type.ADD?16:-16);}return true;}
    @Override public boolean hasEffect(ItemStack stack,int pass){return true;}
}

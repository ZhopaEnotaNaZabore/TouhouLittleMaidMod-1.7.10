package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.monster.EntityFairy;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public final class ItemLegacySpawnEgg extends Item {
    public enum Type { MAID, FAIRY }
    private final Type type;
    public ItemLegacySpawnEgg(String name,Type type){this.type=type;setUnlocalizedName(TouhouLittleMaid.MOD_ID+"."+name);setTextureName(TouhouLittleMaid.MOD_ID+":"+name);}
    @Override public boolean onItemUse(ItemStack stack,EntityPlayer player,World world,int x,int y,int z,int side,float hx,float hy,float hz){if(world.isRemote)return true;EntityLiving entity=type==Type.MAID?new EntityMaid(world):new EntityFairy(world);entity.setLocationAndAngles(x+hx,y+hy+(side==1?1:0),z+hz,world.rand.nextFloat()*360,0);entity.onSpawnWithEgg(null);if(stack.hasDisplayName())entity.setCustomNameTag(stack.getDisplayName());world.spawnEntityInWorld(entity);if(!player.capabilities.isCreativeMode)--stack.stackSize;return true;}
}

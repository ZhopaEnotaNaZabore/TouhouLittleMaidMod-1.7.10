package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.SchedulePos;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

/** Records work/idle/rest points in order and applies them to an owned maid. */
public final class ItemKappaCompass extends Item {
    private static final String DATA="KappaCompassData";
    public ItemKappaCompass(){setUnlocalizedName(TouhouLittleMaid.MOD_ID+".kappa_compass");setTextureName(TouhouLittleMaid.MOD_ID+":kappa_compass");setMaxStackSize(1);}
    @Override public boolean onItemUse(ItemStack stack,EntityPlayer player,World world,int x,int y,int z,int side,float hx,float hy,float hz){if(world.isRemote)return true;if(player.isSneaking()){if(stack.hasTagCompound())stack.getTagCompound().removeTag(DATA);player.addChatMessage(new ChatComponentText("Kappa Compass cleared"));return true;}NBTTagCompound root=stack.hasTagCompound()?stack.getTagCompound():new NBTTagCompound();NBTTagCompound data=root.hasKey(DATA,10)?root.getCompoundTag(DATA):new NBTTagCompound();int count=data.getInteger("Count");if(count>0&&data.getInteger("DimensionId")!=player.dimension){player.addChatMessage(new ChatComponentText("All compass points must be in one dimension"));return true;}if(count>=3){player.addChatMessage(new ChatComponentText("Kappa Compass already has three points"));return true;}String key=count==0?"Work":count==1?"Idle":"Sleep";NBTTagCompound point=new NBTTagCompound();point.setInteger("X",x);point.setInteger("Y",y);point.setInteger("Z",z);data.setTag(key,point);data.setInteger("Count",count+1);data.setInteger("DimensionId",player.dimension);root.setTag(DATA,data);stack.setTagCompound(root);player.addChatMessage(new ChatComponentText("Kappa Compass "+key+": "+x+", "+y+", "+z));world.playSoundAtEntity(player,"random.orb",.8F,1.5F);return true;}
    @Override public boolean itemInteractionForEntity(ItemStack stack,EntityPlayer player,EntityLivingBase target){if(!(target instanceof EntityMaid)||((EntityMaid)target).getOwner()!=player)return false;if(player.worldObj.isRemote)return true;EntityMaid maid=(EntityMaid)target;if(player.isSneaking()){maid.getSchedulePos().clear(maid);maid.setHomeMode(false);player.addChatMessage(new ChatComponentText("Maid home points cleared"));return true;}if(!stack.hasTagCompound()||!stack.getTagCompound().hasKey(DATA,10)){player.addChatMessage(new ChatComponentText("Kappa Compass has no points"));return true;}NBTTagCompound data=stack.getTagCompound().getCompoundTag(DATA);if(data.getInteger("DimensionId")!=maid.dimension){player.addChatMessage(new ChatComponentText("Maid and compass points are in different dimensions"));return true;}SchedulePos.Point fallback=SchedulePos.point((int)maid.posX,(int)maid.posY,(int)maid.posZ);SchedulePos.Point work=read(data,"Work",fallback),idle=read(data,"Idle",work),sleep=read(data,"Sleep",idle);maid.getSchedulePos().setPoints(maid,work,idle,sleep,maid.dimension);player.addChatMessage(new ChatComponentText("Kappa Compass points applied to maid"));return true;}
    private static SchedulePos.Point read(NBTTagCompound data,String key,SchedulePos.Point fallback){if(!data.hasKey(key,10))return fallback;NBTTagCompound p=data.getCompoundTag(key);return SchedulePos.point(p.getInteger("X"),p.getInteger("Y"),p.getInteger("Z"));}
    @Override public boolean hasEffect(ItemStack stack,int pass){return stack.hasTagCompound()&&stack.getTagCompound().hasKey(DATA,10);}
}

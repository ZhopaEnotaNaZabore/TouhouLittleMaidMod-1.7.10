package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public final class TaskIdle implements IMaidTask {
    @Override
    public String getId() {
        return TaskManager.IDLE_ID;
    }

    @Override
    public void onSelected(EntityMaid maid) {
        ItemStack held = maid.getMaidEquipmentInventory().getStackInSlot(0);
        if (held == null) return;
        maid.getMaidEquipmentInventory().setInventorySlotContents(0, null);
        ItemStack remaining = maid.addToMaidInventory(held);
        if (remaining != null) maid.entityDropItem(remaining, 0.0F);
        maid.getMaidEquipmentInventory().markDirty();
    }

    @Override
    public void tick(EntityMaid maid) {
        if (maid.getAttackTarget() != null) {
            maid.setAttackTarget(null);
        }
        tickSnowballGame(maid);
    }

    @SuppressWarnings("unchecked")
    private void tickSnowballGame(EntityMaid maid) {
        int x=(int)Math.floor(maid.posX),y=(int)Math.floor(maid.boundingBox.minY),z=(int)Math.floor(maid.posZ);
        if(maid.isBegging()||(maid.worldObj.getBlock(x,y,z)!=Blocks.snow_layer&&maid.worldObj.getBlock(x,y-1,z)!=Blocks.snow))return;
        NBTTagCompound data=maid.getTaskData(getId());int cooldown=data.getInteger("SnowballCooldown");if(cooldown>0){data.setInteger("SnowballCooldown",cooldown-1);return;}
        EntityLivingBase target=null;EntityLivingBase owner=maid.getOwner();if(owner instanceof EntityPlayer&&validOwnerTarget(maid,(EntityPlayer)owner))target=owner;
        if(target==null){List<EntityMaid> nearby=maid.worldObj.getEntitiesWithinAABB(EntityMaid.class,maid.boundingBox.expand(12,6,12));for(EntityMaid other:nearby)if(other!=maid&&other.isEntityAlive()&&maid.getOwnerId().equals(other.getOwnerId())&&maid.canEntityBeSeen(other)){target=other;break;}}
        if(target==null||maid.getRNG().nextInt(32)!=0)return;double dx=target.posX-maid.posX,dy=target.boundingBox.minY+target.height/3.0D-(maid.posY+maid.getEyeHeight()),dz=target.posZ-maid.posZ;EntitySnowball snowball=new EntitySnowball(maid.worldObj,maid);snowball.setThrowableHeading(dx,dy+Math.sqrt(dx*dx+dz*dz)*.15D,dz,1.6F,1.0F);maid.worldObj.spawnEntityInWorld(snowball);maid.swingItem();maid.playSound("random.bow",.5F,.8F+maid.getRNG().nextFloat()*.2F);data.setInteger("SnowballCooldown",50+maid.getRNG().nextInt(50));maid.markTaskDataDirty();
    }

    private boolean validOwnerTarget(EntityMaid maid,EntityPlayer owner){ItemStack held=owner.getCurrentEquippedItem();return(held==null||held.getItem()==Items.snowball)&&maid.getDistanceSqToEntity(owner)<144&&maid.canEntityBeSeen(owner);}
}

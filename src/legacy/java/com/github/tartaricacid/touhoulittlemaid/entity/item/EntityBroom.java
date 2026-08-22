package com.github.tartaricacid.touhoulittlemaid.entity.item;

import com.github.tartaricacid.touhoulittlemaid.init.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Single-rider 1.7 broom. The modern second maid passenger is API-limited. */
public final class EntityBroom extends Entity {
    private String ownerUuid = "";

    public EntityBroom(World world) {
        super(world);
        setSize(1.375F, 0.5625F);
    }

    public EntityBroom(World world, double x, double y, double z) {
        this(world);
        setPosition(x, y, z);
    }

    @Override
    protected void entityInit() {
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (riddenByEntity instanceof EntityPlayer) {
            EntityPlayer rider = (EntityPlayer) riddenByEntity;
            rotationYaw = rider.rotationYaw;
            rotationPitch = rider.rotationPitch * 0.25F;
            double forward = rider.moveForward;
            double strafe = rider.moveStrafing * 0.5D;
            double yawRad = Math.toRadians(rotationYaw);
            double speed = 0.12D;
            motionX += (-Math.sin(yawRad) * forward + Math.cos(yawRad) * strafe) * speed;
            motionZ += (Math.cos(yawRad) * forward + Math.sin(yawRad) * strafe) * speed;
            motionY += -Math.sin(Math.toRadians(rider.rotationPitch)) * forward * 0.04D;
            if (rider.isSneaking()) motionY -= 0.05D;
            fallDistance = 0.0F;
        } else {
            motionY -= 0.03D;
        }
        double max = 0.6D;
        motionX = Math.max(-max, Math.min(max, motionX));
        motionY = Math.max(-0.3D, Math.min(0.3D, motionY));
        motionZ = Math.max(-max, Math.min(max, motionZ));
        moveEntity(motionX, motionY, motionZ);
        motionX *= 0.91D;
        motionY *= riddenByEntity == null ? 0.98D : 0.85D;
        motionZ *= 0.91D;
    }

    @Override
    public boolean interactFirst(EntityPlayer player) {
        if(!ownerUuid.isEmpty()&&!ownerUuid.equals(player.getUniqueID().toString()))return true;
        if (!worldObj.isRemote && riddenByEntity == null && player.ridingEntity == null) player.mountEntity(this);
        return true;
    }

    @Override
    public void updateRiderPosition() {
        if (riddenByEntity != null) riddenByEntity.setPosition(posX, posY + 0.15D, posZ);
    }

    @Override
    public boolean canBeCollidedWith() { return !isDead && riddenByEntity == null; }
    @Override
    public boolean canBePushed() { return false; }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (!worldObj.isRemote && source.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) source.getEntity();
            if (player.isSneaking() && (ownerUuid.isEmpty() || ownerUuid.equals(player.getUniqueID().toString()))) {
                if (riddenByEntity != null) riddenByEntity.mountEntity(null);
                if (!player.capabilities.isCreativeMode) entityDropItem(new ItemStack(ModItems.BROOM), 0.0F);
                setDead();
            }
            return true;
        }
        return false;
    }

    public void setOwner(EntityPlayer player) { ownerUuid = player == null ? "" : player.getUniqueID().toString(); }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) { ownerUuid = tag.getString("OwnerUUID"); }
    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) { tag.setString("OwnerUUID", ownerUuid); }
}

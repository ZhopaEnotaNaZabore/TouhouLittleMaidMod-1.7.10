package com.github.tartaricacid.touhoulittlemaid.entity.item;

import com.github.tartaricacid.touhoulittlemaid.init.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

/** Placeable rideable chair with stable modern NBT keys. */
public final class EntityChair extends Entity {
    private static final int WATCHER_MODEL = 10;
    private static final int WATCHER_HEIGHT = 11;
    private static final int WATCHER_TAMEABLE_RIDE = 12;
    private String ownerUuid = "";

    public EntityChair(World world) {
        super(world);
        setSize(0.875F, 0.5F);
    }

    public EntityChair(World world, double x, double y, double z, float yaw) {
        this(world);
        setPosition(x, y, z);
        rotationYaw = yaw;
    }

    @Override
    protected void entityInit() {
        dataWatcher.addObject(WATCHER_MODEL, "touhou_little_maid:cushion");
        dataWatcher.addObject(WATCHER_HEIGHT, 0.0F);
        dataWatcher.addObject(WATCHER_TAMEABLE_RIDE, (byte) 1);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!worldObj.isRemote && riddenByEntity == null && canTameablesRide() && ticksExisted % 20 == 0) {
            @SuppressWarnings("unchecked")
            List<EntityTameable> nearby = worldObj.getEntitiesWithinAABB(EntityTameable.class,
                    boundingBox.expand(0.1D, 0.5D, 0.1D));
            for (EntityTameable tameable : nearby) {
                if (!tameable.isSitting() && tameable.ridingEntity == null) {
                    tameable.mountEntity(this);
                    break;
                }
            }
        }
        if (riddenByEntity != null) riddenByEntity.rotationYaw = rotationYaw;
    }

    @Override
    public boolean interactFirst(EntityPlayer player) {
        if (!worldObj.isRemote && riddenByEntity == null && player.ridingEntity == null) player.mountEntity(this);
        return true;
    }

    @Override
    public double getMountedYOffset() { return getMountedHeight(); }

    @Override
    public boolean canBeCollidedWith() { return !isDead; }

    @Override
    public boolean canBePushed() { return false; }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (!worldObj.isRemote && source.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) source.getEntity();
            if (player.isSneaking() && (ownerUuid.isEmpty() || ownerUuid.equals(player.getUniqueID().toString()))) {
                if (riddenByEntity != null) riddenByEntity.mountEntity(null);
                if (!player.capabilities.isCreativeMode) entityDropItem(createChairStack(), 0.0F);
                playSound("dig.cloth", 1.0F, 1.0F);
                setDead();
            }
            return true;
        }
        return false;
    }

    public void setOwner(EntityPlayer player) { ownerUuid = player == null ? "" : player.getUniqueID().toString(); }
    public String getModelId() { return dataWatcher.getWatchableObjectString(WATCHER_MODEL); }
    public void setModelId(String id) { dataWatcher.updateObject(WATCHER_MODEL, id == null ? "touhou_little_maid:cushion" : id); }
    public float getMountedHeight() { return dataWatcher.getWatchableObjectFloat(WATCHER_HEIGHT); }
    public void setMountedHeight(float value) { dataWatcher.updateObject(WATCHER_HEIGHT, Math.max(-0.5F, Math.min(2.5F, value))); }
    public boolean canTameablesRide() { return dataWatcher.getWatchableObjectByte(WATCHER_TAMEABLE_RIDE) != 0; }
    public void setTameablesRide(boolean value) { dataWatcher.updateObject(WATCHER_TAMEABLE_RIDE, (byte) (value ? 1 : 0)); }

    public void applyItemData(NBTTagCompound tag) {
        if (tag.hasKey("ModelId")) setModelId(tag.getString("ModelId"));
        if (tag.hasKey("MountedHeight")) setMountedHeight(tag.getFloat("MountedHeight"));
        if (tag.hasKey("TameableCanRide")) setTameablesRide(tag.getBoolean("TameableCanRide"));
    }

    private ItemStack createChairStack() {
        ItemStack stack = new ItemStack(ModItems.CHAIR);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("ModelId", getModelId());
        tag.setFloat("MountedHeight", getMountedHeight());
        tag.setBoolean("TameableCanRide", canTameablesRide());
        stack.setTagCompound(tag);
        return stack;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        applyItemData(tag);
        ownerUuid = tag.getString("OwnerUUID");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        tag.setString("ModelId", getModelId());
        tag.setFloat("MountedHeight", getMountedHeight());
        tag.setBoolean("TameableCanRide", canTameablesRide());
        tag.setString("OwnerUUID", ownerUuid);
    }
}

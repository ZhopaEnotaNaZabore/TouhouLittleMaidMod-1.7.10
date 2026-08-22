package com.github.tartaricacid.touhoulittlemaid.entity.item;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/** Invisible temporary mount used by joy/board/bed integrations. */
public final class EntitySit extends Entity {
    private static final int WATCHER_JOY_TYPE = 10;
    private int associatedX;
    private int associatedY;
    private int associatedZ;
    private int emptyTicks;

    public EntitySit(World world) {
        super(world);
        setSize(0.5F, 0.1F);
        noClip = true;
    }

    public EntitySit(World world, double x, double y, double z, String joyType,
                     int blockX, int blockY, int blockZ) {
        this(world);
        setPosition(x, y, z);
        setJoyType(joyType);
        associatedX = blockX;
        associatedY = blockY;
        associatedZ = blockZ;
    }

    @Override
    protected void entityInit() { dataWatcher.addObject(WATCHER_JOY_TYPE, ""); }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!worldObj.isRemote) {
            if (riddenByEntity == null) emptyTicks++; else emptyTicks = 0;
            if (riddenByEntity instanceof EntityMaid) {
                EntityMaid maid = (EntityMaid) riddenByEntity;
                maid.rotationYaw = rotationYaw;
                maid.rotationYawHead = rotationYaw;
                if (ticksExisted % 20 == 0 && !getJoyType().isEmpty()
                        && !"fishing".equals(getJoyType())) {
                    maid.getFavorabilityManager().apply(getJoyType(), 2, 24000);
                }
            }
            if (emptyTicks > 10 || posY < -64.0D) setDead();
        }
    }

    @Override
    public double getMountedYOffset() { return -0.25D; }
    @Override
    public boolean canBeCollidedWith() { return false; }
    @Override
    public boolean canBePushed() { return false; }

    public String getJoyType() { return dataWatcher.getWatchableObjectString(WATCHER_JOY_TYPE); }
    public void setJoyType(String type) { dataWatcher.updateObject(WATCHER_JOY_TYPE, type == null ? "" : type); }
    public boolean isAssociatedWith(int x,int y,int z){return associatedX==x&&associatedY==y&&associatedZ==z;}
    public int getAssociatedX() { return associatedX; }
    public int getAssociatedY() { return associatedY; }
    public int getAssociatedZ() { return associatedZ; }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        setJoyType(tag.getString("SitJoyType"));
        associatedX = tag.getInteger("AssociatedBlockX");
        associatedY = tag.getInteger("AssociatedBlockY");
        associatedZ = tag.getInteger("AssociatedBlockZ");
        emptyTicks = tag.getInteger("EmptyTicks");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        tag.setString("SitJoyType", getJoyType());
        tag.setInteger("AssociatedBlockX", associatedX);
        tag.setInteger("AssociatedBlockY", associatedY);
        tag.setInteger("AssociatedBlockZ", associatedZ);
        tag.setInteger("EmptyTicks", emptyTicks);
    }
}

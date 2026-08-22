package com.github.tartaricacid.touhoulittlemaid.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.List;

/** Three-stage cake box entity used as an altar/entity reward. */
public final class EntityBox extends Entity {
    public static final int FIRST_STAGE = 0;
    public static final int SECOND_STAGE = 1;
    public static final int THIRD_STAGE = 2;
    private static final int WATCHER_STAGE = 10;
    private static final int WATCHER_TEXTURE = 11;
    private int thirdStageTicks;

    public EntityBox(World world) {
        super(world);
        setSize(2.0F, 2.0F);
    }

    public EntityBox(World world, double x, double y, double z) {
        this(world);
        setPosition(x, y, z);
        setTextureIndex(rand.nextInt(8));
    }

    @Override
    protected void entityInit() {
        dataWatcher.addObject(WATCHER_STAGE, FIRST_STAGE);
        dataWatcher.addObject(WATCHER_TEXTURE, 0);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!onGround) motionY -= 0.1D;
        moveEntity(motionX, motionY, motionZ);
        motionX *= 0.91D;
        motionY *= 0.98D;
        motionZ *= 0.91D;

        if (!worldObj.isRemote && getOpenStage() == FIRST_STAGE) {
            if (riddenByEntity instanceof EntityTameable) {
                ((EntityTameable) riddenByEntity).addPotionEffect(new PotionEffect(Potion.invisibility.id, 2, 1, false));
            } else if (ticksExisted % 20 == 0) {
                @SuppressWarnings("unchecked")
                List<EntityTameable> tameables = worldObj.getEntitiesWithinAABB(
                        EntityTameable.class, boundingBox.expand(0.1D, 0.5D, 0.1D));
                if (!tameables.isEmpty()) tameables.get(0).mountEntity(this);
            }
        }
        if (!worldObj.isRemote && getOpenStage() == THIRD_STAGE && ++thirdStageTicks > 100) finishOpen();
    }

    @Override
    public boolean interactFirst(EntityPlayer player) {
        if (!worldObj.isRemote) {
            setOpenStage(getOpenStage() + 1);
            playSound("random.chestopen", 1.0F, getOpenStage() >= THIRD_STAGE ? 1.0F : 1.5F);
            if (getOpenStage() > THIRD_STAGE) finishOpen();
        }
        return true;
    }

    private void finishOpen() {
        if (isDead) return;
        if (riddenByEntity != null) riddenByEntity.mountEntity(null);
        entityDropItem(new net.minecraft.item.ItemStack(Items.paper), 0.0F);
        worldObj.createExplosion(this, posX, posY + 0.25D, posZ, 0.0F, false);
        setDead();
    }

    @Override
    public double getMountedYOffset() { return 0.0D; }
    @Override
    public boolean canBeCollidedWith() { return !isDead; }
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) { return false; }

    public int getOpenStage() { return dataWatcher.getWatchableObjectInt(WATCHER_STAGE); }
    private void setOpenStage(int stage) { dataWatcher.updateObject(WATCHER_STAGE, stage); }
    public int getTextureIndex() { return dataWatcher.getWatchableObjectInt(WATCHER_TEXTURE); }
    private void setTextureIndex(int index) { dataWatcher.updateObject(WATCHER_TEXTURE, Math.max(0, Math.min(7, index))); }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        setOpenStage(tag.getInteger("OpenStage"));
        setTextureIndex(tag.getInteger("TextureIndex"));
        thirdStageTicks = tag.getInteger("ThirdStageTicks");
    }
    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        tag.setInteger("OpenStage", getOpenStage());
        tag.setInteger("TextureIndex", getTextureIndex());
        tag.setInteger("ThirdStageTicks", thirdStageTicks);
    }
}

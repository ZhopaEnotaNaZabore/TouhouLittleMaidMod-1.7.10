package com.github.tartaricacid.touhoulittlemaid.entity.projectile;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

/** 1.7.10 implementation of the mod's colored danmaku projectile. */
public final class EntityDanmaku extends EntityThrowable {
    private static final int WATCHER_TYPE = 10;
    private static final int WATCHER_COLOR = 11;
    private static final int WATCHER_DAMAGE = 12;
    private static final int WATCHER_GRAVITY = 13;
    private int impedingLevel;
    private boolean hurtEnderman;

    public EntityDanmaku(World world) {
        super(world);
        setSize(0.25F, 0.25F);
    }

    public EntityDanmaku(World world, EntityLivingBase thrower) {
        super(world, thrower);
        setSize(0.25F, 0.25F);
    }

    public EntityDanmaku(World world, double x, double y, double z) {
        super(world, x, y, z);
        setSize(0.25F, 0.25F);
    }

    @Override
    protected void entityInit() {
        dataWatcher.addObject(WATCHER_TYPE, DanmakuType.PELLET.ordinal());
        dataWatcher.addObject(WATCHER_COLOR, DanmakuColor.RED.ordinal());
        dataWatcher.addObject(WATCHER_DAMAGE, 1.0F);
        dataWatcher.addObject(WATCHER_GRAVITY, 0.01F);
    }

    @Override
    protected float getGravityVelocity() { return dataWatcher.getWatchableObjectFloat(WATCHER_GRAVITY); }

    public DanmakuType getDanmakuType() { return DanmakuType.byOrdinal(dataWatcher.getWatchableObjectInt(WATCHER_TYPE)); }
    public EntityDanmaku setDanmakuType(DanmakuType type) { dataWatcher.updateObject(WATCHER_TYPE, type.ordinal()); return this; }
    public DanmakuColor getColor() { return DanmakuColor.byOrdinal(dataWatcher.getWatchableObjectInt(WATCHER_COLOR)); }
    public EntityDanmaku setColor(DanmakuColor color) { dataWatcher.updateObject(WATCHER_COLOR, color.ordinal()); return this; }
    public float getDamage() { return dataWatcher.getWatchableObjectFloat(WATCHER_DAMAGE); }
    public EntityDanmaku setDamage(float damage) { dataWatcher.updateObject(WATCHER_DAMAGE, Math.max(0.0F, damage)); return this; }
    public EntityDanmaku setGravity(float gravity) { dataWatcher.updateObject(WATCHER_GRAVITY, gravity); return this; }
    public EntityDanmaku setImpedingLevel(int level){impedingLevel=Math.max(0,level);return this;}
    public EntityDanmaku setHurtEnderman(boolean value){hurtEnderman=value;return this;}

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (ticksExisted > 200) setDead();
    }

    @Override
    protected void onImpact(MovingObjectPosition hit) {
        if (worldObj.isRemote) return;
        EntityLivingBase thrower = getThrower();
        if (hit.entityHit != null && hit.entityHit != thrower) {
            if (thrower instanceof EntityMaid && hit.entityHit instanceof EntityPlayer) {
                setDead();
                return;
            }
            if (thrower instanceof EntityTameable && hit.entityHit instanceof EntityTameable) {
                EntityLivingBase ownerA = ((EntityTameable) thrower).getOwner();
                EntityLivingBase ownerB = ((EntityTameable) hit.entityHit).getOwner();
                if (ownerA != null && ownerA == ownerB) {
                    setDead();
                    return;
                }
            }
            if (thrower != null && hit.entityHit instanceof EntityLivingBase
                    && thrower.isOnSameTeam((EntityLivingBase) hit.entityHit)) {
                setDead();
                return;
            }
            DamageSource source=hurtEnderman&&thrower!=null?DamageSource.causeMobDamage(thrower):DamageSource.causeThrownDamage(this, thrower);
            hit.entityHit.attackEntityFrom(source, getDamage());
            if(impedingLevel>0&&hit.entityHit instanceof EntityLivingBase)((EntityLivingBase)hit.entityHit).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id,40+impedingLevel*30,Math.min(3,impedingLevel-1)));
        }
        setDead();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setInteger("DanmakuType", getDanmakuType().ordinal());
        tag.setInteger("DanmakuColor", getColor().ordinal());
        tag.setFloat("Damage", getDamage());
        tag.setFloat("Gravity", getGravityVelocity());
        tag.setInteger("Impeding",impedingLevel);tag.setBoolean("HurtEnderman",hurtEnderman);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        setDanmakuType(DanmakuType.byOrdinal(tag.getInteger("DanmakuType")));
        setColor(DanmakuColor.byOrdinal(tag.getInteger("DanmakuColor")));
        if (tag.hasKey("Damage")) setDamage(tag.getFloat("Damage"));
        if (tag.hasKey("Gravity")) setGravity(tag.getFloat("Gravity"));
        impedingLevel=tag.getInteger("Impeding");hurtEnderman=tag.getBoolean("HurtEnderman");
    }
}

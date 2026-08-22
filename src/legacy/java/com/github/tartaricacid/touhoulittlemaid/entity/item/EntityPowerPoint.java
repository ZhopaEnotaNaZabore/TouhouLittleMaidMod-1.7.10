package com.github.tartaricacid.touhoulittlemaid.entity.item;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.capability.LegacyPlayerPower;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.List;

/** Floating Power resource, adapted from the modern entity to the 1.7 entity API. */
public final class EntityPowerPoint extends Entity {
    private static final int WATCHER_VALUE = 10;
    private int age;
    private int health = 5;
    private int pickupDelay = 10;

    public EntityPowerPoint(World world) {
        super(world);
        setSize(0.5F, 0.5F);
        yOffset = height / 2.0F;
    }

    public EntityPowerPoint(World world, double x, double y, double z, int value) {
        this(world);
        setPosition(x, y, z);
        setValue(value);
        rotationYaw = rand.nextFloat() * 360.0F;
        motionX = (rand.nextDouble() - 0.5D) * 0.2D;
        motionY = rand.nextDouble() * 0.2D;
        motionZ = (rand.nextDouble() - 0.5D) * 0.2D;
    }

    public EntityPowerPoint(World world, EntityPlayer player, int value) {
        this(world, player.posX, player.posY + player.getEyeHeight() - 0.1D, player.posZ, value);
        Vec3 look = player.getLookVec();
        motionX = look.xCoord * 0.55D;
        motionY = look.yCoord * 0.55D + 0.1D;
        motionZ = look.zCoord * 0.55D;
    }

    @Override
    protected void entityInit() {
        dataWatcher.addObject(WATCHER_VALUE, 100);
    }

    public int getValue() {
        return dataWatcher.getWatchableObjectInt(WATCHER_VALUE);
    }

    public static int getPowerValue(int value) {
        if (value >= 485) return 485;
        if (value >= 385) return 385;
        if (value >= 285) return 285;
        if (value >= 185) return 185;
        if (value >= 89) return 89;
        if (value >= 36) return 34;
        if (value >= 17) return 13;
        if (value >= 7) return 7;
        if (value >= 5) return 5;
        return value >= 3 ? 3 : 1;
    }

    public void setValue(int value) {
        dataWatcher.updateObject(WATCHER_VALUE, Math.max(1, value));
    }

    /** Texture tile used by the original 4x4 Power Point sprite sheet. */
    public int getIcon() {
        int value = getValue();
        if (value >= 485) return 10;
        if (value >= 385) return 9;
        if (value >= 285) return 8;
        if (value >= 185) return 7;
        if (value >= 89) return 6;
        if (value >= 36) return 5;
        if (value >= 17) return 4;
        if (value >= 7) return 3;
        if (value >= 5) return 2;
        return value >= 3 ? 1 : 0;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (pickupDelay > 0) --pickupDelay;

        motionY -= 0.03D;
        EntityPlayer player = worldObj.getClosestPlayerToEntity(this, 8.0D);
        if (player != null) {
            double dx = player.posX - posX;
            double dy = player.posY + player.getEyeHeight() * 0.5D - posY;
            double dz = player.posZ - posZ;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance > 0.001D) {
                double pull = 1.0D - distance / 8.0D;
                if (pull > 0.0D) {
                    pull *= pull * 0.1D;
                    motionX += dx / distance * pull;
                    motionY += dy / distance * pull;
                    motionZ += dz / distance * pull;
                }
            }
        }

        moveEntity(motionX, motionY, motionZ);
        float friction = 0.98F;
        if (onGround) {
            friction = worldObj.getBlock(MathHelper.floor_double(posX), MathHelper.floor_double(boundingBox.minY) - 1,
                    MathHelper.floor_double(posZ)).slipperiness * 0.98F;
            motionY *= -0.9D;
        }
        motionX *= friction;
        motionY *= 0.98D;
        motionZ *= friction;

        if (!worldObj.isRemote && pickupDelay <= 0) {
            List maids = worldObj.getEntitiesWithinAABB(EntityMaid.class, boundingBox.expand(0.5D, 0.5D, 0.5D));
            for (Object object : maids) {
                EntityMaid maid = (EntityMaid) object;
                if (maid.isTamed() && maid.isPickupEnabled() && !maid.isSitting()) {
                    maid.setMaidExperience(maid.getMaidExperience() + Math.max(1, getValue() / 4));
                    worldObj.playSoundAtEntity(maid, "random.orb", 0.1F, 1.2F);
                    setDead();
                    break;
                }
            }
        }

        if (++age >= 6000) setDead();
    }

    @Override
    public void onCollideWithPlayer(EntityPlayer player) {
        if (!worldObj.isRemote && pickupDelay <= 0 && !isDead) {
            float powerValue=getValue()/100.0F;
            float stored=LegacyPlayerPower.get(player).add(powerValue);
            int residual=Math.max(0,Math.round((powerValue-stored)*100.0F));
            if(residual>0)player.addExperience(Math.max(1,residual/4));
            worldObj.playSoundAtEntity(player, "random.orb", 0.1F,
                    0.5F * ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.8F));
            setDead();
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (isEntityInvulnerable()) return false;
        setBeenAttacked();
        health = (int) (health - amount);
        if (health <= 0) setDead();
        return true;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        health = tag.hasKey("Health") ? tag.getShort("Health") : 5;
        age = tag.getShort("Age");
        pickupDelay = tag.hasKey("PickupDelay") ? tag.getShort("PickupDelay") : 10;
        setValue(tag.hasKey("Value") ? tag.getInteger("Value") : 100);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        tag.setShort("Health", (short) health);
        tag.setShort("Age", (short) age);
        tag.setShort("PickupDelay", (short) pickupDelay);
        tag.setInteger("Value", getValue());
    }
}

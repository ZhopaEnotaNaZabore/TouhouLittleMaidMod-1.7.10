package com.github.tartaricacid.touhoulittlemaid.entity.projectile;

import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityPowerPoint;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

/** Throwable item which bursts into collectible Power Points on impact. */
public final class EntityThrowPowerPoint extends EntityThrowable {
    public EntityThrowPowerPoint(World world) { super(world); }
    public EntityThrowPowerPoint(World world, EntityLivingBase thrower) { super(world, thrower); }

    @Override protected float getGravityVelocity() { return 0.07F; }

    @Override
    protected void onImpact(MovingObjectPosition hit) {
        if (worldObj.isRemote) return;
        worldObj.playAuxSFX(2002, (int) Math.floor(posX), (int) Math.floor(posY),
                (int) Math.floor(posZ), 16389);
        int remaining = 30 + rand.nextInt(30) + rand.nextInt(30);
        while (remaining > 0) {
            int value = EntityPowerPoint.getPowerValue(remaining);
            remaining -= value;
            worldObj.spawnEntityInWorld(new EntityPowerPoint(worldObj, posX, posY, posZ, value));
        }
        setDead();
    }
}

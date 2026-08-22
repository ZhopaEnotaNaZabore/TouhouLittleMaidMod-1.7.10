package com.github.tartaricacid.touhoulittlemaid.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/** Short-lived extinguishing cloud: clears fire in a 5x3x5 volume. */
public final class EntityExtinguishingAgent extends Entity {
    private final List<EntityMob> fireImmuneMonsters = new ArrayList<EntityMob>();

    public EntityExtinguishingAgent(World world) {
        super(world);
        setSize(0.2F, 0.2F);
    }

    public EntityExtinguishingAgent(World world, double x, double y, double z) {
        this(world);
        setPosition(x, y, z);
    }

    @Override
    protected void entityInit() {
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (worldObj.isRemote) {
            for (int i = 0; i < 4; i++) {
                worldObj.spawnParticle("cloud", posX + rand.nextDouble() * 2.0D - 1.0D,
                        posY + rand.nextDouble() * 0.5D, posZ + rand.nextDouble() * 2.0D - 1.0D,
                        0.0D, 0.1D, 0.0D);
            }
        } else if (ticksExisted == 5) {
            removeBlockFire();
        }

        if (!worldObj.isRemote && ticksExisted % 5 == 0) {
            fireImmuneMonsters.clear();
            removeEntityFire();
            for (EntityMob monster : fireImmuneMonsters) {
                if (monster.isEntityAlive()) monster.attackEntityFrom(DamageSource.magic, 2.0F);
            }
        }
        if (ticksExisted % 4 == 0) playSound("dig.cloth", Math.max(0.2F, 2.0F - ticksExisted * 0.03F), 0.1F);
        if (ticksExisted > 60) setDead();
    }

    @SuppressWarnings("unchecked")
    private void removeEntityFire() {
        List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(
                EntityLivingBase.class, boundingBox.expand(2.0D, 1.0D, 2.0D));
        for (EntityLivingBase entity : entities) {
            entity.extinguish();
            if (entity instanceof EntityBlaze || entity instanceof EntityMagmaCube) {
                fireImmuneMonsters.add((EntityMob) entity);
            }
        }
    }

    private void removeBlockFire() {
        int centerX = MathHelper.floor_double(posX);
        int centerY = MathHelper.floor_double(posY);
        int centerZ = MathHelper.floor_double(posZ);
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -2; z <= 2; z++) {
                    if (worldObj.getBlock(centerX + x, centerY + y, centerZ + z) == Blocks.fire) {
                        worldObj.setBlockToAir(centerX + x, centerY + y, centerZ + z);
                    }
                }
            }
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        ticksExisted = Math.max(0, tag.getInteger("AgentAge"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        tag.setInteger("AgentAge", ticksExisted);
    }
}

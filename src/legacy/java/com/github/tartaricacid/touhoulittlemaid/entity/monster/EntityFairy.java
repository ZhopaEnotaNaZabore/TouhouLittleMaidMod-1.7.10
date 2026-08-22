package com.github.tartaricacid.touhoulittlemaid.entity.monster;

import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityPowerPoint;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.projectile.DanmakuColor;
import com.github.tartaricacid.touhoulittlemaid.entity.projectile.DanmakuType;
import com.github.tartaricacid.touhoulittlemaid.entity.projectile.EntityDanmaku;
import com.github.tartaricacid.touhoulittlemaid.config.LegacyConfig;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityScarecrow;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Hostile maid fairy retaining the modern random variants and danmaku attack. */
public final class EntityFairy extends EntityMob implements IRangedAttackMob {
    private static final int WATCHER_TYPE = 20;
    private static final int WATCHER_BABY = 21;

    public EntityFairy(World world) {
        super(world);
        setSize(0.6F, 1.5F);
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIArrowAttack(this, 1.0D, 20, 30, 8.0F));
        tasks.addTask(3, new EntityAIWander(this, 0.8D));
        tasks.addTask(4, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(5, new EntityAIWatchClosest(this, EntityMaid.class, 8.0F));
        tasks.addTask(6, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityMaid.class, 0, true));
        experienceValue = 5;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(WATCHER_TYPE, 0);
        dataWatcher.addObject(WATCHER_BABY, (byte) 0);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20.0D);
        getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(35.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25D);
        getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(1.5D);
    }

    @Override
    public net.minecraft.entity.IEntityLivingData onSpawnWithEgg(net.minecraft.entity.IEntityLivingData data) {
        data = super.onSpawnWithEgg(data);
        setFairyTypeOrdinal(rand.nextInt(18));
        setBaby(rand.nextInt(10) == 0);
        if (rand.nextInt(20) == 0) {
            setCustomNameTag("rick");
            setAlwaysRenderNameTag(true);
        }
        return data;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        // 1.7 has no flying navigator; this light hover correction preserves the
        // fairy's airborne combat without replacing pathfinding wholesale.
        if (!onGround && motionY < 0.0D) motionY *= 0.6D;
        fallDistance = 0.0F;
    }

    @Override
    protected void fall(float distance) {
    }

    @Override
    public boolean getCanSpawnHere() {
        if (!super.getCanSpawnHere()) return false;
        return !isScarecrowNearby(worldObj, posX, posZ, LegacyConfig.scarecrowRange);
    }

    /** Equivalent of the modern POI square query, restricted to loaded chunks. */
    @SuppressWarnings("unchecked")
    public static boolean isScarecrowNearby(World world, double centerX, double centerZ, int range) {
        if (world == null || range < 0) return false;
        for (Object value : world.loadedTileEntityList) {
            if (!(value instanceof TileEntityScarecrow)) continue;
            TileEntityScarecrow scarecrow = (TileEntityScarecrow) value;
            if (Math.abs((scarecrow.xCoord + 0.5D) - centerX) <= range
                    && Math.abs((scarecrow.zCoord + 0.5D) - centerZ) <= range) return true;
        }
        return false;
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
        if (target == null) return;
        EntityDanmaku shot = new EntityDanmaku(worldObj, this)
                .setDanmakuType(DanmakuType.random(rand))
                .setColor(DanmakuColor.random(rand))
                .setDamage(distanceFactor + difficultyDamage())
                .setGravity(0.0F);
        double dx = target.posX - posX;
        double dy = target.boundingBox.minY + target.height * 0.5D - shot.posY;
        double dz = target.posZ - posZ;
        float velocity = 0.2F * (distanceFactor + 1.0F);
        shot.setThrowableHeading(dx, dy, dz, velocity, 0.2F);
        worldObj.spawnEntityInWorld(shot);
        playSound("random.bow", 0.5F, 1.2F);
    }

    private float difficultyDamage() {
        switch (worldObj.difficultySetting) {
            case HARD: return 2.0F;
            case NORMAL: return 1.5F;
            default: return 1.0F;
        }
    }

    @Override
    public void onDeath(DamageSource source) {
        if (!worldObj.isRemote) {
            int value = isBaby() ? 200 : 100;
            worldObj.spawnEntityInWorld(new EntityPowerPoint(worldObj, posX, posY + 0.5D, posZ, value));
        }
        super.onDeath(source);
    }

    public int getFairyTypeOrdinal() { return dataWatcher.getWatchableObjectInt(WATCHER_TYPE); }
    public void setFairyTypeOrdinal(int ordinal) { dataWatcher.updateObject(WATCHER_TYPE, MathHelper.clamp_int(ordinal, 0, 17)); }
    public boolean isBaby() { return dataWatcher.getWatchableObjectByte(WATCHER_BABY) != 0; }
    public void setBaby(boolean baby) { dataWatcher.updateObject(WATCHER_BABY, (byte) (baby ? 1 : 0)); }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setInteger("FairyType", getFairyTypeOrdinal());
        tag.setBoolean("IsBaby", isBaby());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        setFairyTypeOrdinal(tag.getInteger("FairyType"));
        setBaby(tag.getBoolean("IsBaby"));
    }
}

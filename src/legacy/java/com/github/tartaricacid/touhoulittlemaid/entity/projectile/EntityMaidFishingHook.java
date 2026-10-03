package com.github.tartaricacid.touhoulittlemaid.entity.projectile;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.MaidActivity;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Persistent-looking maid fishing bobber with a server-authoritative catch timer. */
public final class EntityMaidFishingHook extends Entity {
    public static final int EQUIPPED_ROD_SLOT = -2;
    private static final int WATCHER_OWNER_ID = 10;
    private static final int WATCHER_BITING = 11;
    private static final int WATCHER_ROD_SLOT = 12;
    private int waitTime;
    private int life;
    private int biteDelay;
    private String ownerUuid = "";

    public EntityMaidFishingHook(World world) {
        super(world);
        setSize(0.25F, 0.25F);
        ignoreFrustumCheck = true;
    }

    public EntityMaidFishingHook(World world, EntityMaid maid, int rodSlot, double x, double y, double z) {
        this(world);
        setPosition(x, y, z);
        setOwnerId(maid.getEntityId());
        maid.setFishingHookActive(true);
        ownerUuid = maid.getUniqueID().toString();
        dataWatcher.updateObject(WATCHER_ROD_SLOT, rodSlot);
        ItemStack rod = getRod(maid, rodSlot);
        int lure = rod == null ? 0 : EnchantmentHelper.getEnchantmentLevel(Enchantment.field_151369_A.effectId, rod);
        waitTime = Math.max(60, 180 + rand.nextInt(121) - lure * 30);
    }

    @Override
    protected void entityInit() {
        dataWatcher.addObject(WATCHER_OWNER_ID, -1);
        dataWatcher.addObject(WATCHER_BITING, (byte) 0);
        dataWatcher.addObject(WATCHER_ROD_SLOT, -1);
    }

    public EntityMaid getMaidOwner() {
        Entity entity = worldObj.getEntityByID(dataWatcher.getWatchableObjectInt(WATCHER_OWNER_ID));
        if (entity instanceof EntityMaid && (ownerUuid.isEmpty() || entity.getUniqueID().toString().equals(ownerUuid))) return (EntityMaid) entity;
        if(!ownerUuid.isEmpty())for(Object value:worldObj.loadedEntityList)if(value instanceof EntityMaid&&((Entity)value).getUniqueID().toString().equals(ownerUuid)){setOwnerId(((Entity)value).getEntityId());return (EntityMaid)value;}
        return null;
    }

    private void setOwnerId(int id) { dataWatcher.updateObject(WATCHER_OWNER_ID, id); }
    public boolean isBiting() { return dataWatcher.getWatchableObjectByte(WATCHER_BITING) != 0; }

    @Override
    public void onUpdate() {
        super.onUpdate();
        EntityMaid maid = getMaidOwner();
        if (!worldObj.isRemote && maid != null && !maid.hasFishingHook())
            maid.setFishingHookActive(true);
        if (!worldObj.isRemote && (maid == null || !maid.isEntityAlive()
                || !maid.canRemainFishing()
                || getRod(maid, dataWatcher.getWatchableObjectInt(WATCHER_ROD_SLOT)) == null
                || maid.getDistanceSqToEntity(this) > 32.0D * 32.0D)) {
            setDead();
            return;
        }

        int x = MathHelper.floor_double(posX);
        int y = MathHelper.floor_double(posY);
        int z = MathHelper.floor_double(posZ);
        boolean water = worldObj.getBlock(x, y, z).getMaterial() == Material.water
                || worldObj.getBlock(x, y - 1, z).getMaterial() == Material.water;
        if (water) {
            motionX *= 0.3D;
            motionZ *= 0.3D;
            life=0;
            double surface=(worldObj.getBlock(x,y,z).getMaterial()==Material.water?y:y-1)+.85D;
            motionY=MathHelper.clamp_double((surface-posY)*.2D,-.08D,.08D);
            if (!worldObj.isRemote) {
                if(isBiting()) {
                    motionY=-.025D;
                    if(--biteDelay<=0){catchFish(maid);return;}
                } else if (--waitTime<=0) {
                    dataWatcher.updateObject(WATCHER_BITING,(byte)1);biteDelay=10;
                    worldObj.playSoundAtEntity(this,"random.splash",.25F,1F);
                }
            }
        } else {
            motionY -= 0.03D;
            if (++life > 100) setDead();
        }
        moveEntity(motionX, motionY, motionZ);
        motionX *= 0.92D;
        motionY *= 0.92D;
        motionZ *= 0.92D;
    }

    private void catchFish(EntityMaid maid) {
        int rodSlot = dataWatcher.getWatchableObjectInt(WATCHER_ROD_SLOT);
        ItemStack rod = getRod(maid, rodSlot);
        if (rod == null || !(rod.getItem() instanceof net.minecraft.item.ItemFishingRod)) {
            setDead();
            return;
        }
        int luck = EnchantmentHelper.getEnchantmentLevel(Enchantment.field_151370_z.effectId, rod);
        int lure = EnchantmentHelper.getEnchantmentLevel(Enchantment.field_151369_A.effectId, rod);
        ItemStack caught = net.minecraftforge.common.FishingHooks.getRandomFishable(rand, rand.nextFloat(), luck, lure);
        if(caught==null)caught=new ItemStack(Items.fish);
        ItemStack remaining = maid.addToMaidInventory(caught);
        if (remaining != null) maid.entityDropItem(remaining, 0.0F);
        worldObj.spawnEntityInWorld(new net.minecraft.entity.item.EntityXPOrb(
                worldObj,maid.posX,maid.posY+.5D,maid.posZ,rand.nextInt(6)+1));
        rod.damageItem(1, maid);
        if (rodSlot == EQUIPPED_ROD_SLOT) {
            if (rod.stackSize <= 0) maid.getMaidEquipmentInventory().setInventorySlotContents(0, null);
            maid.getMaidEquipmentInventory().markDirty();
        } else {
            if (rod.stackSize <= 0) maid.clearLogicalSlot(rodSlot);
            maid.markLogicalInventoryDirty(rodSlot);
        }
        worldObj.playSoundAtEntity(this, "random.splash", 0.5F, 1.0F);
        maid.swingItem();
        setDead();
    }

    private ItemStack getRod(EntityMaid maid, int rodSlot) {
        ItemStack rod=rodSlot==EQUIPPED_ROD_SLOT ? maid.getMaidEquipmentInventory().getStackInSlot(0)
                : rodSlot>=0 ? maid.getStackInLogicalSlot(rodSlot) : null;
        return rod!=null && rod.stackSize>0 && rod.getItem() instanceof net.minecraft.item.ItemFishingRod ? rod : null;
    }

    @Override
    public void setDead() {
        EntityMaid maid = getMaidOwner();
        if (!worldObj.isRemote && maid != null) maid.setFishingHookActive(false);
        super.setDead();
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        setOwnerId(tag.getInteger("MaidOwnerId"));
        dataWatcher.updateObject(WATCHER_ROD_SLOT, tag.getInteger("RodSlot"));
        waitTime = tag.getInteger("WaitTime");
        life = tag.getInteger("Life");
        biteDelay=MathHelper.clamp_int(tag.getInteger("BiteDelay"),0,10);
        ownerUuid = tag.getString("MaidOwnerUUID");
        dataWatcher.updateObject(WATCHER_BITING, tag.getBoolean("Biting") ? (byte)1 : (byte)0);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        tag.setInteger("MaidOwnerId", dataWatcher.getWatchableObjectInt(WATCHER_OWNER_ID));
        tag.setInteger("RodSlot", dataWatcher.getWatchableObjectInt(WATCHER_ROD_SLOT));
        tag.setInteger("WaitTime", waitTime);
        tag.setInteger("Life", life);
        tag.setInteger("BiteDelay",biteDelay);
        tag.setString("MaidOwnerUUID", ownerUuid);
        tag.setBoolean("Biting", isBiting());
    }
}

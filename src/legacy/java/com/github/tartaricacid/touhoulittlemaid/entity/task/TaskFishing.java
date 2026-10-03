package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import com.github.tartaricacid.touhoulittlemaid.entity.projectile.EntityMaidFishingHook;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit;

import java.util.List;

public final class TaskFishing implements IMaidTask {
    @Override
    public String getId() {
        return TaskManager.FISHING_ID;
    }

    @Override
    public void tick(EntityMaid maid) {
        if (maid.worldObj.isRemote || !maid.isEntityAlive() || maid.isSitting() || maid.isMaidSleeping() || !maid.isWorkingNow()) return;
        if (!ensureMainhandFishingRod(maid)) { onDeselected(maid); return; }
        if (!maid.isPeriodicTick(20)) return;
        if (hasActiveHook(maid)) return;
        FishingSpot spot = findSpot(maid);
        if (spot == null) {onDeselected(maid);return;}
        double distance = maid.getDistanceSq(spot.standX + 0.5D, spot.standY, spot.standZ + 0.5D);
        if (distance > 4.0D) {
            maid.getNavigator().tryMoveToXYZ(spot.standX + 0.5D, spot.standY, spot.standZ + 0.5D, 0.6D);
            return;
        }

        if (!ensureFishingSeat(maid, spot)) return;
        maid.getLookHelper().setLookPosition(spot.waterX+.5D,spot.waterY+.85D,spot.waterZ+.5D,30,30);

        EntityMaidFishingHook hook = new EntityMaidFishingHook(maid.worldObj, maid,
                EntityMaidFishingHook.EQUIPPED_ROD_SLOT,
                spot.waterX + 0.5D, spot.waterY + 0.85D, spot.waterZ + 0.5D);
        if (maid.worldObj.spawnEntityInWorld(hook)) maid.swingItem();
        else hook.setDead(); // Undo the constructor's synchronized fishing flag.
    }

    @Override
    public void onSelected(EntityMaid maid) {
        ensureMainhandFishingRod(maid);
    }

    @Override
    public void onDeselected(EntityMaid maid) {
        if (!maid.worldObj.isRemote) {
            java.util.List<EntityMaidFishingHook> hooks=maid.worldObj.getEntitiesWithinAABB(EntityMaidFishingHook.class,maid.boundingBox.expand(32,16,32));
            for(EntityMaidFishingHook hook:hooks)if(hook.getMaidOwner()==maid)hook.setDead();
        }
        if (maid.ridingEntity instanceof EntitySit
                && "fishing".equals(((EntitySit) maid.ridingEntity).getJoyType())) {
            maid.mountEntity(null);
        }
        maid.setFishingHookActive(false);
    }

    private boolean ensureFishingSeat(EntityMaid maid, FishingSpot spot) {
        if (maid.ridingEntity != null) return maid.ridingEntity instanceof EntitySit
                && "fishing".equals(((EntitySit)maid.ridingEntity).getJoyType());
        EntitySit seat = new EntitySit(maid.worldObj, spot.standX + 0.5D, spot.standY + 0.25D,
                spot.standZ + 0.5D, "fishing", spot.standX, spot.standY, spot.standZ);
        double dx = spot.waterX + 0.5D - seat.posX;
        double dz = spot.waterZ + 0.5D - seat.posZ;
        seat.rotationYaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        if (!maid.worldObj.spawnEntityInWorld(seat)) return false;
        maid.mountEntity(seat);
        maid.getNavigator().clearPathEntity();
        return true;
    }

    private boolean ensureMainhandFishingRod(EntityMaid maid) {
        ItemStack held = maid.getMaidEquipmentInventory().getStackInSlot(0);
        if (held != null && held.stackSize > 0 && held.getItem() instanceof net.minecraft.item.ItemFishingRod) return true;
        int sourceSlot = maid.findAvailableInventorySlot(stack -> stack.getItem() instanceof net.minecraft.item.ItemFishingRod);
        if (sourceSlot < 0) return false;
        ItemStack rod = maid.takeOneFromSlot(sourceSlot);
        if (rod == null) return false;
        maid.getMaidEquipmentInventory().setInventorySlotContents(0, rod);
        if (held != null) {
            ItemStack remaining = maid.addToMaidInventory(held);
            if (remaining != null) maid.entityDropItem(remaining, 0.0F);
        }
        maid.getMaidEquipmentInventory().markDirty();
        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean hasActiveHook(EntityMaid maid) {
        List<EntityMaidFishingHook> hooks = maid.worldObj.getEntitiesWithinAABB(
                EntityMaidFishingHook.class, maid.boundingBox.expand(32.0D, 16.0D, 32.0D));
        for (EntityMaidFishingHook hook : hooks) if (!hook.isDead && hook.getMaidOwner() == maid) return true;
        return false;
    }

    private FishingSpot findSpot(EntityMaid maid) {
        int centerX = (int) Math.floor(maid.posX);
        int centerY = (int) Math.floor(maid.posY);
        int centerZ = (int) Math.floor(maid.posZ);
        FishingSpot nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (int x = centerX - 8; x <= centerX + 8; x++) {
            for (int y = centerY - 3; y <= centerY + 3; y++) {
                for (int z = centerZ - 8; z <= centerZ + 8; z++) {
                    if (!maid.worldObj.blockExists(x,y,z) || !maid.isPositionWithinRestriction(x+.5D,y,z+.5D)
                            || maid.worldObj.getBlock(x, y, z).getMaterial() != Material.water
                            || !maid.worldObj.isAirBlock(x, y + 1, z)) continue;
                    for (int side = 0; side < 4; side++) {
                        int sx = x + (side == 0 ? 1 : side == 1 ? -1 : 0);
                        int sz = z + (side == 2 ? 1 : side == 3 ? -1 : 0);
                        for(int standY=y;standY<=y+1;standY++) {
                            if (!maid.isPositionWithinRestriction(sx+.5D,standY,sz+.5D)
                                    || !com.github.tartaricacid.touhoulittlemaid.entity.ai.MaidTeleportSafety.canStand(maid.worldObj,maid,sx+.5D,standY,sz+.5D)) continue;
                            double distance=maid.getDistanceSq(sx+.5D,standY,sz+.5D);
                            if(distance<nearestDistance){nearest=new FishingSpot(sx,standY,sz,x,y,z);nearestDistance=distance;}
                        }
                    }
                }
            }
        }
        return nearest;
    }

    private static final class FishingSpot {
        private final int standX;
        private final int standY;
        private final int standZ;
        private final int waterX;
        private final int waterY;
        private final int waterZ;

        private FishingSpot(int standX, int standY, int standZ, int waterX, int waterY, int waterZ) {
            this.standX = standX;
            this.standY = standY;
            this.standZ = standZ;
            this.waterX = waterX;
            this.waterY = waterY;
            this.waterZ = waterZ;
        }
    }
}

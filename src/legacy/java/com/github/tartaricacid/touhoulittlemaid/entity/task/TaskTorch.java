package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

public final class TaskTorch implements IMaidTask {
    private static final int LOW_BRIGHTNESS = 9;

    @Override
    public String getId() {
        return TaskManager.TORCH_ID;
    }

    @Override
    public void tick(EntityMaid maid) {
        if (maid.worldObj.isRemote || maid.isSitting() || !maid.isPeriodicTick(20)) return;
        int torchSlot = maid.findInventorySlot(Item.getItemFromBlock(Blocks.torch));
        if (torchSlot < 0) return;
        Target target = findDarkPosition(maid);
        if (target == null) return;
        if (maid.getDistanceSq(target.x + 0.5D, target.y + 1, target.z + 0.5D) > 4.0D) {
            maid.getNavigator().tryMoveToXYZ(target.x + 0.5D, target.y + 1, target.z + 0.5D, 0.65D);
            return;
        }
        if (canPlaceAt(maid, target.x, target.y, target.z)) {
            if(!maid.worldObj.setBlock(target.x, target.y + 1, target.z, Blocks.torch, 0, 3))return;
            maid.takeOneFromSlot(torchSlot);
            maid.worldObj.playSoundEffect(target.x + 0.5D, target.y + 1, target.z + 0.5D,
                    "dig.wood", 1.0F, 0.8F);
            maid.workSwing(new net.minecraft.item.ItemStack(Blocks.torch));
        }
    }

    private Target findDarkPosition(EntityMaid maid) {
        int centerX = (int) Math.floor(maid.posX);
        int centerY = (int) Math.floor(maid.posY);
        int centerZ = (int) Math.floor(maid.posZ);
        Target nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (int x = centerX - 8; x <= centerX + 8; x++) {
            for (int y = centerY - 2; y <= centerY + 2; y++) {
                for (int z = centerZ - 8; z <= centerZ + 8; z++) {
                    if (!maid.isPositionWithinRestriction(x + 0.5D, y + 1, z + 0.5D)
                            || !canPlaceAt(maid, x, y, z)) continue;
                    double distance = maid.getDistanceSq(x + 0.5D, y + 1, z + 0.5D);
                    if (distance < nearestDistance) {
                        nearest = new Target(x, y, z);
                        nearestDistance = distance;
                    }
                }
            }
        }
        return nearest;
    }

    private boolean canPlaceAt(EntityMaid maid, int x, int y, int z) {
        return maid.worldObj.getBlock(x, y, z).getMaterial().isSolid()
                && maid.worldObj.isAirBlock(x, y + 1, z)
                && maid.worldObj.getBlockLightValue(x, y + 1, z) < LOW_BRIGHTNESS
                && Blocks.torch.canPlaceBlockAt(maid.worldObj, x, y + 1, z);
    }

    private static final class Target {
        private final int x;
        private final int y;
        private final int z;

        private Target(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}

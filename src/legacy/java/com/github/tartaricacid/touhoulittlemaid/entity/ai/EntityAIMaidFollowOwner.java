package com.github.tartaricacid.touhoulittlemaid.entity.ai;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.entity.ai.EntityAIFollowOwner;

/** Prevents the vanilla follow goal from pulling a maid out of home mode. */
public final class EntityAIMaidFollowOwner extends EntityAIFollowOwner {
    private final EntityMaid maid;
    private final double speed;
    private int pathTicks;

    public EntityAIMaidFollowOwner(EntityMaid maid, double speed, float startDistance, float stopDistance) {
        super(maid, speed, startDistance, stopDistance);
        this.maid = maid;
        this.speed = speed;
    }

    @Override
    public boolean shouldExecute() {
        // Follow owner is a CORE behaviour in the source and is not disabled
        // merely because the current schedule says REST. Home mode and the
        // vanilla sitting check are the actual movement gates.
        return !maid.isMaidSleeping() && maid.isEntityAlive() && !maid.isHomeMode() && !maid.isRiding() && !maid.getLeashed() && super.shouldExecute();
    }

    @Override public void updateTask() {
        net.minecraft.entity.EntityLivingBase owner = maid.getOwner();
        if (owner == null || owner.worldObj != maid.worldObj || maid.isSitting() || maid.isMaidSleeping()
                || maid.isHomeMode() || maid.isRiding() || maid.getLeashed()) return;
        maid.getLookHelper().setLookPositionWithEntity(owner, 10, maid.getVerticalFaceSpeed());
        if (--pathTicks > 0) return;
        pathTicks = 10;
        if (!maid.getNavigator().tryMoveToEntityLiving(owner, speed) && maid.getDistanceSqToEntity(owner) >= 144
                && owner instanceof net.minecraft.entity.player.EntityPlayer)
            maid.safeTeleportNear((net.minecraft.entity.player.EntityPlayer)owner);
    }
    @Override public void startExecuting() { super.startExecuting(); pathTicks = 0; }

    @Override
    public boolean continueExecuting() {
        return !maid.isMaidSleeping() && maid.isEntityAlive() && !maid.isHomeMode() && !maid.isRiding() && !maid.getLeashed() && super.continueExecuting();
    }
}

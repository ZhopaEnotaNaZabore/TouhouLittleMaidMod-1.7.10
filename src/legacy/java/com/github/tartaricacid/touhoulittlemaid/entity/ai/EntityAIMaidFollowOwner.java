package com.github.tartaricacid.touhoulittlemaid.entity.ai;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.entity.ai.EntityAIFollowOwner;

/** Prevents the vanilla follow goal from pulling a maid out of home mode. */
public final class EntityAIMaidFollowOwner extends EntityAIFollowOwner {
    private final EntityMaid maid;

    public EntityAIMaidFollowOwner(EntityMaid maid, double speed, float startDistance, float stopDistance) {
        super(maid, speed, startDistance, stopDistance);
        this.maid = maid;
    }

    @Override
    public boolean shouldExecute() {
        // Follow owner is a CORE behaviour in the source and is not disabled
        // merely because the current schedule says REST. Home mode and the
        // vanilla sitting check are the actual movement gates.
        return !maid.isHomeMode() && !maid.isRiding() && !maid.getLeashed() && super.shouldExecute();
    }

    @Override
    public boolean continueExecuting() {
        return !maid.isHomeMode() && !maid.isRiding() && !maid.getLeashed() && super.continueExecuting();
    }
}

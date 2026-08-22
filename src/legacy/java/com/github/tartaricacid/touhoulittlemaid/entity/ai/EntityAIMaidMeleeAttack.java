package com.github.tartaricacid.touhoulittlemaid.entity.ai;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;

public final class EntityAIMaidMeleeAttack extends EntityAIAttackOnCollide {
    private final EntityMaid maid;

    public EntityAIMaidMeleeAttack(EntityMaid maid) {
        super(maid, 1.0D, true);
        this.maid = maid;
    }

    @Override
    public boolean shouldExecute() {
        return maid.canEngageCombat() && isMeleeTask() && super.shouldExecute();
    }

    @Override
    public boolean continueExecuting() {
        return maid.canEngageCombat() && isMeleeTask() && super.continueExecuting();
    }

    private boolean isMeleeTask() {
        return TaskManager.ATTACK_ID.equals(maid.getTaskId())
                || TaskManager.FEED_ANIMAL_ID.equals(maid.getTaskId());
    }
}

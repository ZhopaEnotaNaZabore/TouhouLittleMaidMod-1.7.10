package com.github.tartaricacid.touhoulittlemaid.entity.ai;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import net.minecraft.entity.ai.EntityAIArrowAttack;

public final class EntityAIMaidRangedAttack extends EntityAIArrowAttack {
    private final EntityMaid maid;

    public EntityAIMaidRangedAttack(EntityMaid maid) {
        super(maid, 0.8D, 20, 40, 16.0F);
        this.maid = maid;
    }

    @Override
    public boolean shouldExecute() {
        return maid.canEngageCombat() && maid.hasRangedWeaponForCurrentTask()
                && super.shouldExecute();
    }

    @Override
    public boolean continueExecuting() {
        return maid.canEngageCombat() && maid.hasRangedWeaponForCurrentTask()
                && super.continueExecuting();
    }
}

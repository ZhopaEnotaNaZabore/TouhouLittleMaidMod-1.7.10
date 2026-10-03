package com.github.tartaricacid.touhoulittlemaid.entity.ai;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.CombatTargeting;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;

/** Defends the owner when the owner is attacked (OwnerHurtByTarget parity). */
public final class EntityAIMaidOwnerHurtByTarget extends EntityAIOwnerHurtByTarget {
    private final EntityMaid maid;

    public EntityAIMaidOwnerHurtByTarget(EntityMaid maid) {
        super(maid);
        this.maid = maid;
    }

    @Override public boolean shouldExecute() {
        return canRespond() && super.shouldExecute();
    }

    @Override public void startExecuting() {
        super.startExecuting();
        if (!CombatTargeting.isValidTarget(maid, maid.getAttackTarget(),
                TaskManager.combatRange(maid.getTaskId()))) maid.setAttackTarget(null);
    }

    private boolean canRespond() {
        return TaskManager.isCombatTask(maid.getTaskId()) && maid.canRunCombatAI()
                && !maid.isSitting() && maid.isWorkingNow();
    }
}

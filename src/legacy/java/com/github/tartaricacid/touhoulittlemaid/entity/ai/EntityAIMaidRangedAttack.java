package com.github.tartaricacid.touhoulittlemaid.entity.ai;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import net.minecraft.entity.ai.EntityAIArrowAttack;

public final class EntityAIMaidRangedAttack extends EntityAIArrowAttack {
    private final EntityMaid maid;
    private final String taskId;
    private final double range;

    public EntityAIMaidRangedAttack(EntityMaid maid) {
        this(maid, TaskManager.RANGED_ATTACK_ID);
    }

    public EntityAIMaidRangedAttack(EntityMaid maid, String taskId) {
        super(maid, 0.8D, 20, 40, (float) TaskManager.combatRange(taskId));
        this.maid = maid;
        this.taskId = taskId;
        this.range = TaskManager.combatRange(taskId);
    }

    @Override
    public boolean shouldExecute() {
        return taskId.equals(maid.getTaskId()) && maid.canRunCombatAI() && maid.hasRangedWeaponForCurrentTask()
                && super.shouldExecute();
    }

    @Override
    public boolean continueExecuting() {
        return taskId.equals(maid.getTaskId()) && maid.canRunCombatAI() && maid.hasRangedWeaponForCurrentTask()
                && super.continueExecuting();
    }
    @Override
    public void updateTask() {
        net.minecraft.entity.EntityLivingBase target = maid.getAttackTarget();
        maid.updateRangedPresentation(target != null && maid.getEntitySenses().canSee(target)
                && maid.getDistanceSqToEntity(target) <= range * range);
        super.updateTask();
    }
    @Override
    public void resetTask() {
        super.resetTask();
        maid.updateRangedPresentation(false);
    }
}

package com.github.tartaricacid.touhoulittlemaid.entity.ai;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;

public final class EntityAIMaidMeleeAttack extends EntityAIAttackOnCollide {
    private final EntityMaid maid;
    private int nextAttackTick;
    private int nextPathTick;

    public EntityAIMaidMeleeAttack(EntityMaid maid) {
        super(maid, 1.0D, true);
        this.maid = maid;
    }

    @Override
    public boolean shouldExecute() {
        return maid.canRunCombatAI() && isMeleeTask() && super.shouldExecute();
    }

    @Override
    public boolean continueExecuting() {
        return maid.canRunCombatAI() && isMeleeTask() && super.continueExecuting();
    }

    @Override
    public void updateTask() {
        net.minecraft.entity.EntityLivingBase target = maid.getAttackTarget();
        if (target == null || !maid.canRunCombatAI() || !isMeleeTask()) return;
        maid.getLookHelper().setLookPositionWithEntity(target, 30, 30);
        if (maid.ticksExisted >= nextPathTick) {
            nextPathTick = maid.ticksExisted + 4 + maid.getRNG().nextInt(7);
            if (!maid.getNavigator().tryMoveToEntityLiving(target, 1.0D)) nextPathTick += 15;
        }
        tryMeleeAttack(target);
    }

    /** One presentation event per attack, including an empty hand. The 1.7
     * collision task only swings held items and its <=20 gate can restart the
     * animation every few ticks instead of letting an authored swing finish. */
    void tryMeleeAttack(net.minecraft.entity.EntityLivingBase target) {
        if (maid.ticksExisted < nextAttackTick || !maid.canRunCombatAI() || !isMeleeTask()
                || !com.github.tartaricacid.touhoulittlemaid.entity.task.CombatTargeting.isValidTarget(
                        maid, target, TaskManager.combatRange(maid.getTaskId()))) return;
        double reach = maid.width * 2.0F * maid.width * 2.0F + target.width;
        if (maid.getDistanceSq(target.posX, target.boundingBox.minY, target.posZ) > reach
                || !maid.getEntitySenses().canSee(target)) return;
        nextAttackTick = maid.ticksExisted + 20;
        maid.swingItem();
        maid.attackEntityAsMob(target);
    }

    private boolean isMeleeTask() {
        return TaskManager.ATTACK_ID.equals(maid.getTaskId())
                || TaskManager.FEED_ANIMAL_ID.equals(maid.getTaskId());
    }
}

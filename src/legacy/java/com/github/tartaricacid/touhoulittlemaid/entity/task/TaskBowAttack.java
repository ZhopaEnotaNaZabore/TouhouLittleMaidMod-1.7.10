package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.init.Items;

public final class TaskBowAttack implements IMaidTask {
    @Override
    public String getId() {
        return TaskManager.RANGED_ATTACK_ID;
    }

    @Override
    public void tick(EntityMaid maid) {
        LegacyTaskEquipUtil.ensureMainhand(maid, Items.bow);
        if (maid.ticksExisted % 10 == 0) {
            if (maid.hasBowAndArrow() && !maid.isSitting()) {
                CombatTargeting.updateTarget(maid, 16.0D);
            } else {
                maid.setAttackTarget(null);
            }
        }
    }

    @Override
    public void onSelected(EntityMaid maid) { LegacyTaskEquipUtil.ensureMainhand(maid, Items.bow); }

    @Override
    public void onDeselected(EntityMaid maid) {
        maid.setAttackTarget(null);
    }
}

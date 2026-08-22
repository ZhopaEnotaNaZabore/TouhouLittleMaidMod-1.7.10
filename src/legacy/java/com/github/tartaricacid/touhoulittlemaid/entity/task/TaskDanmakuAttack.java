package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.ModItems;

public final class TaskDanmakuAttack implements IMaidTask {
    @Override
    public String getId() { return TaskManager.DANMAKU_ATTACK_ID; }

    @Override
    public void tick(EntityMaid maid) {
        LegacyTaskEquipUtil.ensureMainhand(maid, ModItems.HAKUREI_GOHEI, ModItems.SANAE_GOHEI);
        if (maid.ticksExisted % 10 == 0) {
            if (maid.hasGohei() && !maid.isSitting()) CombatTargeting.updateTarget(maid, 24.0D);
            else maid.setAttackTarget(null);
        }
    }

    @Override
    public void onSelected(EntityMaid maid) {
        LegacyTaskEquipUtil.ensureMainhand(maid, ModItems.HAKUREI_GOHEI, ModItems.SANAE_GOHEI);
    }

    @Override
    public void onDeselected(EntityMaid maid) { maid.setAttackTarget(null); }
}

package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;

public final class TaskAttack implements IMaidTask {
    @Override
    public String getId() {
        return TaskManager.ATTACK_ID;
    }

    @Override
    public void tick(EntityMaid maid) {
        LegacyTaskEquipUtil.ensureAttackWeapon(maid);
        if (maid.ticksExisted % 10 != 0) {
            return;
        }
        if (!maid.isSitting() && LegacyTaskEquipUtil.isAttackWeapon(
                maid.getMaidEquipmentInventory().getStackInSlot(0))) {
            CombatTargeting.updateTarget(maid, 12.0D);
        } else {
            maid.setAttackTarget(null);
        }
    }

    @Override
    public void onSelected(EntityMaid maid) {
        LegacyTaskEquipUtil.ensureAttackWeapon(maid);
    }

    @Override
    public void onDeselected(EntityMaid maid) {
        maid.setAttackTarget(null);
    }
}

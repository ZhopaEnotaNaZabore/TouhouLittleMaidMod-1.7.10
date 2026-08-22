package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.ModItems;

public final class TaskLegacyRangedAttack implements IMaidTask {
    private final String id;
    public TaskLegacyRangedAttack(String id) { this.id = id; }
    @Override public String getId() { return id; }
    @Override public void onSelected(EntityMaid maid) { ensureWeapon(maid); }
    @Override public void tick(EntityMaid maid) {
        ensureWeapon(maid);
        if (maid.ticksExisted % 10 == 0) {
            if (maid.hasRangedWeaponForCurrentTask() && !maid.isSitting()) CombatTargeting.updateTarget(maid, 20);
            else maid.setAttackTarget(null);
        }
    }
    private void ensureWeapon(EntityMaid maid) {
        LegacyTaskEquipUtil.ensureMainhand(maid,
                TaskManager.CROSSBOW_ATTACK_ID.equals(id) ? ModItems.CROSSBOW : ModItems.TRIDENT);
    }
    @Override public void onDeselected(EntityMaid maid) { maid.setAttackTarget(null); }
}

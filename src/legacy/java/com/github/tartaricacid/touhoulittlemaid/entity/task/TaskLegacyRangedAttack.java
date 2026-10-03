package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;

/** Remains inert without a compatible optional weapon. */
public final class TaskLegacyRangedAttack implements IMaidTask {
    private final String id;
    public TaskLegacyRangedAttack(String id) { this.id = id; }
    @Override public String getId() { return id; }
    @Override public void tick(EntityMaid maid) {
        if(!com.github.tartaricacid.touhoulittlemaid.compat.LegacyTConstruct.installed())return;
        LegacyTaskEquipUtil.ensureRangedWeapon(maid);
        if(maid.ticksExisted%10!=0)return;
        if(maid.hasRangedWeaponForCurrentTask())CombatTargeting.updateTarget(maid,TaskManager.combatRange(id));
        else maid.setAttackTarget(null);
    }
    @Override public void onSelected(EntityMaid maid) {
        LegacyTaskEquipUtil.ensureRangedWeapon(maid);
        maid.setAttackTarget(null);
        maid.getNavigator().clearPathEntity();
    }
}

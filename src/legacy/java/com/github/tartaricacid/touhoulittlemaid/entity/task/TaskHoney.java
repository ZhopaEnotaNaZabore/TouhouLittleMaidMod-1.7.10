package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;

/** Registered placeholder: required vanilla mechanics do not exist in 1.7.10. */
public final class TaskHoney implements IMaidTask {
    @Override public String getId() { return TaskManager.HONEY_ID; }
    @Override public void tick(EntityMaid maid) { }
    @Override public void onSelected(EntityMaid maid) {
        maid.setAttackTarget(null);
        maid.getNavigator().clearPathEntity();
    }
}

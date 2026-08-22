package com.github.tartaricacid.touhoulittlemaid.api.task;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;

/** Runtime-neutral contract used by the 1.7.10 task registry. */
public interface IMaidTask {
    String getId();

    void tick(EntityMaid maid);

    default void onSelected(EntityMaid maid) {
    }

    default void onDeselected(EntityMaid maid) {
    }
}

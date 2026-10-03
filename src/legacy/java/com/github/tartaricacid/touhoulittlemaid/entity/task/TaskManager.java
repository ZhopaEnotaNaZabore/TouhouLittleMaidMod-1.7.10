package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TaskManager {
    public static final String IDLE_ID = "touhou_little_maid:idle";
    public static final String ATTACK_ID = "touhou_little_maid:attack";
    public static final String RANGED_ATTACK_ID = "touhou_little_maid:ranged_attack";
    public static final String DANMAKU_ATTACK_ID = "touhou_little_maid:danmaku_attack";
    public static final String FARM_ID = "touhou_little_maid:farm";
    public static final String SUGAR_CANE_ID = "touhou_little_maid:sugar_cane";
    public static final String MELON_ID = "touhou_little_maid:melon";
    public static final String COCOA_ID = "touhou_little_maid:cocoa";
    public static final String GRASS_ID = "touhou_little_maid:grass";
    public static final String SNOW_ID = "touhou_little_maid:snow";
    public static final String FEED_OWNER_ID = "touhou_little_maid:feed";
    public static final String FEED_ANIMAL_ID = "touhou_little_maid:feed_animal";
    public static final String SHEARS_ID = "touhou_little_maid:shears";
    public static final String MILK_ID = "touhou_little_maid:milk";
    public static final String TORCH_ID = "touhou_little_maid:torch";
    public static final String FISHING_ID = "touhou_little_maid:fishing";
    public static final String EXTINGUISHING_ID = "touhou_little_maid:extinguishing";
    public static final String CROSSBOW_ATTACK_ID = "touhou_little_maid:crossbow_attack";
    public static final String TRIDENT_ATTACK_ID = "touhou_little_maid:trident_attack";
    public static final String HONEY_ID = "touhou_little_maid:honey";
    public static final String BOARD_GAMES_ID = "touhou_little_maid:board_games";
    public static final String MINER_ID = "touhou_little_maid:miner";

    private static final Map<String, IMaidTask> TASKS = new LinkedHashMap<String, IMaidTask>();

    static {
        register(new TaskIdle());
        register(new TaskAttack());
        register(new TaskBowAttack());
        register(new TaskDanmakuAttack());
        register(new TaskFarm());
        register(new TaskSugarCane());
        register(new TaskMelon());
        register(new TaskCocoa());
        register(new TaskGrass());
        register(new TaskSnow());
        register(new TaskFeedOwner());
        register(new TaskFeedAnimal());
        register(new TaskShears());
        register(new TaskMilk());
        register(new TaskTorch());
        register(new TaskFishing());
        register(new TaskExtinguishing());
        register(new TaskLegacyRangedAttack(CROSSBOW_ATTACK_ID));
        register(new TaskLegacyRangedAttack(TRIDENT_ATTACK_ID));
        register(new TaskHoney());
        register(new TaskBoardGames());
        register(new TaskMiner());
    }

    private TaskManager() {
    }

    public static void register(IMaidTask task) {
        if (task == null || task.getId() == null || task.getId().isEmpty()) {
            throw new IllegalArgumentException("A maid task must have an id");
        }
        if (TASKS.containsKey(task.getId())) {
            throw new IllegalArgumentException("Duplicate maid task: " + task.getId());
        }
        TASKS.put(task.getId(), task);
    }

    public static IMaidTask get(String id) {
        IMaidTask task = TASKS.get(id);
        return task == null ? TASKS.get(IDLE_ID) : task;
    }

    public static Map<String, IMaidTask> getTasks() {
        return Collections.unmodifiableMap(TASKS);
    }

    public static IMaidTask getByIndex(int index) {
        IMaidTask[] tasks = TASKS.values().toArray(new IMaidTask[TASKS.size()]);
        int wrapped = ((index % tasks.length) + tasks.length) % tasks.length;
        return tasks[wrapped];
    }

    public static int indexOf(String id) {
        int index = 0;
        for (String taskId : TASKS.keySet()) {
            if (taskId.equals(id)) return index;
            index++;
        }
        return 0;
    }

    public static void switchTask(EntityMaid maid, String id) {
        IMaidTask oldTask = get(maid.getTaskId());
        IMaidTask newTask = get(id);
        if (oldTask == newTask) {
            return;
        }
        oldTask.onDeselected(maid);
        maid.setAttackTarget(null);
        maid.getNavigator().clearPathEntity();
        maid.setTaskIdInternal(newTask.getId());
        newTask.onSelected(maid);
    }

    public static boolean isCombatTask(String id) {
        return ATTACK_ID.equals(id) || RANGED_ATTACK_ID.equals(id)
                || DANMAKU_ATTACK_ID.equals(id);
    }

    public static double combatRange(String id) {
        if (DANMAKU_ATTACK_ID.equals(id)) return 24.0D;
        if (RANGED_ATTACK_ID.equals(id)) return 16.0D;
        if (CROSSBOW_ATTACK_ID.equals(id) || TRIDENT_ATTACK_ID.equals(id)) return 20.0D;
        return 12.0D;
    }
}

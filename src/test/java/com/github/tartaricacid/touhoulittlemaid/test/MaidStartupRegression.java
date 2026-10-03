package com.github.tartaricacid.touhoulittlemaid.test;

import cpw.mods.fml.common.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;

/** Exercise the exact postInit profession checks with both FML mod-list states. */
public final class MaidStartupRegression {
    public static void run() throws Exception {
        Loader loader = Loader.instance();
        Field mods = Loader.class.getDeclaredField("namedMods"); mods.setAccessible(true);
        Field controller = Loader.class.getDeclaredField("modController"); controller.setAccessible(true);
        Object savedMods = mods.get(loader), savedController = controller.get(loader);
        try {
            Map<String, ModContainer> installed = new HashMap<String, ModContainer>();
            mods.set(loader, installed);controller.set(loader, new LoadController(loader));
            for (boolean enabled : new boolean[] { false, true, false }) {
                installed.clear();
                if (enabled) {
                    ModMetadata metadata = new ModMetadata();metadata.modId="TConstruct";metadata.name="TConstruct";
                    installed.put("TConstruct", new DummyModContainer(metadata));
                }
                if (Loader.isModLoaded("TConstruct") != enabled) throw new AssertionError("invalid FML fixture");
                LegacyPortSelfTest.validateProfessions();
                if (TaskManager.isCombatTask(TaskManager.CROSSBOW_ATTACK_ID) != enabled
                        || TaskManager.isCombatTask(TaskManager.TRIDENT_ATTACK_ID) != enabled
                        || TaskManager.isCombatTask(TaskManager.HONEY_ID)) throw new AssertionError("profession activation");
            }
        } finally {mods.set(loader,savedMods);controller.set(loader,savedController);}
        System.out.println("Maid startup regression: PASS (postInit profession checks: TC absent, present, removed)");
    }
}

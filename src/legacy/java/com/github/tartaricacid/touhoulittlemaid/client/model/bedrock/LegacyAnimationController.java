package com.github.tartaricacid.touhoulittlemaid.client.model.bedrock;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

/** Source-aligned state controller for common Bedrock/Gecko maid bones. */
public final class LegacyAnimationController {
    private static final String ANIMATION_ROOT = "touhou_little_maid:animation/maid/default/";
    private static final String[] TASK_NAMES = {"attack", "danmaku_attack", "farm", "feed_animal", "idle",
            "milk", "shears", "sugar_cane", "cocoa", "extinguishing", "feed", "grass", "melon",
            "ranged_attack", "snow", "torch"};
    private static final String[] TASK_BONES = {"attack", "danmakuAttack", "farm", "feedAnimal", "idle",
            "milk", "shears", "sugarCane", "cocoa", "extinguishing", "feed", "grass", "melon",
            "rangedAttack", "snow", "torch"};
    private static final String[] ARMOR_BONES = {"helmet", "chestPlate", "chestPlateLeft", "chestPlateMiddle",
            "chestPlateRight", "leggings", "leggingsLeft", "leggingsMiddle", "leggingsRight", "bootsLeft",
            "bootsRight"};
    private LegacyAnimationController() { }

    public static void apply(LegacyBedrockModel model, Entity entity, float age, float swingProgress) {
        float slow = MathHelper.sin(age * 0.05F);
        model.addRotation("armLeft", 0, 0, slow * 0.05F);
        model.addRotation("armRight", 0, 0, -slow * 0.05F);
        model.addRotation("LeftArm", 0, 0, slow * 0.05F);
        model.addRotation("RightArm", 0, 0, -slow * 0.05F);
        model.addRotation("hairPonytailSwing", 0, 0, slow * 0.06F);
        model.addRotation("hairLeftSwing", 0, 0, slow * 0.04F);
        model.addRotation("hairRightSwing", 0, 0, -slow * 0.04F);
        model.addRotation("tail", MathHelper.sin(age * 0.2F) * 0.05F, 0,
                MathHelper.cos(age * 0.2F) * 0.1F);
        model.addRotation("wingLeft", 0, -MathHelper.cos(age * 0.3F) * 0.2F, 0);
        model.addRotation("wingRight", 0, MathHelper.cos(age * 0.3F) * 0.2F, 0);
        model.addOffset("sinFloat", 0, MathHelper.sin(age * 0.08F) * 0.35F, 0);

        if (swingProgress > 0) {
            float eased = 1.0F - (float) Math.pow(1.0F - swingProgress, 4);
            float swing = MathHelper.sin(eased * (float) Math.PI);
            float returnSwing = MathHelper.sin(swingProgress * (float) Math.PI) * -0.525F;
            model.addRotation("armRight", -(swing * 1.2F + returnSwing), 0,
                    MathHelper.sin(swingProgress * (float) Math.PI) * -0.4F);
            model.addRotation("RightArm", -(swing * 1.2F + returnSwing), 0,
                    MathHelper.sin(swingProgress * (float) Math.PI) * -0.4F);
        }

        if (!(entity instanceof EntityMaid)) return;
        EntityMaid maid = (EntityMaid) entity;
        LegacyMaidModelRegistry.Entry modelEntry = LegacyMaidModelRegistry.INSTANCE.getEntry(maid.getModelId());
        boolean sleeping = maid.isMaidSleeping();
        boolean sitting = maid.isSitting() || maid.ridingEntity != null;

        if (sitting && uses(modelEntry, "sit/default.js", true)) {
            model.setBaseRotation("legLeft", -1.134F, 0, -0.262F);
            model.setBaseRotation("legRight", -1.134F, 0, 0.262F);
            model.setBaseRotation("armLeft", -0.798F, 0, 0.274F);
            model.setBaseRotation("armRight", -0.798F, 0, -0.274F);
            model.setBaseRotation("LeftLeg", -1.134F, 0, -0.262F);
            model.setBaseRotation("RightLeg", -1.134F, 0, 0.262F);
            model.setBaseRotation("LeftArm", -0.798F, 0, 0.274F);
            model.setBaseRotation("RightArm", -0.798F, 0, -0.274F);
        }
        if (sitting && uses(modelEntry, "sit/skirt_rotation.js", true))
            model.setBaseRotation("sittingRotationSkirt", -0.567F, 0, 0);
        if (sitting && uses(modelEntry, "sit/skirt_rotation_swing.js", false))
            model.setBaseRotation("sittingRotationSwingSkirt", -0.567F, 0, 0);

        if (uses(modelEntry, "head/beg.js", true)) {
            if (maid.isBegging()) { model.addRotation("head", 0, 0, 0.139F); model.addRotation("Head", 0, 0, 0.139F); }
            model.setVisible("begShow", maid.isBegging());
        }
        if (sleeping && uses(modelEntry, "head/default.js", true)) {
            model.setBaseRotation("head", (float) Math.toRadians(15), 0, 0);
            model.setBaseRotation("Head", (float) Math.toRadians(15), 0, 0);
        }

        long blinkSeed = Math.abs(maid.getUniqueID().getLeastSignificantBits()) % 10L;
        float blinkTime = (age + blinkSeed) % 60.0F;
        boolean blink = sleeping || (55.0F < blinkTime && blinkTime < 60.0F);
        if (uses(modelEntry, "head/blink.js", true)) {
            model.setVisible("blink", blink);
            model.setVisible("blink2", blink);
            model.setVisible("Blink", blink);
            model.setVisible("Blink2", blink);
        }
        if (uses(modelEntry, "head/default.js", true)) model.setVisible("hat", !sleeping);
        if (uses(modelEntry, "wing/default.js", true)) {
            model.setVisible("wingLeft", !sleeping);
            model.setVisible("wingRight", !sleeping);
        }
        if (uses(modelEntry, "tail/default.js", true)) model.setVisible("tail", !sleeping);
        if (uses(modelEntry, "sleep/default.js", false)) {
            model.setVisible("sleepHide", !sleeping);
            model.setVisible("sleepShow", sleeping);
        }

        if (uses(modelEntry, "sit/skirt_hidden.js", false)) {
            model.setVisible("sittingHiddenSkirt", !sitting);
            model.setVisible("_sittingHiddenSkirt", sitting);
        }

        boolean hasBackpack = !"empty".equals(maid.getBackpackType());
        if (uses(modelEntry, "status/backpack.js", false)) {
            model.setVisible("backpackShow", hasBackpack);
            model.setVisible("backpackHidden", !hasBackpack);
        }

        applyTaskVisibility(model, maid, modelEntry);
        applyArmorVisibility(model, maid, modelEntry);
        applyHecatiaVisibility(model, maid, modelEntry);
        if (uses(modelEntry, "health/less_show.js", false)) {
            float healthRatio = maid.getMaxHealth() <= 0 ? 1.0F : maid.getHealth() / maid.getMaxHealth();
            model.setVisible("healthLessQuarterShow", healthRatio <= 0.25F);
            model.setVisible("healthLessHalfShow", healthRatio <= 0.5F);
            model.setVisible("healthLessThreeQuartersShow", healthRatio <= 0.75F);
        }

        // Exact steady fishing pose from hold_mainhand:fishing. Unlike the
        // one-tick swing this remains active for the complete hook lifetime.
        if (maid.hasFishingHook()) {
            setFishingRotation(model, "armLeft", -56.72671F, 19.95595F, 11.62581F);
            setFishingRotation(model, "armRight", -54.68870F, -15.93333F, -15.34595F);
            setFishingRotation(model, "LeftArm", -56.72671F, 19.95595F, 11.62581F);
            setFishingRotation(model, "RightArm", -54.68870F, -15.93333F, -15.34595F);
            setFishingRotation(model, "LeftForeArm", -17.5F, 0, 0);
            setFishingRotation(model, "RightForeArm", -20.0F, 0, 0);
            setFishingRotation(model, "LeftHandLocator", 42.5F, 0, 0);
            setFishingRotation(model, "RightHandLocator", 42.5F, 0, 0);
            setFishingRotation(model, "Arms", 0, 30.0F, 0);
            setFishingRotation(model, "Arm", 0, 30.0F, 0);
        }
    }

    private static void applyTaskVisibility(LegacyBedrockModel model, EntityMaid maid,
                                            LegacyMaidModelRegistry.Entry entry) {
        String taskId = maid.getTaskId();
        for (int i = 0; i < TASK_NAMES.length; i++) {
            if (!uses(entry, "task/" + TASK_NAMES[i] + ".js", false)) continue;
            boolean active = taskId.endsWith(":" + TASK_NAMES[i]);
            model.setVisible(TASK_BONES[i] + "Hidden", !active);
            model.setVisible(TASK_BONES[i] + "Show", active);
        }
    }

    private static void applyArmorVisibility(LegacyBedrockModel model, EntityMaid maid,
                                             LegacyMaidModelRegistry.Entry entry) {
        boolean armorDefault = uses(entry, "armor/default.js", true);
        boolean armorReverse = uses(entry, "armor/reverse.js", true);
        if (!armorDefault && !armorReverse) return;
        boolean[] equipped = {maid.getEquipmentInSlot(4) != null,
                maid.getEquipmentInSlot(3) != null, maid.getEquipmentInSlot(3) != null,
                maid.getEquipmentInSlot(3) != null, maid.getEquipmentInSlot(3) != null,
                maid.getEquipmentInSlot(2) != null, maid.getEquipmentInSlot(2) != null,
                maid.getEquipmentInSlot(2) != null, maid.getEquipmentInSlot(2) != null,
                maid.getEquipmentInSlot(1) != null, maid.getEquipmentInSlot(1) != null};
        for (int i = 0; i < ARMOR_BONES.length; i++) {
            if (armorDefault) model.setVisible(ARMOR_BONES[i], equipped[i]);
            if (armorReverse) model.setVisible("_" + ARMOR_BONES[i], !equipped[i]);
        }
    }

    /** Exact mutually-exclusive head/body layers used by Hecatia in the source animation. */
    private static void applyHecatiaVisibility(LegacyBedrockModel model, EntityMaid maid,
                                               LegacyMaidModelRegistry.Entry entry) {
        if (!uses(entry, "touhou_little_maid:animation/special/hecatia_dimension.js", false)) return;
        int dimension = maid.dimension;
        boolean earth = dimension == 0;
        boolean moon = dimension == 1;
        boolean other = !earth && !moon;
        boolean helmet = maid.getEquipmentInSlot(4) != null;

        model.setVisible("earthHair", earth);
        model.setVisible("logoEarth", earth);
        model.setVisible("earthTop", earth && !helmet);
        model.setVisible("earthSideLeft", !earth);
        model.setVisible("earthSideRight", false);

        model.setVisible("moonHair", moon);
        model.setVisible("logoMoon", moon);
        model.setVisible("moonTop", moon && !helmet);
        model.setVisible("moonSideLeft", false);
        model.setVisible("moonSideRight", !moon);

        model.setVisible("otherHair", other);
        model.setVisible("logoOther", other);
        model.setVisible("otherTop", other && !helmet);
        model.setVisible("otherSideLeft", earth);
        model.setVisible("otherSideRight", moon);
    }

    private static boolean uses(LegacyMaidModelRegistry.Entry entry, String relativePath,
                                boolean partOfDefaultSet) {
        String path = relativePath.indexOf(':') >= 0 ? relativePath : ANIMATION_ROOT + relativePath;
        return entry == null || entry.usesAnimation(path, partOfDefaultSet);
    }

    private static void setFishingRotation(LegacyBedrockModel model, String bone,
                                           float x, float y, float z) {
        float toRadians = (float) Math.PI / 180.0F;
        model.setBaseRotation(bone, x * toRadians, y * toRadians, z * toRadians);
    }
}

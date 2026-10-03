package com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.animation;

import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.animation.LegacyAnimationPose.Bone;

/** Direct-constructor implementation of the named SRC inner animations, in authored order. */
public final class LegacyMaidAnimations {
    private static final float DEG = (float) Math.PI / 180F;
    private static final String BASE = "touhou_little_maid:animation/base/";
    private static final String SPECIAL = "touhou_little_maid:animation/special/";
    private static final String[] TASKS = {"attack", "danmaku_attack", "farm", "feed_animal", "idle", "milk",
            "shears", "sugar_cane", "cocoa", "extinguishing", "feed", "grass", "melon", "ranged_attack", "snow", "torch"};
    private static final String[] TASK_BONES = {"attack", "danmakuAttack", "farm", "feedAnimal", "idle", "milk",
            "shears", "sugarCane", "cocoa", "extinguishing", "feed", "grass", "melon", "rangedAttack", "snow", "torch"};
    private static final String[] ARMOR = {"helmet", "chestPlate", "chestPlateLeft", "chestPlateMiddle", "chestPlateRight",
            "leggings", "leggingsLeft", "leggingsMiddle", "leggingsRight", "bootsLeft", "bootsRight"};
    private LegacyMaidAnimations() { }

    public static void apply(LegacyAnimationPose p, LegacyAnimationProfile profile, LegacyAnimationFrame f) {
        if (profile.gecko) return;
        for (String id : profile.animations) applyOne(p, id, f);
    }

    /** Unknown scripts are not executed or replaced by a generic animation. */
    public static boolean applyOne(LegacyAnimationPose p, String id, LegacyAnimationFrame f) {
        if (id.startsWith(LegacyAnimationProfile.ROOT)) {
            String key = id.substring(LegacyAnimationProfile.ROOT.length());
            if ("head/default.js".equals(key)) {
                Bone b = p.get("head");
                if (b != null) { b.x = (f.sleeping ? 15 : f.pitch) * DEG; b.y = f.yaw * DEG; }
                p.visible("hat", !f.sleeping);
            } else if ("head/blink.js".equals(key)) {
                float time = (f.age + Math.abs(f.uuidSeed % 10)) % 60;
                boolean blink = f.sleeping || (55 < time && time < 60);
                p.visible("blink", blink); p.visible("blink2", blink);
            } else if ("head/beg.js".equals(key)) {
                Bone head = p.get("head"), ahoge = p.get("ahoge");
                if (head != null) head.z = f.begging ? 0.139F : head.baseZ;
                if (ahoge != null && f.begging) {
                    ahoge.x = ahoge.baseX + cos(f.age) * .05F;
                    ahoge.z = ahoge.baseZ + sin(f.age) * .05F;
                }
                p.visible("begShow", f.begging);
            } else if ("head/music_shake.js".equals(key) || "status/backpack_level.js".equals(key)) {
                // These particular SRC implementations are explicitly deprecated no-ops.
            } else if ("head/hurt.js".equals(key)) p.visible("hurtBlink", f.hurt);
            else if ("head/hair_ponytail_swing.js".equals(key)) baseZ(p, "hairPonytailSwing", sin(f.age * .05) * .06F);
            else if ("head/hair_swing.js".equals(key)) {
                baseZ(p, "hairLeftSwing", sin(f.age * .05) * .04F);
                baseZ(p, "hairRightSwing", -sin(f.age * .05) * .04F);
            } else if ("head/ear_shake.js".equals(key) || "head/ear_beg_shake.js".equals(key)) {
                boolean beg = key.contains("beg_");
                float time = (f.age + Math.abs(f.uuidSeed % 10)) % 40;
                float z = (!beg || f.begging) && time < Math.PI * 4 ? Math.abs(sin(time * .25)) * .4F : 0;
                baseZ(p, beg ? "earLeftBegShake" : "earLeftShake", z);
                baseZ(p, beg ? "earRightBegShake" : "earRightShake", -z);
            } else if ("leg/default.js".equals(key)) {
                boolean farm = "farm".equals(f.task) && f.swing > 0;
                if (farm) { p.translate(0, .0713625F, -.35876875F); p.rotate(22.5F, 1, 0, 0); }
                leg(p.get("legLeft"), cos(f.limbSwing * .67) * .3F * f.limbAmount, farm);
                leg(p.get("legRight"), -cos(f.limbSwing * .67) * .3F * f.limbAmount, farm);
            } else if ("arm/default.js".equals(key)) {
                arm(p.get("armLeft"), true, f); arm(p.get("armRight"), false, f);
            } else if ("arm/swing.js".equals(key)) {
                if (f.ranged && !f.mainId.isEmpty()) {
                    if("spear".equals(f.use)) xy(p.get("armRight"), -3.14F+f.pitch*DEG,0);
                    else if("crossbow".equals(f.use)||"charged_crossbow".equals(f.mainCategory)){xy(p.get("armLeft"),-1.3F+f.pitch*DEG,.6F);xy(p.get("armRight"),-1.57F+f.pitch*DEG,-.3F);}
                    else {xy(p.get("armLeft"), -1.396F, .785F); xy(p.get("armRight"), -1.396F, -.174F);}
                }
            } else if ("arm/vertical.js".equals(key)) {
                vertical(p, "armLeftVertical", "armLeft"); vertical(p, "armRightVertical", "armRight");
            } else if ("leg/vertical.js".equals(key)) {
                vertical(p, "legLeftVertical", "legLeft"); vertical(p, "legRightVertical", "legRight");
            } else if ("sit/default.js".equals(key) || "sit/no_leg.js".equals(key)) {
                Bone head = p.get("head"); if (head != null) head.offsetY = 0;
                if (f.riding) riding(p);
                else if (f.sitting) { sittingArms(p); if ("sit/default.js".equals(key)) riding(p); }
            } else if ("sit/skirt_rotation.js".equals(key)) {
                Bone b = p.get("sittingRotationSkirt"); if (b != null) b.x = f.seated() ? -.567F : b.baseX;
            } else if ("sit/skirt_rotation_swing.js".equals(key)) {
                Bone b = p.get("sittingRotationSwingSkirt");
                if (b != null) { b.x = f.seated() ? -.567F : b.baseX; b.z = b.baseZ + (f.seated() ? 0 : sin(f.age * .05) * .03F); }
            } else if ("sit/skirt_hidden.js".equals(key)) {
                p.visible("sittingHiddenSkirt", !f.seated()); p.visible("_sittingHiddenSkirt", f.seated());
            } else if ("wing/default.js".equals(key)) {
                Bone l = p.get("wingLeft"), r = p.get("wingRight");
                if (l != null) l.y = l.baseY - cos(f.age * .3) * .2F;
                if (r != null) r.y = r.baseY + cos(f.age * .3) * .2F;
                p.visible("wingLeft", !f.sleeping); p.visible("wingRight", !f.sleeping);
            } else if ("tail/default.js".equals(key)) {
                Bone b = p.get("tail"); if (b != null) { b.x = b.baseX + sin(f.age * .2) * .05F; b.z = b.baseZ + cos(f.age * .2) * .1F; }
                p.visible("tail", !f.sleeping);
            } else if ("sleep/default.js".equals(key)) {
                p.visible("sleepHide", !f.sleeping); p.visible("sleepShow", f.sleeping);
            } else if ("status/backpack.js".equals(key)) {
                p.visible("backpackShow", f.backpack); p.visible("backpackHidden", !f.backpack);
            } else if ("armor/default.js".equals(key) || "armor/reverse.js".equals(key)) {
                boolean reverse = key.contains("reverse");
                for (int i = 0; i < ARMOR.length; i++) {
                    boolean worn = i == 0 ? f.helmet : i < 5 ? f.chest : i < 9 ? f.leggings : f.boots;
                    p.visible((reverse ? "_" : "") + ARMOR[i], reverse ? !worn : worn);
                }
            } else if ("health/less_show.js".equals(key) || "health/more_show.js".equals(key)) {
                boolean more = key.contains("more");
                String prefix = more ? "healthMore" : "healthLess";
                p.visible(prefix + "QuarterShow", more ? f.healthRatio > .25F : f.healthRatio <= .25F);
                p.visible(prefix + "HalfShow", more ? f.healthRatio > .5F : f.healthRatio <= .5F);
                p.visible(prefix + "ThreeQuartersShow", more ? f.healthRatio > .75F : f.healthRatio <= .75F);
            } else {
                for (int i = 0; i < TASKS.length; i++) if (("task/" + TASKS[i] + ".js").equals(key)) {
                    boolean active = TASKS[i].equals(f.task);
                    p.visible(TASK_BONES[i] + "Show", active); p.visible(TASK_BONES[i] + "Hidden", !active);
                    return true;
                }
                return false;
            }
            return true;
        }
        if ((BASE + "float/default.js").equals(id)) {
            offsetY(p, "sinFloat", sin(f.age * .1) * .05F);
            offsetY(p, "cosFloat", cos(f.age * .1) * .05F);
            offsetY(p, "_sinFloat", -sin(f.age * .1) * .05F);
            offsetY(p, "_cosFloat", -cos(f.age * .1) * .05F);
            return true;
        }
        if ((BASE + "rotation/reciprocate.js").equals(id)) {
            float value = cos(f.age * .3) * .2F;
            Bone x = p.get("xReciprocate"), y = p.get("yReciprocate"), z = p.get("zReciprocate");
            if (x != null) x.x = value; if (y != null) y.y = value; if (z != null) z.z = value;
            return true;
        }
        if (id.startsWith(BASE + "rotation/") && id.endsWith("_speed.js")) {
            String key = id.substring((BASE + "rotation/").length());
            int axis = "xyz".indexOf(key.charAt(0)); if (axis < 0) return false;
            String speed = key.substring(2, key.indexOf("_speed.js"));
            float rate = "high".equals(speed) ? 4 : "normal".equals(speed) ? 1 : .25F;
            String bonePrefix = key.substring(0, 1) + "Rotation" + Character.toUpperCase(speed.charAt(0)) + speed.substring(1);
            for (char suffix = 'A'; suffix <= 'E'; suffix++) {
                Bone b = p.get(bonePrefix + suffix); if (b == null) continue;
                float a = (f.age * rate % 360) * DEG; if (axis == 0) b.x = a; else if (axis == 1) b.y = a; else b.z = a;
            }
            return true;
        }
        if ("authors_and_credits:animation/arm.js".equals(id)) {
            // Literal port of the bundled script. Its deprecated hold-vehicle/trolley
            // queries are always false in SRC EntityMaidWrapper.
            authorArm(p.get("armLeft"), true, f); authorArm(p.get("armRight"), false, f);
            return true;
        }
        if ("authors_and_credits:animation/float.js".equals(id)) {
            Bone leg = p.get("floatLeg"), left = p.get("left"), right = p.get("right");
            Bone armLeft = p.get("armLeft"), armRight = p.get("armRight");
            if (leg != null) leg.x = leg.baseX + (float) Math.atan(f.limbAmount) * 1.25F;
            if (f.sitting) {
                if (armLeft != null) { armLeft.x = armLeft.baseX - 1; armLeft.y = armLeft.baseY + .75F; }
                if (armRight != null) { armRight.x = armRight.baseX - .5F; armRight.y = armRight.baseY - .25F; armRight.z = armRight.baseZ + .25F; }
                if (left != null) { left.z = left.baseZ + .25F; left.x = left.baseX + .3F; }
                if (right != null) right.z = right.baseZ - .5F;
            } else {
                if (left != null) { left.x = left.baseX; left.z = left.baseZ + (float) Math.atan(f.limbAmount) * .75F; }
                if (right != null) right.z = right.baseZ - (float) Math.atan(f.limbAmount) * .75F;
            }
            p.translate(0, f.sleeping ? .5F : .1F * sin(f.age * .075) - .1F, 0);
            return true;
        }
        if ((SPECIAL + "wakasagihime_sit.js").equals(id)) { if (f.sitting) sittingArms(p); return true; }
        if ((SPECIAL + "hecatia_dimension.js").equals(id)) { hecatia(p, f); return true; }
        return false;
    }
    private static void authorArm(Bone b, boolean left, LegacyAnimationFrame f) {
        if (b == null) return;
        b.x = b.baseX; b.y = 0; b.z = (left ? 1 : -1) * cos(f.age * .05) * .05F + (left ? -.4F : .4F);
        if (f.swing > 0 && left == f.swingLeft) {
            double eased = 1 - Math.pow(1 - f.swing, 4);
            b.x -= sin(eased * Math.PI) * 1.2F + sin(f.swing * Math.PI) * -.525F;
            b.z += sin(f.swing * Math.PI) * -.4F;
        }
    }
    private static void arm(Bone b, boolean left, LegacyAnimationFrame f) {
        if (b == null) return;
        if (f.joyArms()) { b.x = -1.3F; return; }
        b.x = (left ? -1 : 1) * cos(f.limbSwing * .67) * .7F * f.limbAmount;
        b.y = b.baseY;
        b.z = b.baseZ + (left ? 1 : -1) * cos(f.age * .05) * .05F;
        if (f.swing > 0 && left == f.swingLeft) {
            double eased = 1 - Math.pow(1 - f.swing, 4);
            b.x -= sin(eased * Math.PI) * 1.2F + sin(f.swing * Math.PI) * -.525F;
            b.z += sin(f.swing * Math.PI) * -.4F;
        }
        if (f.using() && left == f.useLeft) { b.x = b.baseX - 80 * DEG; b.y = b.baseY + (left ? 25 : -20) * DEG; }
    }
    private static void leg(Bone b, float x, boolean farm) { if (b != null) { b.x = x - (farm ? .3927F : 0); b.y = b.baseY; b.z = b.baseZ; } }
    private static void xy(Bone b, float x, float y) { if (b != null) { b.x = x; b.y = y; } }
    private static void xz(Bone b, float x, float z) { if (b != null) { b.x = x; b.z = z; } }
    private static void riding(LegacyAnimationPose p) { xz(p.get("legLeft"), -1.134F, -.262F); xz(p.get("legRight"), -1.134F, .262F); p.translate(0, .3F, 0); }
    private static void sittingArms(LegacyAnimationPose p) { xz(p.get("armLeft"), -.798F, .274F); xz(p.get("armRight"), -.798F, -.274F); }
    private static void vertical(LegacyAnimationPose p, String child, String parent) { Bone a = p.get(child), b = p.get(parent); if (a != null && b != null) { a.x = -b.x; a.z = -b.z; } }
    private static void baseZ(LegacyAnimationPose p, String name, float z) { Bone b = p.get(name); if (b != null) b.z = b.baseZ + z; }
    private static void offsetY(LegacyAnimationPose p, String name, float y) { Bone b = p.get(name); if (b != null) b.offsetY = y; }
    private static float sin(double v) { return (float) Math.sin(v); }
    private static float cos(double v) { return (float) Math.cos(v); }
    private static void hecatia(LegacyAnimationPose p, LegacyAnimationFrame f) {
        boolean earth = f.dimension == 0, moon = f.dimension == 1, other = !earth && !moon;
        p.visible("earthHair", earth); p.visible("logoEarth", earth); p.visible("earthTop", earth && !f.helmet);
        p.visible("earthSideLeft", !earth); p.visible("earthSideRight", false);
        p.visible("moonHair", moon); p.visible("logoMoon", moon); p.visible("moonTop", moon && !f.helmet);
        p.visible("moonSideLeft", false); p.visible("moonSideRight", !moon);
        p.visible("otherHair", other); p.visible("logoOther", other); p.visible("otherTop", other && !f.helmet);
        p.visible("otherSideLeft", earth); p.visible("otherSideRight", moon);
    }
}

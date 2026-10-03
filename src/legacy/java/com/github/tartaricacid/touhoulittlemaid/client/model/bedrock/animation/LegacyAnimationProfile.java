package com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Ordered MaidModelInfo.decorate()/getMaidDefault() animation selection. */
public final class LegacyAnimationProfile {
    public static final String ROOT = "touhou_little_maid:animation/maid/default/";
    public static final String DEFAULT_JSON = "touhou_little_maid:animation/maid.animation.json";
    public static final List<String> DEFAULT = Collections.unmodifiableList(Arrays.asList(
            ROOT + "head/default.js", ROOT + "head/blink.js", ROOT + "head/beg.js",
            ROOT + "head/music_shake.js", ROOT + "leg/default.js", ROOT + "arm/default.js",
            ROOT + "arm/swing.js", ROOT + "arm/vertical.js", ROOT + "sit/default.js",
            ROOT + "armor/default.js", ROOT + "armor/reverse.js", ROOT + "wing/default.js",
            ROOT + "tail/default.js", ROOT + "sit/skirt_rotation.js",
            "touhou_little_maid:animation/base/float/default.js"));
    public final boolean gecko;
    public final List<String> animations;

    public LegacyAnimationProfile(boolean gecko, List<String> authored) {
        this.gecko = gecko;
        List<String> result = new ArrayList<String>();
        if (authored == null || authored.isEmpty()) {
            if (gecko) result.add(DEFAULT_JSON); else result.addAll(DEFAULT);
        } else {
            for (String id : authored) {
                if (gecko) {
                    // MaidModelInfo.decorate() retains JSON paths (not only *.animation.json).
                    if (id.endsWith(".json")) result.add(id);
                } else if ("touhou_little_maid:animation/maid.default.js".equals(id)) {
                    result.addAll(DEFAULT);
                } else result.add(id);
            }
        }
        animations = Collections.unmodifiableList(result);
    }
}

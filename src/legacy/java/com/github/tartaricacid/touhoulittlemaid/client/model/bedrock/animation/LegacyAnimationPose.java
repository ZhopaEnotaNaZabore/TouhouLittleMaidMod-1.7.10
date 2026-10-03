package com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.animation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Mutable frame-local pose. Rotations are radians; offsets are model-space blocks. */
public final class LegacyAnimationPose {
    public final Map<String, Bone> bones = new LinkedHashMap<String, Bone>();
    /** Ordered GL-compatible transforms, also used by attachments. */
    public final List<float[]> transforms = new ArrayList<float[]>();

    public Bone get(String name) { return bones.get(name); }
    public void reset() {
        transforms.clear();
        for (Bone bone : bones.values()) bone.reset();
    }
    public void translate(float x, float y, float z) { transforms.add(new float[]{0, x, y, z}); }
    public void rotate(float degrees, float x, float y, float z) { transforms.add(new float[]{degrees, x, y, z, 1}); }
    public void visible(String name, boolean visible) {
        Bone bone = get(name);
        if (bone != null) bone.visible = visible && bone.baseVisible;
    }
    public static final class Bone {
        public final float baseX, baseY, baseZ;
        public final boolean baseVisible;
        public float x, y, z, offsetX, offsetY, offsetZ, scaleX, scaleY, scaleZ;
        public boolean visible;
        public Bone(float x, float y, float z, boolean visible) {
            baseX = x; baseY = y; baseZ = z; baseVisible = visible;
            reset();
        }
        public void reset() {
            x = baseX; y = baseY; z = baseZ;
            offsetX = offsetY = offsetZ = 0;
            scaleX = scaleY = scaleZ = 1;
            visible = baseVisible;
        }
    }
}

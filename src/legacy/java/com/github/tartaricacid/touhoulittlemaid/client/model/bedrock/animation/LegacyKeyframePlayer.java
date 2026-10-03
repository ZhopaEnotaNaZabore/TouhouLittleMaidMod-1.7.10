package com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.animation;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

/** Per-entity direct-constructor state machine, without a GeckoLib dependency.
 * Layers use SRC controller order and share bounded Molang variables/secondary motion.
 * External mod controllers and sound-event dispatch remain separate from bone playback.
 */
public final class LegacyKeyframePlayer {
    private final LegacyAnimationLibrary library;
    private final Map<String, Layer> layers = new HashMap<String, Layer>();
    private final Map<String, double[]> previous = new HashMap<String, double[]>();
    private final Map<String, Float> resetStarted = new HashMap<String, Float>();
    private final Map<String, double[]> resetFrom = new HashMap<String, double[]>();
    private final Set<String> failed = new HashSet<String>();
    private final Map<String, String> runtimeErrors = new HashMap<String, String>();
    private float lastAge = Float.NEGATIVE_INFINITY;
    private final Map<String,double[]> boneRotations=new HashMap<String,double[]>();
    private final Map<String,Double> variables=new HashMap<String,Double>();
    private final Map<String,LegacySecondaryMotion> physics=new HashMap<String,LegacySecondaryMotion>();
    public LegacyKeyframePlayer(LegacyAnimationLibrary library) { this.library = library; }

    public void apply(LegacyAnimationPose pose, LegacyAnimationFrame f) {
        if (f.age < lastAge) { layers.clear(); previous.clear(); resetStarted.clear(); resetFrom.clear(); variables.clear();physics.clear();boneRotations.clear(); }
        lastAge = f.age;
        for(Map.Entry<String,LegacyAnimationPose.Bone> b:pose.bones.entrySet())if(!boneRotations.containsKey(b.getKey().toLowerCase(java.util.Locale.ROOT))){LegacyAnimationPose.Bone v=b.getValue();boneRotations.put(b.getKey().toLowerCase(java.util.Locale.ROOT),new double[]{Math.toDegrees(v.baseX),Math.toDegrees(v.baseY),Math.toDegrees(v.baseZ)});}
        Map<String, double[]> out = new LinkedHashMap<String, double[]>();
        Map<String, double[]> rotationSum = new HashMap<String, double[]>();
        for (int i = 0; i < 8; i++) layer("pre" + i, "pre_parallel" + i, 0, 0, true, false, f, out, rotationSum);
        String state = mainState(f);
        layer("main", state, 2, 0, !"death".equals(state) && !"attacked".equals(state), false, f, out, rotationSum);
        boolean rightBusy = (f.swing > 0 && !f.swingLeft) || (f.using() && !f.useLeft);
        boolean leftBusy = (f.swing > 0 && f.swingLeft) || (f.using() && f.useLeft);
        layer("off", f.sleeping || leftBusy ? null : conditional("hold_offhand", f.offId, f.offCategory, ""),
                0, 0, true, false, f, out, rotationSum);
        String hold = conditional("hold_mainhand", f.mainId, f.mainCategory, "");
        if (f.fishing && !rightBusy && !f.using() && library.contains("hold_mainhand:fishing")) hold = "hold_mainhand:fishing";
        layer("hand", f.sleeping || rightBusy ? null : hold, 0, 0, true, false, f, out, rotationSum);
        Layer swingLayer = layers.get("swing");
        String swing = f.swingLeft ? "swing_offhand" : "swing_hand";
        boolean unseenEvent = f.swingSequence != 0 && !f.cancelSwing
                && (swingLayer == null || swingLayer.sequence != f.swingSequence);
        if ((f.swing > 0 || unseenEvent) && !f.sleeping) swing = conditional(f.swingLeft ? "swing_offhand" : "swing",
                f.swingLeft ? f.offId : f.mainId, f.swingLeft ? f.offCategory : f.mainCategory, swing);
        else swing = swingLayer == null ? null : swingLayer.name;
        LegacyKeyframeClip pendingSwing = swing == null ? null : library.get(swing);
        if (unseenEvent && f.swing <= 0 && (pendingSwing == null || f.swingTicks > pendingSwing.length * 20)) swing = null;
        if (f.sleeping || f.dead || f.actionsDisabled || f.cancelSwing
                || (!unseenEvent && swingLayer != null && f.swing <= 0 && !swingLayer.alive(f.age, library))) swing = null;
        layer("swing", swing, 2, f.swingSequence, false, false, f, out, rotationSum);
        String usePrefix = f.useLeft ? "use_offhand" : "use_mainhand";
        layer("use", !f.using() || f.sleeping || f.dead || f.actionsDisabled ? null : conditional(usePrefix,
                f.useLeft ? f.offId : f.mainId, f.use, usePrefix), 2, f.useSequence, true, false, f, out, rotationSum);
        layer("misc", f.begging ? "beg" : null, 2, 0, true, false, f, out, rotationSum);
        for (int i = 0; i < 8; i++) layer("parallel" + i, "parallel" + i, 0, 0, true, true, f, out, rotationSum);
        // SRC resets unanimated tracks in one tick. Snapshot once, not every render frame.
        for (String key : previous.keySet()) if (!out.containsKey(key)) {
            if (!resetStarted.containsKey(key)) { resetStarted.put(key, f.age); resetFrom.put(key, previous.get(key)); }
            double alpha = Math.min(1, Math.max(0, f.age - resetStarted.get(key)));
            if (alpha < 1) out.put(key, mix(resetFrom.get(key), neutral(key), alpha));
        }
        for (Map.Entry<String, double[]> e : out.entrySet()) {
            if (!resetFrom.containsKey(e.getKey()) || f.age > resetStarted.get(e.getKey()) + 1) {
                resetStarted.remove(e.getKey()); resetFrom.remove(e.getKey());
            }
            applyTrack(pose, e.getKey(), e.getValue());
        }
        // Reappearing channels cancel a pending rest interpolation.
        for (String key : out.keySet()) if (isAnimated(key)) { resetStarted.remove(key); resetFrom.remove(key); }
        previous.clear(); previous.putAll(out);
        for(Map.Entry<String,LegacyAnimationPose.Bone> b:pose.bones.entrySet()){LegacyAnimationPose.Bone v=b.getValue();boneRotations.put(b.getKey().toLowerCase(java.util.Locale.ROOT),new double[]{Math.toDegrees(v.x),Math.toDegrees(v.y),Math.toDegrees(v.z)});}
    }
    private boolean isAnimated(String key) { for (Layer layer : layers.values()) if (layer.output.containsKey(key)) return true; return false; }

    public String mainState(LegacyAnimationFrame f) {
        if (f.dead) return "death";
        if (f.sleeping) return "sleep";
        if (f.climbing) return f.verticalSpeed > 0 ? "ladder_up" : f.verticalSpeed < 0 ? "ladder_down" : "ladder_stillness";
        if (f.carried && library.contains("vehicle$minecraft:player")) return "vehicle$minecraft:player";
        if (!f.joy.isEmpty() && library.contains(f.joy)) return f.joy;
        if (f.boat) return "boat";
        if (f.riding) return "chair";
        if (f.sitting) return "sit";
        if (f.water) return "swim_stand";
        if (f.hurt && library.contains("attacked")) return "attacked";
        if (!f.onGround) return "jump";
        if (f.sprinting) return "run";
        return f.limbAmount > .05F ? "walk" : "idle";
    }
    private String conditional(String prefix, String id, String category, String fallback) {
        if (id.isEmpty() && prefix.startsWith("hold_")) return library.contains(prefix + ":empty") ? prefix + ":empty" : null;
        if (!id.isEmpty() && library.contains(prefix + "$" + id)) return prefix + "$" + id;
        if (!category.isEmpty() && !"crossbow".equals(category) && !"spear".equals(category)
                && library.contains(prefix + ":" + category)) return prefix + ":" + category;
        return fallback.isEmpty() ? (library.contains(prefix) ? prefix : null) : fallback;
    }
    private void layer(String key, String name, int transition, int sequence, boolean loop, boolean additive,
                       LegacyAnimationFrame f, Map<String, double[]> out, Map<String, double[]> sums) {
        Layer layer = layers.get(key);
        if (layer == null) { layer = new Layer(); layers.put(key, layer); }
        if (name == null || !library.contains(name)) { layer.name = null; layer.output.clear(); return; }
        boolean changed = !name.equals(layer.name) || layer.sequence != sequence;
        if (changed) {
            layer.from = new HashMap<String, double[]>(layer.output);
            layer.name = name; layer.sequence = sequence;
            LegacyKeyframeClip next = library.get(name);
            layer.playback = next == null ? null : next.timeline.playback(f.uuidSeed ^ ((long) sequence << 32) ^ name.hashCode());
            float elapsed = "swing".equals(key) ? f.swingTicks : "use".equals(key) ? f.useTicks : 0;
            layer.started = f.age - Math.max(0, elapsed);
        }
        layer.output.clear();
        LegacyKeyframeClip clip = library.get(name);
        if (clip == null || failed.contains(name)) return;
        layer.playback.context.variables=variables;
        layer.playback.context.physics=physics;
        layer.playback.context.boneRotations=boneRotations;
        Map<String, double[][]> sample;
        try { sample = clip.sample(Math.max(0, f.age - layer.started) / 20D, f,
                loop ? LegacyKeyframeClip.Loop.LOOP : LegacyKeyframeClip.Loop.ONCE, layer.playback); }
        catch (RuntimeException error) { failed.add(name); runtimeErrors.put(name, String.valueOf(error.getMessage())); return; } // atomic clip failure, once per player
        double alpha = transition == 0 ? 1 : Math.min(1, Math.max(0, (f.age - layer.started) / transition));
        for (Map.Entry<String, double[][]> b : sample.entrySet()) for (int channel = 0; channel < 3; channel++) {
            double[] v = b.getValue()[channel];
            if (v == null) continue;
            String track = b.getKey() + "\u0001" + channel;
            double[] from = layer.from.get(track);
            if (from == null) from = out.containsKey(track) ? out.get(track) : neutral(track);
            double[] value = mix(from, v, alpha);
            layer.output.put(track, value);
            if (channel == 0) {
                double[] sum = sums.get(track);
                if (sum == null) sum = new double[3];
                double[] total = new double[]{sum[0] + value[0], sum[1] + value[1], sum[2] + value[2]};
                sums.put(track, total);
                out.put(track, additive ? total : value);
            } else out.put(track, value);
        }
    }
    private static double[] neutral(String key) { return key.endsWith("\u00012") ? new double[]{1, 1, 1} : new double[3]; }
    private static double[] mix(double[] a, double[] b, double t) { return new double[]{a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + (b[2] - a[2]) * t}; }
    private static void applyTrack(LegacyAnimationPose pose, String key, double[] v) {
        int delimiter = key.lastIndexOf('\u0001');
        LegacyAnimationPose.Bone bone = pose.get(key.substring(0, delimiter));
        if (bone == null) return;
        int channel = key.charAt(delimiter + 1) - '0';
        // Gecko -> legacy basis C=diag(-1,-1,1): native -X/-Y angles become +X/+Y.
        if (channel == 0) { bone.x = bone.baseX + (float) Math.toRadians(v[0]); bone.y = bone.baseY + (float) Math.toRadians(v[1]); bone.z = bone.baseZ + (float) Math.toRadians(v[2]); }
        else if (channel == 1) { bone.offsetX = (float) v[0] / 16; bone.offsetY = -(float) v[1] / 16; bone.offsetZ = (float) v[2] / 16; }
        else { bone.scaleX = (float) v[0]; bone.scaleY = (float) v[1]; bone.scaleZ = (float) v[2]; }
    }
    public Map<String, String> runtimeDiagnostics() { return java.util.Collections.unmodifiableMap(runtimeErrors); }
    public Set<String> runtimeFailures() { return java.util.Collections.unmodifiableSet(failed); }
    private static final class Layer {
        LegacyTimeline.Playback playback;
        String name;
        int sequence;
        float started;
        Map<String, double[]> output = new HashMap<String, double[]>(), from = new HashMap<String, double[]>();
        boolean alive(float age, LegacyAnimationLibrary library) {
            LegacyKeyframeClip clip = name == null ? null : library.get(name);
            return clip != null && (age - started) / 20D <= clip.length;
        }
    }
}

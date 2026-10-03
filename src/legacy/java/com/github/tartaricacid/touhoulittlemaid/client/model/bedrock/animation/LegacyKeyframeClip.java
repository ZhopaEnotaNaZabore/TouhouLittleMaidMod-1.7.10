package com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.animation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Real JSON rotation/position/scale tracks. Unsupported channels make this clip unavailable. */
public final class LegacyKeyframeClip {
    public enum Loop { ONCE, LOOP, HOLD }
    public final String name;
    public final Loop loop;
    public final double length;
    public final LegacyTimeline timeline;
    private final LegacyMolang.Expression blendWeight;
    /** Retained for a separate sound-event dispatcher; never discard the bone animation. */
    public final JsonElement soundEffects;
    public final Map<String, Track[]> bones = new LinkedHashMap<String, Track[]>();
    public LegacyKeyframeClip(String name, JsonObject json) {
        this.name = name;
        blendWeight=json.has("blend_weight")?LegacyMolang.compile(json.get("blend_weight").getAsString()):LegacyMolang.constant(1);
        soundEffects=json.get("sound_effects");
        String loopName = json.has("loop") ? json.get("loop").getAsString() : "false";
        loop = "true".equals(loopName) ? Loop.LOOP : "hold_on_last_frame".equals(loopName) ? Loop.HOLD : Loop.ONCE;
        if (!"true".equals(loopName) && !"false".equals(loopName) && !"hold_on_last_frame".equals(loopName))
            throw new IllegalArgumentException("Unsupported loop " + loopName);
        for (Map.Entry<String, JsonElement> e : json.entrySet()) {
            String k = e.getKey();
            if (!"loop".equals(k) && !"animation_length".equals(k) && !"bones".equals(k) && !"timeline".equals(k) && !"blend_weight".equals(k) && !"sound_effects".equals(k))
                throw new IllegalArgumentException("Unsupported animation field " + k);
        }
        timeline = new LegacyTimeline(json);
        double last = 0;
        for (LegacyTimeline.Event event : timeline.events) last = Math.max(last, event.time);
        if (json.has("bones")) for (Map.Entry<String, JsonElement> e : json.getAsJsonObject("bones").entrySet()) {
            JsonObject bone = e.getValue().getAsJsonObject();
            Track[] tracks = new Track[3];
            for (Map.Entry<String, JsonElement> channel : bone.entrySet()) {
                String key = channel.getKey();
                int index = "rotation".equals(key) ? 0 : "position".equals(key) ? 1 : "scale".equals(key) ? 2 : -1;
                if (index < 0) throw new IllegalArgumentException("Unsupported bone channel " + key);
                tracks[index] = new Track(channel.getValue());
                last = Math.max(last, tracks[index].lastTime());
            }
            bones.put(e.getKey(), tracks);
        }
        length = json.has("animation_length") ? finite(json.get("animation_length").getAsDouble()) : last;
        if (length < 0) throw new IllegalArgumentException("Negative animation length");
        // Events beyond explicit length are unreachable, as in SRC; they do not invalidate tracks.
    }
    public double sampleTime(double elapsed, Loop override) {
        if (length <= 0) return Math.max(0, elapsed); // expression-only loops keep advancing.
        Loop mode = override == null ? loop : override;
        return mode == Loop.LOOP ? Math.max(0, elapsed) % length : Math.max(0, Math.min(length, elapsed));
    }
    public Map<String, double[][]> sample(double elapsed, LegacyAnimationFrame frame, Loop override) {
        return sample(elapsed, frame, override, timeline.playback(frame.uuidSeed));
    }
    public Map<String, double[][]> sample(double elapsed, LegacyAnimationFrame frame, Loop override, LegacyTimeline.Playback playback) {
        double time = sampleTime(elapsed, override);
        Loop mode = override == null ? loop : override;
        LegacyMolang.Context context = playback.advance(elapsed, length, mode == Loop.LOOP, frame);
        context.time = time;
        Map<String, double[][]> result = new LinkedHashMap<String, double[][]>();
        double weight=Math.max(0,Math.min(1,finite(blendWeight.eval(context))));
        for (Map.Entry<String, Track[]> e : bones.entrySet()) {
            double[][] values = new double[3][];
            for (int i = 0; i < 3; i++) if (e.getValue()[i] != null) {
                values[i] = e.getValue()[i].sample(time, context);
                for(int axis=0;axis<3;axis++){double neutral=i==2?1:0;values[i][axis]=neutral+(values[i][axis]-neutral)*weight;}
            }
            result.put(e.getKey(), values);
        }
        return result;
    }
    static double finite(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) throw new IllegalArgumentException("Non-finite animation result");
        return value;
    }
    public static final class Track {
        final List<Key> keys = new ArrayList<Key>();
        public Track(JsonElement json) {
            if (!json.isJsonObject()) keys.add(new Key(0, json));
            else {
                for (Map.Entry<String, JsonElement> e : json.getAsJsonObject().entrySet()) {
                    if (keys.size() >= 8192) throw new IllegalArgumentException("Too many keyframes");
                    double time = finite(Double.parseDouble(e.getKey()));
                    if (time < 0) throw new IllegalArgumentException("Negative keyframe time");
                    keys.add(new Key(time, e.getValue()));
                }
                Collections.sort(keys, new Comparator<Key>() { public int compare(Key a, Key b) { return Double.compare(a.time, b.time); } });
            }
            if (keys.isEmpty()) throw new IllegalArgumentException("Empty track");
            for (int i = 1; i < keys.size(); i++) if (keys.get(i).time == keys.get(i - 1).time)
                throw new IllegalArgumentException("Duplicate keyframe time");
        }
        double lastTime() { return keys.get(keys.size() - 1).time; }
        public double[] sample(double time, LegacyMolang.Context context) {
            Key first = keys.get(0);
            if (time < first.time) return first.pre.eval(context);
            int lo = 0, hi = keys.size();
            while (lo < hi) { int mid = (lo + hi) >>> 1; if (keys.get(mid).time <= time) lo = mid + 1; else hi = mid; }
            int rightIndex = lo;
            Key a = keys.get(Math.max(0, rightIndex - 1));
            if (time == a.time || rightIndex == keys.size()) return a.post.eval(context);
            Key b = keys.get(rightIndex);
            double u = (time - a.time) / (b.time - a.time);
            double[] start = a.post.eval(context), end = b.pre.eval(context), out = new double[3];
            // SRC BoneKeyFrameProcessor: destination CatmullRom wins; otherwise use source easing.
            boolean catmull = b.catmull || a.catmull;
            double[] left = catmull ? keys.get(Math.max(0, rightIndex - 2)).post.eval(context) : null;
            double[] right = catmull ? keys.get(Math.min(keys.size() - 1, rightIndex + 1)).pre.eval(context) : null;
            for (int i = 0; i < 3; i++) out[i] = finite(catmull
                    ? catmull(u, left[i], start[i], end[i], right[i]) : start[i] + (end[i] - start[i]) * u);
            return out;
        }
    }
    public static double catmull(double t, double p0, double p1, double p2, double p3) {
        return .5 * (2 * p1 + (-p0 + p2) * t + (2 * p0 - 5 * p1 + 4 * p2 - p3) * t * t
                + (-p0 + 3 * p1 - 3 * p2 + p3) * t * t * t);
    }
    static final class Key {
        final double time;
        final Vector pre, post;
        final boolean catmull;
        Key(double time, JsonElement element) {
            this.time = time;
            if (!element.isJsonObject()) { pre = post = new Vector(element); catmull = false; return; }
            JsonObject object = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> e : object.entrySet()) if (!"pre".equals(e.getKey()) && !"post".equals(e.getKey())
                    && !"vector".equals(e.getKey()) && !"lerp_mode".equals(e.getKey()) && !"easing".equals(e.getKey()))
                throw new IllegalArgumentException("Unsupported keyframe field " + e.getKey());
            JsonElement before = object.has("vector") ? object.get("vector") : object.has("pre") ? object.get("pre") : object.get("post");
            if (before == null) throw new IllegalArgumentException("Missing keyframe vector");
            pre = new Vector(before);
            post = new Vector(object.has("post") ? object.get("post") : before);
            JsonElement easing = object.has("lerp_mode") ? object.get("lerp_mode") : object.get("easing");
            String mode = easing == null ? "linear" : easing.getAsString();
            if (!"linear".equals(mode) && !"catmullrom".equals(mode)) throw new IllegalArgumentException("Unsupported easing " + mode);
            catmull = "catmullrom".equals(mode);
        }
    }
    static final class Vector {
        final LegacyMolang.Expression[] components = new LegacyMolang.Expression[3];
        Vector(JsonElement value) {
            int count = value.isJsonArray() ? value.getAsJsonArray().size() : 1;
            if (count != 1 && count != 3) throw new IllegalArgumentException("Expected scalar or 3-vector");
            for (int i = 0; i < 3; i++) {
                JsonElement v = value.isJsonArray() ? value.getAsJsonArray().get(count == 1 ? 0 : i) : value;
                components[i] = v.getAsJsonPrimitive().isNumber() ? LegacyMolang.constant(v.getAsDouble()) : LegacyMolang.compile(v.getAsString());
            }
        }
        double[] eval(LegacyMolang.Context c) { return new double[]{finite(components[0].eval(c)), finite(components[1].eval(c)), finite(components[2].eval(c))}; }
    }
}

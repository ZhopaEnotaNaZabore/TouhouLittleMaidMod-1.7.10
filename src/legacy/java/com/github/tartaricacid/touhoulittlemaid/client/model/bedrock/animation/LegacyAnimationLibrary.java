package com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.animation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Merges authored files in order, then SRC's missing-name default clips. */
public final class LegacyAnimationLibrary {
    private final Map<String, LegacyKeyframeClip> clips = new LinkedHashMap<String, LegacyKeyframeClip>();
    private final Map<String, String> rejected = new LinkedHashMap<String, String>();
    public void merge(JsonObject root, String origin, boolean defaultsOnly) {
        if (!root.has("animations") || !root.get("animations").isJsonObject())
            throw new IllegalArgumentException("Missing animations object: " + origin);
        for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("animations").entrySet()) {
            String name = e.getKey();
            if (defaultsOnly && contains(name)) continue;
            clips.remove(name); rejected.remove(name);
            try { clips.put(name, new LegacyKeyframeClip(name, e.getValue().getAsJsonObject())); }
            catch (RuntimeException error) { rejected.put(name, origin + ": " + error.getMessage()); }
        }
    }
    /** A rejected authored clip blocks default substitution under that same name. */
    public boolean contains(String name) { return clips.containsKey(name) || rejected.containsKey(name); }
    public LegacyKeyframeClip get(String name) { return clips.get(name); }
    public Map<String, String> diagnostics() { return Collections.unmodifiableMap(rejected); }
    public Map<String, LegacyKeyframeClip> clips() { return Collections.unmodifiableMap(clips); }
}

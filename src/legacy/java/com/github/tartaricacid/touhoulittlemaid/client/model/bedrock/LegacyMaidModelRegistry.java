package com.github.tartaricacid.touhoulittlemaid.client.model.bedrock;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.HashSet;

/** Discovers modern maid_model.json packs through the 1.7 resource manager. */
public final class LegacyMaidModelRegistry implements IResourceManagerReloadListener {
    public static final LegacyMaidModelRegistry INSTANCE = new LegacyMaidModelRegistry();
    private final Map<String, Entry> entries = new HashMap<String, Entry>();
    private final Map<String, LegacyBedrockModel> models = new HashMap<String, LegacyBedrockModel>();
    private boolean loaded;

    private LegacyMaidModelRegistry() {}

    public Entry getEntry(String id) {
        ensureLoaded();
        Entry entry = entries.get(id);
        return entry == null ? entries.get("touhou_little_maid:hakurei_reimu") : entry;
    }

    public LegacyBedrockModel getModel(String id, LegacyBedrockModel fallback) {
        Entry entry = getEntry(id);
        if (entry == null) return fallback;
        LegacyBedrockModel cached = models.get(entry.id);
        if (cached != null) return cached;
        InputStream stream = null;
        try {
            stream = Minecraft.getMinecraft().getResourceManager().getResource(entry.model).getInputStream();
            LegacyBedrockModel model = new LegacyBedrockModel(stream);
            models.put(entry.id, model);
            return model;
        } catch (Exception error) {
            TouhouLittleMaid.LOGGER.error("Unable to load maid model {} from {}", entry.id, entry.model, error);
            return fallback;
        } finally {
            if (stream != null) try { stream.close(); } catch (Exception ignored) {}
        }
    }

    public List<Entry> getEntries() {
        ensureLoaded();
        List<Entry> result = new ArrayList<Entry>(entries.values());
        Collections.sort(result, new Comparator<Entry>() {
            @Override public int compare(Entry first, Entry second) { return first.id.compareTo(second.id); }
        });
        return result;
    }

    private void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
        Set<String> domains = manager.getResourceDomains();
        for (String domain : domains) loadPack(manager, domain);
    }

    private void loadPack(IResourceManager manager, String domain) {
        InputStream stream = null;
        try {
            stream = manager.getResource(new ResourceLocation(domain, "maid_model.json")).getInputStream();
            JsonObject root = new JsonParser().parse(new InputStreamReader(stream, "UTF-8")).getAsJsonObject();
            JsonArray list = root.getAsJsonArray("model_list");
            if (list == null) return;
            for (JsonElement element : list) {
                JsonObject json = element.getAsJsonObject();
                String id = json.get("model_id").getAsString();
                String path = id.substring(id.indexOf(':') + 1);
                String idDomain = id.indexOf(':') < 0 ? domain : id.substring(0, id.indexOf(':'));
                ResourceLocation model = location(json, "model", idDomain, "models/entity/" + path + ".json");
                ResourceLocation texture = location(json, "texture", idDomain, "textures/entity/" + path + ".png");
                float scale = json.has("render_entity_scale") ? json.get("render_entity_scale").getAsFloat() : 1.0F;
                scale = Math.max(0.2F, Math.min(2.0F, scale));
                float itemScale = json.has("render_item_scale") ? json.get("render_item_scale").getAsFloat() : 1.0F;
                boolean showBackpack = !json.has("show_backpack") || json.get("show_backpack").getAsBoolean();
                boolean showCustomHead = !json.has("show_custom_head") || json.get("show_custom_head").getAsBoolean();
                boolean gecko = json.has("is_gecko") && json.get("is_gecko").getAsBoolean();
                Set<String> animations = new HashSet<String>();
                JsonArray animationList = json.getAsJsonArray("animation");
                if (animationList != null) for (JsonElement animation : animationList)
                    animations.add(animation.getAsString());
                Entry base = new Entry(id, model, texture, scale, itemScale, showBackpack, showCustomHead,
                        gecko, animations, animationList == null || animationList.size() == 0);
                entries.put(id, base);

                // CustomModelPack.decorate() in the source exposes every extra texture as a
                // separate selectable model while sharing the same geometry and settings.
                JsonArray extras = json.getAsJsonArray("extra_textures");
                if (extras != null) {
                    for (JsonElement extra : extras) {
                        ResourceLocation extraTexture = resource(extra.getAsString(), domain);
                        String extraId = id + "_" + md5(extraTexture.getResourcePath()).toLowerCase(Locale.US);
                        entries.put(extraId, new Entry(extraId, model, extraTexture, scale, itemScale,
                                showBackpack, showCustomHead, gecko, animations,
                                animationList == null || animationList.size() == 0));
                    }
                }
            }
        } catch (Exception ignored) {
            // A namespace without a maid pack is normal.
        } finally {
            if (stream != null) try { stream.close(); } catch (Exception ignored) {}
        }
    }

    private static ResourceLocation location(JsonObject json, String key, String domain, String fallback) {
        if (!json.has(key)) return new ResourceLocation(domain, fallback);
        return resource(json.get(key).getAsString(), domain);
    }

    private static ResourceLocation resource(String value, String domain) {
        return value.indexOf(':') < 0 ? new ResourceLocation(domain, value) : new ResourceLocation(value);
    }

    private static String md5(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("MD5").digest(value.getBytes(Charset.forName("UTF-8")));
            StringBuilder result = new StringBuilder(32);
            for (byte item : digest) result.append(String.format(Locale.US, "%02x", item & 0xff));
            return result.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("MD5 is unavailable", exception);
        }
    }

    @Override
    public void onResourceManagerReload(IResourceManager manager) {
        loaded = false;
        entries.clear();
        models.clear();
    }

    public static final class Entry {
        public final String id;
        public final ResourceLocation model;
        public final ResourceLocation texture;
        public final float scale;
        public final float itemScale;
        public final boolean showBackpack;
        public final boolean showCustomHead;
        public final boolean gecko;
        private final Set<String> animations;
        private final boolean defaultAnimations;
        private Entry(String id, ResourceLocation model, ResourceLocation texture, float scale, float itemScale,
                      boolean showBackpack, boolean showCustomHead, boolean gecko, Set<String> animations,
                      boolean defaultAnimations) {
            this.id = id; this.model = model; this.texture = texture; this.scale = scale;
            this.itemScale = itemScale; this.showBackpack = showBackpack; this.showCustomHead = showCustomHead;
            this.gecko = gecko;
            this.animations = Collections.unmodifiableSet(new HashSet<String>(animations));
            this.defaultAnimations = defaultAnimations;
        }

        /** Mirrors MaidModelInfo.decorate(): an empty non-Gecko list receives the source default animation set. */
        public boolean usesAnimation(String path, boolean partOfDefaultSet) {
            return animations.contains(path) || (!gecko && defaultAnimations && partOfDefaultSet);
        }
    }
}

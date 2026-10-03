package com.github.tartaricacid.touhoulittlemaid.client.model.bedrock;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.animation.*;
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
    private static final String DEFAULT_ID = "touhou_little_maid:hakurei_reimu";
    private final Map<String, Resolved> resolved = new HashMap<String, Resolved>();
    private final Set<String> failed = new HashSet<String>();
    private final Map<String, LegacyAnimationLibrary> libraries = new HashMap<String, LegacyAnimationLibrary>();
    private Resolved empty;
    private boolean loaded;

    private LegacyMaidModelRegistry() {}

    private final Map<String,String> translations=new HashMap<String,String>();
    private String language="";
    public String translate(String key){
        String current=Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode().toLowerCase(Locale.ROOT);
        if(!current.equals(language)){
            language=current;translations.clear();IResourceManager manager=Minecraft.getMinecraft().getResourceManager();
            for(String lang:new String[]{"en_us",current})for(String domain:new ArrayList<String>(manager.getResourceDomains()))try{
                JsonObject json=readJson(manager,new ResourceLocation(domain,"lang/"+lang+".json"));
                for(Map.Entry<String,JsonElement> e:json.entrySet())if(e.getValue().isJsonPrimitive())translations.put(e.getKey(),e.getValue().getAsString());
            }catch(java.io.IOException absent){}catch(RuntimeException invalid){TouhouLittleMaid.LOGGER.warn("Invalid model language in {}",domain);}
        }
        String value=translations.get(key);return value==null?key:value;
    }

    public Entry getEntry(String id) {
        ensureLoaded();
        Entry entry = entries.get(id);
        return entry == null ? entries.get("touhou_little_maid:hakurei_reimu") : entry;
    }

    public LegacyBedrockModel getModel(String id, LegacyBedrockModel unusedFallback) {
        return resolve(id).model;
    }

    /** Geometry, texture, scale and animation profile are selected as one unit. */
    public Resolved resolve(String id) {
        ensureLoaded();
        Resolved value = resolved.get(id);
        if (value != null) return value;
        Entry requested = entries.get(id);
        value = load(requested);
        if (value == null) value = load(entries.get(DEFAULT_ID));
        if (value == null) {
            if (empty == null) {
                Entry entry = new Entry("<missing>", new ResourceLocation("touhou_little_maid:models/entity/hakurei_reimu.json"),
                        new ResourceLocation("touhou_little_maid:textures/entity/hakurei_reimu.png"), 1, 1, false, false,
                        false, Collections.<String>emptyList(), true);
                empty = new Resolved(entry, new LegacyBedrockModel());
            }
            value = empty;
        }
        resolved.put(id, value);
        return value;
    }
    private Resolved load(Entry entry) {
        if (entry == null || failed.contains(entry.id)) return null;
        Resolved cached = resolved.get(entry.id);
        if (cached != null) return cached;
        IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
        try {
            // Validate and close the selected texture; never put another skin on this geometry.
            try (InputStream texture = manager.getResource(entry.texture).getInputStream()) { if (javax.imageio.ImageIO.read(texture) == null) throw new IllegalArgumentException("Unreadable texture"); }
            LegacyBedrockModel model;
            try (InputStream geometry = manager.getResource(entry.model).getInputStream()) { model = new LegacyBedrockModel(geometry); }
            LegacyAnimationLibrary library = animationLibrary(manager, entry.profile);
            model.configureAnimations(entry.profile, library);
            Resolved value = new Resolved(entry, model);
            resolved.put(entry.id, value);
            return value;
        } catch (Exception error) {
            failed.add(entry.id);
            TouhouLittleMaid.LOGGER.error("Unable to load maid model {} from {}", entry.id, entry.model, error);
            return null;
        }
    }
    private LegacyAnimationLibrary animationLibrary(IResourceManager manager, LegacyAnimationProfile profile) {
        String key = profile.gecko + ":" + profile.animations.toString();
        LegacyAnimationLibrary cached = libraries.get(key);
        if (cached != null) return cached;
        LegacyAnimationLibrary library = new LegacyAnimationLibrary();
        if (profile.gecko) {
            boolean readable = true;
            for (String path : profile.animations) {
                try { library.merge(readJson(manager, new ResourceLocation(path)), path, false); }
                catch (Exception error) {
                    readable = false;
                    TouhouLittleMaid.LOGGER.error("Unable to load animation file {}; missing channels remain at rest", path, error);
                }
            }
            // SRC GeckoModelLoader.registerMaidAnimations fills only absent names.
            // A failed explicit file cannot be reconstructed safely from another default.
            if (readable && !profile.animations.contains(LegacyAnimationProfile.DEFAULT_JSON)) {
                try { library.merge(readJson(manager, new ResourceLocation(LegacyAnimationProfile.DEFAULT_JSON)), LegacyAnimationProfile.DEFAULT_JSON, true); }
                catch (Exception error) { TouhouLittleMaid.LOGGER.error("Unable to load default maid animation clips", error); }
            }
            for (Map.Entry<String, String> problem : library.diagnostics().entrySet())
                TouhouLittleMaid.LOGGER.warn("Unsupported maid animation {}: {}", problem.getKey(), problem.getValue());
        } else {
            for (String path : profile.animations) if (!LegacyMaidAnimations.applyOne(new LegacyAnimationPose(), path, new LegacyAnimationFrame()))
                TouhouLittleMaid.LOGGER.warn("Unsupported maid script {}; no substitute animation is applied", path);
        }
        libraries.put(key, library);
        return library;
    }
    private static JsonObject readJson(IResourceManager manager, ResourceLocation location) throws java.io.IOException {
        try (InputStream stream = manager.getResource(location).getInputStream()) {
            return new JsonParser().parse(new InputStreamReader(stream, Charset.forName("UTF-8"))).getAsJsonObject();
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
        List<String> domains = new ArrayList<String>(manager.getResourceDomains());
        Collections.sort(domains);
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
              try {
                JsonObject json = element.getAsJsonObject();
                String id = json.get("model_id").getAsString();
                if (id.indexOf(':') < 0) id = domain + ":" + id;
                String path = id.substring(id.indexOf(':') + 1);
                String idDomain = id.indexOf(':') < 0 ? domain : id.substring(0, id.indexOf(':'));
                ResourceLocation model = location(json, "model", idDomain, "models/entity/" + path + ".json");
                ResourceLocation texture = location(json, "texture", idDomain, "textures/entity/" + path + ".png");
                float scale = json.has("render_entity_scale") ? json.get("render_entity_scale").getAsFloat() : 1.0F;
                if (Float.isNaN(scale) || Float.isInfinite(scale)) throw new IllegalArgumentException("Invalid model scale");
                // SRC MaidModelInfo.decorate() explicitly clamps entity scale to [0.2, 2].
                scale = Math.max(.2F, Math.min(2F, scale));
                float itemScale = json.has("render_item_scale") ? json.get("render_item_scale").getAsFloat() : 1.0F;
                boolean showBackpack = !json.has("show_backpack") || json.get("show_backpack").getAsBoolean();
                boolean showCustomHead = !json.has("show_custom_head") || json.get("show_custom_head").getAsBoolean();
                boolean gecko = json.has("is_gecko") && json.get("is_gecko").getAsBoolean();
                List<String> animations = new ArrayList<String>();
                JsonArray animationList = json.getAsJsonArray("animation");
                if (animationList != null) for (JsonElement animation : animationList)
                    animations.add(animation.getAsString().indexOf(':') < 0 ? domain + ":" + animation.getAsString() : animation.getAsString());
                Entry base = new Entry(id, model, texture, scale, itemScale, showBackpack, showCustomHead,
                        gecko, animations, animationList == null || animationList.size() == 0);
                base.describe(json, root, domain, 0);
                entries.put(id, base);

                // CustomModelPack.decorate() in the source exposes every extra texture as a
                // separate selectable model while sharing the same geometry and settings.
                JsonArray extras = json.getAsJsonArray("extra_textures");
                if (extras != null) {
                    int variantIndex=0;
                    for (JsonElement extra : extras) {
                        ResourceLocation extraTexture = resource(extra.getAsString(), idDomain);
                        String extraId = id + "_" + md5(extraTexture.getResourcePath()).toLowerCase(Locale.US);
                        Entry variant = new Entry(extraId, model, extraTexture, scale, itemScale,
                                showBackpack, showCustomHead, gecko, animations,
                                animationList == null || animationList.size() == 0);
                        variant.describe(json, root, domain, ++variantIndex);
                        entries.put(extraId, variant);
                    }
                }
              } catch (Exception error) {
                  TouhouLittleMaid.LOGGER.error("Invalid maid model entry in namespace {}", domain, error);
              }
            }
        } catch (java.io.FileNotFoundException absent) {
            // A namespace without a maid pack is normal.
        } catch (Exception error) {
            TouhouLittleMaid.LOGGER.error("Unable to read maid pack in namespace {}", domain, error);
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
        loaded = false; language=""; translations.clear();
        entries.clear();
        resolved.clear(); failed.clear(); libraries.clear(); empty = null;
    }

    public static final class Entry {
        public final String id;
        public String packId = "", packName = "", name = "", soundPack = "", author = "", version = "", date = "";
        public ResourceLocation packIcon;
        public List<String> description = Collections.emptyList(), packDescription = Collections.emptyList();
        public boolean easterEgg;
        public int variant;
        private void describe(JsonObject json, JsonObject root, String domain, int variant) {
            this.variant = variant; packId = domain;
            packName = text(root,"pack_name",domain); name = text(json,"name","{model."+id.replace(':','.')+".name}");
            soundPack = text(json,"use_sound_pack_id",""); author = text(root,"author","");
            version = text(root,"version",""); date = text(root,"date","");
            description = lines(json,"description"); packDescription = lines(root,"description");
            easterEgg = json.has("easter_egg") && !json.get("easter_egg").isJsonNull();
            if(root.has("icon")) packIcon=resource(root.get("icon").getAsString(),domain);
        }
        private static String text(JsonObject object,String key,String fallback) {
            if(!object.has(key)||object.get(key).isJsonNull())return fallback;
            if(object.get(key).isJsonArray()){StringBuilder b=new StringBuilder();for(JsonElement e:object.getAsJsonArray(key)){if(b.length()>0)b.append(", ");b.append(e.getAsString());}return b.toString();}
            return object.get(key).getAsString();
        }
        private static List<String> lines(JsonObject object,String key){
            List<String> result=new ArrayList<String>();if(object.has(key)&&object.get(key).isJsonArray())for(JsonElement e:object.getAsJsonArray(key))result.add(e.getAsString());return Collections.unmodifiableList(result);
        }
        public final ResourceLocation model;
        public final ResourceLocation texture;
        public final float scale;
        public final float itemScale;
        public final boolean showBackpack;
        public final boolean showCustomHead;
        public final boolean gecko;
        public final LegacyAnimationProfile profile;
        private final List<String> animations;
        private final boolean defaultAnimations;
        private Entry(String id, ResourceLocation model, ResourceLocation texture, float scale, float itemScale,
                      boolean showBackpack, boolean showCustomHead, boolean gecko, List<String> animations,
                      boolean defaultAnimations) {
            this.id = id; this.model = model; this.texture = texture; this.scale = scale;
            this.itemScale = itemScale; this.showBackpack = showBackpack; this.showCustomHead = showCustomHead;
            this.gecko = gecko;
            this.animations = Collections.unmodifiableList(new ArrayList<String>(animations));
            this.profile = new LegacyAnimationProfile(gecko, this.animations);
            this.defaultAnimations = defaultAnimations;
        }

        /** Mirrors MaidModelInfo.decorate(): an empty non-Gecko list receives the source default animation set. */
        public boolean usesAnimation(String path, boolean partOfDefaultSet) {
            return profile.animations.contains(path);
        }
    }
    public static final class Resolved {
        public final Entry entry;
        public final LegacyBedrockModel model;
        private Resolved(Entry entry, LegacyBedrockModel model) { this.entry = entry; this.model = model; }
    }
}

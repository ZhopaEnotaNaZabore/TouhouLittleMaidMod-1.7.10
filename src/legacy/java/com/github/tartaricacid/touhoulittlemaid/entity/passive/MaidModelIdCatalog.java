package com.github.tartaricacid.touhoulittlemaid.entity.passive;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/** Server-safe model-id list matching the bundled client model manifests. */
public final class MaidModelIdCatalog {
    private static final String[] BUNDLED_DOMAINS = {"touhou_little_maid", "touhou_little_maid_old"};
    private static final List<String> IDS = load();

    private MaidModelIdCatalog() { }

    public static String random(Random random) {
        return IDS.isEmpty() ? EntityMaid.DEFAULT_MODEL_ID : IDS.get(random.nextInt(IDS.size()));
    }

    public static List<String> getModelIds() { return IDS; }

    private static List<String> load() {
        List<String> ids = new ArrayList<String>();
        for (String domain : BUNDLED_DOMAINS) loadManifest(domain, ids);
        if (!ids.contains(EntityMaid.DEFAULT_MODEL_ID)) ids.add(EntityMaid.DEFAULT_MODEL_ID);
        Collections.sort(ids);
        return Collections.unmodifiableList(ids);
    }

    private static void loadManifest(String domain, List<String> ids) {
        InputStream stream = MaidModelIdCatalog.class.getResourceAsStream("/assets/" + domain + "/maid_model.json");
        if (stream == null) return;
        try {
            JsonObject root = new JsonParser().parse(new InputStreamReader(stream, "UTF-8")).getAsJsonObject();
            JsonArray models = root.getAsJsonArray("model_list");
            if (models == null) return;
            for (JsonElement element : models) {
                JsonObject model = element.getAsJsonObject();
                if (!model.has("model_id")) continue;
                String id = model.get("model_id").getAsString();
                if (!ids.contains(id)) ids.add(id);
                JsonArray textures = model.getAsJsonArray("extra_textures");
                if (textures == null) continue;
                for (JsonElement texture : textures) {
                    String value = texture.getAsString();
                    String path = value.indexOf(':') >= 0 ? value.substring(value.indexOf(':') + 1) : value;
                    String decorated = id + "_" + md5(path);
                    if (!ids.contains(decorated)) ids.add(decorated);
                }
            }
        } catch (Exception ignored) {
            // The default model below keeps spawning functional if a pack manifest is malformed.
        } finally {
            try { stream.close(); } catch (Exception ignored) { }
        }
    }

    private static String md5(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("MD5").digest(value.getBytes(Charset.forName("UTF-8")));
            StringBuilder result = new StringBuilder(32);
            for (byte item : digest) result.append(String.format(Locale.US, "%02x", item & 255));
            return result.toString();
        } catch (Exception error) {
            throw new IllegalStateException("MD5 is unavailable", error);
        }
    }
}

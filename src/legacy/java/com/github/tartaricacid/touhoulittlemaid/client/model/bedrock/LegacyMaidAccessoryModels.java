package com.github.tartaricacid.touhoulittlemaid.client.model.bedrock;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/** Lazy, reload-safe renderer for the Bedrock backpack layers from the source mod. */
public final class LegacyMaidAccessoryModels implements IResourceManagerReloadListener {
    public static final LegacyMaidAccessoryModels INSTANCE = new LegacyMaidAccessoryModels();
    private final Map<String, LegacyBedrockModel> backpacks = new HashMap<String, LegacyBedrockModel>();

    private LegacyMaidAccessoryModels() { }

    public void renderBackpack(EntityMaid maid, LegacyBedrockModel maidModel, float partialTicks) {
        if (maid.isMaidSleeping() || maid.isInvisible()) return;
        String name = modelName(maid.getBackpackType());
        if (name == null) return;
        LegacyBedrockModel model = get(name);
        if (model == null) return;

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        try {
            GL11.glColor4f(1, 1, 1, 1);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glScalef(1.01F, 1.01F, 1.01F);
            if (maidModel == null || !maidModel.applyBackpackPositioning())
                GL11.glTranslatef(0.0F, -0.5F, 0.25F);
            Minecraft.getMinecraft().getTextureManager().bindTexture(texture(name));
            model.render(maid, 0, 0, maid.ticksExisted + partialTicks, 0, 0, 0.0625F);
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private LegacyBedrockModel get(String name) {
        if (backpacks.containsKey(name)) return backpacks.get(name);
        InputStream stream = null;
        LegacyBedrockModel model = null;
        try {
            stream = Minecraft.getMinecraft().getResourceManager().getResource(model(name)).getInputStream();
            model = new LegacyBedrockModel(stream);
        } catch (Exception error) {
            TouhouLittleMaid.LOGGER.warn("Unable to load maid backpack model {}", name, error);
        } finally {
            if (stream != null) try { stream.close(); } catch (Exception ignored) { }
        }
        backpacks.put(name, model);
        return model;
    }

    private static ResourceLocation model(String name) {
        return new ResourceLocation(TouhouLittleMaid.MOD_ID,
                "models/bedrock/entity/backpack/" + name + ".json");
    }

    private static ResourceLocation texture(String name) {
        String texture = "end_chest_backpack".equals(name) ? "ender_chest_backpack" : name;
        return new ResourceLocation(TouhouLittleMaid.MOD_ID,
                "textures/bedrock/entity/backpack/" + texture + ".png");
    }

    private static String modelName(String type) {
        if ("maid_backpack_small".equals(type)) return "small_backpack";
        if ("maid_backpack_middle".equals(type)) return "middle_backpack";
        if ("maid_backpack_big".equals(type)) return "big_backpack";
        if ("crafting_table_backpack".equals(type)) return "crafting_table_backpack";
        if ("ender_chest_backpack".equals(type)) return "end_chest_backpack";
        if ("furnace_backpack".equals(type)) return "furnace_backpack";
        if ("tank_backpack".equals(type)) return "tank_backpack";
        return null;
    }

    public static String modelNameForType(String type) { return modelName(type); }

    @Override
    public void onResourceManagerReload(IResourceManager manager) { backpacks.clear(); }
}

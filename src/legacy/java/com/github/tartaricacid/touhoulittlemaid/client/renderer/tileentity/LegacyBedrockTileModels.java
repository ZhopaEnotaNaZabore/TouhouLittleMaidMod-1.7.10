package com.github.tartaricacid.touhoulittlemaid.client.renderer.tileentity;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.LegacyBedrockModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/** Shared lazy loader/renderer for the original 1.20 Bedrock tile geometry. */
public final class LegacyBedrockTileModels implements IResourceManagerReloadListener {
    public static final LegacyBedrockTileModels INSTANCE = new LegacyBedrockTileModels();
    private final Map<String, LegacyBedrockModel> models = new HashMap<String, LegacyBedrockModel>();

    private LegacyBedrockTileModels() { }

    public void render(TileEntity tile, double x, double y, double z, String name, String texture) {
        render(tile, x, y, z, name, texture, 1.0F);
    }

    public void render(TileEntity tile, double x, double y, double z, String name, String texture, float scale) {
        LegacyBedrockModel model = model(name);
        if (model == null) return;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(x + 0.5D, y + 1.5D * scale, z + 0.5D);
            GL11.glScalef(-scale, -scale, scale);
            int facing = tile.getWorldObj() == null ? 0 : tile.getBlockMetadata() & 3;
            GL11.glRotatef(facing * 90.0F, 0.0F, 1.0F, 0.0F);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(1, 1, 1, 1);
            Minecraft.getMinecraft().getTextureManager().bindTexture(
                    new ResourceLocation(TouhouLittleMaid.MOD_ID, "textures/bedrock/block/" + texture + ".png"));
            model.render(null, 0, 0, 0, 0, 0, 1.0F / 16.0F);
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    public LegacyBedrockModel model(String name) {
        if (models.containsKey(name)) return models.get(name);
        InputStream stream = null;
        try {
            ResourceLocation location = new ResourceLocation(TouhouLittleMaid.MOD_ID,
                    "models/bedrock/block/" + name + ".json");
            stream = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
            LegacyBedrockModel model = new LegacyBedrockModel(stream);
            models.put(name, model);
            return model;
        } catch (Exception error) {
            TouhouLittleMaid.LOGGER.warn("Unable to load Bedrock tile model {}", name, error);
            models.put(name, null);
            return null;
        } finally {
            if (stream != null) try { stream.close(); } catch (Exception ignored) { }
        }
    }

    @Override public void onResourceManagerReload(IResourceManager manager) { models.clear(); }
}

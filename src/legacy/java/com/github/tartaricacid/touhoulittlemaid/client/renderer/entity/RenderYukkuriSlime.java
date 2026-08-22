package com.github.tartaricacid.touhoulittlemaid.client.renderer.entity;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.LegacyBedrockModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Source-compatible Bedrock replacement shared by slime and magma cube. */
public final class RenderYukkuriSlime extends RenderLiving {
    private final ResourceLocation texture;

    public RenderYukkuriSlime(String name) {
        super(load(name), 0.25F);
        texture = new ResourceLocation(TouhouLittleMaid.MOD_ID,
                "textures/bedrock/entity/" + name + ".png");
    }

    private static LegacyBedrockModel load(String name) {
        java.io.InputStream stream = null;
        try {
            stream = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(
                    TouhouLittleMaid.MOD_ID, "models/bedrock/entity/" + name + ".json")).getInputStream();
            return new LegacyBedrockModel(stream);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not load Yukkuri model " + name, exception);
        } finally {
            if (stream != null) try { stream.close(); } catch (Exception ignored) { }
        }
    }

    @Override
    protected void preRenderCallback(EntityLivingBase living, float partialTicks) {
        EntitySlime slime = (EntitySlime) living;
        float size = slime.getSlimeSize();
        float squish = (slime.prevSquishFactor + (slime.squishFactor - slime.prevSquishFactor) * partialTicks)
                / (size * 0.5F + 1.0F);
        float inverse = 1.0F / (squish + 1.0F);
        GL11.glScalef(inverse * size, size / inverse, inverse * size);
        shadowSize = 0.25F * size;
    }

    @Override protected ResourceLocation getEntityTexture(Entity entity) { return texture; }
}

package com.github.tartaricacid.touhoulittlemaid.client.renderer.entity;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.monster.EntityFairy;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLiving;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.LegacyBedrockModel;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Temporary visible model; textures already match the source pack variants. */
public final class RenderFairy extends RenderLiving {
    public RenderFairy() {
        super(loadModel(), 0.4F);
    }

    private static LegacyBedrockModel loadModel() {
        try {
            ResourceLocation geometry = new ResourceLocation(TouhouLittleMaid.MOD_ID,
                    "models/bedrock/entity/maid_fairy.json");
            return new LegacyBedrockModel(Minecraft.getMinecraft().getResourceManager()
                    .getResource(geometry).getInputStream());
        } catch (Exception exception) {
            throw new IllegalStateException("Could not load maid fairy Bedrock model", exception);
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        EntityFairy fairy = (EntityFairy) entity;
        return new ResourceLocation(TouhouLittleMaid.MOD_ID,
                "textures/bedrock/entity/maid_fairy/maid_fairy_" + fairy.getFairyTypeOrdinal() + ".png");
    }

    @Override
    protected void preRenderCallback(EntityLivingBase entity, float partialTicks) {
        EntityFairy fairy = (EntityFairy) entity;
        float scale = fairy.isBaby() ? 0.75F : 1.0F;
        GL11.glScalef(scale, scale, scale);
        if (!fairy.onGround) GL11.glRotatef(-8.0F, 1.0F, 0.0F, 0.0F);
    }
}

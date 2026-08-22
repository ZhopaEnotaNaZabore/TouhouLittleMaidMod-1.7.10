package com.github.tartaricacid.touhoulittlemaid.client.renderer.entity;

import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.LegacyBedrockModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Shared renderer for non-living Bedrock geometry entities. */
public abstract class RenderBedrockEntity extends Render {
    private final LegacyBedrockModel model;

    protected RenderBedrockEntity(ResourceLocation geometry) {
        try {
            model = new LegacyBedrockModel(Minecraft.getMinecraft().getResourceManager()
                    .getResource(geometry).getInputStream());
        } catch (Exception exception) {
            throw new IllegalStateException("Could not load Bedrock model " + geometry, exception);
        }
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        try {
            bindEntityTexture(entity);
            GL11.glColor4f(1, 1, 1, 1);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glTranslatef((float) x, (float) y + 1.501F, (float) z);
            GL11.glRotatef(180.0F - entity.rotationYaw, 0.0F, 1.0F, 0.0F);
            GL11.glScalef(-1.0F, -1.0F, 1.0F);
            model.render(entity, 0, 0, entity.ticksExisted + partialTicks, 0, 0, 0.0625F);
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }
}

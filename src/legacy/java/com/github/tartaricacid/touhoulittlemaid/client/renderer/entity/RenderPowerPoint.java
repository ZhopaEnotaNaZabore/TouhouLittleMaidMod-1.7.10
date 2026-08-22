package com.github.tartaricacid.touhoulittlemaid.client.renderer.entity;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityPowerPoint;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Camera-facing renderer for the original 4x4 Power Point sprite sheet. */
public final class RenderPowerPoint extends Render {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(TouhouLittleMaid.MOD_ID, "textures/entity/power_point.png");

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        EntityPowerPoint point = (EntityPowerPoint) entity;
        int icon = point.getIcon();
        float u0 = (icon % 4) / 4.0F;
        float u1 = u0 + 0.25F;
        float v0 = (icon / 4) / 4.0F;
        float v1 = v0 + 0.25F;

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        try {
            bindEntityTexture(entity);
            GL11.glColor4f(1, 1, 1, 1);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glTranslatef((float) x, (float) y + 0.1F, (float) z);
            GL11.glRotatef(180.0F - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
            GL11.glScalef(0.3F, 0.3F, 0.3F);

            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 1.0F, 0.0F);
            tessellator.addVertexWithUV(-1.0D, -0.25D, 0.0D, u0, v1);
            tessellator.addVertexWithUV(1.0D, -0.25D, 0.0D, u1, v1);
            tessellator.addVertexWithUV(1.0D, 1.75D, 0.0D, u1, v0);
            tessellator.addVertexWithUV(-1.0D, 1.75D, 0.0D, u0, v0);
            tessellator.draw();
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return TEXTURE;
    }
}

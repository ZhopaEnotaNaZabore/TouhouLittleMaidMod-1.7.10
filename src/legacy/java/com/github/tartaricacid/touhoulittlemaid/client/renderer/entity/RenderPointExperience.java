package com.github.tartaricacid.touhoulittlemaid.client.renderer.entity;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Point-item sprite replacement for vanilla experience orbs. */
public final class RenderPointExperience extends Render {
    private static final ResourceLocation TEXTURE = new ResourceLocation(TouhouLittleMaid.MOD_ID,
            "textures/entity/point_item.png");

    public RenderPointExperience() { shadowSize = 0.15F; shadowOpaque = 0.75F; }

    @Override
    public void doRender(Entity raw, double x, double y, double z, float yaw, float partialTicks) {
        EntityXPOrb orb = (EntityXPOrb) raw;
        int icon = orb.getTextureByXP();
        float u0 = (icon % 4 * 16) / 64.0F, u1 = (icon % 4 * 16 + 16) / 64.0F;
        float v0 = (icon / 4 * 16) / 64.0F, v1 = (icon / 4 * 16 + 16) / 64.0F;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        try {
            bindEntityTexture(raw);
            GL11.glTranslatef((float) x, (float) y + 0.1F, (float) z);
            GL11.glRotatef(180.0F - renderManager.playerViewY, 0, 1, 0);
            GL11.glRotatef(-renderManager.playerViewX, 1, 0, 0);
            GL11.glScalef(0.3F, 0.3F, 0.3F);
            GL11.glColor4f(1, 1, 1, 1);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            Tessellator t = Tessellator.instance;
            t.startDrawingQuads();
            t.setColorRGBA(255, 255, 255, 128);
            t.setNormal(0, 1, 0);
            t.addVertexWithUV(-0.5, -0.25, 0, u0, v1);
            t.addVertexWithUV(0.5, -0.25, 0, u1, v1);
            t.addVertexWithUV(0.5, 0.75, 0, u1, v0);
            t.addVertexWithUV(-0.5, 0.75, 0, u0, v0);
            t.draw();
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    @Override protected ResourceLocation getEntityTexture(Entity entity) { return TEXTURE; }
}

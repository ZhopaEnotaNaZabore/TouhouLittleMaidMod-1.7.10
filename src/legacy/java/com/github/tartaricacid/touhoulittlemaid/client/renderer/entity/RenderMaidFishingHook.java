package com.github.tartaricacid.touhoulittlemaid.client.renderer.entity;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.projectile.EntityMaidFishingHook;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public final class RenderMaidFishingHook extends Render {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/particle/particles.png");

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        EntityMaidFishingHook hook = (EntityMaidFishingHook) entity;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try {
            Tessellator t = Tessellator.instance;
            GL11.glPushMatrix();
            try {
                bindEntityTexture(entity);
                GL11.glColor4f(1, 1, 1, 1);
                GL11.glTranslatef((float) x, (float) y, (float) z);
                GL11.glRotatef(180.0F - renderManager.playerViewY, 0, 1, 0);
                GL11.glRotatef(-renderManager.playerViewX, 1, 0, 0);
                // Vanilla 1.7 fishing-hook sprite is cell (1,2) in the 128px
                // particle atlas. The old 0..16 UV selected the wrong effect.
                float u0 = 8.0F / 128.0F, u1 = 16.0F / 128.0F;
                float v0 = 16.0F / 128.0F, v1 = 24.0F / 128.0F;
                t.startDrawingQuads();
                t.addVertexWithUV(-0.25D, -0.25D, 0, u0, v1);
                t.addVertexWithUV(0.25D, -0.25D, 0, u1, v1);
                t.addVertexWithUV(0.25D, 0.25D, 0, u1, v0);
                t.addVertexWithUV(-0.25D, 0.25D, 0, u0, v0);
                t.draw();
            } finally {
                GL11.glPopMatrix();
            }

            EntityMaid maid = hook.getMaidOwner();
            if (maid != null) {
                float bodyYaw = maid.prevRenderYawOffset
                        + (maid.renderYawOffset - maid.prevRenderYawOffset) * partialTicks;
                double angle = bodyYaw * Math.PI / 180.0D;
                double sin = Math.sin(angle), cos = Math.cos(angle);
                double mx = maid.lastTickPosX + (maid.posX - maid.lastTickPosX) * partialTicks
                        - cos * 0.35D - sin * 0.85D - renderManager.viewerPosX;
                double my = maid.lastTickPosY + (maid.posY - maid.lastTickPosY) * partialTicks
                        + maid.getEyeHeight() - 0.45D - renderManager.viewerPosY;
                double mz = maid.lastTickPosZ + (maid.posZ - maid.lastTickPosZ) * partialTicks
                        - sin * 0.35D + cos * 0.85D - renderManager.viewerPosZ;
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_LIGHTING);
                t.startDrawing(GL11.GL_LINE_STRIP);
                t.setColorOpaque_I(0x202020);
                for (int i = 0; i <= 16; i++) {
                    float f = i / 16.0F;
                    t.addVertex(x + (mx - x) * f,
                            y + (my - y) * (f * f + f) * 0.5D + 0.25D,
                            z + (mz - z) * f);
                }
                t.draw();
            }
        } finally {
            GL11.glPopAttrib();
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) { return TEXTURE; }
}

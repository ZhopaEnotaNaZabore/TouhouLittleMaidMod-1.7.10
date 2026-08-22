package com.github.tartaricacid.touhoulittlemaid.client.renderer.entity;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.projectile.EntityDanmaku;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public final class RenderDanmaku extends Render {
    private static final ResourceLocation TEXTURE = new ResourceLocation(TouhouLittleMaid.MOD_ID, "textures/entity/danmaku.png");

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        EntityDanmaku danmaku = (EntityDanmaku) entity;
        double u0 = 32.0D * danmaku.getColor().ordinal() / 416.0D;
        double u1 = (32.0D * danmaku.getColor().ordinal() + 32.0D) / 416.0D;
        double v0 = 32.0D * danmaku.getDanmakuType().ordinal() / 128.0D;
        double v1 = (32.0D * danmaku.getDanmakuType().ordinal() + 32.0D) / 128.0D;
        double size = danmaku.getDanmakuType().getSize();

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
            Tessellator t = Tessellator.instance;
            t.startDrawingQuads();
            t.addVertexWithUV(-size, size, 0, u0, v0);
            t.addVertexWithUV(-size, -size, 0, u0, v1);
            t.addVertexWithUV(size, -size, 0, u1, v1);
            t.addVertexWithUV(size, size, 0, u1, v0);
            t.draw();
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) { return TEXTURE; }
}

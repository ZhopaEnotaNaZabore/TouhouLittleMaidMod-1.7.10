package com.github.tartaricacid.touhoulittlemaid.client.renderer.entity;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public final class RenderMaidPainting extends Render {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            TouhouLittleMaid.MOD_ID, "textures/painting/wine_fox.png");

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partial) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(x, y, z);
            GL11.glRotatef(yaw, 0, 1, 0);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glColor4f(1, 1, 1, 1);
            bindTexture(TEXTURE);
            GL11.glScalef(1 / 16F, 1 / 16F, 1 / 16F);
            Tessellator t = Tessellator.instance;
            t.startDrawingQuads();
            t.setNormal(0, 0, -1);
            t.addVertexWithUV(16, -24, -.5, 0, 1);
            t.addVertexWithUV(-16, -24, -.5, 1, 1);
            t.addVertexWithUV(-16, 24, -.5, 1, 0);
            t.addVertexWithUV(16, 24, -.5, 0, 0);
            t.setNormal(0, 0, 1);
            t.addVertexWithUV(16, 24, .5, 0, 0);
            t.addVertexWithUV(-16, 24, .5, 1, 0);
            t.addVertexWithUV(-16, -24, .5, 1, 1);
            t.addVertexWithUV(16, -24, .5, 0, 1);
            t.draw();
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) { return TEXTURE; }
}

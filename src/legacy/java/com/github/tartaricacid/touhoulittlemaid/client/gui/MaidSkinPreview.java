package com.github.tartaricacid.touhoulittlemaid.client.gui;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.LegacyMaidPreviewContext;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.LegacyMaidModelRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

/** Detached client entity. Never inserted into the world or ticked through AI. */
final class MaidSkinPreview extends EntityMaid {
    boolean walking, begging, seated, floor;
    ItemStack off;
    MaidSkinPreview(World world,String id){super(world);setModelId(id);onGround=true;setPosition(0,0,0);}
    @Override public boolean isBegging(){return begging;}
    @Override public boolean isSitting(){return seated;}
    @Override public ItemStack getOffhandItem(){return off;}
    void tickPreview(){ticksExisted++;prevLimbSwingAmount=limbSwingAmount;limbSwingAmount=walking?.6F:0;limbSwing+=limbSwingAmount;}
    static void draw(MaidSkinPreview entity,int x,int floorY,float size,float yaw,float pitch,float partial,int left,int top,int width,int height){
        Minecraft mc=Minecraft.getMinecraft();RenderManager manager=RenderManager.instance;
        float oldYaw=manager.playerViewY,oldLightX=OpenGlHelper.lastBrightnessX,oldLightY=OpenGlHelper.lastBrightnessY;int oldMode=GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glPushMatrix();
        try(LegacyMaidPreviewContext preview=LegacyMaidPreviewContext.enter(entity,partial)){
            net.minecraft.client.gui.ScaledResolution resolution=new net.minecraft.client.gui.ScaledResolution(mc,mc.displayWidth,mc.displayHeight);
            double sx=mc.displayWidth/(double)resolution.getScaledWidth(),sy=mc.displayHeight/(double)resolution.getScaledHeight();
            GL11.glEnable(GL11.GL_SCISSOR_TEST);GL11.glScissor((int)(left*sx),(int)((resolution.getScaledHeight()-top-height)*sy),(int)(width*sx),(int)(height*sy));
            GL11.glEnable(GL11.GL_COLOR_MATERIAL);GL11.glEnable(GL11.GL_DEPTH_TEST);GL11.glDepthMask(true);GL11.glColor4f(1,1,1,1);
            GL11.glTranslatef(x,floorY,100);GL11.glScalef(-size,size,size);GL11.glRotatef(180,0,0,1);
            RenderHelper.enableStandardItemLighting();GL11.glRotatef(pitch,1,0,0);GL11.glRotatef(yaw,0,1,0);
            entity.renderYawOffset=entity.prevRenderYawOffset=0;entity.rotationYaw=entity.prevRotationYaw=0;
            entity.rotationYawHead=entity.prevRotationYawHead=0;entity.rotationPitch=entity.prevRotationPitch=0;
            if(entity.floor){
                GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_LIGHTING);GL11.glColor4f(.45F,.45F,.45F,1);
                GL11.glBegin(GL11.GL_QUADS);GL11.glVertex3f(-1,0,-1);GL11.glVertex3f(-1,0,1);GL11.glVertex3f(1,0,1);GL11.glVertex3f(1,0,-1);GL11.glEnd();
                GL11.glEnable(GL11.GL_TEXTURE_2D);RenderHelper.enableStandardItemLighting();GL11.glColor4f(1,1,1,1);
            }
            manager.playerViewY=180;OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,240,240);
            if(entity.ridingEntity instanceof com.github.tartaricacid.touhoulittlemaid.entity.item.EntityChair)
                manager.renderEntityWithPosYaw(entity.ridingEntity,0,-.95,0,0,partial);
            manager.renderEntityWithPosYaw(entity,0,0,0,0,partial);
        } finally {
            manager.playerViewY=oldYaw;OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,oldLightX,oldLightY);RenderHelper.disableStandardItemLighting();
            GL11.glPopMatrix();GL11.glPopAttrib();GL11.glMatrixMode(oldMode);
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);GL11.glColor4f(1,1,1,1);
        }
    }
    static float iconScale(LegacyMaidModelRegistry.Entry entry,float base){
        float scale=entry.itemScale;return base*(Float.isNaN(scale)||Float.isInfinite(scale)?1:Math.max(.1F,Math.min(3F,scale)));
    }
}

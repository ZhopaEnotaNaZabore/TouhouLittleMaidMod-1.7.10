package com.github.tartaricacid.touhoulittlemaid.client.gui;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;
/** Screen-local thumbnails. Release GL textures on close, resize and page changes. */
final class MaidSkinIconCache {
    private final Map<String,Integer> textures=new HashMap<String,Integer>();
    boolean draw(String id,int x,int y,int width,int height){
        Integer texture=textures.get(id);if(texture==null)return false;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try{GL11.glEnable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_LIGHTING);GL11.glColor4f(1,1,1,1);GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture);
            Tessellator t=Tessellator.instance;t.startDrawingQuads();t.addVertexWithUV(x,y+height,0,0,0);t.addVertexWithUV(x+width,y+height,0,1,0);t.addVertexWithUV(x+width,y,0,1,1);t.addVertexWithUV(x,y,0,0,1);t.draw();
        }finally{GL11.glPopAttrib();}return true;
    }
    void capture(String id,int x,int y,int width,int height){
        Minecraft mc=Minecraft.getMinecraft();ScaledResolution r=new ScaledResolution(mc,mc.displayWidth,mc.displayHeight);
        double sx=mc.displayWidth/(double)r.getScaledWidth(),sy=mc.displayHeight/(double)r.getScaledHeight();
        int px=(int)(x*sx),py=(int)((r.getScaledHeight()-y-height)*sy),w=(int)(width*sx),h=(int)(height*sy);
        if(px<0||py<0||px+w>mc.displayWidth||py+h>mc.displayHeight)return;
        GL11.glPushAttrib(GL11.GL_TEXTURE_BIT);int texture=GL11.glGenTextures();
        try{GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_NEAREST);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_NEAREST);
            GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D,0,GL11.GL_RGBA,px,py,w,h,0);textures.put(id,texture);
        }finally{GL11.glPopAttrib();}
    }
    void clear(){for(Integer texture:textures.values())GL11.glDeleteTextures(texture);textures.clear();}
}

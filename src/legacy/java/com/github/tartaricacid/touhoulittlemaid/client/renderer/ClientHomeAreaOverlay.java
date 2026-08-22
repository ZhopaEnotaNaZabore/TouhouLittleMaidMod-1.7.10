package com.github.tartaricacid.touhoulittlemaid.client.renderer;

import com.github.tartaricacid.touhoulittlemaid.client.gui.ClientMaidHomeData;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.SchedulePos;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;import net.minecraft.client.renderer.Tessellator;import net.minecraft.entity.Entity;import net.minecraftforge.client.event.RenderWorldLastEvent;import org.lwjgl.opengl.GL11;
import net.minecraftforge.event.world.WorldEvent;

/** Coloured home-area wireframes for the nearest owned maid with synced data. */
public final class ClientHomeAreaOverlay {
    @SubscribeEvent public void unload(WorldEvent.Unload event){if(event.world.isRemote)ClientMaidHomeData.clear();}
    @SubscribeEvent public void render(RenderWorldLastEvent event){Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.theWorld==null)return;EntityMaid nearest=null;double best=1024;for(Object value:mc.theWorld.loadedEntityList)if(value instanceof EntityMaid){EntityMaid maid=(EntityMaid)value;double d=maid.getDistanceSqToEntity(mc.thePlayer);if(d<best&&maid.getOwner()==mc.thePlayer&&ClientMaidHomeData.get(maid.getEntityId())!=null){nearest=maid;best=d;}}if(nearest==null)return;ClientMaidHomeData.Entry home=ClientMaidHomeData.get(nearest.getEntityId());if(!home.configured||home.dimension!=mc.thePlayer.dimension)return;Entity camera=mc.renderViewEntity;double px=camera.lastTickPosX+(camera.posX-camera.lastTickPosX)*event.partialTicks,py=camera.lastTickPosY+(camera.posY-camera.lastTickPosY)*event.partialTicks,pz=camera.lastTickPosZ+(camera.posZ-camera.lastTickPosZ)*event.partialTicks;GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushMatrix();try{GL11.glTranslated(-px,-py,-pz);GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_LIGHTING);GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);GL11.glLineWidth(2);box(home.work,SchedulePos.WORK_RANGE,.2F,.8F,1);box(home.idle,SchedulePos.IDLE_RANGE,.2F,1,.3F);box(home.sleep,SchedulePos.SLEEP_RANGE,.8F,.3F,1);}finally{GL11.glPopMatrix();GL11.glPopAttrib();}}
    private void box(int[] p,int radius,float r,float g,float b){GL11.glColor4f(r,g,b,.75F);double x1=p[0]-radius,z1=p[2]-radius,x2=p[0]+radius+1,z2=p[2]+radius+1,y=p[1]+.05;Tessellator t=Tessellator.instance;t.startDrawing(GL11.GL_LINE_LOOP);t.addVertex(x1,y,z1);t.addVertex(x2,y,z1);t.addVertex(x2,y,z2);t.addVertex(x1,y,z2);t.draw();}
}

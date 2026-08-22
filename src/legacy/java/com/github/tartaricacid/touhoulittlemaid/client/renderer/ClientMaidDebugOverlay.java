package com.github.tartaricacid.touhoulittlemaid.client.renderer;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;import cpw.mods.fml.common.eventhandler.SubscribeEvent;import net.minecraft.client.Minecraft;import net.minecraftforge.client.event.RenderGameOverlayEvent;
import java.util.HashMap;import java.util.Map;
/** F3 diagnostics for loaded maids without adding an always-on HUD. */
public final class ClientMaidDebugOverlay{
 @SubscribeEvent public void render(RenderGameOverlayEvent.Text event){Minecraft mc=Minecraft.getMinecraft();if(!mc.gameSettings.showDebugInfo||mc.theWorld==null)return;int count=0,working=0,home=0;Map<String,Integer> tasks=new HashMap<String,Integer>();for(Object value:mc.theWorld.loadedEntityList)if(value instanceof EntityMaid){EntityMaid maid=(EntityMaid)value;count++;if(maid.getCurrentActivity()==com.github.tartaricacid.touhoulittlemaid.entity.ai.MaidActivity.WORK)working++;if(maid.isHomeMode())home++;String id=maid.getTaskId();tasks.put(id,tasks.containsKey(id)?tasks.get(id)+1:1);}event.left.add("TLM maids: "+count+" working: "+working+" home: "+home);if(!tasks.isEmpty())event.left.add("TLM tasks: "+tasks);}
}

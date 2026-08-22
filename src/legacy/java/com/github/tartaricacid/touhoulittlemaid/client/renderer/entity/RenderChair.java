package com.github.tartaricacid.touhoulittlemaid.client.renderer.entity;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.LegacyBedrockModel;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityChair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

/** Dynamic bundled/resource-pack chair Bedrock renderer with a cushion fallback. */
public final class RenderChair extends Render implements IResourceManagerReloadListener {
    private static final String FALLBACK="cushion";
    private final Map<String,LegacyBedrockModel> models=new HashMap<String,LegacyBedrockModel>();
    @Override public void doRender(Entity entity,double x,double y,double z,float yaw,float partialTicks){EntityChair chair=(EntityChair)entity;String path=path(chair.getModelId());LegacyBedrockModel model=get(path);if(model==null)return;GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushMatrix();try{bindTexture(texture(path));GL11.glColor4f(1,1,1,1);GL11.glEnable(GL11.GL_TEXTURE_2D);GL11.glTranslatef((float)x,(float)y+1.501F,(float)z);GL11.glRotatef(180-entity.rotationYaw,0,1,0);GL11.glScalef(-1,-1,1);model.render(entity,0,0,entity.ticksExisted+partialTicks,0,0,.0625F);}finally{GL11.glPopMatrix();GL11.glPopAttrib();}}
    private LegacyBedrockModel get(String path){if(models.containsKey(path))return models.get(path);LegacyBedrockModel model=null;try{model=load(path);}catch(Exception ignored){try{model=load(FALLBACK);}catch(Exception failure){TouhouLittleMaid.LOGGER.error("Could not load chair Bedrock model or fallback",failure);}}models.put(path,model);return model;}
    private LegacyBedrockModel load(String path)throws Exception{java.io.InputStream in=Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(TouhouLittleMaid.MOD_ID,"models/entity/"+path+".json")).getInputStream();try{return new LegacyBedrockModel(in);}finally{try{in.close();}catch(Exception ignored){}}}
    private ResourceLocation texture(String path){ResourceLocation selected=new ResourceLocation(TouhouLittleMaid.MOD_ID,"textures/entity/"+path+".png");try{Minecraft.getMinecraft().getResourceManager().getResource(selected);return selected;}catch(Exception ignored){return new ResourceLocation(TouhouLittleMaid.MOD_ID,"textures/entity/"+FALLBACK+".png");}}
    private String path(String id){if(id==null||id.isEmpty())return FALLBACK;int split=id.indexOf(':');String value=split>=0?id.substring(split+1):id;return value.matches("[a-z0-9_./-]+")?value:FALLBACK;}
    @Override protected ResourceLocation getEntityTexture(Entity entity){return texture(path(((EntityChair)entity).getModelId()));}
    @Override public void onResourceManagerReload(IResourceManager manager){models.clear();}
}

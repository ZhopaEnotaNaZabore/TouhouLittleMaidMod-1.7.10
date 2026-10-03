package com.github.tartaricacid.touhoulittlemaid.client.renderer.tileentity;

import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityAltar;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

/** Displays the six synchronized altar offerings as world items. */
public final class RenderAltar extends TileEntitySpecialRenderer {
    @Override public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        TileEntityAltar altar=(TileEntityAltar)tile;if(altar.getWorldObj()==null)return;
        if(altar.isFormed()){
            renderStructure(altar,x,y,z,partialTicks);return;
        }
        LegacyBedrockTileModels.INSTANCE.render(altar, x, y, z, "altar", "altar");
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try { for(int slot=0;slot<altar.getSizeInventory();slot++){ItemStack stack=altar.getStackInSlot(slot);if(stack==null)continue;double a=Math.PI*2*slot/altar.getSizeInventory();EntityItem item=new EntityItem(altar.getWorldObj(),0,0,0,stack);item.age=(int)(altar.getWorldObj().getTotalWorldTime()%6000);item.hoverStart=(float)a;RenderManager.instance.renderEntityWithPosYaw(item,x+.5+Math.cos(a)*.32,y+1.12+Math.sin((altar.getWorldObj().getTotalWorldTime()+slot*7+partialTicks)*.08)*.04,z+.5+Math.sin(a)*.32,0,partialTicks);} }
        finally { GL11.glPopAttrib(); }
    }
    private void renderStructure(TileEntityAltar altar,double x,double y,double z,float ticks){
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushMatrix();
        try{
            if(altar.isRenderCore()){
                GL11.glPushMatrix();
                switch(altar.getFacing()){
                    case 0:GL11.glTranslated(x+1,y-1.5,z-3);GL11.glRotatef(180,0,1,0);break;
                    case 1:GL11.glTranslated(x+4,y-1.5,z+1);GL11.glRotatef(90,0,1,0);break;
                    case 3:GL11.glTranslated(x-3,y-1.5,z);GL11.glRotatef(270,0,1,0);break;
                    default:GL11.glTranslated(x,y-1.5,z+4);
                }
                GL11.glScalef(-1,-1,1);
                bindTexture(new net.minecraft.util.ResourceLocation("touhou_little_maid:textures/bedrock/block/altar.png"));
                GL11.glColor4f(1,1,1,1);GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
                com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.LegacyBedrockModel model=LegacyBedrockTileModels.INSTANCE.model("altar");
                if(model!=null)model.renderStatic(1F/16F,null,null);
                GL11.glPopMatrix();
            }
            ItemStack stack=altar.getStackInSlot(0);
            if(altar.isPedestal()&&stack!=null){
                EntityItem item=new EntityItem(altar.getWorldObj(),0,0,0,stack.copy());
                item.age=(int)(altar.getWorldObj().getTotalWorldTime()%6000);item.hoverStart=0;
                RenderManager.instance.renderEntityWithPosYaw(item,x+.5,y+1.15,z+.5,0,ticks);
            }
        }finally{GL11.glPopMatrix();GL11.glPopAttrib();}
    }
}

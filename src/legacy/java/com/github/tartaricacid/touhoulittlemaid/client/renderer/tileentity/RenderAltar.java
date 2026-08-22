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
        LegacyBedrockTileModels.INSTANCE.render(altar, x, y, z, "altar", "altar");
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try { for(int slot=0;slot<altar.getSizeInventory();slot++){ItemStack stack=altar.getStackInSlot(slot);if(stack==null)continue;double a=Math.PI*2*slot/altar.getSizeInventory();EntityItem item=new EntityItem(altar.getWorldObj(),0,0,0,stack);item.age=(int)(altar.getWorldObj().getTotalWorldTime()%6000);item.hoverStart=(float)a;RenderManager.instance.renderEntityWithPosYaw(item,x+.5+Math.cos(a)*.32,y+1.12+Math.sin((altar.getWorldObj().getTotalWorldTime()+slot*7+partialTicks)*.08)*.04,z+.5+Math.sin(a)*.32,0,partialTicks);} }
        finally { GL11.glPopAttrib(); }
    }
}

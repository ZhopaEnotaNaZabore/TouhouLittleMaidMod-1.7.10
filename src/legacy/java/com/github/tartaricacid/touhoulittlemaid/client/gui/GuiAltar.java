package com.github.tartaricacid.touhoulittlemaid.client.gui;

import com.github.tartaricacid.touhoulittlemaid.inventory.container.ContainerAltar;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityAltar;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public final class GuiAltar extends GuiContainer {
    private static final ResourceLocation TEXTURE=new ResourceLocation("textures/gui/container/generic_54.png");
    public GuiAltar(InventoryPlayer player, TileEntityAltar altar) { super(new ContainerAltar(player,altar)); ySize=133; }
    @Override protected void drawGuiContainerBackgroundLayer(float ticks,int mouseX,int mouseY) {
        GL11.glColor4f(1,1,1,1); mc.getTextureManager().bindTexture(TEXTURE);
        // Use the blank header as the six-slot panel, then the vanilla player inventory.
        drawTexturedModalRect(guiLeft,guiTop,0,0,176,17);
        drawTexturedModalRect(guiLeft,guiTop+17,0,0,176,17);
        drawTexturedModalRect(guiLeft,guiTop+34,0,0,176,3);
        drawTexturedModalRect(guiLeft,guiTop+37,0,126,176,96);
        for(int i=0;i<6;i++) drawTexturedModalRect(guiLeft+34+i*18,guiTop+19,7,17,18,18);
    }
    @Override protected void drawGuiContainerForegroundLayer(int mouseX,int mouseY) {
        fontRendererObj.drawString(I18n.format("container.tlm.altar"),8,6,4210752);
        fontRendererObj.drawString(I18n.format("container.inventory"),8,40,4210752);
    }
}

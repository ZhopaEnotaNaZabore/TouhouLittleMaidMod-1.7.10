package com.github.tartaricacid.touhoulittlemaid.client.gui;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.capability.LegacyPlayerPower;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.ContainerMaidBeacon;
import com.github.tartaricacid.touhoulittlemaid.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.network.message.MessageMaidBeaconAction;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityMaidBeacon;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

public final class GuiMaidBeacon extends GuiContainer {
    private static final ResourceLocation BG=new ResourceLocation(TouhouLittleMaid.MOD_ID,"textures/gui/maid_beacon.png");
    private final TileEntityMaidBeacon beacon;
    public GuiMaidBeacon(TileEntityMaidBeacon beacon){super(new ContainerMaidBeacon(beacon));this.beacon=beacon;xSize=300;ySize=113;}
    @Override public void initGui(){super.initGui();buttonList.clear();for(int i=0;i<5;i++)buttonList.add(new GuiButton(10+i,guiLeft+146+i*26,guiTop+19,24,20,""+(i+1)));buttonList.add(new GuiButton(20,guiLeft+118,guiTop+72,76,20,StatCollector.translateToLocal("gui.touhou_little_maid.maid_beacon.add_one")));buttonList.add(new GuiButton(21,guiLeft+196,guiTop+72,76,20,StatCollector.translateToLocal("gui.touhou_little_maid.maid_beacon.min_one")));buttonList.add(new GuiButton(22,guiLeft+118,guiTop+94,154,20,overflowText(beacon.isOverflowDelete())));}
    @Override protected void actionPerformed(GuiButton button){if(button.id>=10&&button.id<15){int index=button.id-10;if(beacon.getPotionIndex()==index)index=-1;beacon.setPotionIndex(index);send(MessageMaidBeaconAction.EFFECT,index);}else if(button.id==20)send(MessageMaidBeaconAction.DEPOSIT,1);else if(button.id==21)send(MessageMaidBeaconAction.TAKE,1);else if(button.id==22){boolean next=!beacon.isOverflowDelete();beacon.setOverflowDelete(next);button.displayString=overflowText(next);send(MessageMaidBeaconAction.OVERFLOW,next?1:0);} }
    private String overflowText(boolean delete){return StatCollector.translateToLocal(delete?"gui.touhou_little_maid.maid_beacon.overflow_delete_true":"gui.touhou_little_maid.maid_beacon.overflow_delete_false");}
    private void send(int action,int value){NetworkHandler.channel.sendToServer(new MessageMaidBeaconAction(beacon,action,value));}
    @Override protected void drawGuiContainerBackgroundLayer(float partial,int mouseX,int mouseY){GL11.glColor4f(1,1,1,1);mc.getTextureManager().bindTexture(BG);drawTexturedModalRect(guiLeft,guiTop+2,0,0,142,111);drawTexturedModalRect(guiLeft+118,guiTop+1,44,111,154,15);drawTexturedModalRect(guiLeft+146,guiTop+46,58,128,74,9);drawTexturedModalRect(guiLeft+146,guiTop+59,58,128,74,9);int stored=(int)(74*beacon.getStoragePower()/Math.max(.001F,beacon.getMaxStorage()));drawTexturedModalRect(guiLeft+146,guiTop+48,58,138,stored,5);float playerPower=mc.thePlayer==null?0:LegacyPlayerPower.get(mc.thePlayer).get();drawTexturedModalRect(guiLeft+146,guiTop+61,58,143,(int)(74*playerPower/LegacyPlayerPower.MAX),5);}
    @Override public void updateScreen(){super.updateScreen();int selected=beacon.getPotionIndex();for(Object value:buttonList){GuiButton button=(GuiButton)value;if(button.id>=10&&button.id<15){int number=button.id-9;button.displayString=button.id-10==selected?"["+number+"]":Integer.toString(number);}}}
    @Override protected void drawGuiContainerForegroundLayer(int mouseX,int mouseY){fontRendererObj.drawString(String.format("%.2f",beacon.getStoragePower()),240,46,0xffffff);fontRendererObj.drawString(String.format("%.2f",LegacyPlayerPower.get(mc.thePlayer).get()),240,60,0xffffff);int effect=beacon.getPotionIndex();String cost=StatCollector.translateToLocalFormatted("gui.touhou_little_maid.maid_beacon.cost_power",String.format("%.2f",effect<0?0:beacon.getEffectCost()*900));fontRendererObj.drawString(cost,195-fontRendererObj.getStringWidth(cost)/2,5,effect<0?0x404040:0x9b1b1b);}
}

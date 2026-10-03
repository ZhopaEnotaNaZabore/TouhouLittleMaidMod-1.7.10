package com.github.tartaricacid.touhoulittlemaid.client.gui;

import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.LegacyMaidModelRegistry.Entry;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityChair;
import net.minecraft.client.gui.*;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

/** Interactive model inspection with local-only pose and equipment switches. */
final class GuiMaidSkinDetails extends GuiScreen {
    private static final ResourceLocation ATLAS=new ResourceLocation("touhou_little_maid:textures/gui/skin_detail.png");
    private static final String[] KEYS={"beg","walk","sit","ride","helmet","chest_plate","leggings","boots","main_hand","off_hand"};
    private final GuiMaidSkins browser;
    private final Entry entry;
    private final MaidSkinPreview preview;
    private final boolean[] toggled=new boolean[10];
    private float yaw=25,pitch,zoom=75,panX,panY;
    private int dragX,dragY;
    private boolean floor=true;
    GuiMaidSkinDetails(GuiMaidSkins browser,Entry entry){this.browser=browser;this.entry=entry;preview=new MaidSkinPreview(browser.maid.worldObj,entry.id);}
    private String text(String key){return GuiMaidSkins.tr("gui.touhou_little_maid.skin."+key);}
    @Override public void initGui(){buttons();}
    private void buttons(){buttonList.clear();
        buttonList.add(new GuiButton(0,4,4,76,20,text("back")));
        GuiButton apply=new GuiButton(1,width-84,4,80,20,text("apply"));apply.enabled=!entry.easterEgg;buttonList.add(apply);
        buttonList.add(new GuiButton(2,84,4,76,20,text("reset")));
        buttonList.add(new GuiButton(3,4,height-24,124,20,text("floor")+": "+(floor?"+":"-")));
        for(int i=0;i<KEYS.length;i++)buttonList.add(new GuiButton(10+i,4,30+i*16,124,15,(toggled[i]?"+ ":"- ")+GuiMaidSkins.tr("gui.touhou_little_maid.skin_details."+KEYS[i])));
    }
    @Override public void updateScreen(){preview.tickPreview();if(!browser.maid.isEntityAlive()||mc.thePlayer.getDistanceSqToEntity(browser.maid)>=64)mc.displayGuiScreen(null);}
    @Override public void drawScreen(int mx,int my,float partial){
        drawDefaultBackground();drawRect(132,25,width,height,0xff333333);drawRect(0,25,132,height,0xff535963);
        GL11.glColor4f(1,1,1,1);mc.getTextureManager().bindTexture(ATLAS);
        for(int x=0;x<width;x+=128)drawTexturedModalRect(x,24,0,12,Math.min(128,width-x),12);
        preview.floor=floor;
        MaidSkinPreview.draw(preview,(int)((width+132)/2F+panX),(int)(height*.8F+panY),MaidSkinPreview.iconScale(entry,zoom),yaw,pitch,partial,133,36,Math.max(1,width-133),Math.max(1,height-36));
        fontRendererObj.drawSplitString(GuiMaidSkins.name(entry),138,39,Math.max(1,width-146),0xffffff);
        fontRendererObj.drawSplitString(text("camera"),138,height-25,Math.max(1,width-146),0xdddddd);
        super.drawScreen(mx,my,partial);
        if(mx>132&&my>=36&&my<60){java.util.List<String> lines=new java.util.ArrayList<String>();lines.add(entry.id);for(String d:entry.description)lines.addAll(fontRendererObj.listFormattedStringToWidth(GuiMaidSkins.label(d),240));drawHoveringText(lines,mx,my,fontRendererObj);}
    }
    @Override protected void actionPerformed(GuiButton b){
        if(b.id==0){mc.displayGuiScreen(browser);return;}if(b.id==1){browser.apply(entry);mc.displayGuiScreen(browser);return;}
        if(b.id==2){yaw=25;pitch=0;zoom=75;panX=panY=0;return;}if(b.id==3){floor=!floor;buttons();return;}
        int i=b.id-10;if(i<0||i>=10)return;toggled[i]=!toggled[i];
        preview.begging=toggled[0];preview.walking=toggled[1];preview.seated=toggled[2];
        if(i==3)preview.ridingEntity=toggled[3]?new EntityChair(preview.worldObj):null;
        if(i>=4&&i<=7){net.minecraft.item.Item[] armor={Items.diamond_helmet,Items.diamond_chestplate,Items.diamond_leggings,Items.diamond_boots};preview.setCurrentItemOrArmor(8-i,toggled[i]?new ItemStack(armor[i-4]):null);}
        if(i==8)preview.setCurrentItemOrArmor(0,toggled[i]?new ItemStack(Items.diamond_sword):null);
        if(i==9)preview.off=toggled[i]?new ItemStack(net.minecraft.init.Blocks.torch):null;
        buttons();
    }
    @Override protected void mouseClicked(int x,int y,int button){super.mouseClicked(x,y,button);dragX=x;dragY=y;}
    @Override protected void mouseClickMove(int x,int y,int button,long elapsed){
        if(dragX>132&&dragY>35){if(button==0){yaw+=(x-dragX);pitch=Math.max(-80,Math.min(80,pitch+(y-dragY)));}else if(button==1){panX+=x-dragX;panY+=y-dragY;}}
        dragX=x;dragY=y;
    }
    @Override public void handleMouseInput(){super.handleMouseInput();int wheel=Mouse.getEventDWheel();if(wheel!=0)zoom=Math.max(10,Math.min(180,zoom*(wheel>0?1.1F:.9F)));}
    @Override protected void keyTyped(char c,int key){if(key==1)mc.displayGuiScreen(browser);}
    @Override public boolean doesGuiPauseGame(){return false;}
}

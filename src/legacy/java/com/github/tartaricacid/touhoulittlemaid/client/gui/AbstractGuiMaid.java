package com.github.tartaricacid.touhoulittlemaid.client.gui;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.network.message.MessageOpenMaidGui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Common 1.7.10 implementation of the 1.20 maid screen frame.
 *
 * The original screen is 256x256.  Keeping that coordinate system is
 * important: the left 80 pixels belong to the maid preview/status panel and
 * the player inventory starts at x=88.  The old temporary port used a vanilla
 * 176-pixel container and consequently placed controls on top of one another.
 */
abstract class AbstractGuiMaid extends GuiContainer {
    protected static final ResourceLocation MAIN_BG = texture("maid_gui_main.png");
    protected static final ResourceLocation SIDE = texture("maid_gui_side.png");
    protected static final ResourceLocation BUTTONS = texture("maid_gui_button.png");
    protected static final ResourceLocation BACKPACK = texture("maid_gui_backpack.png");
    protected static final ResourceLocation BAUBLE = texture("maid_gui_bauble.png");
    protected static final ResourceLocation BAUBLE_BUTTON = texture("bauble_button.png");
    protected static final ResourceLocation TASK_PANEL = texture("maid_gui_task.png");

    protected final EntityMaid maid;
    private final int currentTab;

    protected AbstractGuiMaid(Container container, EntityMaid maid, int currentTab) {
        super(container);
        this.maid = maid;
        this.currentTab = currentTab;
        this.xSize = 256;
        this.ySize = 256;
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(TouhouLittleMaid.MOD_ID, "textures/gui/" + name);
    }

    @Override
    public void initGui() {
        super.initGui();
        // Original top tabs: main, task configuration and maid equipment.
        addTab(100, 0, 94, 107);
        addTab(101, 3, 119, 132);
        addTab(102, 2, 144, 157);
        buttonList.add(new SkinButton(guiLeft+62,guiTop+14));
    }

    private static final class SkinButton extends GuiButton {
        SkinButton(int x,int y){super(103,x,y,9,9,"");}
        @Override public void drawButton(net.minecraft.client.Minecraft mc,int mx,int my){if(!visible)return;
            boolean hover=mx>=xPosition&&mx<xPosition+width&&my>=yPosition&&my<yPosition+height;
            mc.getTextureManager().bindTexture(BUTTONS);GL11.glColor4f(1,1,1,1);
            drawTexturedModalRect(xPosition,yPosition,72,hover?53:43,9,9);
        }
    }

    private void addTab(int id, int targetTab, int x, int textureX) {
        buttonList.add(new MaidTabButton(id, guiLeft + x, guiTop + 5,
                textureX, targetTab == currentTab));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 103) {mc.displayGuiScreen(new GuiMaidSkins(maid));return;}
        if (button.id >= 100 && button.id <= 102) {
            int target = button.id == 100 ? 0 : button.id == 101 ? 3 : 2;
            if (target != currentTab) {
                NetworkHandler.channel.sendToServer(new MessageOpenMaidGui(maid.getEntityId(), target));
            }
        }
    }

    protected final void openBaubleTab() {
        if (currentTab != 1) {
            NetworkHandler.channel.sendToServer(new MessageOpenMaidGui(maid.getEntityId(), 1));
        } else {
            NetworkHandler.channel.sendToServer(new MessageOpenMaidGui(maid.getEntityId(), 0));
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glEnable(GL11.GL_BLEND);
        mc.getTextureManager().bindTexture(MAIN_BG);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, 256, 256);
        drawPageBackground();
        drawBaseStatus();
        GL11.glPopAttrib();

        // GuiInventory changes lighting and matrices internally, so render it
        // after the flat background and isolate its state from slot rendering.
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        float previousViewYaw=net.minecraft.client.renderer.entity.RenderManager.instance.playerViewY;
        try(com.github.tartaricacid.touhoulittlemaid.client.renderer.LegacyMaidPreviewContext preview=
                    com.github.tartaricacid.touhoulittlemaid.client.renderer.LegacyMaidPreviewContext.enter(maid,partialTicks)){
            double sx=mc.displayWidth/(double)width, sy=mc.displayHeight/(double)height;
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor((int)((guiLeft+5)*sx),(int)((height-guiTop-108)*sy),(int)(73*sx),(int)(103*sy));
            GuiInventory.func_147046_a(guiLeft + 40, guiTop + 106, 38,
                    guiLeft + 40 - mouseX, guiTop + 62 - mouseY, maid);
        } finally {
            net.minecraft.client.renderer.entity.RenderManager.instance.playerViewY=previousViewYaw;
            GL11.glPopMatrix();GL11.glPopAttrib();
        }
        GL11.glColor4f(1, 1, 1, 1);
    }

    protected abstract void drawPageBackground();

    protected final void drawBackpackPanel() {
        mc.getTextureManager().bindTexture(BACKPACK);
        drawTexturedModalRect(guiLeft + 85, guiTop + 36, 0, 0, 165, 128);
    }

    protected final void drawBaublePanel() {
        mc.getTextureManager().bindTexture(BAUBLE);
        drawTexturedModalRect(guiLeft + 85, guiTop + 36, 0, 0, 165, 128);
    }

    protected final GuiButton createBaubleButton(int id, boolean open) {
        return new MaidBaubleButton(id, guiLeft + 85, guiTop + 97, open);
    }

    protected final void drawSlot(int x, int y) {
        drawRect(guiLeft + x - 1, guiTop + y - 1, guiLeft + x + 17, guiTop + y + 17, 0xFF373737);
        drawRect(guiLeft + x, guiTop + y, guiLeft + x + 16, guiTop + y + 16, 0xFF8B8B8B);
    }

    private void drawBaseStatus() {
        double health = maid.getMaxHealth() <= 0 ? 0 : maid.getHealth() / maid.getMaxHealth();
        double[] values={health,maid.getTotalArmorValue()/20.0,(maid.getMaidExperience()%120)/120.0,maid.getFavorabilityManager().getLevelPercent()};
        mc.getTextureManager().bindTexture(SIDE);
        for(int i=0;i<4;i++){
            int y=guiTop+113+11*i;
            drawTexturedModalRect(guiLeft+5,y,0,9,47,9);
            drawTexturedModalRect(guiLeft+7,y+2,2,18+5*i,(int)(43*Math.max(0,Math.min(1,values[i]))),5);
            drawTexturedModalRect(guiLeft+53,y,i*9,0,9,9);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawStatusNumber((int) maid.getHealth(), 114);
        drawStatusNumber(maid.getTotalArmorValue(), 125);
        drawStatusNumber(maid.getMaidExperience() / 120, 136);
        drawStatusNumber(maid.getFavorabilityManager().getLevel(), 147);
        fontRendererObj.drawString(net.minecraft.util.StatCollector.translateToLocal("container.inventory"), 88, 164, 0x404040);
        drawPageForeground(mouseX, mouseY);
    }

    private void drawStatusNumber(int value, int y) {
        GL11.glPushMatrix();
        GL11.glScalef(0.5F, 0.5F, 1);
        fontRendererObj.drawString(Integer.toString(value), 126, y * 2 + 4, 0x404040);
        GL11.glPopMatrix();
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        int x = mouseX - guiLeft, y = mouseY - guiTop;
        for(Object value:buttonList){
            GuiButton button=(GuiButton)value;
            if(button.id<100||button.id>103||!button.visible)continue;
            if(mouseX>=button.xPosition&&mouseX<button.xPosition+button.width&&mouseY>=button.yPosition&&mouseY<button.yPosition+button.height){
                String key=button.id==103?"gui.touhou_little_maid.button.skin":button.id==100?"container.inventory":button.id==101?"gui.touhou_little_maid.task_configuration":"gui.touhou_little_maid.equipment";
                drawHoveringText(java.util.Collections.singletonList(net.minecraft.util.StatCollector.translateToLocal(key)),mouseX,mouseY,fontRendererObj);
            }
        }
        if (x >= 5 && x < 78 && y >= 5 && y < 108 && !(x>=62&&x<71&&y>=14&&y<23))
            drawHoveringText(java.util.Collections.singletonList(maid.getCommandSenderName()), mouseX, mouseY, fontRendererObj);
        if (x >= 5 && x < 78 && y >= 113 && y < 157) {
            int row = Math.min(3, (y - 113) / 11);
            String[] keys = {"health", "armor", "experience", "favorability"};
            String[] values = {(int)maid.getHealth()+"/"+(int)maid.getMaxHealth(), Integer.toString(maid.getTotalArmorValue()), Integer.toString(maid.getMaidExperience()), Integer.toString(maid.getFavorability())};
            drawHoveringText(java.util.Collections.singletonList(net.minecraft.util.StatCollector.translateToLocal("gui.touhou_little_maid.status."+keys[row])+": "+values[row]), mouseX, mouseY, fontRendererObj);
        }
    }

    protected abstract void drawPageForeground(int mouseX, int mouseY);

    private static final class MaidTabButton extends GuiButton {
        private final int textureX;
        private final boolean selected;

        private MaidTabButton(int id, int x, int y, int textureX, boolean selected) {
            super(id, x, y, 24, 26, "");
            this.textureX = textureX;
            this.selected = selected;
            this.enabled = !selected;
        }

        @Override
        public void drawButton(net.minecraft.client.Minecraft minecraft, int mouseX, int mouseY) {
            if (!visible) return;
            minecraft.getTextureManager().bindTexture(SIDE);
            GL11.glColor4f(1, 1, 1, 1);
            if (selected) {
                drawTexturedModalRect(xPosition, yPosition, textureX, 21, width, height);
            }
            drawTexturedModalRect(xPosition + 4, yPosition + 6, textureX, 47, 16, 16);
        }
    }

    /** Exact 54x63 control used by the source screen (closed/open rows). */
    private static final class MaidBaubleButton extends GuiButton {
        private final boolean open;

        private MaidBaubleButton(int id, int x, int y, boolean open) {
            super(id, x, y, 54, 63, "");
            this.open = open;
        }

        @Override
        public void drawButton(net.minecraft.client.Minecraft minecraft, int mouseX, int mouseY) {
            if (!visible) return;
            minecraft.getTextureManager().bindTexture(BAUBLE_BUTTON);
            GL11.glColor4f(1, 1, 1, 1);
            drawTexturedModalRect(xPosition, yPosition, 0, open ? 63 : 0, width, height);
        }
    }
}

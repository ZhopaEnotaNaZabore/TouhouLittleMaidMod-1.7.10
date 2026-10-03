package com.github.tartaricacid.touhoulittlemaid.client.gui;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.ContainerMaid;
import com.github.tartaricacid.touhoulittlemaid.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.network.message.MessageCycleMaidTask;
import com.github.tartaricacid.touhoulittlemaid.network.message.MessageMaidConfig;
import com.github.tartaricacid.touhoulittlemaid.network.message.MessageRequestMaidHome;
import com.github.tartaricacid.touhoulittlemaid.network.message.MessageSetMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public final class GuiMaid extends AbstractGuiMaid {
    private static boolean taskListOpen;
    private static int taskPage;
    public GuiMaid(InventoryPlayer inventory, EntityMaid maid) {
        super(new ContainerMaid(inventory, maid), maid, 0);
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(new TaskTextureButton(0, guiLeft + 4, guiTop + 159, 71, 21, BUTTONS, 0, 42, ""));
        buttonList.add(new ControlButton(2, guiLeft + 9, guiTop + 187, true));
        buttonList.add(new ControlButton(3, guiLeft + 9, guiTop + 206, false));
        buttonList.add(new ControlButton(4, guiLeft + 30, guiTop + 206, false));
        buttonList.add(new GuiButton(5, guiLeft + 51, guiTop + 206, 20, 20, "S"));
        GuiButton backpack = new GuiButton(7, guiLeft + 72, guiTop + 187, 12, 14, "B");
        backpack.enabled = hasSpecialBackpackScreen();
        buttonList.add(backpack);
        buttonList.add(createBaubleButton(6, false));
        if (taskListOpen) addTaskListButtons();
        NetworkHandler.channel.sendToServer(new MessageRequestMaidHome(maid.getEntityId()));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        // Only the three actual tab buttons belong to AbstractGuiMaid. The
        // previous broad >=100 check also swallowed profession rows 200-211
        // and their paging controls 220-222, so the task picker looked usable
        // but could never send MessageSetMaidTask.
        if (button.id >= 100 && button.id <= 103) {
            super.actionPerformed(button);
            return;
        }
        switch (button.id) {
            case 0:
                taskListOpen = !taskListOpen;
                rebuildButtons();
                break;
            case 2:
                sendConfig(MessageMaidConfig.CYCLE_SCHEDULE);
                button.displayString = shortSchedule();
                break;
            case 3:
                sendConfig(MessageMaidConfig.TOGGLE_HOME);
                break;
            case 4:
                sendConfig(MessageMaidConfig.TOGGLE_PICKUP);
                break;
            case 5:
                sendConfig(MessageMaidConfig.TOGGLE_SITTING);
                break;
            case 6:
                openBaubleTab();
                break;
            case 7:
                sendConfig(MessageMaidConfig.OPEN_BACKPACK);
                break;
            default:
                if (button.id >= 200 && button.id < 212) selectTask(button.id - 200);
                else if (button.id == 220 && taskPage > 0) { taskPage--; rebuildButtons(); }
                else if (button.id == 221 && (taskPage + 1) * 12 < TaskManager.getTasks().size()) { taskPage++; rebuildButtons(); }
                else if (button.id == 222) { taskListOpen = false; rebuildButtons(); }
                break;
        }
    }

    @Override public void updateScreen() {
        super.updateScreen();
        for (Object value : buttonList) {
            GuiButton button = (GuiButton)value;
            if (button.id == 2) button.displayString = shortSchedule();


            if (button.id == 5) button.displayString = (maid.isSitting() ? "\u00a7a" : "\u00a7c") + "S";
            if (button.id == 7) button.enabled = hasSpecialBackpackScreen();
        }
    }

    @Override public void drawScreen(int mouseX, int mouseY, float ticks) {
        super.drawScreen(mouseX, mouseY, ticks);
        for (Object value : buttonList) {
            GuiButton button = (GuiButton)value;
            if (!button.visible || mouseX < button.xPosition || mouseX >= button.xPosition + button.width
                    || mouseY < button.yPosition || mouseY >= button.yPosition + button.height) continue;
            String key = button.id == 3 ? "home" : button.id == 4 ? "pickup" : button.id == 5 ? "sitting" : button.id == 7 ? "backpack" : null;
            String text = key == null ? null : net.minecraft.util.StatCollector.translateToLocal("gui.touhou_little_maid.control."+key);
            if (button.id == 0) text = readableTask(maid.getTaskId());
            if (button.id == 2) {
                long time=(maid.worldObj.getWorldTime()%24000+24000)%24000;
                text=net.minecraft.util.StatCollector.translateToLocal("gui.touhou_little_maid.schedule."+maid.getSchedule().name().toLowerCase(java.util.Locale.ROOT))
                    +String.format(java.util.Locale.ROOT,"  %02d:%02d",(time/1000+6)%24,(time%1000)*60/1000);
            }
            if (button.id == 7 && !hasSpecialBackpackScreen()) text = net.minecraft.util.StatCollector.translateToLocal("gui.touhou_little_maid.control.backpack_open");
            if (button.id >= 200 && button.id < 212) {
                List<IMaidTask> tasks = new ArrayList<IMaidTask>(TaskManager.getTasks().values());
                int index = taskPage*12+button.id-200;
                if (index < tasks.size()) text = net.minecraft.util.StatCollector.translateToLocal(tasks.get(index).getId().replace(":", ".").replace("touhou_little_maid.", "task.touhou_little_maid."));
            }
            if (text != null) drawHoveringText(java.util.Collections.singletonList(text),mouseX,mouseY,fontRendererObj);
        }
    }

    private boolean hasSpecialBackpackScreen() {
        String type=maid.getBackpackType();
        return "crafting_table_backpack".equals(type) || "ender_chest_backpack".equals(type)
                || "furnace_backpack".equals(type) || "tank_backpack".equals(type);
    }

    private void sendConfig(int action) {
        NetworkHandler.channel.sendToServer(new MessageMaidConfig(maid.getEntityId(), action));
    }

    @Override
    protected void drawPageBackground() {
        drawBackpackPanel();
        if (taskListOpen) {
            mc.getTextureManager().bindTexture(TASK_PANEL);
            GL11.glColor4f(1, 1, 1, 1);
            drawTexturedModalRect(guiLeft - 89, guiTop, 0, 0, 89, 256);
        }
    }

    @Override
    protected void drawPageForeground(int mouseX, int mouseY) {
        drawTaskIcon(maid.getTaskId(),6,161);
        fontRendererObj.drawString(shortTaskId(maid.getTaskId()), 26, 165, 0x333333);
        if (taskListOpen) fontRendererObj.drawString((taskPage + 1) + "/" + ((TaskManager.getTasks().size() + 11) / 12), -48, 12, 0x333333);
    }

    private String shortTaskId(String id) {
        return fontRendererObj.trimStringToWidth(readableTask(id == null ? "idle" : id), 47);
    }

    private String shortSchedule() {
        String key = "gui.touhou_little_maid.schedule." + maid.getSchedule().name().toLowerCase(java.util.Locale.ROOT);
        return fontRendererObj.trimStringToWidth(net.minecraft.util.StatCollector.translateToLocal(key), 55);
    }

    private void addTaskListButtons() {
        List<IMaidTask> tasks = new ArrayList<IMaidTask>(TaskManager.getTasks().values());
        if (taskPage * 12 >= tasks.size()) taskPage = 0;
        for (int count = 0; count < 12; count++) {
            int index = taskPage * 12 + count;
            if (index >= tasks.size()) break;
            String id = tasks.get(index).getId();
            buttonList.add(new TaskTextureButton(200 + count, guiLeft - 89, guiTop + 23 + 19 * count,
                    83, 19, TASK_PANEL, 93, 28, readableTask(id)));
        }
        buttonList.add(new TaskTextureButton(220, guiLeft - 89, guiTop + 9, 16, 13, TASK_PANEL, 110, 0, ""));
        buttonList.add(new TaskTextureButton(221, guiLeft - 72, guiTop + 9, 16, 13, TASK_PANEL, 93, 0, ""));
        for(Object value:buttonList){GuiButton b=(GuiButton)value;if(b.id==220)b.enabled=taskPage>0;if(b.id==221)b.enabled=(taskPage+1)*12<tasks.size();}
        buttonList.add(new TaskTextureButton(222, guiLeft - 19, guiTop + 9, 13, 13, TASK_PANEL, 127, 0, ""));
    }

    private void selectTask(int row) {
        int index = taskPage * 12 + row;
        List<IMaidTask> tasks = new ArrayList<IMaidTask>(TaskManager.getTasks().values());
        if (index < 0 || index >= tasks.size()) return;
        NetworkHandler.channel.sendToServer(new MessageSetMaidTask(maid.getEntityId(), tasks.get(index).getId()));
        taskListOpen = false;
        rebuildButtons();
    }

    private void rebuildButtons() { buttonList.clear(); initGui(); }

    @Override public void handleMouseInput() {
        super.handleMouseInput();
        int wheel=org.lwjgl.input.Mouse.getEventDWheel();
        if(!taskListOpen||wheel==0)return;
        int x=org.lwjgl.input.Mouse.getEventX()*width/mc.displayWidth-guiLeft;
        int y=height-org.lwjgl.input.Mouse.getEventY()*height/mc.displayHeight-1-guiTop;
        if(x < -89||x>=0||y<0||y>=256)return;
        int next=Math.max(0,Math.min((TaskManager.getTasks().size()-1)/12,taskPage+(wheel<0?1:-1)));
        if(next!=taskPage){taskPage=next;rebuildButtons();}
    }

    private static String readableTask(String id) {
        int split = id.indexOf(':');
        String value = split < 0 ? id : id.substring(split + 1);
        String key = "task.touhou_little_maid." + value;
        String translated = net.minecraft.util.StatCollector.translateToLocal(key);
        if (!key.equals(translated)) value = translated;
        return value;
    }

    private final class ControlButton extends GuiButton {
        private final boolean schedule;
        ControlButton(int id,int x,int y,boolean schedule){super(id,x,y,schedule?61:20,schedule?13:20,"");this.schedule=schedule;}
        @Override public void drawButton(net.minecraft.client.Minecraft minecraft,int mx,int my){
            if(!visible)return;
            boolean hover=mx>=xPosition&&mx<xPosition+width&&my>=yPosition&&my<yPosition+height;
            minecraft.getTextureManager().bindTexture(BUTTONS);GL11.glColor4f(1,1,1,1);
            if(schedule){int row="DAY".equals(maid.getSchedule().name())?0:"NIGHT".equals(maid.getSchedule().name())?1:2;drawTexturedModalRect(xPosition,yPosition,82,43+14*row,width,height);}
            else {boolean active=id==3?maid.isHomeMode():maid.isPickupEnabled();drawTexturedModalRect(xPosition,yPosition,(id==3?0:42)+(active?21:0),hover?21:0,width,height);}
        }
    }
    private void drawTaskIcon(String id,int x,int y){
        net.minecraft.item.ItemStack stack=LegacyTaskIcons.get(id);if(stack==null)return;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
        itemRender.renderItemAndEffectIntoGUI(fontRendererObj,mc.getTextureManager(),stack,x,y);
        GL11.glPopAttrib();
    }

    private final class TaskTextureButton extends GuiButton {
        private final net.minecraft.util.ResourceLocation texture;
        private final int u, v;
        TaskTextureButton(int id, int x, int y, int width, int height,
                          net.minecraft.util.ResourceLocation texture, int u, int v, String text) {
            super(id, x, y, width, height, text); this.texture = texture; this.u = u; this.v = v;
        }
        @Override public void drawButton(net.minecraft.client.Minecraft minecraft, int mouseX, int mouseY) {
            if (!visible) return;
            boolean hover = mouseX >= xPosition && mouseY >= yPosition
                    && mouseX < xPosition + width && mouseY < yPosition + height;
            minecraft.getTextureManager().bindTexture(texture);
            GL11.glColor4f(1, 1, 1, 1);
            drawTexturedModalRect(xPosition, yPosition, u, v + (hover && enabled ? (v == 28 ? 20 : v == 42 ? 22 : v == 0 ? 14 : 0) : 0), width, height);
            if (!displayString.isEmpty()) {
                if(id>=200&&id<212){
                    List<IMaidTask> tasks=new ArrayList<IMaidTask>(TaskManager.getTasks().values());
                    int index=taskPage*12+id-200;
                    if(index<tasks.size())drawTaskIcon(tasks.get(index).getId(),xPosition+2,yPosition+2);
                    minecraft.fontRenderer.drawString(minecraft.fontRenderer.trimStringToWidth(displayString,57),xPosition+23,yPosition+6,0x333333);
                }else drawCenteredString(minecraft.fontRenderer,displayString,xPosition+width/2,yPosition+(height-8)/2,0x333333);
            }
        }
    }
}

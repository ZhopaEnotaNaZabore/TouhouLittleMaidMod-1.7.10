package com.github.tartaricacid.touhoulittlemaid.client.gui;

import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.LegacyMaidModelRegistry;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.ContainerModelSwitcher;
import com.github.tartaricacid.touhoulittlemaid.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.network.message.MessageModelSwitcherEdit;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityModelSwitcher;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * Java 8/Forge 1.7.10 counterpart of the 1.20 ModelSwitcherGui.
 * The left side edits the six-row configured list; the right side acts as the
 * model picker which is a separate screen in the source version.
 */
public final class GuiModelSwitcher extends GuiContainer {
    private static final int ROWS = 6;
    private final TileEntityModelSwitcher switcher;
    private int availablePage, configuredPage, selected = -1;
    private int lastFingerprint = Integer.MIN_VALUE;
    private GuiTextField nameField;

    public GuiModelSwitcher(TileEntityModelSwitcher switcher) {
        super(new ContainerModelSwitcher(switcher));
        this.switcher = switcher;
        xSize = 300;
        ySize = 220;
    }

    @Override public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        rebuild();
    }

    @Override public void onGuiClosed() {
        commitName();
        Keyboard.enableRepeatEvents(false);
        super.onGuiClosed();
    }

    private void rebuild() {
        buttonList.clear();
        nameField = null;
        lastFingerprint = fingerprint();
        List<LegacyMaidModelRegistry.Entry> all = LegacyMaidModelRegistry.INSTANCE.getEntries();
        for (int row = 0; row < ROWS; row++) {
            int i = availablePage * ROWS + row;
            if (i < all.size()) buttonList.add(new GuiButton(1000 + row, guiLeft + 158,
                    guiTop + 28 + row * 20, 132, 18, shorten(all.get(i).id, 21)));
            int c = configuredPage * ROWS + row;
            if (c < switcher.getInfoList().size()) {
                TileEntityModelSwitcher.ModeInfo mode = switcher.getInfoList().get(c);
                String prefix = c == switcher.getIndex() ? "> " : "";
                buttonList.add(new GuiButton(2000 + row, guiLeft + 10, guiTop + 28 + row * 20,
                        132, 18, prefix + shorten(mode.modelId, 14) + " @" + (int) mode.yaw));
            }
        }
        buttonList.add(new GuiButton(10, guiLeft + 10, guiTop + 192, 30, 18, "<"));
        buttonList.add(new GuiButton(11, guiLeft + 42, guiTop + 192, 30, 18, ">"));
        buttonList.add(new GuiButton(12, guiLeft + 158, guiTop + 192, 30, 18, "<"));
        buttonList.add(new GuiButton(13, guiLeft + 190, guiTop + 192, 30, 18, ">"));
        buttonList.add(new GuiButton(20, guiLeft + 74, guiTop + 192, 42, 18, "Del"));
        buttonList.add(new GuiButton(21, guiLeft + 118, guiTop + 192, 38, 18, "Use"));
        buttonList.add(new GuiButton(22, guiLeft + 222, guiTop + 192, 20, 18, "L"));
        buttonList.add(new GuiButton(23, guiLeft + 244, guiTop + 192, 20, 18, "R"));
        buttonList.add(new GuiButton(24, guiLeft + 266, guiTop + 192, 24, 18, "+"));
        if (selected >= 0 && selected < switcher.getInfoList().size()) {
            nameField = new GuiTextField(fontRendererObj, guiLeft + 10, guiTop + 162, 96, 18);
            nameField.setMaxStringLength(64);
            nameField.setText(switcher.getInfoList().get(selected).name);
            buttonList.add(new GuiButton(25, guiLeft + 108, guiTop + 162, 34, 18, "Set"));
        }
    }

    @Override public void updateScreen() {
        super.updateScreen();
        if (nameField != null) nameField.updateCursorCounter();
        if (lastFingerprint != fingerprint()) {
            if (configuredPage * ROWS >= switcher.getInfoList().size())
                configuredPage = Math.max(0, (switcher.getInfoList().size() - 1) / ROWS);
            if (selected >= switcher.getInfoList().size()) selected = -1;
            rebuild();
        }
    }

    @Override protected void actionPerformed(GuiButton b) {
        if (b.id != 25) commitName();
        List<LegacyMaidModelRegistry.Entry> all = LegacyMaidModelRegistry.INSTANCE.getEntries();
        if (b.id >= 1000 && b.id < 1000 + ROWS) {
            int i = availablePage * ROWS + b.id - 1000;
            if (i < all.size()) send(MessageModelSwitcherEdit.ADD, -1, all.get(i).id);
        } else if (b.id >= 2000 && b.id < 2000 + ROWS) {
            selected = configuredPage * ROWS + b.id - 2000;
            rebuild();
        } else if (b.id == 10 && configuredPage > 0) {
            configuredPage--; selected = -1; rebuild();
        } else if (b.id == 11 && (configuredPage + 1) * ROWS < switcher.getInfoList().size()) {
            configuredPage++; selected = -1; rebuild();
        } else if (b.id == 12 && availablePage > 0) {
            availablePage--; rebuild();
        } else if (b.id == 13 && (availablePage + 1) * ROWS < all.size()) {
            availablePage++; rebuild();
        } else if (b.id == 20) send(MessageModelSwitcherEdit.REMOVE, selected, "");
        else if (b.id == 21) send(MessageModelSwitcherEdit.APPLY, selected, "");
        else if (b.id == 22) send(MessageModelSwitcherEdit.ROTATE_LEFT, selected, "");
        else if (b.id == 23) send(MessageModelSwitcherEdit.ROTATE_RIGHT, selected, "");
        else if (b.id == 24) send(MessageModelSwitcherEdit.CAPTURE, -1, "");
        else if (b.id == 25 && nameField != null)
            send(MessageModelSwitcherEdit.RENAME, selected, nameField.getText());
    }

    private void send(int action, int index, String value) {
        if (action != MessageModelSwitcherEdit.ADD && action != MessageModelSwitcherEdit.CAPTURE && index < 0) return;
        NetworkHandler.channel.sendToServer(new MessageModelSwitcherEdit(switcher, action, index, value));
    }

    private void commitName() {
        if (nameField == null || selected < 0 || selected >= switcher.getInfoList().size()) return;
        String value = nameField.getText();
        if (!value.equals(switcher.getInfoList().get(selected).name))
            send(MessageModelSwitcherEdit.RENAME, selected, value);
    }

    @Override protected void keyTyped(char character, int keyCode) {
        if (nameField != null && nameField.textboxKeyTyped(character, keyCode)) return;
        super.keyTyped(character, keyCode);
    }

    @Override protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (nameField != null) nameField.mouseClicked(mouseX, mouseY, button);
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (nameField != null) nameField.drawTextBox();
    }

    @Override protected void drawGuiContainerBackgroundLayer(float partial, int mouseX, int mouseY) {
        GL11.glColor4f(1, 1, 1, 1);
        drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, 0xEE202020);
        drawRect(guiLeft + 5, guiTop + 22, guiLeft + 147, guiTop + 156, 0xFF606060);
        drawRect(guiLeft + 153, guiTop + 22, guiLeft + 295, guiTop + 156, 0xFF606060);
    }

    @Override protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString("Configured (" + switcher.getInfoList().size() + ")", 10, 8, 0xFFFFFF);
        fontRendererObj.drawString("Available (" + LegacyMaidModelRegistry.INSTANCE.getEntries().size() + ")", 158, 8, 0xFFFFFF);
        fontRendererObj.drawString("page " + (configuredPage + 1), 10, 148, 0xAAAAAA);
        fontRendererObj.drawString("page " + (availablePage + 1), 158, 148, 0xAAAAAA);
        fontRendererObj.drawString(switcher.getBoundMaid() == null ? "Maid: not loaded" : "Maid: bound", 105, 211, 0xAAAAAA);
    }

    private int fingerprint() {
        int hash = 31 + switcher.getIndex();
        for (TileEntityModelSwitcher.ModeInfo info : switcher.getInfoList()) {
            hash = hash * 31 + info.modelId.hashCode();
            hash = hash * 31 + info.name.hashCode();
            hash = hash * 31 + Float.floatToIntBits(info.yaw);
        }
        return hash;
    }

    private static String shorten(String value, int length) {
        return value.length() > length ? value.substring(0, length - 1) + "~" : value;
    }
}

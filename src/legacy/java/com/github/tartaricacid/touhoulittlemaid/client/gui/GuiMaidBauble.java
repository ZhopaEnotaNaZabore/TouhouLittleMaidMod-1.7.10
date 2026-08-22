package com.github.tartaricacid.touhoulittlemaid.client.gui;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.ContainerMaidBauble;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;

public final class GuiMaidBauble extends AbstractGuiMaid {
    public GuiMaidBauble(InventoryPlayer player, EntityMaid maid) {
        super(new ContainerMaidBauble(player, maid), maid, 1);
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(createBaubleButton(6, true));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 6) {
            openBaubleTab();
        } else {
            super.actionPerformed(button);
        }
    }

    @Override
    protected void drawPageBackground() {
        drawBaublePanel();
        int level = maid.getFavorabilityManager().getLevel();
        if (level < 2) drawLockedRows(81, 115);
        if (level < 3) drawLockedRows(117, 151);
    }

    private void drawLockedRows(int top, int bottom) {
        drawRect(guiLeft + 152, guiTop + top, guiLeft + 240, guiTop + bottom, 0xAA222222);
        mc.getTextureManager().bindTexture(BAUBLE);
        drawTexturedModalRect(guiLeft + 190, guiTop + (top + bottom - 11) / 2, 165, 0, 11, 11);
    }

    @Override
    protected void drawPageForeground(int mouseX, int mouseY) {
        // The source page has no textual title here; the 54x63 texture is the
        // page indicator.  A label at this position overlaps its upper slots.
    }
}

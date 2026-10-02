package com.github.tartaricacid.touhoulittlemaid.client.gui;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.ContainerMaidTask;
import net.minecraft.entity.player.InventoryPlayer;

public final class GuiMaidTask extends AbstractGuiMaid {
    public GuiMaidTask(InventoryPlayer player, EntityMaid maid) {
        super(new ContainerMaidTask(player, maid), maid, 3);
    }

    @Override
    protected void drawPageBackground() {
        drawSlot(143, 45);
        for (int n = 0; n < 9; n++) drawSlot(88 + n * 18, 82);
    }

    @Override
    protected void drawPageForeground(int mouseX, int mouseY) {
        fontRendererObj.drawString(net.minecraft.util.StatCollector.translateToLocal("gui.touhou_little_maid.task_configuration"), 88, 27, 0x404040);
        fontRendererObj.drawString(net.minecraft.util.StatCollector.translateToLocal("gui.touhou_little_maid.hidden_item"), 143, 35, 0x404040);
        fontRendererObj.drawString(net.minecraft.util.StatCollector.translateToLocal("gui.touhou_little_maid.profession_tools"), 88, 70, 0x404040);
    }
}

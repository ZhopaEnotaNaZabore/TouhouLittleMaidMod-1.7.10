package com.github.tartaricacid.touhoulittlemaid.client.gui;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.ContainerMaidEquipment;
import net.minecraft.entity.player.InventoryPlayer;

public final class GuiMaidEquipment extends AbstractGuiMaid {
    public GuiMaidEquipment(InventoryPlayer player, EntityMaid maid) {
        super(new ContainerMaidEquipment(player, maid), maid, 2);
    }

    @Override
    protected void drawPageBackground() {
        drawSlot(87, 77);
        drawSlot(121, 77);
        for (int n = 0; n < 4; n++) drawSlot(94 + (n % 2) * 20, 37 + (n / 2) * 20);
    }

    @Override
    protected void drawPageForeground(int mouseX, int mouseY) {
        fontRendererObj.drawString("Equipment", 88, 27, 0x404040);
        fontRendererObj.drawString("Main", 83, 96, 0x404040);
        fontRendererObj.drawString("Off", 119, 96, 0x404040);
    }
}

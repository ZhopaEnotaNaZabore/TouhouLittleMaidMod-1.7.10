package com.github.tartaricacid.touhoulittlemaid.client.gui;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.ContainerMaidTank;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
public final class GuiMaidTank extends GuiContainer {
    public GuiMaidTank(InventoryPlayer player,EntityMaid maid){super(new ContainerMaidTank(player,maid));xSize=176;ySize=166;}
    @Override protected void drawGuiContainerBackgroundLayer(float partial,int mouseX,int mouseY){
        drawRect(guiLeft,guiTop,guiLeft+xSize,guiTop+ySize,0xffc6c6c6);
        for(Object value:inventorySlots.inventorySlots){net.minecraft.inventory.Slot slot=(net.minecraft.inventory.Slot)value;
            int x=guiLeft+slot.xDisplayPosition,y=guiTop+slot.yDisplayPosition;drawRect(x-1,y-1,x+17,y+17,0xff373737);drawRect(x,y,x+16,y+16,0xff8b8b8b);}
        ContainerMaidTank tank=(ContainerMaidTank)inventorySlots;drawRect(guiLeft+76,guiTop+28,guiLeft+100,guiTop+57,0xff373737);
        int fill=Math.max(0,Math.min(27,tank.amount*27/10000));drawRect(guiLeft+77,guiTop+56-fill,guiLeft+99,guiTop+56,0xff3979bc);
    }
    @Override protected void drawGuiContainerForegroundLayer(int x,int y){
        ContainerMaidTank tank=(ContainerMaidTank)inventorySlots;
        fontRendererObj.drawString(StatCollector.translateToLocal("container.touhou_little_maid.tank"),8,6,0x404040);
        fontRendererObj.drawString(StatCollector.translateToLocal("gui.touhou_little_maid.tank.fill"),28,23,0x404040);
        fontRendererObj.drawString(StatCollector.translateToLocal("gui.touhou_little_maid.tank.drain"),105,23,0x404040);
        String name="";Fluid fluid=FluidRegistry.getFluid(tank.fluidId);
        if(tank.fluidId==-2)name=StatCollector.translateToLocal("item.milk.name");else if(fluid!=null)name=fluid.getLocalizedName(new FluidStack(fluid,tank.amount));
        String amount=tank.amount+" / 10000 mB";fontRendererObj.drawString(amount,(176-fontRendererObj.getStringWidth(amount))/2,60,0x404040);
        fontRendererObj.drawString(fontRendererObj.trimStringToWidth(name,160),8,72,0x404040);
    }
}

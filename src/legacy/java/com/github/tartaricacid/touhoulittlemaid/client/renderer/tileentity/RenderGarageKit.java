package com.github.tartaricacid.touhoulittlemaid.client.renderer.tileentity;

import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityGarageKit;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public final class RenderGarageKit extends TileEntitySpecialRenderer {
    @Override
    public void renderTileEntityAt(TileEntity raw, double x, double y, double z, float partialTicks) {
        TileEntityGarageKit garage = (TileEntityGarageKit) raw;
        LegacyBedrockTileModels.INSTANCE.render(garage, x, y, z,
                "statue_base", "statue_base", 0.5F);
        RenderMaidDisplayTile.render(garage, garage.getExtraData(), x, y + 0.106640625D, z,
                garage.getFacing(), 0.5F);
    }
}

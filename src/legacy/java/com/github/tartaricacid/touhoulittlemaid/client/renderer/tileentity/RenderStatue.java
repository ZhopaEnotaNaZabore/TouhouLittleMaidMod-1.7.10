package com.github.tartaricacid.touhoulittlemaid.client.renderer.tileentity;

import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityStatue;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public final class RenderStatue extends TileEntitySpecialRenderer {
    private static final float[] SCALES = {0.5F, 1.0F, 2.0F, 3.0F};

    @Override
    public void renderTileEntityAt(TileEntity raw, double x, double y, double z, float partialTicks) {
        TileEntityStatue statue = (TileEntityStatue) raw;
        if (!statue.isCoreBlock()) return;
        int index = Math.max(0, Math.min(SCALES.length - 1, statue.getStatueSize()));
        float scale = SCALES[index];
        LegacyBedrockTileModels.INSTANCE.render(statue, x, y, z,
                "statue_base", "statue_base", scale);
        RenderMaidDisplayTile.render(statue, statue.getExtraMaidData(), x, y + 0.106640625D * scale, z,
                statue.getFacing(), scale);
    }
}

package com.github.tartaricacid.touhoulittlemaid.client.renderer.tileentity;

import com.github.tartaricacid.touhoulittlemaid.tileentity.*;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

/** Selects the matching original Bedrock model for ordinary furniture tiles. */
public final class RenderFurnitureTile extends TileEntitySpecialRenderer {
    @Override public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        String name;
        String texture;
        if (tile instanceof TileEntityKeyboard) name = texture = "keyboard";
        else if (tile instanceof TileEntityBookshelf) name = texture = "bookshelf";
        else if (tile instanceof TileEntityComputer) name = texture = "computer";
        else if (tile instanceof TileEntityShrine) name = texture = "shrine";
        else if (tile instanceof TileEntityPicnicMat) name = texture = "picnic_mat";
        else if (tile instanceof TileEntitySnackCabinet) name = texture = "snack_cabinet";
        else if (tile instanceof TileEntityMaidBed) {
            String colour = bedColour(((TileEntityMaidBed) tile).getColor());
            name = "maid_bed/" + colour; texture = name;
        }
        else return;
        LegacyBedrockTileModels.INSTANCE.render(tile, x, y, z, name, texture);
    }

    private static String bedColour(int dye) {
        switch (dye) {
            case 15: return "black";
            case 13: return "green";
            case 11: return "blue";
            case 10: return "purple";
            case 6: return "pink";
            case 4: return "yellow";
            default: return "white";
        }
    }
}

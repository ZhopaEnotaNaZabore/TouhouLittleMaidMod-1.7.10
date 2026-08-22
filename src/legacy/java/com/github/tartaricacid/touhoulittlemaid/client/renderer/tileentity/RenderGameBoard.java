package com.github.tartaricacid.touhoulittlemaid.client.renderer.tileentity;

import com.github.tartaricacid.touhoulittlemaid.api.game.chess.Position;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityCChess;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityGomoku;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityWChess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.LegacyBedrockModel;
import com.github.tartaricacid.touhoulittlemaid.block.BlockBoardGame;
import org.lwjgl.opengl.GL11;

/** Lightweight world renderer for all three persistent board states. */
public final class RenderGameBoard extends TileEntitySpecialRenderer {
    private static final String[] WESTERN_PIECES = {null, null, null, null, null, null, null, null,
            "KING_W", "QUEEN_W", "ROOK_W", "BISHOP_W", "KNIGHT_W", "PAWN_W", null, null,
            "KING_B", "QUEEN_B", "ROOK_B", "BISHOP_B", "KNIGHT_B", "PAWN_B", null};
    private static final String[] CHINESE_PIECES = {null, null, null, null, null, null, null, null,
            "ShuaiRed", "ShiRed", "XiangRed", "MaRed", "JuRed", "PaoRed", "BingRed", null,
            "JiangBlack", "ShiBlack", "XiangBlack", "MaBlack", "JuBlack", "PaoBlack", "ZuBlack"};

    @Override public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        String name = tile instanceof TileEntityGomoku ? "gomoku" : tile instanceof TileEntityWChess ? "wchess" : "cchess";
        // The modern boards occupy 2x2, 3x3 and 4x4 multiblocks. The 1.7 port
        // intentionally uses one interactive block, therefore geometry and
        // hit coordinates must be reduced to the same one-block footprint.
        boolean expanded = tile.getBlockType() instanceof BlockBoardGame
                && ((BlockBoardGame) tile.getBlockType()).hasFullStructure(tile.getWorldObj(),tile.xCoord,tile.yCoord,tile.zCoord);
        float footprint = expanded ? 3.0F : 1.0F;
        float boardScale = (tile instanceof TileEntityGomoku ? 0.5F : tile instanceof TileEntityWChess ? 1.0F / 3.0F : 0.25F) * footprint;
        LegacyBedrockTileModels.INSTANCE.render(tile, x, y, z, name, name, boardScale);
        if (tile instanceof TileEntityGomoku) renderGomoku((TileEntityGomoku) tile, x, y, z,footprint);
        else if (tile instanceof TileEntityWChess) renderWestern((TileEntityWChess) tile, x, y, z,footprint);
        else if (tile instanceof TileEntityCChess) renderChinese((TileEntityCChess) tile, x, y, z,footprint);
    }
    private void renderGomoku(TileEntityGomoku board, double x, double y, double z,float footprint) {
        LegacyBedrockModel model = LegacyBedrockTileModels.INSTANCE.model("gomoku_piece");
        for(int row=0;row<15;row++)for(int col=0;col<15;col++){int stone=board.get(col,row);if(stone!=0)
            renderPiece(board, model, "main", stone==1?"gomoku_black_piece":"gomoku_white_piece",
                    x,y,z,scaled(.0394+col*.0658,footprint),scaled(.0394+row*.0658,footprint),.5F*footprint,footprint);}
    }
    private void renderWestern(TileEntityWChess board, double x, double y, double z,float footprint) {
        LegacyBedrockModel model = LegacyBedrockTileModels.INSTANCE.model("wchess_pieces");
        byte[] squares=board.getPosition().squares;
        for(int row=0;row<8;row++)for(int col=0;col<8;col++){
            int square=Position.COORD_XY(Position.FILE_LEFT+col,Position.RANK_TOP+row);
            int piece=squares[square]&255;
            if(piece<WESTERN_PIECES.length&&WESTERN_PIECES[piece]!=null){double px=scaled(.208333+col*.083333,footprint),pz=scaled(.208333+row*.083333,footprint);
                renderPiece(board,model,WESTERN_PIECES[piece],"wchess_pieces",x,y,z,px,pz,.30F*footprint,footprint);
                if(square==board.getSelected())renderPiece(board,model,"SELECT","wchess_pieces",x,y,z,px,pz,.30F*footprint,footprint);}
        }
    }
    private void renderChinese(TileEntityCChess board, double x, double y, double z,float footprint) {
        LegacyBedrockModel model = LegacyBedrockTileModels.INSTANCE.model("cchess_pieces");
        byte[] squares=board.getPosition().squares;
        for(int row=0;row<10;row++)for(int col=0;col<9;col++){
            int square=com.github.tartaricacid.touhoulittlemaid.api.game.xqwlight.Position.COORD_XY(
                    com.github.tartaricacid.touhoulittlemaid.api.game.xqwlight.Position.FILE_LEFT+col,
                    com.github.tartaricacid.touhoulittlemaid.api.game.xqwlight.Position.RANK_TOP+row);
            int piece=squares[square]&255;
            if(piece<CHINESE_PIECES.length&&CHINESE_PIECES[piece]!=null){double px=scaled(.196+col*.076,footprint),pz=scaled(.158+row*.076,footprint);
                renderPiece(board,model,CHINESE_PIECES[piece],"cchess_pieces",x,y,z,px,pz,.35F*footprint,footprint);
                if(square==board.getSelected())renderPiece(board,model,"Selected","cchess_pieces",x,y,z,px,pz,.35F*footprint,footprint);}
        }
    }

    private static double scaled(double normalized,float footprint){return .5D+(normalized-.5D)*footprint;}

    private void renderPiece(TileEntity tile, LegacyBedrockModel model, String bone, String texture,
                             double x, double y, double z, double localX, double localZ, float scale,float footprint) {
        if (model == null) return;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS); GL11.glPushMatrix();
        try {
            GL11.glTranslated(x + .5D, y + .045D*footprint + 1.5D * scale, z + .5D);
            // Match LegacyBedrockTileModels' reflected X axis before rotating.
            // Previously positions used R while the board used Sx*R, placing
            // pieces on the mirrored/hidden side of the enlarged geometry.
            GL11.glScalef(-1.0F,1.0F,1.0F);
            GL11.glRotatef((tile.getBlockMetadata() & 3) * 90.0F, 0, 1, 0);
            GL11.glTranslated(localX - .5D, 0, localZ - .5D);
            GL11.glScalef(scale, -scale, scale);
            GL11.glEnable(GL11.GL_TEXTURE_2D); GL11.glColor4f(1,1,1,1);
            Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(
                    TouhouLittleMaid.MOD_ID,"textures/bedrock/block/"+texture+".png"));
            model.renderBone(bone,null,1.0F/16.0F);
        } finally { GL11.glPopMatrix(); GL11.glPopAttrib(); }
    }
}

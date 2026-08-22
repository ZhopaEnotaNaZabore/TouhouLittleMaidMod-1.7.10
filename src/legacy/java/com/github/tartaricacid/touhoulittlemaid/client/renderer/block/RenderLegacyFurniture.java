package com.github.tartaricacid.touhoulittlemaid.client.renderer.block;

import com.github.tartaricacid.touhoulittlemaid.init.ModBlocks;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

/** Multi-cuboid replacement for modern blockstate/JSON furniture models. */
public final class RenderLegacyFurniture implements ISimpleBlockRenderingHandler {
    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        if (block == ModBlocks.SCARECROW) {
            GL11.glPushMatrix();
            GL11.glTranslatef(-0.5F, -0.65F, -0.5F);
            GL11.glScalef(0.65F, 0.65F, 0.65F);
            LegacyScarecrowModel.renderInventory(Tessellator.instance, block.getIcon(0, 0), block.getIcon(0, 1));
            GL11.glPopMatrix();
            return;
        }
        if (block == ModBlocks.MAID_BEACON) {
            GL11.glPushMatrix();
            GL11.glTranslatef(-0.5F, -0.72F, -0.5F);
            GL11.glScalef(0.55F, 0.55F, 0.55F);
            LegacyScarecrowModel.renderBeaconInventory(Tessellator.instance, block.getIcon(0, metadata));
            GL11.glPopMatrix();
            return;
        }
        GL11.glPushMatrix();
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        for (double[] box : boxes(block)) renderInventoryBox(block, metadata, renderer, box);
        GL11.glPopMatrix();
        renderer.setRenderBounds(0, 0, 0, 1, 1, 1);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block,
                                    int modelId, RenderBlocks renderer) {
        if (block == ModBlocks.SCARECROW) {
            int metadata=world.getBlockMetadata(x,y,z),half=metadata&1,facing=(metadata>>1)&3;
            LegacyScarecrowModel.renderWorld(Tessellator.instance,block.getIcon(0,metadata),half!=0,facing,
                    x,y,z,block.getMixedBrightnessForBlock(world,x,y,z));
            return true;
        }
        if (block == ModBlocks.MAID_BEACON) {
            int metadata=world.getBlockMetadata(x,y,z);
            if(metadata==0){
                LegacyScarecrowModel.renderBeaconWorld(Tessellator.instance,block.getIcon(0,metadata),false,0,
                        x,y,z,block.getMixedBrightnessForBlock(world,x,y,z));
                LegacyScarecrowModel.renderBeaconWorld(Tessellator.instance,block.getIcon(0,metadata),true,0,
                        x,y+1,z,block.getMixedBrightnessForBlock(world,x,y,z));
                return true;
            }
            boolean upper=metadata==1||metadata==2;
            int rotation=metadata==2?1:0;
            LegacyScarecrowModel.renderBeaconWorld(Tessellator.instance,block.getIcon(0,metadata),upper,rotation,
                    x,y,z,block.getMixedBrightnessForBlock(world,x,y,z));
            return true;
        }
        // These blocks are rendered from their original Bedrock geometry by a
        // TESR. The cuboid implementation remains their inventory fallback.
        if (usesBedrockTileModel(block)) return true;
        boolean rendered = false;
        for (double[] box : boxes(block)) {
            renderer.setRenderBounds(box[0], box[1], box[2], box[3], box[4], box[5]);
            rendered |= renderer.renderStandardBlock(block, x, y, z);
        }
        renderer.setRenderBounds(0, 0, 0, 1, 1, 1);
        return rendered;
    }

    @Override public boolean shouldRender3DInInventory(int modelId) { return true; }
    @Override public int getRenderId() { return LegacyBlockRenderIds.FURNITURE; }

    private static boolean usesBedrockTileModel(Block block) {
        return block == ModBlocks.ALTAR || block == ModBlocks.GOMOKU || block == ModBlocks.CCHESS
                || block == ModBlocks.WCHESS || block == ModBlocks.MAID_BED || block == ModBlocks.KEYBOARD
                || block == ModBlocks.BOOKSHELF || block == ModBlocks.COMPUTER || block == ModBlocks.SHRINE
                || block == ModBlocks.PICNIC_MAT || block == ModBlocks.SNACK_CABINET
                || block == ModBlocks.STATUE || block == ModBlocks.GARAGE_KIT;
    }

    private static double[][] boxes(Block block) {
        if (block == ModBlocks.GOMOKU || block == ModBlocks.CCHESS || block == ModBlocks.WCHESS)
            return new double[][]{{0, .62, 0, 1, .75, 1}, {.08, 0, .08, .2, .62, .2}, {.8, 0, .08, .92, .62, .2}, {.08, 0, .8, .2, .62, .92}, {.8, 0, .8, .92, .62, .92}};
        if (block == ModBlocks.MAID_BED)
            return new double[][]{{0, .12, 0, 1, .5, 1}, {0, 0, 0, 1, .14, 1}, {.04, 0, .04, .14, .22, .14}, {.86, 0, .04, .96, .22, .14}, {.04, 0, .86, .14, .22, .96}, {.86, 0, .86, .96, .22, .96}};
        if (block == ModBlocks.PICNIC_MAT) return new double[][]{{0, 0, 0, 1, .0625, 1}};
        if (block == ModBlocks.ALTAR)
            return new double[][]{{.08, 0, .08, .92, .18, .92}, {.28, .18, .28, .72, .72, .72}, {0, .72, 0, 1, .9, 1}};
        if (block == ModBlocks.STATUE)
            return new double[][]{{.18, 0, .18, .82, .16, .82}, {.32, .16, .32, .68, .72, .68}, {.25, .72, .25, .75, 1, .75}};
        if (block == ModBlocks.GARAGE_KIT)
            return new double[][]{{.28, 0, .28, .72, .12, .72}, {.38, .12, .38, .62, .88, .62}, {.25, .88, .25, .75, 1, .75}};
        if (block == ModBlocks.KEYBOARD)
            return new double[][]{{.08, 0, .18, .92, .14, .82}, {.16, .14, .3, .84, .24, .7}};
        if (block == ModBlocks.COMPUTER)
            return new double[][]{{.12, 0, .12, .88, .12, .88}, {.22, .12, .3, .78, .82, .7}, {.32, .82, .36, .68, .96, .64}};
        if (block == ModBlocks.BOOKSHELF || block == ModBlocks.SNACK_CABINET || block == ModBlocks.SHRINE)
            return new double[][]{{.08, 0, .08, .92, 1, .92}, {0, 0, 0, 1, .1, 1}, {0, .9, 0, 1, 1, 1}};
        return new double[][]{{.08, 0, .08, .92, 1, .92}};
    }

    private static void renderInventoryBox(Block block, int metadata, RenderBlocks renderer, double[] box) {
        renderer.setRenderBounds(box[0], box[1], box[2], box[3], box[4], box[5]);
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads(); t.setNormal(0, -1, 0); renderer.renderFaceYNeg(block, 0, 0, 0, block.getIcon(0, metadata)); t.draw();
        t.startDrawingQuads(); t.setNormal(0, 1, 0); renderer.renderFaceYPos(block, 0, 0, 0, block.getIcon(1, metadata)); t.draw();
        t.startDrawingQuads(); t.setNormal(0, 0, -1); renderer.renderFaceZNeg(block, 0, 0, 0, block.getIcon(2, metadata)); t.draw();
        t.startDrawingQuads(); t.setNormal(0, 0, 1); renderer.renderFaceZPos(block, 0, 0, 0, block.getIcon(3, metadata)); t.draw();
        t.startDrawingQuads(); t.setNormal(-1, 0, 0); renderer.renderFaceXNeg(block, 0, 0, 0, block.getIcon(4, metadata)); t.draw();
        t.startDrawingQuads(); t.setNormal(1, 0, 0); renderer.renderFaceXPos(block, 0, 0, 0, block.getIcon(5, metadata)); t.draw();
    }
}

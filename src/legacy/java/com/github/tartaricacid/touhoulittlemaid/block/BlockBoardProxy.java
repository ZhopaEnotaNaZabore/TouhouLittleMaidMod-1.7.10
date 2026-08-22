package com.github.tartaricacid.touhoulittlemaid.block;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;

import java.util.Random;

/** Invisible, stateless hit/collision section of a 3x3 board. */
public final class BlockBoardProxy extends Block {
    private static boolean removingStructure;

    public BlockBoardProxy() {
        super(Material.wood);
        setBlockName("touhou_little_maid.board_proxy");
        // The proxy never renders, but 1.7 still stitches a block icon for
        // every registered block. Reuse an existing board texture to keep the
        // resource atlas clean.
        setBlockTextureName(TouhouLittleMaid.MOD_ID + ":gomoku");
        setHardness(2.0F);
        setResistance(3.0F);
        setBlockBounds(0, 0, 0, 1, 0.25F, 1);
    }

    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public int getRenderType() { return -1; }
    @Override public int quantityDropped(Random random) { return 0; }
    @Override public Item getItemDropped(int metadata, Random random, int fortune) { return null; }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        int metadata = world.getBlockMetadata(x, y, z);
        int offsetX = metadata % 3 - 1;
        int offsetZ = metadata / 3 - 1;
        int centerX = x - offsetX;
        int centerZ = z - offsetZ;
        Block center = world.getBlock(centerX, y, centerZ);
        return center instanceof BlockBoardGame && ((BlockBoardGame) center).activatePart(
                world, centerX, y, centerZ, player, side, offsetX, offsetZ, hitX, hitY, hitZ);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block oldBlock, int metadata) {
        if (!removingStructure) {
            int centerX = x - (metadata % 3 - 1);
            int centerZ = z - (metadata / 3 - 1);
            if (world.getBlock(centerX, y, centerZ) instanceof BlockBoardGame) {
                world.func_147480_a(centerX, y, centerZ, true);
            }
        }
        super.breakBlock(world, x, y, z, oldBlock, metadata);
    }

    public static void setRemovingStructure(boolean removing) { removingStructure = removing; }
}

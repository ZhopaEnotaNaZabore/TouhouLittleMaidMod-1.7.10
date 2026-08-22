package com.github.tartaricacid.touhoulittlemaid.block;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.init.ModBlocks;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityGarageKit;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityStatue;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public final class BlockStatue extends BlockContainer {
    private boolean restoringStructure;
    public BlockStatue() {
        super(Material.clay);
        setBlockName(TouhouLittleMaid.MOD_ID + ".statue"); setBlockTextureName("minecraft:clay");
        setHardness(1.0F); setResistance(2.0F); setCreativeTab(CreativeTabs.tabDecorations);
    }
    @Override public TileEntity createNewTileEntity(World world, int meta) { return new TileEntityStatue(); }
    @Override public boolean isOpaqueCube(){return false;} @Override public boolean renderAsNormalBlock(){return false;} @Override public int getRenderType(){return com.github.tartaricacid.touhoulittlemaid.client.renderer.block.LegacyBlockRenderIds.FURNITURE;}
    @Override public void updateTick(World world, int x, int y, int z, java.util.Random random) {
        if (!world.isRemote && world.getBlock(x, y - 1, z) == Blocks.fire) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof TileEntityStatue && ((TileEntityStatue) tile).getStatueSize() == 0) {
                TileEntityStatue statue = (TileEntityStatue) tile;
                NBTTagCompoundHolder holder = new NBTTagCompoundHolder(statue.getFacing(), statue.getExtraMaidData());
                world.setBlock(x, y, z, ModBlocks.GARAGE_KIT, 0, 3);
                TileEntity replacement = world.getTileEntity(x, y, z);
                if (replacement instanceof TileEntityGarageKit) ((TileEntityGarageKit) replacement).setData(holder.facing, holder.data);
                return;
            }
        }
        world.scheduleBlockUpdate(x, y, z, this, 20);
    }
    @Override public void onBlockAdded(World world, int x, int y, int z) { world.scheduleBlockUpdate(x, y, z, this, 20); }
    @Override public int tickRate(World world) { return 20; }
    @Override public Item getItemDropped(int meta, Random random, int fortune) { return Item.getItemFromBlock(Blocks.clay); }

    @Override
    public void breakBlock(World world, int x, int y, int z, net.minecraft.block.Block block, int meta) {
        if (!world.isRemote && !restoringStructure) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof TileEntityStatue) {
                List<int[]> positions = ((TileEntityStatue) tile).getAllBlocks();
                restoringStructure = true;
                try {
                    for (int[] pos : positions) {
                        if ((pos[0] != x || pos[1] != y || pos[2] != z)
                                && world.getBlock(pos[0], pos[1], pos[2]) == this) {
                            world.setBlock(pos[0], pos[1], pos[2], Blocks.clay, 0, 3);
                        }
                    }
                } finally {
                    restoringStructure = false;
                }
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }
    private static final class NBTTagCompoundHolder {
        final int facing; final net.minecraft.nbt.NBTTagCompound data;
        NBTTagCompoundHolder(int facing, net.minecraft.nbt.NBTTagCompound data) { this.facing = facing; this.data = data; }
    }
}

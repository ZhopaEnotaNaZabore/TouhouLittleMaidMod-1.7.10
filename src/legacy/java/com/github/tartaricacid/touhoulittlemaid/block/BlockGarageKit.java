package com.github.tartaricacid.touhoulittlemaid.block;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityGarageKit;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.Random;

public final class BlockGarageKit extends BlockContainer {
    private boolean dropping;
    public BlockGarageKit() {
        super(Material.clay);
        setBlockName(TouhouLittleMaid.MOD_ID + ".garage_kit");
        setBlockTextureName("minecraft:clay"); setHardness(1.0F); setResistance(2.0F);
        setCreativeTab(CreativeTabs.tabDecorations);
        setBlockBounds(0.25F, 0, 0.25F, 0.75F, 1, 0.75F);
    }
    @Override public TileEntity createNewTileEntity(World world, int meta) { return new TileEntityGarageKit(); }
    @Override public boolean isOpaqueCube(){return false;} @Override public boolean renderAsNormalBlock(){return false;} @Override public int getRenderType(){return com.github.tartaricacid.touhoulittlemaid.client.renderer.block.LegacyBlockRenderIds.FURNITURE;}
    @Override public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        // The source block faces the placer (opposite to the placer's look direction).
        int facing = (MathHelper.floor_double(placer.rotationYaw * 4 / 360 + 0.5D) + 2) & 3;
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityGarageKit) {
            NBTTagCompound data = stack.hasTagCompound() && stack.getTagCompound().hasKey("EntityInfo", 10)
                    ? stack.getTagCompound().getCompoundTag("EntityInfo") : new NBTTagCompound();
            ((TileEntityGarageKit) tile).setData(facing, data);
        }
    }
    @Override public void breakBlock(World world, int x, int y, int z, net.minecraft.block.Block block, int meta) {
        if (!world.isRemote && !dropping) {
            TileEntity tile = world.getTileEntity(x, y, z);
            ItemStack stack = new ItemStack(Item.getItemFromBlock(this));
            if (tile instanceof TileEntityGarageKit) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setTag("EntityInfo", ((TileEntityGarageKit) tile).getExtraData()); stack.setTagCompound(tag);
            }
            world.spawnEntityInWorld(new EntityItem(world, x + .5D, y + .5D, z + .5D, stack));
        }
        super.breakBlock(world, x, y, z, block, meta);
    }
    @Override public Item getItemDropped(int meta, Random random, int fortune) { return null; }
}

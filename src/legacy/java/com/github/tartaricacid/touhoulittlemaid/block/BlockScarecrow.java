package com.github.tartaricacid.touhoulittlemaid.block;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityScarecrow;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.Random;
import java.util.List;

/** Two-block scarecrow used by fairy spawn exclusion. Metadata 0=lower, 1=upper. */
public final class BlockScarecrow extends Block {
    private IIcon lower;
    private IIcon upper;
    private final ThreadLocal<Boolean> removingPair=new ThreadLocal<Boolean>(){
        @Override protected Boolean initialValue(){return false;}
    };

    public BlockScarecrow() {
        super(Material.wood);
        setBlockName(TouhouLittleMaid.MOD_ID + ".scarecrow");
        setHardness(0.2F);
        setStepSound(soundTypeGrass);
        setCreativeTab(CreativeTabs.tabDecorations);
    }

    @Override
    public void registerBlockIcons(IIconRegister register) {
        lower = register.registerIcon(TouhouLittleMaid.MOD_ID + ":scarecrow_lower");
        upper = register.registerIcon(TouhouLittleMaid.MOD_ID + ":scarecrow_upper");
    }

    @Override
    public IIcon getIcon(int side, int meta) { return (meta & 1) != 0 ? lower : upper; }
    @Override public boolean isOpaqueCube(){return false;} @Override public boolean renderAsNormalBlock(){return false;} @Override public int getRenderType(){return com.github.tartaricacid.touhoulittlemaid.client.renderer.block.LegacyBlockRenderIds.FURNITURE;}

    @Override public boolean hasTileEntity(int metadata) { return (metadata & 1) == 0; }
    @Override public TileEntity createTileEntity(World world, int metadata) {
        return (metadata & 1) == 0 ? new TileEntityScarecrow() : null;
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return y < world.getHeight() - 1 && world.isAirBlock(x, y, z) && world.isAirBlock(x, y + 1, z)
                && World.doesBlockHaveSolidTopSurface(world, x, y - 1, z);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        if (!world.isRemote) {
            int facing=MathHelper.floor_double(placer.rotationYaw*4.0F/360.0F+0.5D)&3;
            world.setBlockMetadataWithNotify(x,y,z,facing<<1,3);
            world.setBlock(x,y+1,z,this,(facing<<1)|1,3);
        }
    }

    @Override
    public void setBlockBoundsBasedOnState(net.minecraft.world.IBlockAccess world,int x,int y,int z){
        int meta=world.getBlockMetadata(x,y,z),facing=(meta>>1)&3;
        if((meta&1)==0)setBlockBounds(1/16F,0,1/16F,15/16F,1,15/16F);
        else if(facing==0)setBlockBounds(4/16F,0,2.5F/16F,12/16F,7.5F/16F,8.5F/16F);
        else if(facing==1)setBlockBounds(2.5F/16F,0,4/16F,8.5F/16F,7.5F/16F,12/16F);
        else if(facing==2)setBlockBounds(4/16F,0,7.5F/16F,12/16F,7.5F/16F,13.5F/16F);
        else setBlockBounds(7.5F/16F,0,4/16F,13.5F/16F,7.5F/16F,12/16F);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB mask,
                                        List boxes, Entity entity) {
        int meta=world.getBlockMetadata(x,y,z);
        if((meta&1)==0){
            addCollisionBox(x,y,z,mask,boxes,1/16D,0,1/16D,15/16D,4/16D,15/16D);
            addCollisionBox(x,y,z,mask,boxes,2/16D,4/16D,2/16D,14/16D,8/16D,14/16D);
            addCollisionBox(x,y,z,mask,boxes,4.5D/16D,8/16D,4.5D/16D,11.5D/16D,1,11.5D/16D);
            return;
        }
        setBlockBoundsBasedOnState(world,x,y,z);
        super.addCollisionBoxesToList(world,x,y,z,mask,boxes,entity);
    }

    private static void addCollisionBox(int x,int y,int z,AxisAlignedBB mask,List boxes,
                                        double minX,double minY,double minZ,double maxX,double maxY,double maxZ){
        AxisAlignedBB box=AxisAlignedBB.getBoundingBox(x+minX,y+minY,z+minZ,x+maxX,y+maxY,z+maxZ);
        if(mask.intersectsWith(box))boxes.add(box);
    }

    @Override
    public void onBlockHarvested(World world,int x,int y,int z,int meta,EntityPlayer player){
        if((meta&1)!=0&&player.capabilities.isCreativeMode&&world.getBlock(x,y-1,z)==this)
            world.setBlockToAir(x,y-1,z);
        super.onBlockHarvested(world,x,y,z,meta,player);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        if(!removingPair.get()){removingPair.set(true);try{
            if ((meta&1) == 0 && world.getBlock(x, y + 1, z) == this) world.setBlockToAir(x, y + 1, z);
            if ((meta&1) != 0 && world.getBlock(x, y - 1, z) == this) {
                int lowerMeta=world.getBlockMetadata(x,y-1,z);
                world.setBlockToAir(x,y-1,z);
                if(!world.isRemote)dropBlockAsItem(world,x,y-1,z,lowerMeta,0);
            }
        }finally{removingPair.set(false);}}
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public Item getItemDropped(int meta, Random random, int fortune) { return (meta&1)!=0 ? null : Item.getItemFromBlock(this); }
    @Override
    public int quantityDropped(int meta, int fortune, Random random) { return (meta&1)!=0 ? 0 : 1; }
    @Override public int damageDropped(int meta){return 0;}
}

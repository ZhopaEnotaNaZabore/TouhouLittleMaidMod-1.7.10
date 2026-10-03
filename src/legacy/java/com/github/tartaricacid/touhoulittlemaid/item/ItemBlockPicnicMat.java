package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.block.BlockPicnicMat;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityPicnicMat;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;

public final class ItemBlockPicnicMat extends ItemBlock {
    public ItemBlockPicnicMat(Block block){super(block);setMaxStackSize(1);}
    @Override public boolean placeBlockAt(ItemStack stack,EntityPlayer player,World world,int x,int y,int z,int side,float hx,float hy,float hz,int meta){
        Block[] old=new Block[25];int[] metadata=new int[25];int n=0;
        for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++){
            int px=x+dx,pz=z+dz;
            if(!world.blockExists(px,y,pz)||!world.getBlock(px,y,pz).isReplaceable(world,px,y,pz)||world.getTileEntity(px,y,pz)!=null
                ||!world.isSideSolid(px,y-1,pz,ForgeDirection.UP)||!player.canPlayerEdit(px,y,pz,side,stack)||!world.canMineBlock(player,px,y,pz))return false;
            old[n]=world.getBlock(px,y,pz);metadata[n++]=world.getBlockMetadata(px,y,pz);
        }
        if(world.isRemote)return true;
        BlockPicnicMat block=(BlockPicnicMat)field_150939_a;
        int facing=(MathHelper.floor_double(player.rotationYaw*4F/360F+.5D)+2)&3,placed=0;
        block.changingStructure=true;
        try{
            for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++){
                int px=x+dx,pz=z+dz;
                if(!world.setBlock(px,y,pz,block,facing,2))throw new IllegalStateException("Picnic placement refused");
                placed++;
                TileEntityPicnicMat tile=(TileEntityPicnicMat)world.getTileEntity(px,y,pz);
                if(tile==null)throw new IllegalStateException("Picnic tile missing");
                tile.setCenterPos(x,y,z);
            }
            TileEntityPicnicMat center=(TileEntityPicnicMat)world.getTileEntity(x,y,z);
            if(stack.hasTagCompound()&&stack.getTagCompound().hasKey("PicnicBasketContainer",10)){
                NBTTagCompound tag=new NBTTagCompound();center.writeToNBT(tag);
                tag.setTag("Items",stack.getTagCompound().getCompoundTag("PicnicBasketContainer").getTagList("Items",10).copy());center.readFromNBT(tag);center.markDirty();
            }
        }catch(RuntimeException failure){
            com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid.LOGGER.warn("Picnic placement rolled back",failure);
            n=0;for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++){if(n<placed)world.setBlock(x+dx,y,z+dz,old[n],metadata[n],3);n++;}return false;
        }finally{block.changingStructure=false;}
        if(!world.captureBlockSnapshots)for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)world.notifyBlocksOfNeighborChange(x+dx,y,z+dz,block);
        return true;
    }
}

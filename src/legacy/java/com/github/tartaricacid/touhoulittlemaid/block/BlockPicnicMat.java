package com.github.tartaricacid.touhoulittlemaid.block;

import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityPicnicMat;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/** One central inventory and 24 linked, non-rendering mat parts. */
public final class BlockPicnicMat extends BlockInventoryDevice {
    public boolean changingStructure;
    public BlockPicnicMat(){super("picnic_mat",Type.PICNIC_MAT);}
    @Override public Item getItemDropped(int meta,java.util.Random random,int fortune){return null;}
    @Override public boolean onBlockActivated(World world,int x,int y,int z,EntityPlayer player,int side,float hx,float hy,float hz){
        TileEntity raw=world.getTileEntity(x,y,z);if(!(raw instanceof TileEntityPicnicMat))return false;
        TileEntityPicnicMat part=(TileEntityPicnicMat)raw;
        if(!world.blockExists(part.getCenterX(),part.getCenterY(),part.getCenterZ()))return false;
        raw=world.getTileEntity(part.getCenterX(),part.getCenterY(),part.getCenterZ());
        if(!(raw instanceof TileEntityPicnicMat)||!((TileEntityPicnicMat)raw).isCenter())return false;
        TileEntityPicnicMat center=(TileEntityPicnicMat)raw;
        ItemStack held=player.getCurrentEquippedItem();
        if(held!=null&&held.getItem() instanceof ItemFood){
            if(world.isRemote)return true;
            int moved=center.insertFood(held);
            if(moved>0){held.stackSize-=moved;if(held.stackSize==0)player.inventory.setInventorySlotContents(player.inventory.currentItem,null);}
            return moved>0;
        }
        if(held==null&&player.isSneaking()){
            if(world.isRemote)return true;
            for(int slot=8;slot>=0;slot--){ItemStack out=center.getStackInSlotOnClosing(slot);if(out!=null){if(!player.inventory.addItemStackToInventory(out))player.entityDropItem(out,0);return true;}}
        }
        return false;
    }
    @Override public void breakBlock(World world,int x,int y,int z,Block block,int meta){
        if(world.isRemote||changingStructure||world.restoringBlockSnapshots){world.removeTileEntity(x,y,z);return;}
        TileEntity raw=world.getTileEntity(x,y,z);
        if(!(raw instanceof TileEntityPicnicMat)){world.removeTileEntity(x,y,z);return;}
        TileEntityPicnicMat part=(TileEntityPicnicMat)raw;
        int cx=part.getCenterX(),cy=part.getCenterY(),cz=part.getCenterZ();
        raw=world.blockExists(cx,cy,cz)?world.getTileEntity(cx,cy,cz):null;
        if(raw instanceof TileEntityPicnicMat&&((TileEntityPicnicMat)raw).isCenter()){
            TileEntityPicnicMat center=(TileEntityPicnicMat)raw;
            ItemStack drop=new ItemStack(this);NBTTagCompound data=new NBTTagCompound();center.writeToNBT(data);
            NBTTagCompound root=new NBTTagCompound(),storage=new NBTTagCompound();storage.setTag("Items",data.getTagList("Items",10).copy());storage.setInteger("Size",9);
            root.setTag("PicnicBasketContainer",storage);drop.setTagCompound(root);
            world.spawnEntityInWorld(new EntityItem(world,cx+.5,cy+.2,cz+.5,drop));center.removeSeats();
        }
        changingStructure=true;
        try{for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++){
            int px=cx+dx,pz=cz+dz;if(px==x&&cy==y&&pz==z)continue;
            if(!world.blockExists(px,cy,pz)||world.getBlock(px,cy,pz)!=this)continue;
            TileEntity other=world.getTileEntity(px,cy,pz);
            if(other instanceof TileEntityPicnicMat){TileEntityPicnicMat t=(TileEntityPicnicMat)other;
                if(t.getCenterX()==cx&&t.getCenterY()==cy&&t.getCenterZ()==cz)world.setBlockToAir(px,cy,pz);
            }
        }}finally{changingStructure=false;}
        world.removeTileEntity(x,y,z);
    }
}

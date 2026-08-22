package com.github.tartaricacid.touhoulittlemaid.block;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.item.ItemBlockMaidBeacon;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityMaidBeacon;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Source-compatible two-block Shrine Lamp. Metadata: 1=upper N/S, 2=upper W/E, 3=lower; 0 is legacy single-block. */
public final class BlockMaidBeacon extends Block {
    private final ThreadLocal<Boolean> removingPair = new ThreadLocal<Boolean>() {
        @Override protected Boolean initialValue() { return false; }
    };
    private final Set<String> creativeBreaks = Collections.synchronizedSet(new HashSet<String>());

    public BlockMaidBeacon() {
        super(Material.wood);
        setBlockName(TouhouLittleMaid.MOD_ID + ".maid_beacon");
        setBlockTextureName(TouhouLittleMaid.MOD_ID + ":maid_beacon");
        setHardness(2); setResistance(2); setCreativeTab(CreativeTabs.tabDecorations);
    }

    @Override public boolean hasTileEntity(int meta) { return meta == 0 || meta == 1 || meta == 2; }
    @Override public TileEntity createTileEntity(World world, int meta) {
        return hasTileEntity(meta) ? new TileEntityMaidBeacon() : null;
    }
    @Override public boolean isOpaqueCube(){return false;}
    @Override public boolean renderAsNormalBlock(){return false;}
    @Override public int getRenderType(){return com.github.tartaricacid.touhoulittlemaid.client.renderer.block.LegacyBlockRenderIds.FURNITURE;}
    @Override public int getLightValue(net.minecraft.world.IBlockAccess world,int x,int y,int z){int meta=world.getBlockMetadata(x,y,z);return meta==1||meta==2?15:0;}

    @Override
    public boolean canPlaceBlockAt(World world,int x,int y,int z){
        return y<world.getHeight()-1&&world.isAirBlock(x,y,z)&&world.isAirBlock(x,y+1,z)
                && World.doesBlockHaveSolidTopSurface(world,x,y-1,z);
    }

    @Override
    public void onBlockPlacedBy(World world,int x,int y,int z,EntityLivingBase placer,ItemStack stack){
        if(world.isRemote)return;
        int facing=MathHelper.floor_double(placer.rotationYaw*4.0F/360.0F+0.5D)&3;
        int upperMeta=(facing==0||facing==2)?1:2;
        world.setBlockMetadataWithNotify(x,y,z,3,3);
        world.removeTileEntity(x,y,z);
        world.setBlock(x,y+1,z,this,upperMeta,3);
        TileEntity tile=world.getTileEntity(x,y+1,z);
        if(tile instanceof TileEntityMaidBeacon)loadItem((TileEntityMaidBeacon)tile,stack,world,x,y+1,z);
    }

    @Override
    public boolean onBlockActivated(World world,int x,int y,int z,EntityPlayer player,int side,float hitX,float hitY,float hitZ){
        int meta=world.getBlockMetadata(x,y,z);
        if(meta==3&&world.getBlock(x,y+1,z)==this){y++;meta=world.getBlockMetadata(x,y,z);}
        TileEntity tile=world.getTileEntity(x,y,z);
        if(!(tile instanceof TileEntityMaidBeacon))return false;
        TileEntityMaidBeacon beacon=(TileEntityMaidBeacon)tile;
        if(!world.isRemote){
            com.github.tartaricacid.touhoulittlemaid.network.NetworkHandler.channel.sendTo(
                    new com.github.tartaricacid.touhoulittlemaid.network.message.MessagePlayerPower(
                            com.github.tartaricacid.touhoulittlemaid.capability.LegacyPlayerPower.get(player).get()),
                    (net.minecraft.entity.player.EntityPlayerMP)player);
            player.openGui(TouhouLittleMaid.instance,com.github.tartaricacid.touhoulittlemaid.proxy.CommonProxy.MAID_BEACON_GUI_ID,world,x,y,z);
        }
        return true;
    }

    @Override
    public void onNeighborBlockChange(World world,int x,int y,int z,Block neighbor){
        if(world.isRemote||removingPair.get())return;
        int meta=world.getBlockMetadata(x,y,z);
        boolean invalid=meta==3?world.getBlock(x,y+1,z)!=this:(meta==1||meta==2)&&world.getBlock(x,y-1,z)!=this;
        if(invalid)world.setBlockToAir(x,y,z);
    }

    @Override
    public void setBlockBoundsBasedOnState(net.minecraft.world.IBlockAccess world,int x,int y,int z){
        int meta=world.getBlockMetadata(x,y,z);
        if(meta==1||meta==2)setBlockBounds(3/16F,1/16F,3/16F,13/16F,1,13/16F);
        else setBlockBounds(6.5F/16F,0,6.5F/16F,9.5F/16F,26F/16F,9.5F/16F);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addCollisionBoxesToList(World world,int x,int y,int z,AxisAlignedBB mask,List boxes,Entity entity){
        int meta=world.getBlockMetadata(x,y,z);
        if(meta==3||meta==0){
            AxisAlignedBB box=AxisAlignedBB.getBoundingBox(x+6.5D/16D,y,z+6.5D/16D,x+9.5D/16D,y+26D/16D,z+9.5D/16D);
            if(mask.intersectsWith(box))boxes.add(box);
        }else super.addCollisionBoxesToList(world,x,y,z,mask,boxes,entity);
    }

    @Override
    public void onBlockHarvested(World world,int x,int y,int z,int meta,EntityPlayer player){
        if(player.capabilities.isCreativeMode){
            creativeBreaks.add(key(world,x,y,z));
            if(meta==3)creativeBreaks.add(key(world,x,y+1,z));
            else if(meta==1||meta==2)creativeBreaks.add(key(world,x,y-1,z));
        }
        super.onBlockHarvested(world,x,y,z,meta,player);
    }

    @Override
    public void breakBlock(World world,int x,int y,int z,Block block,int meta){
        boolean noDrop=creativeBreaks.remove(key(world,x,y,z));
        if(!removingPair.get()){
            removingPair.set(true);
            try{
                TileEntityMaidBeacon beacon=findBeacon(world,x,y,z,meta);
                if(meta==3&&world.getBlock(x,y+1,z)==this)world.setBlockToAir(x,y+1,z);
                else if((meta==1||meta==2)&&world.getBlock(x,y-1,z)==this)world.setBlockToAir(x,y-1,z);
                if(!world.isRemote&&!noDrop)world.spawnEntityInWorld(new EntityItem(world,x+.5,y+.5,z+.5,toItem(beacon)));
            }finally{removingPair.set(false);}
        }
        super.breakBlock(world,x,y,z,block,meta);
    }

    private TileEntityMaidBeacon findBeacon(World world,int x,int y,int z,int meta){
        TileEntity tile=world.getTileEntity(x,y,z);
        if(tile instanceof TileEntityMaidBeacon)return (TileEntityMaidBeacon)tile;
        if(meta==3){tile=world.getTileEntity(x,y+1,z);if(tile instanceof TileEntityMaidBeacon)return (TileEntityMaidBeacon)tile;}
        return null;
    }

    private ItemStack toItem(TileEntityMaidBeacon beacon){
        ItemStack stack=new ItemStack(Item.getItemFromBlock(this));
        if(beacon!=null){NBTTagCompound storage=new NBTTagCompound();beacon.writeToNBT(storage);storage.removeTag("x");storage.removeTag("y");storage.removeTag("z");NBTTagCompound forgeData=new NBTTagCompound();forgeData.setInteger("PotionIndex",beacon.getPotionIndex());forgeData.setFloat("StoragePower",beacon.getStoragePower());forgeData.setBoolean("OverflowDelete",beacon.isOverflowDelete());storage.setTag("ForgeData",forgeData);NBTTagCompound root=new NBTTagCompound();root.setTag(ItemBlockMaidBeacon.STORAGE,storage);stack.setTagCompound(root);}
        return stack;
    }

    private void loadItem(TileEntityMaidBeacon beacon,ItemStack stack,World world,int x,int y,int z){
        if(stack.hasTagCompound()&&stack.getTagCompound().hasKey(ItemBlockMaidBeacon.STORAGE,10)){
            NBTTagCompound data=stack.getTagCompound().getCompoundTag(ItemBlockMaidBeacon.STORAGE);
            if(data.hasKey("ForgeData",10))data=data.getCompoundTag("ForgeData");
            beacon.readFromNBT(data);
        }
        beacon.xCoord=x;beacon.yCoord=y;beacon.zCoord=z;beacon.setWorldObj(world);beacon.markDirty();
    }

    private static String key(World world,int x,int y,int z){return world.provider.dimensionId+":"+x+":"+y+":"+z;}
    @Override public Item getItemDropped(int meta,java.util.Random random,int fortune){return null;}
}

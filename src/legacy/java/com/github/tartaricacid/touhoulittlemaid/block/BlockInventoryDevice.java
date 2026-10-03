package com.github.tartaricacid.touhoulittlemaid.block;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.item.ItemFilm;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityInventory;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityPicnicMat;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityShrine;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntitySnackCabinet;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.util.MathHelper;

public class BlockInventoryDevice extends BlockContainer {
    public enum Type { SHRINE, PICNIC_MAT, SNACK_CABINET }
    private final Type type;

    public BlockInventoryDevice(String name, Type type) {
        super(type == Type.PICNIC_MAT ? Material.cloth : Material.wood);
        this.type = type;
        setBlockName(TouhouLittleMaid.MOD_ID + "." + name);
        setBlockTextureName("minecraft:stone");
        setHardness(2.0F);
        setCreativeTab(CreativeTabs.tabDecorations);
        if(type==Type.PICNIC_MAT)setBlockBounds(0,0,0,1,0.0625F,1);
    }

    @Override public TileEntity createNewTileEntity(World world, int meta) {
        if (type == Type.SHRINE) return new TileEntityShrine();
        if (type == Type.PICNIC_MAT) return new TileEntityPicnicMat();
        return new TileEntitySnackCabinet();
    }
    @Override public boolean isOpaqueCube(){return false;} @Override public boolean renderAsNormalBlock(){return false;} @Override public int getRenderType(){return com.github.tartaricacid.touhoulittlemaid.client.renderer.block.LegacyBlockRenderIds.FURNITURE;}

    @Override public void onBlockPlacedBy(World world,int x,int y,int z,EntityLivingBase placer,ItemStack stack){int facing=(MathHelper.floor_double(placer.rotationYaw*4.0F/360.0F+0.5D)+2)&3;world.setBlockMetadataWithNotify(x,y,z,facing,3);}

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileEntityInventory)) return false;
        TileEntityInventory inventory = (TileEntityInventory) tile;
        if (type == Type.SHRINE) return useShrine(world, x, y, z, player, inventory);
        if (type == Type.PICNIC_MAT) {
            ItemStack held=player.getCurrentEquippedItem();
            if(held!=null&&held.getItem() instanceof net.minecraft.item.ItemFood){if(!world.isRemote){for(int slot=0;slot<inventory.getSizeInventory();slot++){ItemStack in=inventory.getStackInSlot(slot);if(in==null){ItemStack copy=held.copy();copy.stackSize=held.stackSize;inventory.setInventorySlotContents(slot,copy);if(!player.capabilities.isCreativeMode)player.inventory.setInventorySlotContents(player.inventory.currentItem,null);break;}if(in.isItemEqual(held)&&ItemStack.areItemStackTagsEqual(in,held)&&in.stackSize<in.getMaxStackSize()){int moved=Math.min(held.stackSize,in.getMaxStackSize()-in.stackSize);in.stackSize+=moved;if(!player.capabilities.isCreativeMode){held.stackSize-=moved;if(held.stackSize<=0)player.inventory.setInventorySlotContents(player.inventory.currentItem,null);}inventory.markDirty();break;}}}return true;}
            if(held==null&&player.isSneaking()){if(!world.isRemote){for(int slot=inventory.getSizeInventory()-1;slot>=0;slot--){ItemStack take=inventory.getStackInSlotOnClosing(slot);if(take!=null){if(!player.inventory.addItemStackToInventory(take))player.entityDropItem(take,0);break;}}}return true;}
        }
        if (!world.isRemote) player.displayGUIChest(inventory);
        return true;
    }

    private boolean useShrine(World world, int x, int y, int z, EntityPlayer player, TileEntityInventory shrine) {
        ItemStack stored = shrine.getStackInSlot(0);
        ItemStack held = player.getCurrentEquippedItem();
        if (player.isSneaking() && stored != null) {
            if (!world.isRemote) {
                shrine.setInventorySlotContents(0, null);
                if (!player.inventory.addItemStackToInventory(stored)) player.entityDropItem(stored, 0.0F);
            }
            return true;
        }
        if (stored == null && held != null && shrine.isItemValidForSlot(0, held)) {
            if (!world.isRemote) {
                ItemStack one = held.copy(); one.stackSize = 1;
                shrine.setInventorySlotContents(0, one);
                if (!player.capabilities.isCreativeMode && --held.stackSize <= 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            }
            return true;
        }
        if (stored != null && held == null) {
            if (!player.capabilities.isCreativeMode && player.getHealth() < player.getMaxHealth() / 2.0F + 1.0F) return true;
            if (!world.isRemote) {
                if (ItemFilm.filmToMaid(stored, world, x, y + 1, z, player)) {
                    if (!player.capabilities.isCreativeMode) player.setHealth(0.25F);
                    if (stored.stackSize <= 0) shrine.setInventorySlotContents(0, null);
                    else shrine.markDirty();
                }
            }
            return true;
        }
        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, net.minecraft.block.Block block, int meta) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!world.isRemote && tile instanceof TileEntityInventory) {
            if(tile instanceof TileEntityPicnicMat)((TileEntityPicnicMat)tile).removeSeats();
            TileEntityInventory inventory = (TileEntityInventory) tile;
            for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack != null) world.spawnEntityInWorld(new EntityItem(world, x + 0.5D, y + 0.5D, z + 0.5D, stack));
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override public void onBlockAdded(World world,int x,int y,int z){super.onBlockAdded(world,x,y,z);if(type==Type.PICNIC_MAT){TileEntity tile=world.getTileEntity(x,y,z);if(tile instanceof TileEntityPicnicMat)((TileEntityPicnicMat)tile).setCenterPos(x,y,z);}}

    @Override public boolean hasComparatorInputOverride() { return type == Type.SNACK_CABINET; }
    @Override public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
        TileEntity tile = world.getTileEntity(x, y, z);
        return tile instanceof TileEntityInventory ? net.minecraft.inventory.Container.calcRedstoneFromInventory((TileEntityInventory) tile) : 0;
    }
}

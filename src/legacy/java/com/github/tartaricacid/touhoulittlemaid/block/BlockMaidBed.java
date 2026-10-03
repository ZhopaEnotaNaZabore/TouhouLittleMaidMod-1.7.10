package com.github.tartaricacid.touhoulittlemaid.block;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.init.ModBlocks;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityMaidBed;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.Random;

/** Two-block maid bed: low two metadata bits are facing, bit 3 marks the head. */
public final class BlockMaidBed extends BlockContainer {
    private boolean removingOtherHalf;
    public BlockMaidBed() {
        super(Material.cloth);
        setBlockName(TouhouLittleMaid.MOD_ID + ".maid_bed");
        setBlockTextureName("minecraft:wool_colored_pink");
        setHardness(0.2F);
        setStepSound(soundTypeCloth);
        setCreativeTab(CreativeTabs.tabDecorations);
        setBlockBounds(0, 0, 0, 1, 0.5625F, 1);
    }

    @Override public TileEntity createNewTileEntity(World world, int meta) {
        return (meta & 8) != 0 ? new TileEntityMaidBed() : null;
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return super.canPlaceBlockAt(world, x, y, z) && World.doesBlockHaveSolidTopSurface(world, x, y - 1, z);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int facing = MathHelper.floor_double(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        int dx = facing == 1 ? -1 : facing == 3 ? 1 : 0;
        int dz = facing == 0 ? 1 : facing == 2 ? -1 : 0;
        if (!world.isAirBlock(x + dx, y, z + dz)
                || !World.doesBlockHaveSolidTopSurface(world, x + dx, y - 1, z + dz)) {
            world.setBlockToAir(x, y, z);
            if (placer instanceof EntityPlayer && !((EntityPlayer) placer).capabilities.isCreativeMode) {
                ((EntityPlayer) placer).inventory.addItemStackToInventory(stack.copy());
            }
            return;
        }
        world.setBlockMetadataWithNotify(x, y, z, facing, 3);
        world.setBlock(x + dx, y, z + dz, this, 8 | facing, 3);
        TileEntity tile = world.getTileEntity(x + dx, y, z + dz);
        if (tile instanceof TileEntityMaidBed && stack.hasTagCompound() && stack.getTagCompound().hasKey("BedColor")) {
            int saved=stack.getTagCompound().getInteger("BedColor");
            ((TileEntityMaidBed) tile).setColor(stack.getTagCompound().getBoolean("ModernBedColor")?saved:15-saved);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        ItemStack held = player.getCurrentEquippedItem();
        if (held == null || held.getItem() != Items.dye) return false;
        int meta = world.getBlockMetadata(x, y, z);
        int facing = meta & 3;
        if ((meta & 8) == 0) {
            x += facing == 1 ? -1 : facing == 3 ? 1 : 0;
            z += facing == 0 ? 1 : facing == 2 ? -1 : 0;
        }
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityMaidBed) {
            int modernColor=15-(held.getItemDamage()&15);
            if(!isAvailableColor(modernColor))return false;
            if (world.isRemote) return true;
            ((TileEntityMaidBed) tile).setColor(modernColor);
            if (!player.capabilities.isCreativeMode && --held.stackSize <= 0) {
                player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            }
            return true;
        }
        return false;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, net.minecraft.block.Block block, int meta) {
        if (removingOtherHalf) {
            super.breakBlock(world, x, y, z, block, meta);
            return;
        }
        int facing = meta & 3;
        boolean head = (meta & 8) != 0;
        int dx = facing == 1 ? -1 : facing == 3 ? 1 : 0;
        int dz = facing == 0 ? 1 : facing == 2 ? -1 : 0;
        int otherX = head ? x - dx : x + dx;
        int otherZ = head ? z - dz : z + dz;
        int color = 6;
        TileEntity tile = head ? world.getTileEntity(x, y, z) : world.getTileEntity(otherX, y, otherZ);
        if (tile instanceof TileEntityMaidBed) color = ((TileEntityMaidBed) tile).getColor();
        if (!world.isRemote) {
            ItemStack drop = new ItemStack(Item.getItemFromBlock(this));
            NBTTagCompound tag = new NBTTagCompound(); tag.setInteger("BedColor", color);tag.setBoolean("ModernBedColor",true); drop.setTagCompound(tag);
            world.spawnEntityInWorld(new EntityItem(world, x + 0.5D, y + 0.5D, z + 0.5D, drop));
        }
        if (!world.isRemote && world.getBlock(otherX, y, otherZ) == this
                && world.getBlockMetadata(otherX, y, otherZ) == (meta ^ 8)) {
            removingOtherHalf = true;
            try { world.setBlockToAir(otherX, y, otherZ); }
            finally { removingOtherHalf = false; }
        }
        if(!world.isRemote)removeBedSeats(world,head?x:otherX,y,head?z:otherZ);
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override public Item getItemDropped(int meta, Random random, int fortune) { return null; }
    @Override public boolean isOpaqueCube(){return false;} @Override public boolean renderAsNormalBlock(){return false;} @Override public int getRenderType(){return com.github.tartaricacid.touhoulittlemaid.client.renderer.block.LegacyBlockRenderIds.FURNITURE;}
    @Override public boolean isBed(net.minecraft.world.IBlockAccess world, int x, int y, int z, EntityLivingBase player) {
        return player instanceof com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
    }

    private static boolean isAvailableColor(int id){return id==0||id==15||id==4||id==11||id==13||id==10||id==6;}
    @SuppressWarnings("unchecked") private static void removeBedSeats(World world,int x,int y,int z){java.util.List<com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit> seats=world.getEntitiesWithinAABB(com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit.class,net.minecraft.util.AxisAlignedBB.getBoundingBox(x-1,y-1,z-1,x+2,y+2,z+2));for(com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit sit:seats)if("bed".equals(sit.getJoyType())&&sit.isAssociatedWith(x,y,z))sit.setDead();}
}

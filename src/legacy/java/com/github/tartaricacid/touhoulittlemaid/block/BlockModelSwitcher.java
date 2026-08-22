package com.github.tartaricacid.touhoulittlemaid.block;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityModelSwitcher;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.IIcon;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;

import java.util.List;

public final class BlockModelSwitcher extends BlockContainer {
    private boolean dropping;
    private final IIcon[] icons = new IIcon[3];
    public BlockModelSwitcher() {
        super(Material.rock); setBlockName(TouhouLittleMaid.MOD_ID + ".model_switcher");
        setBlockTextureName(TouhouLittleMaid.MOD_ID + ":model_switcher_1"); setHardness(50); setResistance(1200);
        setCreativeTab(CreativeTabs.tabRedstone);
    }
    @Override public TileEntity createNewTileEntity(World world, int meta) { return new TileEntityModelSwitcher(); }
    @Override public void registerBlockIcons(IIconRegister register) {
        icons[0] = register.registerIcon(TouhouLittleMaid.MOD_ID + ":model_switcher_1");
        icons[1] = register.registerIcon(TouhouLittleMaid.MOD_ID + ":model_switcher_2");
        icons[2] = register.registerIcon(TouhouLittleMaid.MOD_ID + ":model_switcher_3");
        blockIcon = icons[0];
    }
    @Override public IIcon getIcon(int side, int metadata) {
        if (side < 2) return icons[0] == null ? blockIcon : icons[0];
        boolean rotated = (metadata & 1) != 0;
        boolean zFace = side == 2 || side == 3;
        int texture = zFace == rotated ? 2 : 1;
        return icons[texture] == null ? blockIcon : icons[texture];
    }
    @Override public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        // The source block faces the placer (opposite their look direction).
        int facing = (MathHelper.floor_double(placer.rotationYaw * 4.0F / 360.0F + 0.5D) + 2) & 3;
        world.setBlockMetadataWithNotify(x, y, z, facing, 2);
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityModelSwitcher && stack.hasTagCompound()
                && stack.getTagCompound().hasKey(com.github.tartaricacid.touhoulittlemaid.item.ItemBlockModelSwitcher.STORAGE, 10))
            ((TileEntityModelSwitcher) tile).readStorage(stack.getTagCompound().getCompoundTag(
                    com.github.tartaricacid.touhoulittlemaid.item.ItemBlockModelSwitcher.STORAGE));
    }
    @Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                               int side, float hitX, float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileEntityModelSwitcher)) return false;
        TileEntityModelSwitcher switcher = (TileEntityModelSwitcher) tile;
        if (!world.isRemote) {
            if(switcher.hasOwner()&&!switcher.isOwnedBy(player)){player.addChatMessage(new ChatComponentText("This Model Switcher belongs to another maid owner"));return true;}
            if (player.isSneaking()) {
                EntityMaid maid = nearestOwnedMaid(world, x, y, z, player);
                if (maid != null) switcher.capture(maid);
            } else player.openGui(TouhouLittleMaid.instance, com.github.tartaricacid.touhoulittlemaid.proxy.CommonProxy.MODEL_SWITCHER_GUI_ID, world, x, y, z);
            player.addChatMessage(new ChatComponentText("Model switcher: " + switcher.getInfoList().size()
                    + " entries, index " + switcher.getIndex()));
        }
        return true;
    }
    @Override public void onNeighborBlockChange(World world, int x, int y, int z, net.minecraft.block.Block neighbor) {
        if (world.isRemote) return;
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileEntityModelSwitcher)) return;
        TileEntityModelSwitcher switcher = (TileEntityModelSwitcher) tile;
        int facing = world.getBlockMetadata(x, y, z) & 3;
        int[] fx = {0, -1, 0, 1}, fz = {1, 0, -1, 0};
        int leftX = -fz[facing], leftZ = fx[facing];
        int rightX = fz[facing], rightZ = -fx[facing];
        boolean left = world.getIndirectPowerLevelTo(x + leftX, y, z + leftZ, powerSide(leftX, leftZ)) > 0;
        boolean right = world.getIndirectPowerLevelTo(x + rightX, y, z + rightZ, powerSide(rightX, rightZ)) > 0;
        boolean powered = left || right;
        if (powered && !switcher.isPowered()) switcher.cycle(left ? 1 : -1);
        switcher.setPowered(powered);
    }
    private static int powerSide(int dx, int dz) {
        if (dz < 0) return 2;
        if (dz > 0) return 3;
        if (dx < 0) return 4;
        return 5;
    }
    @Override public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) {
        int facing = world.getBlockMetadata(x, y, z) & 3;
        int[] fx = {0, -1, 0, 1}, fz = {1, 0, -1, 0};
        int leftX = -fz[facing], leftZ = fx[facing];
        int rightX = fz[facing], rightZ = -fx[facing];
        return side == powerSide(leftX, leftZ) || side == powerSide(rightX, rightZ);
    }
    @Override public void breakBlock(World world, int x, int y, int z, net.minecraft.block.Block block, int meta) {
        if (!world.isRemote && !dropping) {
            TileEntity tile = world.getTileEntity(x, y, z);
            ItemStack stack = new ItemStack(Item.getItemFromBlock(this));
            if (tile instanceof TileEntityModelSwitcher) {
                NBTTagCompound storage = new NBTTagCompound();
                ((TileEntityModelSwitcher) tile).writeStorage(storage);
                NBTTagCompound root = new NBTTagCompound();
                root.setTag(com.github.tartaricacid.touhoulittlemaid.item.ItemBlockModelSwitcher.STORAGE, storage);
                stack.setTagCompound(root);
            }
            world.spawnEntityInWorld(new EntityItem(world, x + .5D, y + .5D, z + .5D, stack));
        }
        super.breakBlock(world, x, y, z, block, meta);
    }
    @Override public Item getItemDropped(int meta, java.util.Random random, int fortune) { return null; }
    @SuppressWarnings("unchecked")
    private EntityMaid nearestOwnedMaid(World world, int x, int y, int z, EntityPlayer player) {
        List<EntityMaid> maids = world.getEntitiesWithinAABB(EntityMaid.class,
                net.minecraft.util.AxisAlignedBB.getBoundingBox(x - 8, y - 4, z - 8, x + 9, y + 5, z + 9));
        EntityMaid nearest = null; double distance = Double.MAX_VALUE;
        for (EntityMaid maid : maids) if (maid.getOwner() == player) {
            double candidate = maid.getDistanceSq(x + .5D, y + .5D, z + .5D);
            if (candidate < distance) { nearest = maid; distance = candidate; }
        }
        return nearest;
    }
}

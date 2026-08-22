package com.github.tartaricacid.touhoulittlemaid.block;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityBookshelf;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityComputer;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityJoy;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityKeyboard;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Shared 1.7 implementation for furniture that can create an EntitySit mount. */
public final class BlockJoy extends BlockContainer {
    public enum Type { KEYBOARD, BOOKSHELF, COMPUTER }

    private final Type type;

    public BlockJoy(String name, Type type) {
        super(type == Type.COMPUTER ? Material.iron : Material.wood);
        this.type = type;
        setBlockName(TouhouLittleMaid.MOD_ID + "." + name);
        setBlockTextureName("minecraft:stone");
        setHardness(2.0F);
        setResistance(3.0F);
        setCreativeTab(CreativeTabs.tabDecorations);
        if (type == Type.KEYBOARD) setBlockBounds(0.25F, 0, 0.25F, 0.75F, 0.625F, 0.75F);
        else if (type == Type.BOOKSHELF) setBlockBounds(0.0625F, 0, 0.0625F, 0.9375F, 0.3125F, 0.9375F);
        else setBlockBounds(0, 0, 0, 1, 0.875F, 1);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        switch (type) {
            case KEYBOARD: return new TileEntityKeyboard();
            case BOOKSHELF: return new TileEntityBookshelf();
            default: return new TileEntityComputer();
        }
    }
    @Override public boolean isOpaqueCube(){return false;} @Override public boolean renderAsNormalBlock(){return false;} @Override public int getRenderType(){return com.github.tartaricacid.touhoulittlemaid.client.renderer.block.LegacyBlockRenderIds.FURNITURE;}

    public String getJoyType() {
        switch (type) {
            case KEYBOARD: return "Keyboard";
            case BOOKSHELF: return "BookShelf";
            default: return "Computer";
        }
    }

    public double getSitYOffset() {
        switch (type) {
            case KEYBOARD: return 0.625D;
            case BOOKSHELF: return 0.375D;
            default: return 1.0D;
        }
    }

    public float getSitYawOffset() {
        return type == Type.BOOKSHELF ? -90.0F : type == Type.COMPUTER ? 180.0F : 0.0F;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        if (player.getCurrentEquippedItem() != null) return false;
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileEntityJoy)) return false;
        TileEntityJoy joy = (TileEntityJoy) tile;
        EntitySit old = joy.getSitEntity();
        if (old != null && old.riddenByEntity != null) return true;
        if (!world.isRemote) {
            if (old != null) old.setDead();
            EntitySit sit = new EntitySit(world, x + 0.5D, y + getSitYOffset(), z + 0.5D,
                    getJoyType(), x, y, z);
            sit.rotationYaw = (world.getBlockMetadata(x, y, z) & 3) * 90.0F + getSitYawOffset();
            world.spawnEntityInWorld(sit);
            joy.setSitEntity(sit);
            player.mountEntity(sit);
        }
        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int facing = MathHelper.floor_double(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        world.setBlockMetadataWithNotify(x, y, z, facing, 3);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, net.minecraft.block.Block block, int meta) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityJoy) ((TileEntityJoy) tile).removeSitEntity();
        super.breakBlock(world, x, y, z, block, meta);
    }
}

package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.init.ModBlocks;
import com.github.tartaricacid.touhoulittlemaid.init.ModItems;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityStatue;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/** Clay + a maid photo -> the largest valid statue volume, matching the modern implementation. */
public final class ItemChisel extends Item {
    private static final int[][] DIMENSIONS = {
            {1, 1, 1}, {1, 2, 1}, {2, 4, 2}, {3, 6, 3}
    };
    public ItemChisel() {
        setUnlocalizedName(TouhouLittleMaid.MOD_ID + ".chisel");
        setTextureName(TouhouLittleMaid.MOD_ID + ":chisel");
        setMaxStackSize(1); setMaxDamage(64);
    }

    @Override
    public boolean onItemUse(ItemStack chisel, EntityPlayer player, World world, int x, int y, int z,
                             int side, float hitX, float hitY, float hitZ) {
        if (world.getBlock(x, y, z) != Blocks.clay) return false;
        ItemStack photo = findPhoto(player);
        if (photo == null) {
            if (!world.isRemote) player.addChatMessage(new net.minecraft.util.ChatComponentTranslation("message.touhou_little_maid.chisel_photo"));
            return true;
        }
        if (!world.isRemote) {
            NBTTagCompound maidData = (NBTTagCompound) photo.getTagCompound().getCompoundTag(ItemCamera.MAID_INFO).copy();
            for (int size = DIMENSIONS.length - 1; size >= 0; size--) {
                List<int[]> positions = findClayVolume(world, x, y, z, side, DIMENSIONS[size]);
                if (positions == null) continue;

                for (int[] pos : positions) world.setBlock(pos[0], pos[1], pos[2], ModBlocks.STATUE, 0, 3);
                int facing = facingFromSide(side);
                for (int[] pos : positions) {
                    TileEntity tile = world.getTileEntity(pos[0], pos[1], pos[2]);
                    if (tile instanceof TileEntityStatue) {
                        boolean core = pos[0] == x && pos[1] == y && pos[2] == z;
                        ((TileEntityStatue) tile).setForgeData(size, core, x, y, z, facing,
                                positions, core ? maidData : null);
                    }
                }
                chisel.damageItem(size + 1, player);
                world.playSoundEffect(x + .5D, y + .5D, z + .5D, "random.anvil_land", .5F, 1.5F);
                return true;
            }
        }
        return true;
    }

    private List<int[]> findClayVolume(World world, int originX, int originY, int originZ,
                                       int side, int[] dimensions) {
        List<int[]> positions = new ArrayList<int[]>(dimensions[0] * dimensions[1] * dimensions[2]);
        for (int x = 0; x < dimensions[0]; x++) {
            for (int y = 0; y < dimensions[1]; y++) {
                for (int z = 0; z < dimensions[2]; z++) {
                    int px = originX;
                    int pz = originZ;
                    switch (side) {
                        case 4: // west
                            px += x; pz += z; break;
                        case 3: // south
                            px += x; pz -= z; break;
                        case 5: // east
                            px -= x; pz -= z; break;
                        case 2: // north
                        default: // vertical faces use north, as in the source implementation
                            px -= x; pz += z; break;
                    }
                    int py = originY + y;
                    if (world.getBlock(px, py, pz) != Blocks.clay) return null;
                    positions.add(new int[]{px, py, pz});
                }
            }
        }
        return positions;
    }

    /** Render-facing encoding: south=0, east=1, north=2, west=3. */
    private int facingFromSide(int side) {
        switch (side) {
            case 3: return 0;
            case 5: return 1;
            case 4: return 3;
            case 2:
            default: return 2;
        }
    }

    private ItemStack findPhoto(EntityPlayer player) {
        for (ItemStack stack : player.inventory.mainInventory) if (stack != null && stack.getItem() == ModItems.PHOTO
                && stack.hasTagCompound() && stack.getTagCompound().hasKey(ItemCamera.MAID_INFO, 10)) return stack;
        return null;
    }
}

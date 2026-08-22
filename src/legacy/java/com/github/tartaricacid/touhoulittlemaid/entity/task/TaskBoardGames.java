package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.block.BlockBoardGame;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.ModBlocks;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityJoy;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;

/** 1.7.10 counterpart of MaidBoardGameTask and TaskBoardGames. */
public final class TaskBoardGames implements IMaidTask {
    private static final String JOY_TYPE = "Gomoku";

    @Override
    public String getId() { return TaskManager.BOARD_GAMES_ID; }

    @Override
    public void tick(EntityMaid maid) {
        if (maid.ridingEntity instanceof EntitySit
                && JOY_TYPE.equals(((EntitySit) maid.ridingEntity).getJoyType())) {
            EntitySit sit = (EntitySit) maid.ridingEntity;
            Block block = maid.worldObj.getBlock(sit.getAssociatedX(), sit.getAssociatedY(), sit.getAssociatedZ());
            if (block instanceof BlockBoardGame) ((BlockBoardGame) block).updateMaidSeat(maid.worldObj,
                    sit.getAssociatedX(), sit.getAssociatedY(), sit.getAssociatedZ(), sit);
            return;
        }
        if (!maid.isPeriodicTick(20)) return;

        int baseX = MathHelper.floor_double(maid.posX);
        int baseY = MathHelper.floor_double(maid.posY);
        int baseZ = MathHelper.floor_double(maid.posZ);
        TileEntityJoy nearest = null;
        BlockBoardGame nearestBlock = null;
        double nearestDistance = Double.MAX_VALUE;
        for (int y = baseY - 4; y <= baseY + 4; y++) {
            for (int x = baseX - 12; x <= baseX + 12; x++) {
                for (int z = baseZ - 12; z <= baseZ + 12; z++) {
                    Block block = maid.worldObj.getBlock(x, y, z);
                    if (block != ModBlocks.GOMOKU && block != ModBlocks.CCHESS && block != ModBlocks.WCHESS) continue;
                    if (!maid.isPositionWithinRestriction(x + 0.5D, y + 0.5D, z + 0.5D)) continue;
                    TileEntity tile = maid.worldObj.getTileEntity(x, y, z);
                    if (!(tile instanceof TileEntityJoy) || ((TileEntityJoy) tile).getSitEntity() != null) continue;
                    double distance = maid.getDistanceSq(x + 0.5D, y + 0.5D, z + 0.5D);
                    if (distance < nearestDistance) {
                        nearest = (TileEntityJoy) tile;
                        nearestBlock = (BlockBoardGame) block;
                        nearestDistance = distance;
                    }
                }
            }
        }
        if (nearest == null) return;
        double[] seatPosition = nearestBlock.getMaidSeatPosition(maid.worldObj,
                nearest.xCoord, nearest.yCoord, nearest.zCoord);
        double seatDistance = maid.getDistanceSq(seatPosition[0], seatPosition[1], seatPosition[2]);
        if (seatDistance > 1.0D) {
            maid.getNavigator().tryMoveToXYZ(seatPosition[0], seatPosition[1], seatPosition[2], 0.6D);
            return;
        }
        nearestBlock.startMaidGame(maid.worldObj, nearest.xCoord, nearest.yCoord, nearest.zCoord,
                nearest, maid);
    }

    @Override
    public void onDeselected(EntityMaid maid) {
        if (maid.ridingEntity instanceof EntitySit
                && JOY_TYPE.equals(((EntitySit) maid.ridingEntity).getJoyType())) maid.mountEntity(null);
        maid.getNavigator().clearPathEntity();
    }
}

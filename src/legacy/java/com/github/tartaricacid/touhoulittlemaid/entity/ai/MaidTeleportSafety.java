package com.github.tartaricacid.touhoulittlemaid.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Checks the whole body and loaded surroundings before a follow teleport. */
public final class MaidTeleportSafety {
    private MaidTeleportSafety() { }
    public static boolean canStand(World world, EntityLivingBase entity, double x, double y, double z) {
        if (world == null || !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                || Math.abs(x) >= 30000000D || Math.abs(z) >= 30000000D
                || y < 1 || y + entity.height >= 256) return false;
        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(x-entity.width/2D,y,z-entity.width/2D,
                x+entity.width/2D,y+entity.height,z+entity.width/2D);
        // Collision collection also visits immediately adjacent blocks.
        for (int bx=MathHelper.floor_double(box.minX)-1;bx<=MathHelper.floor_double(box.maxX)+1;bx++)
            for (int bz=MathHelper.floor_double(box.minZ)-1;bz<=MathHelper.floor_double(box.maxZ)+1;bz++)
                if (!world.blockExists(bx,(int)y,bz)) return false;
        int bx=MathHelper.floor_double(x), by=MathHelper.floor_double(y)-1, bz=MathHelper.floor_double(z);
        if (!World.doesBlockHaveSolidTopSurface(world,bx,by,bz)) return false;
        for (int ix=MathHelper.floor_double(box.minX);ix<=MathHelper.floor_double(box.maxX);ix++)
            for (int iy=MathHelper.floor_double(y);iy<=MathHelper.floor_double(box.maxY);iy++)
                for (int iz=MathHelper.floor_double(box.minZ);iz<=MathHelper.floor_double(box.maxZ);iz++)
                    if (world.getBlock(ix,iy,iz)==Blocks.fire || world.getBlock(ix,iy,iz)==Blocks.cactus) return false;
        return world.getCollidingBoundingBoxes(entity,box).isEmpty()
                && world.checkNoEntityCollision(box,entity) && !world.isAnyLiquid(box);
    }
}

package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityTameable;

import java.util.List;

public final class CombatTargeting {
    private CombatTargeting() {
    }

    @SuppressWarnings("unchecked")
    static void updateTarget(EntityMaid maid, double horizontalRange) {
        if (!maid.canRunCombatAI()) {
            maid.setAttackTarget(null);
            maid.getNavigator().clearPathEntity();
            return;
        }
        EntityLivingBase owner = maid.getOwner();
        if (owner != null && isValidTarget(maid, owner.getAITarget(), horizontalRange)) {
            maid.setAttackTarget(owner.getAITarget());
            return;
        }
        if (owner != null && isValidTarget(maid, owner.getLastAttacker(), horizontalRange)) {
            maid.setAttackTarget(owner.getLastAttacker());
            return;
        }
        if (isValidTarget(maid, maid.getAITarget(), horizontalRange)) {
            maid.setAttackTarget(maid.getAITarget());
            return;
        }

        EntityLivingBase target = maid.getAttackTarget();
        if (isValidTarget(maid, target, horizontalRange)) return;

        List<EntityMob> nearby = maid.worldObj.getEntitiesWithinAABB(
                EntityMob.class, maid.boundingBox.expand(horizontalRange, 4.0D, horizontalRange));
        EntityMob nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (EntityMob candidate : nearby) {
            double distance = maid.getDistanceSqToEntity(candidate);
            if (distance < nearestDistance && maid.canEntityBeSeen(candidate)
                    && isValidTarget(maid, candidate, horizontalRange)) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }
        maid.setAttackTarget(nearest);
    }

    public static boolean isValidTarget(EntityMaid maid, EntityLivingBase target, double range) {
        return target != null && target != maid && target.worldObj == maid.worldObj
                && target.isEntityAlive() && target != maid.getOwner()
                && !(target instanceof net.minecraft.entity.player.EntityPlayer && ((net.minecraft.entity.player.EntityPlayer) target).capabilities.disableDamage)
                && maid.getDistanceSqToEntity(target) <= range * range
                && maid.isPositionWithinRestriction(target.posX, target.posY, target.posZ)
                && !maid.isOnSameTeam(target)
                && !(target instanceof net.minecraft.entity.passive.EntityVillager)
                && !(target instanceof EntityTameable && ((EntityTameable) target).isTamed())
                && !(target instanceof EntityMaid && ((EntityMaid) target).getOwner() == maid.getOwner());
    }
}

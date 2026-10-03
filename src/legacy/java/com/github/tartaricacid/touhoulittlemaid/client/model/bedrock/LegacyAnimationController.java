package com.github.tartaricacid.touhoulittlemaid.client.model.bedrock;

import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.animation.LegacyAnimationFrame;
import com.github.tartaricacid.touhoulittlemaid.entity.animation.MaidActionState;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit;
import com.github.tartaricacid.touhoulittlemaid.init.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.item.*;
import net.minecraft.init.Items;

/** Converts synchronized entity state to frame inputs; the model owns its effective animation profile. */
public final class LegacyAnimationController {
    private LegacyAnimationController() { }
    public static LegacyAnimationFrame frame(Entity entity, float limbSwing, float amount, float age,
                                               float yaw, float pitch, float vanillaSwing) {
        LegacyAnimationFrame f = new LegacyAnimationFrame();
        f.limbSwing = limbSwing; f.limbAmount = amount; f.age = age; f.yaw = yaw; f.pitch = pitch;
        if (!(entity instanceof EntityMaid)) return f;
        EntityMaid maid = (EntityMaid) entity;
        float time = maid.ticksExisted + Math.max(0, Math.min(1, age - maid.ticksExisted));
        MaidActionState action = maid.getActionState();
        f.sleeping = maid.isMaidSleeping(); f.sitting = maid.isSitting();
        f.riding = maid.ridingEntity != null && !f.sleeping;
        f.carried = maid.ridingEntity instanceof net.minecraft.entity.player.EntityPlayer;
        f.boat = maid.ridingEntity instanceof EntityBoat; f.begging = maid.isBegging();
        f.fishing = maid.hasFishingHook(); f.uuidSeed = maid.getUniqueID().getLeastSignificantBits(); f.dimension = maid.dimension;
        f.healthRatio = maid.getMaxHealth() <= 0 ? 1 : maid.getHealth() / maid.getMaxHealth();
        f.health=maid.getHealth();f.maxHealth=maid.getMaxHealth();
        f.foodLevel=maid.getHunger()/5F;f.sneaking=maid.isSneaking();f.wet=maid.isWet();f.bodyYaw=maid.renderYawOffset;
        f.position[0]=maid.posX;f.position[1]=maid.posY;f.position[2]=maid.posZ;
        float partial=Math.max(0,Math.min(1,age-maid.ticksExisted));
        f.positionDelta[0]=(maid.posX-maid.prevPosX)*partial;
        f.positionDelta[1]=(maid.posY-maid.prevPosY)*partial;
        f.positionDelta[2]=(maid.posZ-maid.prevPosZ)*partial;
        if(Math.hypot(f.positionDelta[0],f.positionDelta[2])>=.0001){
            double angle=Math.atan2(f.positionDelta[2],f.positionDelta[0])-Math.toRadians(90+maid.prevRotationYaw+(maid.rotationYaw-maid.prevRotationYaw)*partial);
            f.inputVertical=(float)Math.cos(angle);f.inputHorizontal=(float)Math.sin(angle);
        }
        f.hurt = maid.hurtTime > 0; f.dead = !maid.isEntityAlive(); f.sprinting = maid.isSprinting();
        f.water = maid.isInWater(); f.climbing = maid.isOnLadder(); f.verticalSpeed = (float) maid.motionY; f.onGround = maid.onGround;
        f.groundSpeed = (float) (20 * Math.sqrt(maid.motionX * maid.motionX + maid.motionZ * maid.motionZ));
        f.verticalDisplacement = (float) (maid.posY - maid.prevPosY);
        f.yawSpeed = 20 * (maid.rotationYaw - maid.prevRotationYaw);
        f.task = maid.getTaskId().substring(maid.getTaskId().indexOf(':') + 1);
        if (maid.ridingEntity instanceof EntitySit) {
            f.joy = ((EntitySit) maid.ridingEntity).getJoyType().toLowerCase(java.util.Locale.ROOT);
            if (f.joy.startsWith("on")) f.joy = f.joy.substring(2);
            if ("homemeal".equals(f.joy)) f.joy = "picnic";
        }
        f.use = action.kind(time); f.useLeft = action.useLeft(); f.useTicks = action.useElapsed(time); f.useSequence = action.useSequence();
        f.ranged = action.isRanged() && action.using(time);
        f.swing = action.swingProgress(time); f.swingLeft = action.swingLeft();
        // Initial vanilla swing remains usable before the first action snapshot arrives.
        if (action.swingSequence() == 0) { f.swing = vanillaSwing; f.swingLeft = false; }
        f.swingSequence = action.swingSequence(); f.swingTicks = action.swingSequence() == 0 ? f.swing * 6 : action.swingElapsed(time);
        f.cancelSwing = !action.hasSwingEvent() && f.swing <= 0;
        if (f.sleeping || f.dead || "honey".equals(f.task) || "crossbow_attack".equals(f.task) || "trident_attack".equals(f.task)) {
            f.actionsDisabled = true; f.use = ""; f.swing = 0; f.ranged = false;
        }
        ItemStack main = maid.getHeldItem(), off = maid.getOffhandItem(), shown = action.displayItem(time);
        if (f.using() && shown != null) { if (f.useLeft) off = shown; else main = shown; }
        f.mainId = id(main); f.offId = id(off); f.mainCategory = category(main); f.offCategory = category(off);
        f.backpack = !"empty".equals(maid.getBackpackType());
        f.helmet = maid.getEquipmentInSlot(4) != null; f.chest = maid.getEquipmentInSlot(3) != null;
        f.leggings = maid.getEquipmentInSlot(2) != null; f.boots = maid.getEquipmentInSlot(1) != null;
        if (f.sleeping || f.sitting || f.riding) f.limbAmount = 0;
        return f;
    }
    private static String id(ItemStack stack) { return stack == null ? "" : String.valueOf(Item.itemRegistry.getNameForObject(stack.getItem())); }
    private static String category(ItemStack stack) {
        if (stack == null) return "";
        Item item = stack.getItem();
        if (item == Items.bow) return "bow";
        if (item == ModItems.HAKUREI_GOHEI || item == ModItems.SANAE_GOHEI) return "gohei";
        if (item instanceof ItemSword) return "sword";
        if (item instanceof ItemAxe) return "axe";
        if (item instanceof ItemPickaxe) return "pickaxe";
        if (item instanceof ItemSpade) return "shovel";
        if (item instanceof ItemHoe) return "hoe";
        if (stack.getItemUseAction() == EnumAction.eat) return "eat";
        if (stack.getItemUseAction() == EnumAction.drink) return "drink";
        if (stack.getItemUseAction() == EnumAction.block) return "block";
        return "";
    }
}

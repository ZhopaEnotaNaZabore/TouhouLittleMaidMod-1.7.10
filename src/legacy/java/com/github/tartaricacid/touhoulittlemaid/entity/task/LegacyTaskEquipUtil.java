package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.compat.LegacyTConstruct;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.SharedMonsterAttributes;
import com.github.tartaricacid.touhoulittlemaid.compat.miner.LegacyMiningToolCompat;

/** Java 8 counterpart of the source TaskEquipUtil main-hand transaction. */
final class LegacyTaskEquipUtil {
    private LegacyTaskEquipUtil() { }

    static boolean ensureMainhand(EntityMaid maid, Item... accepted) {
        ItemStack held = maid.getMaidEquipmentInventory().getStackInSlot(0);
        if (matches(held, accepted)) return true;
        int sourceSlot = -1;
        for (Item item : accepted) {
            sourceSlot = maid.findInventorySlot(item);
            if (sourceSlot >= 0) break;
        }
        if (sourceSlot < 0) return false;
        ItemStack replacement = maid.takeOneFromSlot(sourceSlot);
        if (replacement == null) return false;
        maid.getMaidEquipmentInventory().setInventorySlotContents(0, replacement);
        if (held != null) {
            ItemStack remaining = maid.addToMaidInventory(held);
            if (remaining != null) maid.entityDropItem(remaining, 0.0F);
        }
        maid.getMaidEquipmentInventory().markDirty();
        return true;
    }

    static boolean ensureAttackWeapon(EntityMaid maid) {
        ItemStack held = maid.getMaidEquipmentInventory().getStackInSlot(0);
        if (isAttackWeapon(held)) return true;
        int sourceSlot = maid.findAvailableInventorySlot(LegacyTaskEquipUtil::isAttackWeapon);
        if (sourceSlot < 0) return false;
        ItemStack replacement = maid.takeOneFromSlot(sourceSlot);
        if (replacement == null) return false;
        maid.getMaidEquipmentInventory().setInventorySlotContents(0, replacement);
        if (held != null) {
            ItemStack remaining = maid.addToMaidInventory(held);
            if (remaining != null) maid.entityDropItem(remaining, 0.0F);
        }
        maid.getMaidEquipmentInventory().markDirty();
        return true;
    }

    static boolean ensureRangedWeapon(EntityMaid maid) {
        java.util.function.Predicate<ItemStack> accepted=s->LegacyTConstruct.matchesTask(s,maid.getTaskId())
                || TaskManager.RANGED_ATTACK_ID.equals(maid.getTaskId()) && s!=null && s.stackSize>0
                && com.github.tartaricacid.touhoulittlemaid.item.ItemAnimationGun.isBowWeapon(s.getItem());
        ItemStack held=maid.getHeldItem();if(accepted.test(held))return true;
        int slot=maid.findAvailableInventorySlot(accepted);if(slot<0)return false;
        ItemStack replacement=maid.takeOneFromSlot(slot);if(replacement==null)return false;
        maid.getMaidEquipmentInventory().setInventorySlotContents(0,replacement);
        if(held!=null){ItemStack remainder=maid.addToMaidInventory(held);if(remainder!=null)maid.entityDropItem(remainder,0);}
        maid.getMaidEquipmentInventory().markDirty();return true;
    }

    static boolean ensureMiningTool(EntityMaid maid) {
        ItemStack held = maid.getMaidEquipmentInventory().getStackInSlot(0);
        if (LegacyMiningToolCompat.isMiningTool(held)) return true;
        int sourceSlot = maid.findAvailableInventorySlot(LegacyMiningToolCompat::isMiningTool);
        if (sourceSlot < 0) return false;
        ItemStack replacement = maid.takeOneFromSlot(sourceSlot);
        if (replacement == null) return false;
        maid.getMaidEquipmentInventory().setInventorySlotContents(0, replacement);
        if (held != null) {
            ItemStack remaining = maid.addToMaidInventory(held);
            if (remaining != null) maid.entityDropItem(remaining, 0.0F);
        }
        maid.getMaidEquipmentInventory().markDirty();
        return true;
    }

    static boolean isAttackWeapon(ItemStack stack) {
        if (LegacyTConstruct.tool(stack)) return LegacyTConstruct.melee(stack);
        return stack != null && stack.stackSize>0 && stack.getItem().getAttributeModifiers(stack)
                .containsKey(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName());
    }

    static boolean matches(ItemStack stack, Item... accepted) {
        if (stack == null) return false;
        for (Item item : accepted) if (stack.getItem() == item) return true;
        return false;
    }
}

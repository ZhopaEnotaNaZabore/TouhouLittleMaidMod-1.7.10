package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.inventory.IInventory;
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
        int sourceSlot = findAttackWeapon(maid.getMaidTaskInventory(), 200);
        if (sourceSlot < 0) sourceSlot = findAttackWeapon(maid.getMaidInventory(), 0);
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

    static boolean ensureMiningTool(EntityMaid maid) {
        ItemStack held = maid.getMaidEquipmentInventory().getStackInSlot(0);
        if (LegacyMiningToolCompat.isMiningTool(held)) return true;
        int sourceSlot = findMiningTool(maid.getMaidTaskInventory(), 200);
        if (sourceSlot < 0) sourceSlot = findMiningTool(maid.getMaidInventory(), 0);
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
        return stack != null && stack.getItem().getAttributeModifiers(stack)
                .containsKey(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName());
    }

    private static int findAttackWeapon(IInventory inventory, int offset) {
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            if (isAttackWeapon(inventory.getStackInSlot(slot))) return offset + slot;
        }
        return -1;
    }

    private static int findMiningTool(IInventory inventory, int offset) {
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            if (LegacyMiningToolCompat.isMiningTool(inventory.getStackInSlot(slot))) return offset + slot;
        }
        return -1;
    }

    static boolean matches(ItemStack stack, Item... accepted) {
        if (stack == null) return false;
        for (Item item : accepted) if (stack.getItem() == item) return true;
        return false;
    }
}

package com.github.tartaricacid.touhoulittlemaid.inventory.container;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public final class ContainerMaid extends Container {
    private final EntityMaid maid;

    public ContainerMaid(InventoryPlayer playerInventory, EntityMaid maid) {
        this.maid = maid;
        // The source backpack page always contains hands and the four armor
        // slots.  They are part of the 165x128 backpack texture, not a second
        // decorative grid.
        addSlotToContainer(new Slot(maid.getMaidEquipmentInventory(), 0, 87, 77));
        addSlotToContainer(new Slot(maid.getMaidEquipmentInventory(), 1, 121, 77));
        addArmorSlot(5, 0, 94, 37);   // helmet
        addArmorSlot(4, 1, 114, 37);  // chestplate
        addArmorSlot(3, 2, 94, 57);   // leggings
        addArmorSlot(2, 3, 114, 57);  // boots
        final int[] rowY = {37, 59, 82, 100, 123, 141};
        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 6; column++) {
                final int maidSlot = column + row * 6;
                addSlotToContainer(new Slot(maid.getMaidInventory(), maidSlot,
                        143 + column * 18, rowY[row]) {
                    @Override public boolean isItemValid(ItemStack stack) { return maidSlot < ContainerMaid.this.maid.getBackpackCapacity(); }
                    @Override public boolean canTakeStack(EntityPlayer player) { return maidSlot < ContainerMaid.this.maid.getBackpackCapacity(); }
                });
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlotToContainer(new Slot(playerInventory, column + row * 9 + 9,
                        88 + column * 18, 174 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlotToContainer(new Slot(playerInventory, column, 88 + column * 18, 232));
        }
    }

    private void addArmorSlot(final int inventoryIndex, final int armorType, int x, int y) {
        addSlotToContainer(new Slot(maid.getMaidEquipmentInventory(), inventoryIndex, x, y) {
            @Override public boolean isItemValid(ItemStack stack) {
                return stack != null && stack.getItem() instanceof ItemArmor
                        && ((ItemArmor) stack.getItem()).armorType == armorType;
            }
            @Override public int getSlotStackLimit() { return 1; }
        });
    }

    public EntityMaid getMaid() {
        return maid;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return maid.isEntityAlive() && maid.getOwner() == player && player.getDistanceSqToEntity(maid) < 64.0D;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = (Slot) inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return null;
        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        final int maidSlots = 42;
        if (index < maidSlots) {
            if (!mergeItemStack(stack, maidSlots, maidSlots + 36, true)) return null;
        } else {
            if (stack.getItem() instanceof ItemArmor) {
                int equipmentIndex = 2 + ((ItemArmor) stack.getItem()).armorType;
                if (!mergeItemStack(stack, equipmentIndex, equipmentIndex + 1, false)
                        && !mergeItemStack(stack, 6, 6 + maid.getBackpackCapacity(), false)) return null;
            } else if (!mergeItemStack(stack, 6, 6 + maid.getBackpackCapacity(), false)) {
                return null;
            }
        }
        if (stack.stackSize == 0) slot.putStack(null); else slot.onSlotChanged();
        if (stack.stackSize == original.stackSize) return null;
        slot.onPickupFromSlot(player, stack);
        return original;
    }
}

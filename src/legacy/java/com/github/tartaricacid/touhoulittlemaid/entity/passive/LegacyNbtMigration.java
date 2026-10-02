package com.github.tartaricacid.touhoulittlemaid.entity.passive;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/** Converts the stable 1.20 maid tags into the representation understood by 1.7.10. */
public final class LegacyNbtMigration {
    private static final String BACKPACK_TYPE = "MaidBackpackType";
    private static final String BACKPACK_LEVEL = "MaidBackpackLevel";
    private static final String[] HANDLER_INVENTORIES = {
            "MaidInventory", "MaidBaubleInventory", "MaidHideInventory", "MaidTaskInventory"
    };

    private LegacyNbtMigration() {
    }

    public static void normalize(NBTTagCompound root) {
        if (!root.hasKey("MaidPickup") && root.hasKey("MaidIsPickup")) root.setBoolean("MaidPickup",root.getBoolean("MaidIsPickup"));
        if (!root.hasKey("MaidHomeMode") && root.hasKey("MaidIsHome")) root.setBoolean("MaidHomeMode",root.getBoolean("MaidIsHome"));
        normalizeUuids(root);
        normalizeBackpack(root);
        normalizeScheduleDimension(root);
        for (String key : HANDLER_INVENTORIES) {
            normalizeInventory(root, key);
        }
    }

    private static void normalizeScheduleDimension(NBTTagCompound root) {
        if (!root.hasKey("MaidSchedulePos", 10)) return;
        NBTTagCompound schedule = root.getCompoundTag("MaidSchedulePos");
        if (schedule.hasKey("DimensionId", 3) || !schedule.hasKey("Dimension", 8)) return;
        String dimension = schedule.getString("Dimension");
        if ("minecraft:the_nether".equals(dimension)) schedule.setInteger("DimensionId", -1);
        else if ("minecraft:the_end".equals(dimension)) schedule.setInteger("DimensionId", 1);
        else if ("minecraft:overworld".equals(dimension)) schedule.setInteger("DimensionId", 0);
    }

    private static void normalizeUuids(NBTTagCompound root) {
        if (root.hasKey("UUID", 11) && (!root.hasKey("UUIDMost") || !root.hasKey("UUIDLeast"))) {
            long[] value = uuid(root.getIntArray("UUID"));
            if (value != null) { root.setLong("UUIDMost", value[0]); root.setLong("UUIDLeast", value[1]); }
        }
        if (root.hasKey("Owner", 11) && !root.hasKey("OwnerUUID", 8)) {
            long[] value = uuid(root.getIntArray("Owner"));
            if (value != null) root.setString("OwnerUUID", new java.util.UUID(value[0], value[1]).toString());
        }
    }

    private static long[] uuid(int[] value) {
        if (value == null || value.length != 4) return null;
        return new long[]{((long)value[0] << 32) | (value[1] & 0xffffffffL),
                ((long)value[2] << 32) | (value[3] & 0xffffffffL)};
    }

    private static void normalizeBackpack(NBTTagCompound root) {
        if (!root.hasKey(BACKPACK_TYPE, 8) && root.hasKey(BACKPACK_LEVEL, 3)) {
            switch (root.getInteger(BACKPACK_LEVEL)) {
                case 1:
                    root.setString(BACKPACK_TYPE, "maid_backpack_small");
                    break;
                case 2:
                    root.setString(BACKPACK_TYPE, "maid_backpack_middle");
                    break;
                case 3:
                    root.setString(BACKPACK_TYPE, "maid_backpack_big");
                    break;
                default:
                    root.setString(BACKPACK_TYPE, "empty");
            }
        }
        if (root.hasKey(BACKPACK_TYPE, 8)) {
            String id = root.getString(BACKPACK_TYPE);
            int separator = id.indexOf(':');
            if (separator >= 0 && separator + 1 < id.length()) {
                id = id.substring(separator + 1);
            }
            if ("small_backpack".equals(id)) id = "maid_backpack_small";
            else if ("middle_backpack".equals(id)) id = "maid_backpack_middle";
            else if ("big_backpack".equals(id)) id = "maid_backpack_big";
            else if ("tank".equals(id)) id = "tank_backpack";
            root.setString(BACKPACK_TYPE, id);
        }
        // 1.20 stores specialised backpack state in MaidBackpackData.  Forge's
        // FluidTank serialises its fluid as {Tanks:{FluidName,Amount}}.
        if (root.hasKey("MaidBackpackData", 10)) {
            NBTTagCompound data = root.getCompoundTag("MaidBackpackData");
            if (data.hasKey("Tanks", 10)) migrateTank(root, data.getCompoundTag("Tanks"));
        }
        root.removeTag(BACKPACK_LEVEL);
    }

    private static void migrateTank(NBTTagCompound root, NBTTagCompound tank) {
        String fluid = tank.getString("FluidName");
        if (fluid.isEmpty()) fluid = tank.getString("Fluid");
        if (!fluid.isEmpty()) root.setString("MaidBackpackFluid", legacyFluidId(fluid));
        if (tank.hasKey("Amount", 3)) {
            root.setInteger("MaidBackpackFluidAmount", Math.max(0, Math.min(10000, tank.getInteger("Amount"))));
        }
    }

    private static String legacyFluidId(String fluid) {
        return fluid.startsWith("minecraft:") ? fluid.substring("minecraft:".length()) : fluid;
    }

    private static void normalizeInventory(NBTTagCompound root, String key) {
        if (!root.hasKey(key)) return;
        NBTBase raw = root.getTag(key);
        NBTTagList items;
        if (raw instanceof NBTTagCompound) {
            items = ((NBTTagCompound) raw).getTagList("Items", 10);
            root.setTag(key, items);
        } else if (raw instanceof NBTTagList) {
            items = (NBTTagList) raw;
        } else {
            return;
        }
        for (int index = 0; index < items.tagCount(); index++) {
            normalizeItem(items.getCompoundTagAt(index));
        }
    }

    private static void normalizeItem(NBTTagCompound stack) {
        if (!stack.hasKey("id", 8)) return;
        String registryName = stack.getString("id");
        int separator = registryName.indexOf(':');
        String domain = separator >= 0 ? registryName.substring(0, separator) : "minecraft";
        String path = separator >= 0 ? registryName.substring(separator + 1) : registryName;
        Item item = GameRegistry.findItem(domain, path);
        if (item != null) {
            stack.setShort("id", (short) Item.getIdFromItem(item));
        }
    }

    /** Converts one modern string-id ItemStack tag for TileEntity migrations. */
    public static void normalizeItemStack(NBTTagCompound stack) { normalizeItem(stack); }
}

package com.github.tartaricacid.touhoulittlemaid.tileentity;

import net.minecraft.nbt.NBTTagCompound;

/** Modern custom fields are nested; native saves take precedence after migration. */
public final class LegacyTileNbt {
    private LegacyTileNbt() { }
    public static NBTTagCompound data(NBTTagCompound root) {
        NBTTagCompound result = root.hasKey("ForgeData", 10)
                ? (NBTTagCompound) root.getCompoundTag("ForgeData").copy() : new NBTTagCompound();
        for (Object entry : root.func_150296_c()) {
            String key = (String) entry;
            if (!"ForgeData".equals(key)) result.setTag(key, root.getTag(key).copy());
        }
        return result;
    }
}

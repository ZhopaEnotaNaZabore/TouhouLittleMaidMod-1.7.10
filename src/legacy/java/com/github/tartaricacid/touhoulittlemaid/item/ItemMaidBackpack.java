package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public final class ItemMaidBackpack extends Item {
    private final String type;
    private final int capacity;
    public ItemMaidBackpack(String type, int capacity) {
        this.type = type; this.capacity = capacity; setMaxStackSize(1);
        setUnlocalizedName(TouhouLittleMaid.MOD_ID + "." + type); setTextureName(TouhouLittleMaid.MOD_ID + ":" + type);
        setCreativeTab(CreativeTabs.tabMisc);
    }
    public String getBackpackType() { return type; }
    public int getCapacity() { return capacity; }
}

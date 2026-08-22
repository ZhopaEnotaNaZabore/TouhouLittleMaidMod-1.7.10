package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

/** 1.7 representation of the gohei used to select and power danmaku combat. */
public final class ItemHakureiGohei extends Item {
    public ItemHakureiGohei(String name) {
        setUnlocalizedName(TouhouLittleMaid.MOD_ID + "." + name);
        setTextureName(TouhouLittleMaid.MOD_ID + ":" + name);
        setMaxStackSize(1);
        setMaxDamage(384);
        setCreativeTab(CreativeTabs.tabCombat);
    }
}

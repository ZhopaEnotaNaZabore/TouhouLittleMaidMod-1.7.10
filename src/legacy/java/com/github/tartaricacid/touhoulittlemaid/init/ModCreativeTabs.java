package com.github.tartaricacid.touhoulittlemaid.init;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public final class ModCreativeTabs {
    public static final CreativeTabs TLM = new CreativeTabs("touhou_little_maid") {
        @Override public Item getTabIconItem() { return ModItems.HAKUREI_GOHEI; }
    };
    private ModCreativeTabs() { }
}

package com.github.tartaricacid.touhoulittlemaid.init;

import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;

/** Replaces the modern global loot modifier with 1.7 chest hooks. */
public final class LegacyLoot {
    private LegacyLoot(){}
    public static void init(){
        String[] categories={ChestGenHooks.DUNGEON_CHEST,ChestGenHooks.MINESHAFT_CORRIDOR,ChestGenHooks.VILLAGE_BLACKSMITH,
                ChestGenHooks.PYRAMID_DESERT_CHEST,ChestGenHooks.PYRAMID_JUNGLE_CHEST,ChestGenHooks.STRONGHOLD_LIBRARY};
        for(String category:categories)ChestGenHooks.addItem(category,new WeightedRandomChestContent(new ItemStack(ModItems.POWER_POINT),1,3,8));
    }
}

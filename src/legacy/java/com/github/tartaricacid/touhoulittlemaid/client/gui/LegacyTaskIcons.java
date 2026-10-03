package com.github.tartaricacid.touhoulittlemaid.client.gui;

import net.minecraft.init.Items;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import com.github.tartaricacid.touhoulittlemaid.init.ModItems;

/** SRC profession icons using the items available in vanilla 1.7.10. */
final class LegacyTaskIcons {
    private LegacyTaskIcons() { }
    static ItemStack get(String id) {
        String task=id==null?"idle":id.substring(id.indexOf(':')+1);
        if("attack".equals(task))return new ItemStack(Items.diamond_sword);
        if("ranged_attack".equals(task))return new ItemStack(Items.bow);
        if("danmaku_attack".equals(task))return new ItemStack(ModItems.HAKUREI_GOHEI);
        if("farm".equals(task))return new ItemStack(Items.iron_hoe);
        if("sugar_cane".equals(task))return new ItemStack(Items.reeds);
        if("cocoa".equals(task))return new ItemStack(Items.dye,1,3);
        if("melon".equals(task))return new ItemStack(Items.melon);
        if("grass".equals(task))return new ItemStack(Blocks.tallgrass,1,1);
        if("snow".equals(task))return new ItemStack(Items.snowball);
        if("feed".equals(task))return new ItemStack(Items.cooked_beef);
        if("feed_animal".equals(task))return new ItemStack(Items.wheat);
        if("milk".equals(task))return new ItemStack(Items.milk_bucket);
        if("shears".equals(task))return new ItemStack(Items.shears);
        if("fishing".equals(task))return new ItemStack(Items.fishing_rod);
        if("torch".equals(task))return new ItemStack(Blocks.torch);
        if("idle".equals(task))return new ItemStack(Items.feather);
        if("extinguishing".equals(task))return new ItemStack(ModItems.EXTINGUISHER);
        if("board_games".equals(task))return new ItemStack(ModItems.GOMOKU_BOARD_STATE);
        if("miner".equals(task))return new ItemStack(Items.iron_pickaxe);
        return null;
    }
}

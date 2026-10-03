package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

/** 1.7 representation of the gohei used to select and power danmaku combat. */
public final class ItemHakureiGohei extends Item {
    @Override public boolean onItemUse(net.minecraft.item.ItemStack stack, net.minecraft.entity.player.EntityPlayer player,
            net.minecraft.world.World world,int x,int y,int z,int side,float hx,float hy,float hz) {
        return com.github.tartaricacid.touhoulittlemaid.block.LegacyAltarStructure.tryBuild(world,player,stack,x,y,z,side);
    }
    public ItemHakureiGohei(String name) {
        setUnlocalizedName(TouhouLittleMaid.MOD_ID + "." + name);
        setTextureName(TouhouLittleMaid.MOD_ID + ":" + name);
        setMaxStackSize(1);
        setMaxDamage(384);
        setCreativeTab(CreativeTabs.tabCombat);
    }
}

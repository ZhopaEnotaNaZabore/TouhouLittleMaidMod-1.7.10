package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Curios-free 1.7 representation of a maid accessory. */
public final class ItemMaidBauble extends Item {
    public enum Type { DROWN, EXPLOSION, EXTRA_LIFE, FALL, FIRE, MAGNET, MAGIC, NIMBLE, PROJECTILE, MUTE }
    private final Type type;

    public ItemMaidBauble(String name, Type type, int durability) {
        this.type = type; setUnlocalizedName(TouhouLittleMaid.MOD_ID + "." + name);
        setTextureName(TouhouLittleMaid.MOD_ID + ":" + name); setMaxStackSize(1); setCreativeTab(CreativeTabs.tabMisc);
        if (durability > 0) setMaxDamage(durability);
    }
    public Type getType() { return type; }
    @Override public boolean hasEffect(ItemStack stack, int pass) { return true; }
}

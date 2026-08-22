package com.github.tartaricacid.touhoulittlemaid.item;
import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;import net.minecraft.creativetab.CreativeTabs;import net.minecraft.item.Item;
/** Explicit compatibility items for post-1.7 vanilla weapons. */
public final class ItemLegacyRangedWeapon extends Item{public enum Type{CROSSBOW,TRIDENT}private final Type type;public ItemLegacyRangedWeapon(String name,Type type,int durability){this.type=type;setUnlocalizedName(TouhouLittleMaid.MOD_ID+"."+name);setTextureName(type==Type.CROSSBOW?"minecraft:bow_standby":"minecraft:iron_sword");setMaxStackSize(1);setMaxDamage(durability);setCreativeTab(CreativeTabs.tabCombat);}public Type getType(){return type;}}

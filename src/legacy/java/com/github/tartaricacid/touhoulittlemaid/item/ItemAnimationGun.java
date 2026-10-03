package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.init.Items;

/** Arrow-powered stand-in for external gun integrations; vanilla bow ballistics. */
public final class ItemAnimationGun extends ItemBow {
    private final String animationType;
    public ItemAnimationGun(String type) {
        animationType = type;
        setUnlocalizedName(TouhouLittleMaid.MOD_ID + ".animation_" + type);
        setTextureName("bow");
    }
    public String getAnimationType() { return animationType; }
    public static boolean isBowWeapon(Item item) { return item == Items.bow || item instanceof ItemAnimationGun
            || com.github.tartaricacid.touhoulittlemaid.compat.LegacyAvaritia.isBow(item); }
}

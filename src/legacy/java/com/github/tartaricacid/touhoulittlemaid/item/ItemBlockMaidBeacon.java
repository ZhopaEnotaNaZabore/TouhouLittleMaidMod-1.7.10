package com.github.tartaricacid.touhoulittlemaid.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;
import com.google.common.collect.Multimap;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;

/** Persistent 1.7 ItemBlock counterpart of the source ItemMaidBeacon. */
public final class ItemBlockMaidBeacon extends ItemBlock {
    public static final String STORAGE = "StorageData";

    public ItemBlockMaidBeacon(Block block) {
        super(block);
        setMaxStackSize(1);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
        float power = 0;
        NBTTagCompound root = stack.getTagCompound();
        if (root != null && root.hasKey(STORAGE, 10)) {
            NBTTagCompound data = root.getCompoundTag(STORAGE);
            if (data.hasKey("ForgeData", 10)) data = data.getCompoundTag("ForgeData");
            float saved = data.getFloat("StoragePower");
            if (!Float.isNaN(saved) && !Float.isInfinite(saved)) power = Math.max(0, saved);
        }
        lines.add(net.minecraft.util.StatCollector.translateToLocalFormatted(
                "tooltips.touhou_little_maid.maid_beacon.desc",String.format("%.2f",power)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Multimap getItemAttributeModifiers(){
        Multimap modifiers=super.getItemAttributeModifiers();
        modifiers.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(),
                new AttributeModifier(field_111210_e,"Shrine Lamp modifier",3.0D,0));
        return modifiers;
    }
}

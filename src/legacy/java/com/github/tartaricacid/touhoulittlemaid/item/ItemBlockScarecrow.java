package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.config.LegacyConfig;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import java.util.List;

/** Item form retaining the source block's configurable spawn-exclusion tooltip. */
public final class ItemBlockScarecrow extends ItemBlock {
    public ItemBlockScarecrow(Block block) {
        super(block);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
        super.addInformation(stack, player, lines, advanced);
        int range = LegacyConfig.scarecrowRange;
        lines.add("\u00a77" + StatCollector.translateToLocalFormatted(
                "tooltips.touhou_little_maid.scarecrow.desc", range, range));
    }
}

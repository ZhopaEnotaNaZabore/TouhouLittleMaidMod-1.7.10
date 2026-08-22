package com.github.tartaricacid.touhoulittlemaid.block;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

/** Registry-compatible shell upgraded to a tile-backed block subsystem by subsystem. */
public class BlockLegacyDevice extends Block {
    public BlockLegacyDevice(String name, Material material) {
        super(material);
        setBlockName(TouhouLittleMaid.MOD_ID + "." + name);
        setBlockTextureName("minecraft:stone");
        setHardness(2.0F);
        setResistance(3.0F);
        setCreativeTab(CreativeTabs.tabDecorations);
    }
}

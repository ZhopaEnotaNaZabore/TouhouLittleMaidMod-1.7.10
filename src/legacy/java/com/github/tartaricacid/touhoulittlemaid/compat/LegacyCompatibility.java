package com.github.tartaricacid.touhoulittlemaid.compat;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.init.ModBlocks;
import com.github.tartaricacid.touhoulittlemaid.init.ModItems;
import cpw.mods.fml.common.Loader;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/** Dependency-free compatibility surface; optional mods are never linked directly. */
public final class LegacyCompatibility {
    private LegacyCompatibility() { }
    public static void init() {
        LegacyHoneyCompat.init();
        OreDictionary.registerOre("toolGohei",new ItemStack(ModItems.HAKUREI_GOHEI,1,OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre("toolGohei",new ItemStack(ModItems.SANAE_GOHEI,1,OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre("itemPowerPoint",new ItemStack(ModItems.POWER_POINT,1,OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre("blockMaidAltar",new ItemStack(ModBlocks.ALTAR));
        OreDictionary.registerOre("backpackMaid",new ItemStack(ModItems.MAID_BACKPACK_SMALL));
        OreDictionary.registerOre("backpackMaid",new ItemStack(ModItems.MAID_BACKPACK_MIDDLE));
        OreDictionary.registerOre("backpackMaid",new ItemStack(ModItems.MAID_BACKPACK_BIG));
        report("NotEnoughItems","NEI will display all GameRegistry recipes; altar recipes remain in-world");
        report("MineTweaker3","OreDictionary names expose core ingredients to MineTweaker/CraftTweaker");
        report("Thaumcraft","generic inventory/task behavior is enabled; no hard dependency");
        report("gregtech","OreDictionary recipes and maid item transport are enabled; no hard dependency");
        report("Baubles","maid accessories use the dedicated maid inventory, independent of player Baubles");
        if(LegacyHoneyCompat.usesExternalOutput())TouhouLittleMaid.LOGGER.info("Compatibility: honey task uses an external Forestry/Gendustry product");
        if(Loader.isModLoaded("gregtech")){
            int[] probe=com.github.tartaricacid.touhoulittlemaid.compat.miner.LegacyMiningToolCompat.oreDictionaryProbe();
            TouhouLittleMaid.LOGGER.info("Compatibility: miner recognized {}/{} registered GregTech mining-tool stacks",probe[1],probe[0]);
        }
    }
    private static void report(String id,String message){if(Loader.isModLoaded(id))TouhouLittleMaid.LOGGER.info("Compatibility: {} detected - {}",id,message);}
}

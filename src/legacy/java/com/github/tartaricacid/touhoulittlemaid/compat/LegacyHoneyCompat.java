package com.github.tartaricacid.touhoulittlemaid.compat;

import com.github.tartaricacid.touhoulittlemaid.init.ModItems;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/** Optional Forestry/Gendustry output bridge without linking either API. */
public final class LegacyHoneyCompat {
    private static ItemStack externalOutput;
    private LegacyHoneyCompat() { }
    public static void init() {
        if (Loader.isModLoaded("Forestry") || Loader.isModLoaded("forestry") || Loader.isModLoaded("gendustry")) {
            String[] ores={"dropHoney","dropHoneydew","foodHoney","itemHoney"};
            for(String ore:ores)for(ItemStack stack:OreDictionary.getOres(ore))if(stack!=null&&stack.getItem()!=ModItems.HONEY_BOTTLE){externalOutput=stack.copy();externalOutput.stackSize=1;break;}
            if(externalOutput==null){String[][] candidates={{"Forestry","honeyDrop"},{"Forestry","honeyedSlice"},{"forestry","honey_drop"},{"gendustry","HoneyComb"}};for(String[] id:candidates){Item item=GameRegistry.findItem(id[0],id[1]);if(item!=null){externalOutput=new ItemStack(item);break;}}}
        }
        OreDictionary.registerOre("foodHoney",new ItemStack(ModItems.HONEY_BOTTLE));
        OreDictionary.registerOre("dropHoney",new ItemStack(ModItems.HONEY_BOTTLE));
    }
    public static ItemStack createOutput(){return externalOutput==null?new ItemStack(ModItems.HONEY_BOTTLE):externalOutput.copy();}
    public static boolean usesExternalOutput(){return externalOutput!=null;}
}

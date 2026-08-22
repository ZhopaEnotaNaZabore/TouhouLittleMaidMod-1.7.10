package com.github.tartaricacid.touhoulittlemaid.init;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/** Vanilla 1.7 crafting equivalents for recipes that are not altar-only. */
public final class LegacyRecipes {
    private LegacyRecipes(){}
    public static void init(){
        GameRegistry.addRecipe(new ItemStack(ModItems.HAKUREI_GOHEI),"  D"," SP","S P",'D',Items.diamond,'S',Items.stick,'P',Items.paper);
        GameRegistry.addRecipe(new ItemStack(ModItems.SANAE_GOHEI)," PD"," SP","S  ",'D',Items.diamond,'S',Items.stick,'P',Items.paper);
        GameRegistry.addRecipe(new ItemStack(ModItems.CHAIR),"WWW","IPI",'W',new ItemStack(Blocks.wool,1,OreDictionary.WILDCARD_VALUE),'I',Items.iron_ingot,'P',new ItemStack(Blocks.planks,1,OreDictionary.WILDCARD_VALUE));
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.POWER_POINT),Items.redstone,Items.glowstone_dust);
        GameRegistry.addRecipe(new ItemStack(ModItems.CAMERA),"QQQ","QGQ","IRI",'Q',Items.quartz,'G',Blocks.glass_pane,'I',Items.iron_ingot,'R',Items.redstone);
        GameRegistry.addRecipe(new ItemStack(ModItems.SERVANT_BELL)," G ","GNG"," S ",'G',Items.gold_ingot,'N',Items.gold_nugget,'S',Items.stick);
        GameRegistry.addRecipe(new ItemStack(ModItems.TRUMPET)," GG","G I"," I ",'G',Items.gold_ingot,'I',Items.iron_ingot);
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.ENTITY_ID_COPY),Items.leather,Items.paper);
        GameRegistry.addRecipe(new ItemStack(ModItems.SMART_SLAB_INIT),"IRI","RDR","IRI",'I',Items.iron_ingot,'R',Items.redstone,'D',Items.diamond);
    }
}

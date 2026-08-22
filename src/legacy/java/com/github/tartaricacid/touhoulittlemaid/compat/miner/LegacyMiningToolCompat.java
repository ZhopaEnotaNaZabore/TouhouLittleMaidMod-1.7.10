package com.github.tartaricacid.touhoulittlemaid.compat.miner;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Set;
import java.lang.reflect.Method;

/** Dependency-free baseline; installed-mod adapters may refine these decisions. */
public final class LegacyMiningToolCompat {
    private LegacyMiningToolCompat() { }

    public static boolean isMiningTool(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return false;
        try {
            Set<String> classes = stack.getItem().getToolClasses(stack);
            if (classes != null && (classes.contains("pickaxe") || classes.contains("hammer"))) return true;
            for (int oreId : OreDictionary.getOreIDs(stack)) {
                String name = OreDictionary.getOreName(oreId);
                if ("craftingToolPickaxe".equals(name) || "craftingToolMiningDrill".equals(name)
                        || "craftingToolJackHammer".equals(name)) return true;
            }
            // GT5 and GT6 MetaTools encode the tool type in per-stack stats and
            // often expose no stable Forge tool class. Reflect only after the
            // registry namespace identifies GregTech, keeping it fully optional.
            return isGregTechMiningTool(stack);
        } catch (Throwable incompatibleOptionalMod) {
            return false;
        }
    }

    public static boolean canHarvest(ItemStack stack, Block block, int metadata) {
        if (!isMiningTool(stack) || block == null) return false;
        try {
            if (stack.getItem().canHarvestBlock(block, stack)) return true;
            String required = block.getHarvestTool(metadata);
            return required == null ? stack.getItem().getDigSpeed(stack, block, metadata) > 1.0F
                    : ForgeHooks.canToolHarvestBlock(block, metadata, stack);
        } catch (Throwable incompatibleOptionalMod) {
            return false;
        }
    }

    public static boolean isAreaTool(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return false;
        GameRegistry.UniqueIdentifier key = GameRegistry.findUniqueIdentifierFor(stack.getItem());
        if (key == null) return false;
        String mod = key.modId.toLowerCase(java.util.Locale.ROOT);
        String name = key.name.toLowerCase(java.util.Locale.ROOT);
        return ("tconstruct".equals(mod) && name.contains("hammer"))
                || ("gregtech".equals(mod) && name.contains("hammer"));
    }

    private static boolean isGregTechMiningTool(ItemStack stack) {
        GameRegistry.UniqueIdentifier key = GameRegistry.findUniqueIdentifierFor(stack.getItem());
        if (key == null || !"gregtech".equalsIgnoreCase(key.modId)) return false;
        try {
            Method getStats = stack.getItem().getClass().getMethod("getToolStats", ItemStack.class);
            Object stats = getStats.invoke(stack.getItem(), stack);
            if (stats == null) return false;
            Method mining = stats.getClass().getMethod("isMiningTool");
            return Boolean.TRUE.equals(mining.invoke(stats));
        } catch (Exception missingVersionSpecificApi) {
            return false;
        }
    }

    public static int[] oreDictionaryProbe() {
        String[] names = {"craftingToolPickaxe", "craftingToolMiningDrill",
                "craftingToolJackHammer", "craftingToolHardHammer"};
        int candidates = 0, accepted = 0;
        for (String name : names) for (ItemStack stack : OreDictionary.getOres(name)) {
            candidates++;
            if (isMiningTool(stack)) accepted++;
        }
        return new int[]{candidates, accepted};
    }
}

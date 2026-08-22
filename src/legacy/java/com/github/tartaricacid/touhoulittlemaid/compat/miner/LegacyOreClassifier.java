package com.github.tartaricacid.touhoulittlemaid.compat.miner;

import com.github.tartaricacid.touhoulittlemaid.config.LegacyConfig;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.oredict.OreDictionary;
import java.lang.reflect.Method;

/** Metadata-aware ore classification shared by the optional miner adapters. */
public final class LegacyOreClassifier {
    private static final String GT_RETRIEVABLE = "gregapi.block.IBlockRetrievable";
    private static volatile Method gtStackMethod;
    private static volatile boolean gtStackMethodUnavailable;

    private LegacyOreClassifier() { }

    public static boolean isOre(World world, int x, int y, int z, Block block, int metadata) {
        if (block == null || block == Blocks.air) return false;
        String id = registryId(block);
        if (matches(LegacyConfig.minerOreDenyList, id, metadata)) return false;
        if (matches(LegacyConfig.minerOreAllowList, id, metadata)) return true;
        boolean gregTechPrefixOre = isGregTechPrefixBlock(block);
        // GT6 PrefixBlock ores intentionally use a tiny TileEntity to store their
        // extended material metadata. Other machines/inventories remain denied.
        if (!gregTechPrefixOre && (block.hasTileEntity(metadata) || world.getTileEntity(x, y, z) != null)) return false;
        if (isVanillaOre(block)) return true;
        ItemStack state = gregTechPrefixOre ? exactGregTechState(world, x, y, z, block)
                : stackFor(block, metadata);
        if (state == null) return false;
        for (int oreId : OreDictionary.getOreIDs(state)) {
            String name = OreDictionary.getOreName(oreId);
            if (isOreDictionaryName(name)) return true;
        }
        return false;
    }

    private static ItemStack stackFor(Block block, int metadata) {
        Item item = Item.getItemFromBlock(block);
        return item == null ? null : new ItemStack(item, 1, metadata);
    }

    private static boolean isGregTechPrefixBlock(Block block) {
        Class<?> type = block.getClass();
        while (type != null) {
            if (type.getName().startsWith("gregapi.block.prefixblock.PrefixBlock")) return true;
            for (Class<?> contract : type.getInterfaces())
                if ("gregapi.block.IPrefixBlock".equals(contract.getName())) return true;
            type = type.getSuperclass();
        }
        return false;
    }

    private static ItemStack exactGregTechState(World world, int x, int y, int z, Block block) {
        try {
            Method method = gtStackMethod;
            if (method == null) {
                if (gtStackMethodUnavailable) return null;
                Class<?> contract = Class.forName(GT_RETRIEVABLE, false, block.getClass().getClassLoader());
                if (!contract.isInstance(block)) return null;
                // Resolve the method on GT6's server-safe interface. Calling
                // getMethod on PrefixBlock itself makes the JVM resolve all of
                // its public methods, including registerBlockIcons(IIconRegister),
                // which crashes a dedicated server where client classes do not exist.
                method = contract.getMethod("getItemStackFromBlock",
                        IBlockAccess.class, Integer.TYPE, Integer.TYPE, Integer.TYPE, Byte.TYPE);
                gtStackMethod = method;
            }
            return (ItemStack) method.invoke(block, world, x, y, z, (byte) 1);
        } catch (ClassNotFoundException unsupportedGtVersion) {
            gtStackMethodUnavailable = true;
            return null;
        } catch (ReflectiveOperationException unsupportedGtVersion) {
            return null;
        } catch (LinkageError unsafeOptionalDependency) {
            gtStackMethodUnavailable = true;
            return null;
        }
    }

    public static boolean isOreDictionaryName(String name) {
        if (name == null) return false;
        String lower = name.toLowerCase(java.util.Locale.ROOT);
        return lower.startsWith("ore") || lower.startsWith("denseore")
                || lower.startsWith("poorore") || lower.startsWith("smallore");
    }

    private static boolean isVanillaOre(Block block) {
        return block == Blocks.coal_ore || block == Blocks.iron_ore || block == Blocks.gold_ore
                || block == Blocks.lapis_ore || block == Blocks.redstone_ore
                || block == Blocks.lit_redstone_ore || block == Blocks.diamond_ore
                || block == Blocks.emerald_ore || block == Blocks.quartz_ore;
    }

    private static String registryId(Block block) {
        GameRegistry.UniqueIdentifier key = GameRegistry.findUniqueIdentifierFor(block);
        return key == null ? "" : (key.modId + ":" + key.name).toLowerCase(java.util.Locale.ROOT);
    }

    private static boolean matches(String[] entries, String id, int metadata) {
        if (entries == null || id.isEmpty()) return false;
        for (String raw : entries) {
            if (raw == null) continue;
            String entry = raw.trim().toLowerCase(java.util.Locale.ROOT);
            if (entry.equals(id) || entry.equals(id + "@*") || entry.equals(id + "@" + metadata)) return true;
        }
        return false;
    }
}

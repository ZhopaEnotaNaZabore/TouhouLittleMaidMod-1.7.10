package com.github.tartaricacid.touhoulittlemaid.crafting;

import com.github.tartaricacid.touhoulittlemaid.init.ModBlocks;
import com.github.tartaricacid.touhoulittlemaid.init.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Built-in equivalents of the supported 1.20 altar JSON recipes. Ingredients are shapeless. */
public final class LegacyAltarRecipes {
    public static final class Recipe {
        final ItemStack output; final int cost; final Ingredient[] ingredients; final boolean lightning;
        Recipe(ItemStack output, int cost, Ingredient... ingredients) { this(output,cost,false,ingredients); }
        Recipe(ItemStack output,int cost,boolean lightning,Ingredient...ingredients){this.output=output;this.cost=cost;this.lightning=lightning;this.ingredients=ingredients;}
        public ItemStack output() { return output==null?null:output.copy(); } public int cost() { return cost; } public boolean isLightning(){return lightning;}
    }
    private static final class Ingredient {
        final Item item; final int meta;
        Ingredient(Item item, int meta) { this.item = item; this.meta = meta; }
        boolean matches(ItemStack stack) { return stack != null && stack.getItem() == item && (meta < 0 || stack.getItemDamage() == meta); }
    }
    private static Ingredient i(Item item) { return new Ingredient(item, -1); }
    private static Ingredient i(Item item, int meta) { return new Ingredient(item, meta); }
    private static Ingredient b(net.minecraft.block.Block block) { return i(Item.getItemFromBlock(block)); }
    private static ItemStack out(net.minecraft.block.Block block) { return new ItemStack(block); }
    private static final Ingredient PLANK = b(Blocks.planks);
    private static final Ingredient STICK = i(Items.stick);
    private static final List<Recipe> RECIPES = Arrays.asList(
        new Recipe(out(ModBlocks.BOOKSHELF), 100, PLANK, PLANK, PLANK, PLANK, i(Items.book), i(Items.diamond)),
        new Recipe(new ItemStack(ModItems.BROOM), 200, b(Blocks.hay_block), b(Blocks.hay_block), b(Blocks.hay_block), STICK, STICK, i(Items.ender_eye)),
        new Recipe(out(ModBlocks.CCHESS), 100, PLANK, PLANK, PLANK, i(Items.dye, 0), i(Items.dye, 1), i(Items.diamond)),
        new Recipe(out(ModBlocks.COMPUTER), 100, PLANK, PLANK, PLANK, b(Blocks.noteblock), b(Blocks.lever), i(Items.diamond)),
        new Recipe(new ItemStack(ModItems.EXTINGUISHER), 200, i(Items.clay_ball), i(Items.clay_ball), i(Items.clay_ball), i(Items.clay_ball), i(Items.iron_ingot), i(Items.dye, 1)),
        new Recipe(out(ModBlocks.GOMOKU), 100, PLANK, PLANK, PLANK, i(Items.dye, 0), i(Items.dye, 15), i(Items.diamond)),
        new Recipe(new ItemStack(ModItems.HAKUREI_GOHEI), 150, STICK, STICK, STICK, i(Items.paper), i(Items.paper), i(Items.paper)),
        new Recipe(out(ModBlocks.KEYBOARD), 100, PLANK, PLANK, PLANK, PLANK, b(Blocks.noteblock), i(Items.diamond)),
        new Recipe(out(ModBlocks.MAID_BEACON), 200, PLANK, i(Items.dye, 1), PLANK, b(Blocks.obsidian), i(Items.diamond), b(Blocks.obsidian)),
        new Recipe(out(ModBlocks.MAID_BED), 200, b(Blocks.wool), b(Blocks.wool), b(Blocks.wool), PLANK, PLANK, PLANK),
        new Recipe(new ItemStack(ModItems.SANAE_GOHEI), 150, STICK, STICK, STICK, STICK, i(Items.paper), i(Items.paper)),
        new Recipe(out(ModBlocks.SCARECROW), 200, b(Blocks.hay_block), b(Blocks.hay_block), i(Item.getItemFromBlock(Blocks.stone), 0), i(Item.getItemFromBlock(Blocks.stone), 0), i(Items.redstone), i(Items.redstone)),
        new Recipe(out(ModBlocks.WCHESS), 100, PLANK, PLANK, PLANK, i(Items.dye, 0), i(Items.dye, 15), i(Items.emerald)),
        new Recipe(new ItemStack(ModItems.SPAWN_BOX), 500, i(Items.diamond), i(Items.dye, 4), i(Items.gold_ingot), i(Items.redstone), i(Items.iron_ingot), i(Items.coal, 0)),
        new Recipe(new ItemStack(ModItems.CHISEL), 200, STICK, STICK, i(Items.iron_ingot), i(Items.iron_ingot), i(Items.dye,11), i(Items.dye,1)),
        new Recipe(new ItemStack(ModItems.KAPPA_COMPASS), 100, b(Blocks.obsidian), b(Blocks.obsidian), b(Blocks.obsidian), i(Items.dye,6), i(Items.redstone), i(Items.redstone)),
        new Recipe(new ItemStack(ModItems.WIRELESS_IO), 200, i(Items.ender_pearl), b(Blocks.chest), b(Blocks.hopper)),
        new Recipe(new ItemStack(ModItems.RED_FOX_SCROLL), 100, i(Items.paper), i(Items.paper), i(Items.paper), i(Items.paper), i(Items.dye,1), i(Items.diamond)),
        new Recipe(new ItemStack(ModItems.WHITE_FOX_SCROLL), 100, i(Items.paper), i(Items.paper), i(Items.paper), i(Items.paper), i(Items.dye,15), i(Items.diamond)),
        new Recipe(new ItemStack(ModItems.CAMERA),200,b(Blocks.quartz_block),b(Blocks.quartz_block),b(Blocks.quartz_block),b(Blocks.quartz_block),b(Blocks.obsidian),b(Blocks.obsidian)),
        new Recipe(new ItemStack(ModItems.CRAFTING_TABLE_BACKPACK),200,i(ModItems.MAID_BACKPACK_MIDDLE),b(Blocks.crafting_table)),
        new Recipe(new ItemStack(ModItems.ENDER_CHEST_BACKPACK),200,i(ModItems.MAID_BACKPACK_MIDDLE),b(Blocks.ender_chest)),
        new Recipe(new ItemStack(ModItems.FURNACE_BACKPACK),200,i(ModItems.MAID_BACKPACK_MIDDLE),b(Blocks.furnace)),
        new Recipe(new ItemStack(ModItems.TANK_BACKPACK),200,i(ModItems.MAID_BACKPACK_MIDDLE),i(Items.bucket)),
        new Recipe(new ItemStack(ModItems.MAID_BACKPACK_SMALL),100,i(Item.getItemFromBlock(Blocks.wool),14),i(Item.getItemFromBlock(Blocks.wool),14),i(Item.getItemFromBlock(Blocks.wool),14),i(Item.getItemFromBlock(Blocks.wool),14),i(Items.iron_ingot),i(Item.getItemFromBlock(Blocks.wool),14)),
        new Recipe(new ItemStack(ModItems.MAID_BACKPACK_MIDDLE),200,i(Item.getItemFromBlock(Blocks.wool),6),i(Item.getItemFromBlock(Blocks.wool),6),i(Item.getItemFromBlock(Blocks.wool),6),i(Item.getItemFromBlock(Blocks.wool),6),i(Items.gold_ingot),i(Item.getItemFromBlock(Blocks.wool),6)),
        new Recipe(new ItemStack(ModItems.MAID_BACKPACK_BIG),300,i(Item.getItemFromBlock(Blocks.wool),7),i(Item.getItemFromBlock(Blocks.wool),7),i(Item.getItemFromBlock(Blocks.wool),7),i(Item.getItemFromBlock(Blocks.wool),7),i(Items.diamond),i(Item.getItemFromBlock(Blocks.wool),7)),
        new Recipe(new ItemStack(ModItems.DROWN_PROTECT_BAUBLE),200,i(Items.nether_wart),i(Items.dye,10),i(Items.fish),i(Items.fish),i(Items.fish),i(Items.fish)),
        new Recipe(new ItemStack(ModItems.EXPLOSION_PROTECT_BAUBLE),200,i(Items.nether_wart),i(Items.dye,14),b(Blocks.obsidian),b(Blocks.obsidian),b(Blocks.obsidian),b(Blocks.obsidian)),
        new Recipe(new ItemStack(ModItems.FALL_PROTECT_BAUBLE),200,i(Items.nether_wart),i(Items.dye,11),i(Items.feather),i(Items.feather),i(Items.feather),i(Items.feather)),
        new Recipe(new ItemStack(ModItems.FIRE_PROTECT_BAUBLE),200,i(Items.nether_wart),i(Items.dye,1),i(Items.blaze_powder),i(Items.blaze_powder),i(Items.blaze_powder),i(Items.blaze_powder)),
        new Recipe(new ItemStack(ModItems.MAGIC_PROTECT_BAUBLE),200,i(Items.nether_wart),i(Items.dye,6),i(Items.sugar),i(Items.sugar),i(Items.sugar),i(Items.sugar)),
        new Recipe(new ItemStack(ModItems.PROJECTILE_PROTECT_BAUBLE),200,i(Items.nether_wart),i(Items.dye,4),i(Items.iron_ingot),i(Items.iron_ingot),i(Items.iron_ingot),i(Items.iron_ingot)),
        new Recipe(new ItemStack(ModItems.ITEM_MAGNET_BAUBLE),200,i(Items.redstone),i(Items.redstone),i(Items.redstone),i(Items.iron_ingot),i(Items.iron_ingot),i(Items.iron_ingot)),
        new Recipe(new ItemStack(ModItems.MUTE_BAUBLE),200,b(Blocks.wool),b(Blocks.wool),b(Blocks.wool),i(Items.clay_ball),i(Items.clay_ball),i(Items.clay_ball)),
        new Recipe(new ItemStack(ModItems.NIMBLE_FABRIC),200,i(Items.ender_pearl),i(Items.ender_pearl),i(Items.ender_pearl),b(Blocks.wool),b(Blocks.wool),b(Blocks.wool)),
        new Recipe(new ItemStack(ModItems.ULTRAMARINE_ORB_ELIXIR),300,i(Items.emerald),i(Items.ender_pearl),i(Items.dye,6),i(Items.dye,6),i(Items.dye,6),i(Items.dye,6)),
        new Recipe(new ItemStack(ModItems.TRUMPET),200,i(Items.gold_ingot),i(Items.gold_ingot),i(Items.iron_ingot),i(Items.iron_ingot),i(Items.iron_ingot),b(Blocks.noteblock)),
        new Recipe(new ItemStack(ModItems.SERVANT_BELL),200,i(Items.gold_ingot),i(Items.gold_ingot),i(Items.gold_nugget),i(Items.gold_nugget),STICK,STICK),
        new Recipe(out(ModBlocks.PICNIC_MAT),200,b(Blocks.chest),i(Items.reeds),i(Items.reeds),i(Items.reeds),i(Items.reeds),b(Blocks.carpet)),
        new Recipe(out(ModBlocks.SNACK_CABINET),100,b(Blocks.chest),PLANK,PLANK,PLANK,b(Blocks.glass_pane),b(Blocks.glass_pane)),
        new Recipe(null,200,true,i(Items.gunpowder),i(Items.gunpowder),i(Items.gunpowder),i(Items.blaze_powder),i(Items.blaze_powder),i(Items.blaze_powder))
    );

    private LegacyAltarRecipes() { }
    public static int recipeCount(){return RECIPES.size();}
    public static Recipe find(ItemStack[] inputs) {
        int count = 0; for (ItemStack stack : inputs) if (stack != null) count++;
        for (Recipe recipe : RECIPES) {
            if (recipe.ingredients.length != count) continue;
            List<ItemStack> remaining = new ArrayList<ItemStack>();
            for (ItemStack stack : inputs) if (stack != null) remaining.add(stack);
            boolean ok = true;
            for (Ingredient ingredient : recipe.ingredients) {
                int found = -1; for (int n = 0; n < remaining.size(); n++) if (ingredient.matches(remaining.get(n))) { found = n; break; }
                if (found < 0) { ok = false; break; } remaining.remove(found);
            }
            if (ok) return recipe;
        }
        return null;
    }
}

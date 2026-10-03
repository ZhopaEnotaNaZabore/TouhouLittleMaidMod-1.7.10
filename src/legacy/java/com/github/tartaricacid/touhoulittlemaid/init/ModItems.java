package com.github.tartaricacid.touhoulittlemaid.init;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.item.ItemExtinguisher;
import com.github.tartaricacid.touhoulittlemaid.item.ItemPowerPoint;
import com.github.tartaricacid.touhoulittlemaid.item.ItemHakureiGohei;
import com.github.tartaricacid.touhoulittlemaid.item.ItemChair;
import com.github.tartaricacid.touhoulittlemaid.item.ItemBroom;
import com.github.tartaricacid.touhoulittlemaid.item.ItemSpawnBox;
import com.github.tartaricacid.touhoulittlemaid.item.ItemFilm;
import com.github.tartaricacid.touhoulittlemaid.item.ItemMaidBauble;
import com.github.tartaricacid.touhoulittlemaid.item.ItemMaidBackpack;
import com.github.tartaricacid.touhoulittlemaid.item.ItemCamera;
import com.github.tartaricacid.touhoulittlemaid.item.ItemPhoto;
import com.github.tartaricacid.touhoulittlemaid.item.ItemServantBell;
import com.github.tartaricacid.touhoulittlemaid.item.ItemTrumpet;
import com.github.tartaricacid.touhoulittlemaid.item.ItemLegacyRangedWeapon;
import com.github.tartaricacid.touhoulittlemaid.item.ItemBoardState;
import com.github.tartaricacid.touhoulittlemaid.item.ItemMaidPainting;
import com.github.tartaricacid.touhoulittlemaid.item.ItemChisel;
import com.github.tartaricacid.touhoulittlemaid.item.ItemSmartSlab;
import com.github.tartaricacid.touhoulittlemaid.item.ItemFavorabilityTool;
import com.github.tartaricacid.touhoulittlemaid.item.ItemKappaCompass;
import com.github.tartaricacid.touhoulittlemaid.item.ItemWirelessIO;
import com.github.tartaricacid.touhoulittlemaid.item.ItemLegacySpawnEgg;
import com.github.tartaricacid.touhoulittlemaid.item.ItemFoxScroll;
import com.github.tartaricacid.touhoulittlemaid.item.ItemEntityIdCopy;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import java.lang.reflect.Field;

public final class ModItems {
    public static final com.github.tartaricacid.touhoulittlemaid.item.ItemAnimationGun ANIMATION_RIFLE = new com.github.tartaricacid.touhoulittlemaid.item.ItemAnimationGun("rifle");
    public static final com.github.tartaricacid.touhoulittlemaid.item.ItemAnimationGun ANIMATION_PISTOL = new com.github.tartaricacid.touhoulittlemaid.item.ItemAnimationGun("pistol");
    public static final com.github.tartaricacid.touhoulittlemaid.item.ItemAnimationGun ANIMATION_RPG = new com.github.tartaricacid.touhoulittlemaid.item.ItemAnimationGun("rpg");
    public static final ItemExtinguisher EXTINGUISHER = new ItemExtinguisher();
    public static final Item OWNER_CONVERSION_TOOL = simpleItem("owner_conversion_tool", 1);
    public static final ItemPowerPoint POWER_POINT = new ItemPowerPoint();
    public static final ItemHakureiGohei HAKUREI_GOHEI = new ItemHakureiGohei("hakurei_gohei");
    public static final ItemHakureiGohei SANAE_GOHEI = new ItemHakureiGohei("sanae_gohei");
    public static final ItemChair CHAIR = new ItemChair();
    public static final ItemBroom BROOM = new ItemBroom();
    public static final ItemSpawnBox SPAWN_BOX = new ItemSpawnBox();
    public static final ItemFilm FILM = new ItemFilm();
    public static final ItemMaidBauble DROWN_PROTECT_BAUBLE = bauble("drown_protect_bauble", ItemMaidBauble.Type.DROWN, 6);
    public static final ItemMaidBauble EXPLOSION_PROTECT_BAUBLE = bauble("explosion_protect_bauble", ItemMaidBauble.Type.EXPLOSION, 6);
    public static final ItemMaidBauble ULTRAMARINE_ORB_ELIXIR = bauble("ultramarine_orb_elixir", ItemMaidBauble.Type.EXTRA_LIFE, 6);
    public static final ItemMaidBauble FALL_PROTECT_BAUBLE = bauble("fall_protect_bauble", ItemMaidBauble.Type.FALL, 6);
    public static final ItemMaidBauble FIRE_PROTECT_BAUBLE = bauble("fire_protect_bauble", ItemMaidBauble.Type.FIRE, 6);
    public static final ItemMaidBauble ITEM_MAGNET_BAUBLE = bauble("item_magnet_bauble", ItemMaidBauble.Type.MAGNET, 0);
    public static final ItemMaidBauble MAGIC_PROTECT_BAUBLE = bauble("magic_protect_bauble", ItemMaidBauble.Type.MAGIC, 6);
    public static final ItemMaidBauble NIMBLE_FABRIC = bauble("nimble_fabric", ItemMaidBauble.Type.NIMBLE, 6);
    public static final ItemMaidBauble PROJECTILE_PROTECT_BAUBLE = bauble("projectile_protect_bauble", ItemMaidBauble.Type.PROJECTILE, 6);
    public static final ItemMaidBauble MUTE_BAUBLE = bauble("mute_bauble", ItemMaidBauble.Type.MUTE, 0);
    public static final ItemMaidBackpack MAID_BACKPACK_SMALL = backpack("maid_backpack_small", 12);
    public static final ItemMaidBackpack MAID_BACKPACK_MIDDLE = backpack("maid_backpack_middle", 24);
    public static final ItemMaidBackpack MAID_BACKPACK_BIG = backpack("maid_backpack_big", 36);
    public static final ItemMaidBackpack CRAFTING_TABLE_BACKPACK = backpack("crafting_table_backpack", 18);
    public static final ItemMaidBackpack ENDER_CHEST_BACKPACK = backpack("ender_chest_backpack", 6);
    public static final ItemMaidBackpack FURNACE_BACKPACK = backpack("furnace_backpack", 18);
    public static final ItemMaidBackpack TANK_BACKPACK = backpack("tank_backpack", 18);
    public static final ItemCamera CAMERA = new ItemCamera();
    public static final ItemPhoto PHOTO = new ItemPhoto();
    public static final ItemServantBell SERVANT_BELL = new ItemServantBell();
    public static final ItemTrumpet TRUMPET = new ItemTrumpet();
    public static final Item SUBSTITUTE_JIZO = simpleItem("substitute_jizo", 1);
    public static final ItemLegacyRangedWeapon CROSSBOW = new ItemLegacyRangedWeapon("crossbow", ItemLegacyRangedWeapon.Type.CROSSBOW, 326);
    public static final ItemLegacyRangedWeapon TRIDENT = new ItemLegacyRangedWeapon("trident", ItemLegacyRangedWeapon.Type.TRIDENT, 250);
    public static final Item HONEY_BOTTLE = simpleItem("honey_bottle", 16);
    public static final ItemBoardState GOMOKU_BOARD_STATE=new ItemBoardState("gomoku_board_state",ItemBoardState.Type.GOMOKU);
    public static final ItemBoardState CCHESS_BOARD_STATE=new ItemBoardState("cchess_board_state",ItemBoardState.Type.CCHESS);
    public static final ItemBoardState WCHESS_BOARD_STATE=new ItemBoardState("wchess_board_state",ItemBoardState.Type.WCHESS);
    public static final ItemMaidPainting WINE_FOX_PAINTING=new ItemMaidPainting();
    public static final ItemChisel CHISEL=new ItemChisel();
    public static final ItemSmartSlab SMART_SLAB_INIT=new ItemSmartSlab("smart_slab_empty",ItemSmartSlab.Type.INIT);
    public static final ItemSmartSlab SMART_SLAB_EMPTY=new ItemSmartSlab("smart_slab_empty",ItemSmartSlab.Type.EMPTY);
    public static final ItemSmartSlab SMART_SLAB_HAS_MAID=new ItemSmartSlab("smart_slab_has_maid",ItemSmartSlab.Type.HAS_MAID);
    public static final ItemFavorabilityTool FAVORABILITY_TOOL_ADD=new ItemFavorabilityTool("favorability_tool_add",ItemFavorabilityTool.Type.ADD);
    public static final ItemFavorabilityTool FAVORABILITY_TOOL_REDUCE=new ItemFavorabilityTool("favorability_tool_reduce",ItemFavorabilityTool.Type.REDUCE);
    public static final ItemFavorabilityTool FAVORABILITY_TOOL_FULL=new ItemFavorabilityTool("favorability_tool_full",ItemFavorabilityTool.Type.FULL);
    public static final ItemKappaCompass KAPPA_COMPASS=new ItemKappaCompass();
    public static final ItemWirelessIO WIRELESS_IO=new ItemWirelessIO();
    public static final ItemLegacySpawnEgg MAID_SPAWN_EGG=new ItemLegacySpawnEgg("maid_spawn_egg",ItemLegacySpawnEgg.Type.MAID);
    public static final ItemLegacySpawnEgg FAIRY_SPAWN_EGG=new ItemLegacySpawnEgg("fairy_spawn_egg",ItemLegacySpawnEgg.Type.FAIRY);
    public static final ItemFoxScroll RED_FOX_SCROLL=new ItemFoxScroll("red_fox_scroll",ItemFoxScroll.Type.MAIDS);
    public static final ItemFoxScroll WHITE_FOX_SCROLL=new ItemFoxScroll("white_fox_scroll",ItemFoxScroll.Type.TOMBSTONES);
    public static final ItemEntityIdCopy ENTITY_ID_COPY=new ItemEntityIdCopy();
    public static final Item MONSTER_LIST=simpleItem("monster_list",1);

    private ModItems() {
    }

    public static void init() {
        assignCreativeTab();
        GameRegistry.registerItem(EXTINGUISHER, "extinguisher");
        GameRegistry.registerItem(OWNER_CONVERSION_TOOL, "owner_conversion_tool");
        GameRegistry.registerItem(POWER_POINT, "power_point");
        GameRegistry.registerItem(HAKUREI_GOHEI, "hakurei_gohei");
        GameRegistry.registerItem(SANAE_GOHEI, "sanae_gohei");
        GameRegistry.registerItem(CHAIR, "chair");
        GameRegistry.registerItem(BROOM, "broom");
        GameRegistry.registerItem(SPAWN_BOX, "spawn_box");
        GameRegistry.registerItem(FILM, "film");
        GameRegistry.registerItem(ANIMATION_RIFLE, "animation_rifle");
        GameRegistry.registerItem(ANIMATION_PISTOL, "animation_pistol");
        GameRegistry.registerItem(ANIMATION_RPG, "animation_rpg");
        GameRegistry.registerItem(DROWN_PROTECT_BAUBLE, "drown_protect_bauble");
        GameRegistry.registerItem(EXPLOSION_PROTECT_BAUBLE, "explosion_protect_bauble");
        GameRegistry.registerItem(ULTRAMARINE_ORB_ELIXIR, "ultramarine_orb_elixir");
        GameRegistry.registerItem(FALL_PROTECT_BAUBLE, "fall_protect_bauble");
        GameRegistry.registerItem(FIRE_PROTECT_BAUBLE, "fire_protect_bauble");
        GameRegistry.registerItem(ITEM_MAGNET_BAUBLE, "item_magnet_bauble");
        GameRegistry.registerItem(MAGIC_PROTECT_BAUBLE, "magic_protect_bauble");
        GameRegistry.registerItem(NIMBLE_FABRIC, "nimble_fabric");
        GameRegistry.registerItem(PROJECTILE_PROTECT_BAUBLE, "projectile_protect_bauble");
        GameRegistry.registerItem(MUTE_BAUBLE, "mute_bauble");
        GameRegistry.registerItem(MAID_BACKPACK_SMALL, "maid_backpack_small");
        GameRegistry.registerItem(MAID_BACKPACK_MIDDLE, "maid_backpack_middle");
        GameRegistry.registerItem(MAID_BACKPACK_BIG, "maid_backpack_big");
        GameRegistry.registerItem(CRAFTING_TABLE_BACKPACK, "crafting_table_backpack");
        GameRegistry.registerItem(ENDER_CHEST_BACKPACK, "ender_chest_backpack");
        GameRegistry.registerItem(FURNACE_BACKPACK, "furnace_backpack");
        GameRegistry.registerItem(TANK_BACKPACK, "tank_backpack");
        GameRegistry.registerItem(CAMERA, "camera"); GameRegistry.registerItem(PHOTO, "photo");
        GameRegistry.registerItem(SERVANT_BELL, "servant_bell"); GameRegistry.registerItem(TRUMPET, "trumpet");
        GameRegistry.registerItem(SUBSTITUTE_JIZO, "substitute_jizo");
        GameRegistry.registerItem(CROSSBOW,"crossbow");GameRegistry.registerItem(TRIDENT,"trident");GameRegistry.registerItem(HONEY_BOTTLE,"honey_bottle");
        GameRegistry.registerItem(GOMOKU_BOARD_STATE,"gomoku_board_state");GameRegistry.registerItem(CCHESS_BOARD_STATE,"cchess_board_state");GameRegistry.registerItem(WCHESS_BOARD_STATE,"wchess_board_state");
        GameRegistry.registerItem(WINE_FOX_PAINTING,"wine_fox_painting");
        GameRegistry.registerItem(CHISEL,"chisel");
        GameRegistry.registerItem(SMART_SLAB_INIT,"smart_slab_init");GameRegistry.registerItem(SMART_SLAB_EMPTY,"smart_slab_empty");GameRegistry.registerItem(SMART_SLAB_HAS_MAID,"smart_slab_has_maid");
        GameRegistry.registerItem(FAVORABILITY_TOOL_ADD,"favorability_tool_add");GameRegistry.registerItem(FAVORABILITY_TOOL_REDUCE,"favorability_tool_reduce");GameRegistry.registerItem(FAVORABILITY_TOOL_FULL,"favorability_tool_full");
        GameRegistry.registerItem(KAPPA_COMPASS,"kappa_compass");GameRegistry.registerItem(WIRELESS_IO,"wireless_io");
        GameRegistry.registerItem(MAID_SPAWN_EGG,"maid_spawn_egg");GameRegistry.registerItem(FAIRY_SPAWN_EGG,"fairy_spawn_egg");
        GameRegistry.registerItem(RED_FOX_SCROLL,"red_fox_scroll");GameRegistry.registerItem(WHITE_FOX_SCROLL,"white_fox_scroll");GameRegistry.registerItem(ENTITY_ID_COPY,"entity_id_copy");GameRegistry.registerItem(MONSTER_LIST,"monster_list");
    }

    private static void assignCreativeTab() {
        try {
            for (Field field : ModItems.class.getFields()) if (Item.class.isAssignableFrom(field.getType())) {
                Item item = (Item) field.get(null);
                if (item != null) item.setCreativeTab(ModCreativeTabs.TLM);
            }
        } catch (IllegalAccessException error) {
            throw new IllegalStateException("Could not populate TLM creative tab", error);
        }
    }

    private static Item simpleItem(String name, int stackSize) {
        return new Item().setUnlocalizedName(TouhouLittleMaid.MOD_ID + "." + name)
                .setTextureName(TouhouLittleMaid.MOD_ID + ":" + name)
                .setMaxStackSize(stackSize).setCreativeTab(CreativeTabs.tabMisc);
    }
    private static ItemMaidBauble bauble(String name, ItemMaidBauble.Type type, int durability) {
        return new ItemMaidBauble(name, type, durability);
    }
    private static ItemMaidBackpack backpack(String name, int capacity) { return new ItemMaidBackpack(name, capacity); }
}

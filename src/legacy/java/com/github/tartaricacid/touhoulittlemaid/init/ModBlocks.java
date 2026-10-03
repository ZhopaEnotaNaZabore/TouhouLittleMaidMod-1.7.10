package com.github.tartaricacid.touhoulittlemaid.init;

import com.github.tartaricacid.touhoulittlemaid.block.BlockLegacyDevice;
import com.github.tartaricacid.touhoulittlemaid.block.BlockScarecrow;
import com.github.tartaricacid.touhoulittlemaid.block.BlockJoy;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityKeyboard;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityBookshelf;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityComputer;
import com.github.tartaricacid.touhoulittlemaid.block.BlockInventoryDevice;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityShrine;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityPicnicMat;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntitySnackCabinet;
import com.github.tartaricacid.touhoulittlemaid.block.BlockMaidBed;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityMaidBed;
import com.github.tartaricacid.touhoulittlemaid.block.BlockGarageKit;
import com.github.tartaricacid.touhoulittlemaid.block.BlockStatue;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityGarageKit;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityStatue;
import com.github.tartaricacid.touhoulittlemaid.block.BlockMaidBeacon;
import com.github.tartaricacid.touhoulittlemaid.block.BlockModelSwitcher;
import com.github.tartaricacid.touhoulittlemaid.block.BlockBoardGame;
import com.github.tartaricacid.touhoulittlemaid.block.BlockBoardProxy;
import com.github.tartaricacid.touhoulittlemaid.block.BlockAltar;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityMaidBeacon;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityModelSwitcher;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityGomoku;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityCChess;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityWChess;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityAltar;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityScarecrow;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import java.lang.reflect.Field;
import com.github.tartaricacid.touhoulittlemaid.item.ItemBlockModelSwitcher;
import com.github.tartaricacid.touhoulittlemaid.item.ItemBlockMaidBeacon;
import com.github.tartaricacid.touhoulittlemaid.item.ItemBlockScarecrow;

public final class ModBlocks {
    public static final Block MAID_BED = new BlockMaidBed();
    public static final Block ALTAR = new BlockAltar();
    public static final Block STATUE = new BlockStatue();
    public static final Block GARAGE_KIT = new BlockGarageKit();
    public static final Block MAID_BEACON = new BlockMaidBeacon();
    public static final Block MODEL_SWITCHER = new BlockModelSwitcher();
    public static final Block PICNIC_MAT = new com.github.tartaricacid.touhoulittlemaid.block.BlockPicnicMat();
    public static final Block GOMOKU = new BlockBoardGame("gomoku", BlockBoardGame.Type.GOMOKU);
    public static final Block CCHESS = new BlockBoardGame("cchess", BlockBoardGame.Type.CCHESS);
    public static final Block WCHESS = new BlockBoardGame("wchess", BlockBoardGame.Type.WCHESS);
    public static final Block BOARD_PROXY = new BlockBoardProxy();
    public static final Block KEYBOARD = new BlockJoy("keyboard", BlockJoy.Type.KEYBOARD);
    public static final Block BOOKSHELF = new BlockJoy("bookshelf", BlockJoy.Type.BOOKSHELF);
    public static final Block COMPUTER = new BlockJoy("computer", BlockJoy.Type.COMPUTER);
    public static final Block SHRINE = new BlockInventoryDevice("shrine", BlockInventoryDevice.Type.SHRINE);
    public static final BlockScarecrow SCARECROW = new BlockScarecrow();
    public static final Block SNACK_CABINET = new BlockInventoryDevice("snack_cabinet", BlockInventoryDevice.Type.SNACK_CABINET);

    private ModBlocks() {
    }

    private static Block device(String name, Material material) { return new BlockLegacyDevice(name, material); }

    public static void init() {
        assignCreativeTab();
        register(MAID_BED, "maid_bed"); register(ALTAR, "altar"); register(STATUE, "statue");
        register(GARAGE_KIT, "garage_kit"); GameRegistry.registerBlock(MAID_BEACON, ItemBlockMaidBeacon.class, "maid_beacon");
        GameRegistry.registerBlock(MODEL_SWITCHER, ItemBlockModelSwitcher.class, "model_switcher"); GameRegistry.registerBlock(PICNIC_MAT, com.github.tartaricacid.touhoulittlemaid.item.ItemBlockPicnicMat.class, "picnic_mat");
        register(GOMOKU, "gomoku"); register(CCHESS, "cchess"); register(WCHESS, "wchess");
        register(BOARD_PROXY, "board_proxy");
        register(KEYBOARD, "keyboard"); register(BOOKSHELF, "bookshelf"); register(COMPUTER, "computer");
        register(SHRINE, "shrine"); GameRegistry.registerBlock(SCARECROW, ItemBlockScarecrow.class, "scarecrow"); register(SNACK_CABINET, "snack_cabinet");
        GameRegistry.registerTileEntity(TileEntityKeyboard.class, "touhou_little_maid:keyboard");
        GameRegistry.registerTileEntity(TileEntityBookshelf.class, "touhou_little_maid:bookshelf");
        GameRegistry.registerTileEntity(TileEntityComputer.class, "touhou_little_maid:computer");
        GameRegistry.registerTileEntity(TileEntityShrine.class, "touhou_little_maid:shrine");
        GameRegistry.registerTileEntity(TileEntityPicnicMat.class, "touhou_little_maid:picnic_mat");
        GameRegistry.registerTileEntity(TileEntitySnackCabinet.class, "touhou_little_maid:snack_cabinet");
        GameRegistry.registerTileEntity(TileEntityMaidBed.class, "touhou_little_maid:maid_bed");
        GameRegistry.registerTileEntity(TileEntityGarageKit.class, "touhou_little_maid:garage_kit");
        GameRegistry.registerTileEntity(TileEntityStatue.class, "touhou_little_maid:statue");
        GameRegistry.registerTileEntity(TileEntityMaidBeacon.class, "touhou_little_maid:maid_beacon");
        GameRegistry.registerTileEntity(TileEntityModelSwitcher.class, "touhou_little_maid:model_switcher");
        GameRegistry.registerTileEntity(TileEntityGomoku.class, "touhou_little_maid:gomoku");
        GameRegistry.registerTileEntity(TileEntityCChess.class, "touhou_little_maid:cchess");
        GameRegistry.registerTileEntity(TileEntityWChess.class, "touhou_little_maid:wchess");
        GameRegistry.registerTileEntity(TileEntityAltar.class, "touhou_little_maid:altar");
        GameRegistry.registerTileEntity(TileEntityScarecrow.class, "touhou_little_maid:scarecrow");
    }

    private static void register(Block block, String name) { GameRegistry.registerBlock(block, name); }

    private static void assignCreativeTab() {
        try {
            for (Field field : ModBlocks.class.getFields()) if (Block.class.isAssignableFrom(field.getType())) {
                Block block = (Block) field.get(null);
                if (block != null && block != BOARD_PROXY) block.setCreativeTab(ModCreativeTabs.TLM);
            }
        } catch (IllegalAccessException error) {
            throw new IllegalStateException("Could not populate TLM creative tab", error);
        }
    }
}

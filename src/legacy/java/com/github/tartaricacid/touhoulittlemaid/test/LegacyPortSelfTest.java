package com.github.tartaricacid.touhoulittlemaid.test;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.LegacyNbtMigration;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.touhoulittlemaid.init.ModBlocks;
import com.github.tartaricacid.touhoulittlemaid.init.ModEnchantments;
import com.github.tartaricacid.touhoulittlemaid.init.ModItems;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.lang.reflect.Field;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import cpw.mods.fml.common.Loader;

public final class LegacyPortSelfTest {
    private LegacyPortSelfTest() {
    }

    public static void run() {
        check(TaskManager.getTasks().size() == 22, "profession registry must contain 21 source professions plus EXTRAS miner");
        check(TaskManager.getTasks().containsKey(TaskManager.MINER_ID), "EXTRAS miner profession missing");
        check(TaskManager.isCombatTask(TaskManager.ATTACK_ID)
                && TaskManager.isCombatTask(TaskManager.RANGED_ATTACK_ID)
                && TaskManager.isCombatTask(TaskManager.DANMAKU_ATTACK_ID)
                && !TaskManager.isCombatTask(TaskManager.MINER_ID), "combat profession classification invalid");
        // Keep unsupported professions registered for saved task IDs, but inert.
        for (String stubId : new String[] { TaskManager.CROSSBOW_ATTACK_ID,
                TaskManager.TRIDENT_ATTACK_ID, TaskManager.HONEY_ID }) {
            check(TaskManager.getTasks().containsKey(stubId), "stub profession missing: " + stubId);
            check(!TaskManager.isCombatTask(stubId), "stub classified as combat: " + stubId);
            TaskManager.getTasks().get(stubId).tick(null);
        }
        check(com.github.tartaricacid.touhoulittlemaid.compat.miner.LegacyOreClassifier.isOreDictionaryName("oreCopper"), "standard mod ore name rejected");
        check(com.github.tartaricacid.touhoulittlemaid.compat.miner.LegacyOreClassifier.isOreDictionaryName("denseOreTungsten"), "dense mod ore name rejected");
        check(!com.github.tartaricacid.touhoulittlemaid.compat.miner.LegacyOreClassifier.isOreDictionaryName("blockCopper"), "storage block classified as ore");
        if(Loader.isModLoaded("gregtech")){
            int[] tools=com.github.tartaricacid.touhoulittlemaid.compat.miner.LegacyMiningToolCompat.oreDictionaryProbe();
            check(tools[0]==0||tools[1]>0,"installed GregTech exposes mining tools but miner recognizes none");
        }
        Set<String> ids = new HashSet<String>(TaskManager.getTasks().keySet());
        check(ids.size() == TaskManager.getTasks().size(), "duplicate profession id");
        check(ModEnchantments.IMPEDING.effectId != ModEnchantments.SPEEDY.effectId
                && ModEnchantments.SPEEDY.effectId != ModEnchantments.ENDERS_ENDER.effectId,
                "duplicate enchantment id");
        validateRegistries();
        check(resource("/assets/touhou_little_maid/sounds.json"), "sounds.json missing");
        check(resource("/assets/touhou_little_maid/ai/legacy_prompt.txt"), "LLM prompt missing");
        check(resource("/assets/touhou_little_maid/maid_model.json"), "default model manifest missing");
        check(resource("/assets/touhou_little_maid/models/entity/hakurei_reimu.json"),
                "default maid model missing");
        check(resource("/assets/touhou_little_maid/textures/entity/hakurei_reimu.png"),
                "default maid texture missing");
        check(resource("/assets/touhou_little_maid/models/entity/cushion.json"),
                "default chair model missing");
        check(resource("/assets/touhou_little_maid/textures/entity/cushion.png"),
                "default chair texture missing");
        check(resource("/assets/touhou_little_maid/models/bedrock/entity/maid_fairy.json"),
                "fairy model missing");
        check(resource("/assets/touhou_little_maid/models/bedrock/entity/reimu_yukkuri.json"),
                "Reimu Yukkuri model missing");
        check(resource("/assets/touhou_little_maid/models/bedrock/entity/marisa_yukkuri.json"),
                "Marisa Yukkuri model missing");
        check(resource("/assets/touhou_little_maid/textures/entity/point_item.png"),
                "point-item experience texture missing");
        validateMaidGuiResources();
        validateBackpacks();
        validateScarecrowResources();
        validateBedrockTileResources();
        NBTTagCompound uuid = new NBTTagCompound();
        uuid.setLong("UUIDMost", 1);
        uuid.setLong("UUIDLeast", 2);
        check(com.github.tartaricacid.touhoulittlemaid.world.MaidWorldIndex.uuid(uuid) != null,
                "UUID migration helper failed");
        validateModernNbtMigration();
        validateModelSwitcherStorage();
        validateAltarStorage();
        validateStatueAndGarageStorage();
        validateMaidBeaconStorage();
        validateBoardStorage();
        validateBoardGameplay();
        validateBoardSeatOffsets();
        validateBoardHitGrid();
        validateJoyAndShrineStorage();
        validatePicnicAndBedStorage();
        validateMaidModelSpawnCatalog();
        check(com.github.tartaricacid.touhoulittlemaid.crafting.LegacyAltarRecipes.recipeCount()==42,"altar recipe coverage incomplete");
        TouhouLittleMaid.LOGGER.info("Legacy port self-test passed: {} professions, registries/resources/NBT valid",
                TaskManager.getTasks().size());
    }

    private static void validateRegistries() {
        for (Field field : ModItems.class.getFields()) {
            try {
                if (Item.class.isAssignableFrom(field.getType())) {
                    Item item = (Item) field.get(null);
                    check(item != null && Item.getIdFromItem(item) > 0, "unregistered item " + field.getName());
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        for (Field field : ModBlocks.class.getFields()) {
            try {
                if (Block.class.isAssignableFrom(field.getType())) {
                    Block block = (Block) field.get(null);
                    check(block != null && Block.getIdFromBlock(block) > 0, "unregistered block " + field.getName());
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private static void validateModernNbtMigration() {
        NBTTagCompound root = new NBTTagCompound();
        root.setString("MaidBackpackType", "touhou_little_maid:maid_backpack_small");
        java.util.UUID identity=java.util.UUID.fromString("12345678-1234-5678-9abc-def012345678");
        root.setIntArray("UUID",uuidArray(identity));root.setIntArray("Owner",uuidArray(identity));
        NBTTagCompound handler = new NBTTagCompound();
        handler.setInteger("Size", 18);
        NBTTagList items = new NBTTagList();
        NBTTagCompound apple = new NBTTagCompound();
        apple.setByte("Slot", (byte) 2);
        apple.setString("id", "minecraft:apple");
        apple.setByte("Count", (byte) 3);
        items.appendTag(apple);
        handler.setTag("Items", items);
        root.setTag("MaidInventory", handler);
        NBTTagCompound schedule = new NBTTagCompound();
        schedule.setString("Dimension", "minecraft:the_nether");
        root.setTag("MaidSchedulePos", schedule);

        LegacyNbtMigration.normalize(root);
        check("maid_backpack_small".equals(root.getString("MaidBackpackType")),
                "namespaced backpack migration failed");
        NBTTagList migrated = root.getTagList("MaidInventory", 10);
        check(migrated.tagCount() == 1, "item handler inventory migration failed");
        check(migrated.getCompoundTagAt(0).getShort("id") == (short) Item.getIdFromItem(Items.apple),
                "string item id migration failed");
        check(root.getLong("UUIDMost")==identity.getMostSignificantBits()&&root.getLong("UUIDLeast")==identity.getLeastSignificantBits(),"modern entity UUID migration failed");
        check(identity.toString().equals(root.getString("OwnerUUID")),"modern owner UUID migration failed");
        check(root.getCompoundTag("MaidSchedulePos").getInteger("DimensionId") == -1,
                "modern schedule dimension migration failed");

        String[][] aliases = {{"small_backpack", "maid_backpack_small"},
                {"middle_backpack", "maid_backpack_middle"}, {"big_backpack", "maid_backpack_big"},
                {"tank", "tank_backpack"}};
        for (String[] alias : aliases) {
            NBTTagCompound backpack = new NBTTagCompound();
            backpack.setString("MaidBackpackType", "touhou_little_maid:" + alias[0]);
            LegacyNbtMigration.normalize(backpack);
            check(alias[1].equals(backpack.getString("MaidBackpackType")),
                    "modern backpack id migration failed: " + alias[0]);
        }
        NBTTagCompound tankRoot = new NBTTagCompound(), backpackData = new NBTTagCompound(), tank = new NBTTagCompound();
        tankRoot.setString("MaidBackpackType", "touhou_little_maid:tank");
        tank.setString("FluidName", "minecraft:water"); tank.setInteger("Amount", 10000);
        backpackData.setTag("Tanks", tank); tankRoot.setTag("MaidBackpackData", backpackData);
        LegacyNbtMigration.normalize(tankRoot);
        check("tank_backpack".equals(tankRoot.getString("MaidBackpackType"))
                        && "water".equals(tankRoot.getString("MaidBackpackFluid"))
                        && tankRoot.getInteger("MaidBackpackFluidAmount") == 10000,
                "modern tank backpack state migration failed");
    }

    private static void validateBedrockTileResources() {
        String[] models = {"altar", "gomoku", "cchess", "wchess", "keyboard", "bookshelf",
                "computer", "shrine", "picnic_mat", "snack_cabinet", "maid_bed/black", "maid_bed/blue",
                "maid_bed/green", "maid_bed/pink", "maid_bed/purple", "maid_bed/white", "maid_bed/yellow",
                "statue_base"};
        for (String name : models) {
            check(resource("/assets/touhou_little_maid/models/bedrock/block/" + name + ".json"),
                    "Bedrock tile model missing: " + name);
            check(resource("/assets/touhou_little_maid/textures/bedrock/block/" + name + ".png"),
                    "Bedrock tile texture missing: " + name);
        }
        String[] pieceModels = {"gomoku_piece", "cchess_pieces", "wchess_pieces"};
        for (String name : pieceModels) check(resource(
                "/assets/touhou_little_maid/models/bedrock/block/" + name + ".json"),
                "Bedrock game-piece model missing: " + name);
        String[] pieceTextures = {"gomoku_black_piece", "gomoku_white_piece", "cchess_pieces", "wchess_pieces"};
        for (String name : pieceTextures) check(resource(
                "/assets/touhou_little_maid/textures/bedrock/block/" + name + ".png"),
                "Bedrock game-piece texture missing: " + name);
        validatePieceBones("gomoku_piece", new String[]{"main"});
        validatePieceBones("wchess_pieces", new String[]{"KING_W", "QUEEN_W", "ROOK_W", "BISHOP_W",
                "KNIGHT_W", "PAWN_W", "KING_B", "QUEEN_B", "ROOK_B", "BISHOP_B", "KNIGHT_B",
                "PAWN_B", "SELECT"});
        validatePieceBones("cchess_pieces", new String[]{"ShuaiRed", "ShiRed", "XiangRed", "MaRed",
                "JuRed", "PaoRed", "BingRed", "JiangBlack", "ShiBlack", "XiangBlack", "MaBlack",
                "JuBlack", "PaoBlack", "ZuBlack", "Selected"});
    }

    private static void validateScarecrowResources() {
        check(resource("/assets/touhou_little_maid/textures/block/scarecrow_lower.png"),
                "scarecrow upper-section texture missing");
        check(resource("/assets/touhou_little_maid/textures/block/scarecrow_upper.png"),
                "scarecrow base texture missing");
        validateBlockModelElements("scarecrow_lower", 4);
        validateBlockModelElements("scarecrow_upper", 20);
        validateBlockModelElements("maid_beacon_down", 9);
        validateBlockModelElements("maid_beacon_up", 15);
        check(ModBlocks.SCARECROW.hasTileEntity(0), "scarecrow lower POI tile missing");
        check(!ModBlocks.SCARECROW.hasTileEntity(1), "scarecrow upper half must not duplicate POI tile");
        check(ModBlocks.SCARECROW.createTileEntity(null, 0)
                        instanceof com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityScarecrow,
                "scarecrow POI tile factory failed");
        check(ModBlocks.MAID_BEACON.hasTileEntity(0), "legacy one-block Shrine Lamp migration tile missing");
        check(!ModBlocks.MAID_BEACON.hasTileEntity(3), "Shrine Lamp lower half must not own state");
        check(ModBlocks.MAID_BEACON.hasTileEntity(1) && ModBlocks.MAID_BEACON.hasTileEntity(2),
                "Shrine Lamp upper state tile missing");
    }

    private static void validateBlockModelElements(String name, int minimum) {
        String path="/assets/touhou_little_maid/models/block/"+name+".json";
        InputStream stream=LegacyPortSelfTest.class.getResourceAsStream(path);
        check(stream!=null,"block model missing: "+name);
        try{
            JsonObject root=new JsonParser().parse(new InputStreamReader(stream,"UTF-8")).getAsJsonObject();
            JsonArray elements=root.getAsJsonArray("elements");
            check(elements!=null&&elements.size()>=minimum,"incomplete block model: "+name);
        }catch(Exception error){throw new IllegalStateException("cannot validate block model "+name,error);}
        finally{try{stream.close();}catch(Exception ignored){}}
    }

    private static void validatePieceBones(String model, String[] required) {
        String path = "/assets/touhou_little_maid/models/bedrock/block/" + model + ".json";
        InputStream stream = LegacyPortSelfTest.class.getResourceAsStream(path);
        check(stream != null, "game-piece model cannot be opened: " + model);
        try {
            JsonObject root = new JsonParser().parse(new InputStreamReader(stream, "UTF-8")).getAsJsonObject();
            JsonArray geometries = root.getAsJsonArray("minecraft:geometry");
            check(geometries != null && geometries.size() > 0, "invalid Bedrock geometry: " + model);
            Set<String> names = new HashSet<String>();
            JsonArray bones = geometries.get(0).getAsJsonObject().getAsJsonArray("bones");
            if (bones != null) for (JsonElement bone : bones) {
                JsonObject object = bone.getAsJsonObject();
                if (object.has("name")) names.add(object.get("name").getAsString());
            }
            for (String name : required) check(names.contains(name),
                    "game-piece bone missing from " + model + ": " + name);
        } catch (Exception e) {
            throw new IllegalStateException("cannot validate game-piece model " + model, e);
        } finally {
            try { stream.close(); } catch (Exception ignored) { }
        }
    }

    private static void validateBoardGameplay() {
        com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityGomoku gomoku =
                new com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityGomoku();
        for (int x = 0; x < 5; x++) {
            check(gomoku.place(x, 0), "Gomoku rejected a legal black move");
            if (x < 4) check(gomoku.place(x, 2), "Gomoku rejected a legal white move");
        }
        check(gomoku.getWinner() == 1 && !gomoku.place(7, 7),
                "Gomoku win/terminal-state handling failed");
        gomoku.reset();
        check(gomoku.place(7, 7), "Gomoku reset did not restore play");
        gomoku.makeComputerMove();
        check(gomoku.getMoves() == 2, "Gomoku computer did not answer the player move");

        com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityWChess western =
                new com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityWChess();
        int westFrom = com.github.tartaricacid.touhoulittlemaid.api.game.chess.Position.COORD_XY(
                com.github.tartaricacid.touhoulittlemaid.api.game.chess.Position.FILE_LEFT + 4,
                com.github.tartaricacid.touhoulittlemaid.api.game.chess.Position.RANK_TOP + 6);
        int westTo = com.github.tartaricacid.touhoulittlemaid.api.game.chess.Position.COORD_XY(
                com.github.tartaricacid.touhoulittlemaid.api.game.chess.Position.FILE_LEFT + 4,
                com.github.tartaricacid.touhoulittlemaid.api.game.chess.Position.RANK_TOP + 4);
        western.select(westFrom);
        check(western.move(westTo) && western.getPosition().sdPlayer != 0,
                "Western chess rejected e2-e4 or failed to switch turns");
        western.makeComputerMove();
        check(western.getPosition().sdPlayer == 0,
                "Western chess AI did not produce a legal reply");

        com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityCChess chinese =
                new com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityCChess();
        int chineseFrom = com.github.tartaricacid.touhoulittlemaid.api.game.xqwlight.Position.COORD_XY(
                com.github.tartaricacid.touhoulittlemaid.api.game.xqwlight.Position.FILE_LEFT,
                com.github.tartaricacid.touhoulittlemaid.api.game.xqwlight.Position.RANK_TOP + 6);
        int chineseTo = com.github.tartaricacid.touhoulittlemaid.api.game.xqwlight.Position.COORD_XY(
                com.github.tartaricacid.touhoulittlemaid.api.game.xqwlight.Position.FILE_LEFT,
                com.github.tartaricacid.touhoulittlemaid.api.game.xqwlight.Position.RANK_TOP + 5);
        chinese.select(chineseFrom);
        check(chinese.move(chineseTo) && chinese.getPosition().sdPlayer != 0,
                "Xiangqi rejected a legal pawn move or failed to switch turns");
        chinese.makeComputerMove();
        check(chinese.getPosition().sdPlayer == 0,
                "Xiangqi AI did not produce a legal reply");
    }

    private static void validateBoardSeatOffsets() {
        com.github.tartaricacid.touhoulittlemaid.block.BlockBoardGame gomoku =
                (com.github.tartaricacid.touhoulittlemaid.block.BlockBoardGame) ModBlocks.GOMOKU;
        com.github.tartaricacid.touhoulittlemaid.block.BlockBoardGame western =
                (com.github.tartaricacid.touhoulittlemaid.block.BlockBoardGame) ModBlocks.WCHESS;
        com.github.tartaricacid.touhoulittlemaid.block.BlockBoardGame chinese =
                (com.github.tartaricacid.touhoulittlemaid.block.BlockBoardGame) ModBlocks.CCHESS;
        for (int facing = 0; facing < 4; facing++) {
            int[] go = gomoku.getMaidSeatOffset(facing);
            int[] west = western.getMaidSeatOffset(facing);
            int[] china = chinese.getMaidSeatOffset(facing);
            check(Math.abs(go[0]) + Math.abs(go[1]) == 1,
                    "Gomoku maid seat is not outside its board");
            check(Math.abs(west[0]) + Math.abs(west[1]) == 1,
                    "Western chess maid seat is not outside its board");
            check(west[0] == china[0] && west[1] == china[1],
                    "Chess and xiangqi seat orientation diverged");
        }
    }

    private static void validateBoardHitGrid() {
        float previous = -1.0F;
        for (int part = -1; part <= 1; part++) {
            float start = com.github.tartaricacid.touhoulittlemaid.block.BlockBoardGame
                    .normalizePartHit(part, 0.0F);
            float middle = com.github.tartaricacid.touhoulittlemaid.block.BlockBoardGame
                    .normalizePartHit(part, 0.5F);
            float end = com.github.tartaricacid.touhoulittlemaid.block.BlockBoardGame
                    .normalizePartHit(part, 1.0F);
            check(start >= 0.0F && middle > start && end > middle && end <= 1.0F,
                    "3x3 board hit section is outside normalized bounds");
            check(start >= previous, "3x3 board hit sections overlap out of order");
            previous = end;
        }
        check(com.github.tartaricacid.touhoulittlemaid.block.BlockBoardGame
                        .normalizePartHit(-1, -2.0F) == 0.0F
                && com.github.tartaricacid.touhoulittlemaid.block.BlockBoardGame
                        .normalizePartHit(1, 2.0F) == 1.0F,
                "3x3 board hit clamping failed");
        for(int i=0;i<8;i++)check(com.github.tartaricacid.touhoulittlemaid.block.BlockBoardGame
                .floorGrid((float)((-.875+i*.25)/3.0+.5),-1.0,.25,8)==i,
                "Western chess 3x3 square hit mapping failed at "+i);
        for(int i=0;i<15;i++)check(com.github.tartaricacid.touhoulittlemaid.block.BlockBoardGame
                .nearestGrid((float)((-1.3818+i*.1974)/3.0+.5),-1.3818,.1974,15)==i,
                "Gomoku 3x3 intersection hit mapping failed at "+i);
        for(int i=0;i<9;i++)check(com.github.tartaricacid.touhoulittlemaid.block.BlockBoardGame
                .nearestGrid((float)((-.912+i*.228)/3.0+.5),-.912,.228,9)==i,
                "Xiangqi 3x3 intersection hit mapping failed at "+i);
        float[][] corners={{0,0},{1,0},{1,1},{0,1}};
        for(int facing=0;facing<4;facing++){
            Set<String> mapped=new HashSet<String>();
            for(float[] corner:corners){float[] point=com.github.tartaricacid.touhoulittlemaid.block.BlockBoardGame
                    .mapHitForFacing(corner[0],corner[1],facing);mapped.add(point[0]+":"+point[1]);}
            check(mapped.size()==4,"Board facing hit transform collapsed corners at "+facing);
        }
    }

    private static void validateModelSwitcherStorage() {
        NBTTagCompound storage = new NBTTagCompound();
        storage.setString("entity_uuid", "12345678-1234-5678-9abc-def012345678");
        storage.setString("owner_uuid", "87654321-4321-8765-cba9-876543210fed");
        storage.setInteger("list_index", 1);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < 2; i++) {
            NBTTagCompound mode = new NBTTagCompound();
            mode.setString("model_id", i == 0 ? "touhou_little_maid:hakurei_reimu" : "touhou_little_maid:cirno");
            mode.setString("text", "mode" + i); mode.setFloat("yaw", i * 90.0F); list.appendTag(mode);
        }
        storage.setTag("info_list", list);
        com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityModelSwitcher switcher =
                new com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityModelSwitcher();
        switcher.readStorage(storage);
        switcher.addMode("touhou_little_maid:yakumo_ran");
        switcher.rotateMode(2, 90);
        switcher.renameMode(2, "Ran");
        switcher.removeMode(0);
        NBTTagCompound saved = new NBTTagCompound(); switcher.writeStorage(saved);
        check(saved.getTagList("info_list", 10).tagCount() == 2 && saved.getInteger("list_index") == 1,
                "Model Switcher list/index item round-trip failed");
        check(saved.getTagList("info_list",10).getCompoundTagAt(1).getFloat("yaw") == 90.0F,
                "Model Switcher rotate/edit round-trip failed");
        check("Ran".equals(saved.getTagList("info_list",10).getCompoundTagAt(1).getString("text")),
                "Model Switcher name edit round-trip failed");
        check(storage.getString("entity_uuid").equals(saved.getString("entity_uuid"))
                        && storage.getString("owner_uuid").equals(saved.getString("owner_uuid")),
                "Model Switcher UUID item round-trip failed");
        NBTTagCompound modernRoot = new NBTTagCompound(), forgeData = new NBTTagCompound();
        java.util.UUID modernUuid = java.util.UUID.fromString("12345678-1234-5678-9abc-def012345678");
        forgeData.setIntArray("entity_uuid", uuidArray(modernUuid));
        forgeData.setInteger("list_index", 0);
        NBTTagList modernList = new NBTTagList(); NBTTagCompound modernMode = new NBTTagCompound();
        modernMode.setString("model_id", "touhou_little_maid:cirno"); modernMode.setString("text", "Ice");
        modernMode.setInteger("direction", 3); modernList.appendTag(modernMode); forgeData.setTag("info_list", modernList);
        modernRoot.setTag("ForgeData", forgeData); switcher.readStorage(modernRoot);
        NBTTagCompound migrated = new NBTTagCompound(); switcher.writeStorage(migrated);
        check(modernUuid.toString().equals(migrated.getString("entity_uuid"))
                        && migrated.getTagList("info_list",10).getCompoundTagAt(0).getFloat("yaw") == 270.0F,
                "Model Switcher 1.20 ForgeData/UUID/direction migration failed");
        check(Item.getItemFromBlock(ModBlocks.MODEL_SWITCHER)
                        instanceof com.github.tartaricacid.touhoulittlemaid.item.ItemBlockModelSwitcher,
                "Model Switcher persistent ItemBlock is not registered");
    }

    private static void validateAltarStorage() {
        com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityAltar altar =
                new com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityAltar();
        altar.setInventorySlotContents(0, new net.minecraft.item.ItemStack(Items.diamond));
        NBTTagCompound emptyUpdate = new NBTTagCompound(); emptyUpdate.setTag("Items", new NBTTagList());
        emptyUpdate.setFloat("Power", Float.NaN); altar.readFromNBT(emptyUpdate);
        check(altar.getStackInSlot(0) == null, "Tile inventory client update left a ghost stack");
        check(altar.getPower() == 0, "Altar accepted invalid Power NBT");
        NBTTagCompound excessive = new NBTTagCompound(); excessive.setTag("Items", new NBTTagList());
        excessive.setFloat("Power", 999999); altar.readFromNBT(excessive);
        check(altar.getPower() == com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityAltar.MAX_POWER,
                "Altar Power NBT was not clamped");
        net.minecraft.item.ItemStack offering = new net.minecraft.item.ItemStack(Items.diamond, 64);
        NBTTagCompound offeringData = new NBTTagCompound(); offeringData.setString("Audit", "preserve");
        offering.setTagCompound(offeringData);
        altar.setInventorySlotContents(0, offering);
        altar.setInventorySlotContents(1, new net.minecraft.item.ItemStack(Items.coal));
        altar.consumeOfferings();
        check(altar.getStackInSlot(0).stackSize == 63
                && "preserve".equals(altar.getStackInSlot(0).getTagCompound().getString("Audit"))
                && altar.getStackInSlot(1) == null && altar.getStackInSlot(2) == null,
                "Altar crafting lost excess offerings or their NBT");
    }

    private static void validateStatueAndGarageStorage() {
        NBTTagCompound modernStatue = new NBTTagCompound();
        modernStatue.setInteger("StatueSize", 99);
        modernStatue.setBoolean("CoreBlock", true);
        modernStatue.setString("StatueFacing", "east");
        NBTTagCompound core = new NBTTagCompound();
        core.setInteger("X", 7); core.setInteger("Y", 8); core.setInteger("Z", 9);
        modernStatue.setTag("CoreBlockPos", core);
        NBTTagList blocks = new NBTTagList();
        for (int i = 0; i < 60; i++) {
            NBTTagCompound pos = new NBTTagCompound();
            pos.setInteger("X", i); pos.setInteger("Y", 1); pos.setInteger("Z", 2); blocks.appendTag(pos);
        }
        modernStatue.setTag("AllBlocks", blocks);
        com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityStatue statue =
                new com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityStatue();
        statue.readFromNBT(modernStatue);
        check(statue.getStatueSize() == 3 && statue.getFacing() == 1,
                "Statue size/facing migration failed");
        check(statue.getCoreX() == 7 && statue.getCoreY() == 8 && statue.getCoreZ() == 9,
                "Statue core position migration failed");
        check(statue.getAllBlocks().size() == 54, "Statue block-list safety limit failed");
        java.util.List<int[]> copied = statue.getAllBlocks(); copied.get(0)[0] = -100;
        check(statue.getAllBlocks().get(0)[0] == 0, "Statue exposed mutable block positions");

        NBTTagCompound modernGarage = new NBTTagCompound();
        modernGarage.setString("GarageKitFacing", "west");
        com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityGarageKit garage =
                new com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityGarageKit();
        garage.readFromNBT(modernGarage);
        check(garage.getFacing() == 3 && garage.getExtraData().hasNoTags(),
                "Garage Kit modern facing/empty data migration failed");
    }

    private static void validateMaidBeaconStorage() {
        com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityMaidBeacon beacon =
                new com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityMaidBeacon();
        NBTTagCompound invalid = new NBTTagCompound();
        invalid.setInteger("PotionIndex", 100); invalid.setFloat("StoragePower", Float.NaN);
        beacon.readFromNBT(invalid);
        check(beacon.getPotionIndex() == 4 && beacon.getStoragePower() == 0,
                "Maid Beacon invalid NBT was not normalized");
        beacon.setStoragePower(Float.POSITIVE_INFINITY);
        check(beacon.getStoragePower() == 0, "Maid Beacon accepted non-finite Power");
        check(Math.abs(beacon.getEffectCost() - 0.001F) < 0.000001F,
                "Maid Beacon source effect-cost scaling was lost");
        int[] potionIds = {net.minecraft.potion.Potion.moveSpeed.id, net.minecraft.potion.Potion.fireResistance.id,
                net.minecraft.potion.Potion.damageBoost.id, net.minecraft.potion.Potion.resistance.id,
                net.minecraft.potion.Potion.regeneration.id};
        for (int index = 0; index < potionIds.length; index++) {
            beacon.setPotionIndex(index);
            check(beacon.getSelectedPotionId() == potionIds[index],
                    "Maid Beacon effect mapping failed at index " + index);
        }
        check(Item.getItemFromBlock(ModBlocks.MAID_BEACON)
                        instanceof com.github.tartaricacid.touhoulittlemaid.item.ItemBlockMaidBeacon,
                "Maid Beacon persistent ItemBlock is not registered");
        check(Item.getItemFromBlock(ModBlocks.MAID_BEACON).getItemAttributeModifiers()
                        .containsKey(net.minecraft.entity.SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName()),
                "Shrine Lamp source weapon modifier is missing");
        com.github.tartaricacid.touhoulittlemaid.capability.LegacyPlayerPower playerPower =
                new com.github.tartaricacid.touhoulittlemaid.capability.LegacyPlayerPower();
        check(Math.abs(playerPower.add(6)-5)<0.0001F&&Math.abs(playerPower.take(1)-1)<0.0001F
                        &&Math.abs(playerPower.get()-4)<0.0001F,
                "Shrine Lamp player Power bounds/transfer failed");
        NBTTagCompound powerNbt=new NBTTagCompound();playerPower.saveNBTData(powerNbt);
        com.github.tartaricacid.touhoulittlemaid.capability.LegacyPlayerPower restoredPower=
                new com.github.tartaricacid.touhoulittlemaid.capability.LegacyPlayerPower();
        restoredPower.loadNBTData(powerNbt);
        check(Math.abs(restoredPower.get()-4)<0.0001F,"player Power NBT round-trip failed");
    }

    private static void validateBoardStorage() {
        NBTTagCompound modern = new NBTTagCompound();
        NBTTagList rows = new NBTTagList();
        for (int x = 0; x < 15; x++) {
            int[] row = new int[15];
            if (x == 3) row[7] = 2;
            rows.appendTag(new net.minecraft.nbt.NBTTagIntArray(row));
        }
        modern.setTag("ChessData", rows); modern.setInteger("ChessCounter", 1);
        modern.setInteger("Statue", 1); modern.setBoolean("PlayerTurn", false);
        NBTTagCompound point = new NBTTagCompound(); point.setInteger("x", 3); point.setInteger("y", 7);
        point.setInteger("type", 2); modern.setTag("LatestChessPoint", point);
        com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityGomoku gomoku =
                new com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityGomoku();
        gomoku.readFromNBT(modern);
        check(gomoku.get(3, 7) == 2 && gomoku.getLatest() == 7 * 15 + 3 && gomoku.getWinner() == 2,
                "Gomoku 1.20 board/turn/result migration failed");

        NBTTagCompound chess = new NBTTagCompound();
        chess.setString("ChessData", com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityWChess.INITIAL);
        chess.setInteger("SelectChessPoint", Integer.MAX_VALUE); chess.setInteger("ChessCounter", -4);
        com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityWChess western =
                new com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityWChess();
        western.readFromNBT(chess);
        check(western.getSelected() == 0, "Western chess accepted an unsafe selected square");

        java.util.UUID sit = java.util.UUID.fromString("12345678-1234-5678-9abc-def012345678");
        NBTTagCompound joy = new NBTTagCompound(); joy.setIntArray("SitId", uuidArray(sit));
        gomoku.readFromNBT(joy);
        NBTTagCompound saved = new NBTTagCompound(); gomoku.writeToNBT(saved);
        check(sit.toString().equals(saved.getString("SitId")), "Joy SitId UUID-array migration failed");
    }

    private static void validateJoyAndShrineStorage() {
        com.github.tartaricacid.touhoulittlemaid.block.BlockJoy keyboard =
                (com.github.tartaricacid.touhoulittlemaid.block.BlockJoy) ModBlocks.KEYBOARD;
        com.github.tartaricacid.touhoulittlemaid.block.BlockJoy bookshelf =
                (com.github.tartaricacid.touhoulittlemaid.block.BlockJoy) ModBlocks.BOOKSHELF;
        com.github.tartaricacid.touhoulittlemaid.block.BlockJoy computer =
                (com.github.tartaricacid.touhoulittlemaid.block.BlockJoy) ModBlocks.COMPUTER;
        check("Keyboard".equals(keyboard.getJoyType()) && keyboard.getSitYOffset() == 0.625D,
                "Keyboard source sit pose was lost");
        check("BookShelf".equals(bookshelf.getJoyType()) && bookshelf.getSitYawOffset() == -90.0F,
                "Bookshelf source sit pose was lost");
        check("Computer".equals(computer.getJoyType()) && computer.getSitYOffset() == 1.0D
                        && computer.getSitYawOffset() == 180.0F,
                "Computer source sit pose was lost");

        NBTTagCompound root = new NBTTagCompound(), forgeData = new NBTTagCompound(), handler = new NBTTagCompound();
        NBTTagList items = new NBTTagList(); NBTTagCompound film = new NBTTagCompound();
        film.setByte("Slot", (byte) 0); film.setString("id", "touhou_little_maid:film");
        film.setByte("Count", (byte) 1); film.setShort("Damage", (short) 0); items.appendTag(film);
        handler.setTag("Items", items); handler.setInteger("Size", 1); forgeData.setTag("StorageItem", handler);
        root.setTag("ForgeData", forgeData);
        com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityShrine shrine =
                new com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityShrine();
        shrine.readFromNBT(root);
        check(shrine.getStackInSlot(0) != null && shrine.getStackInSlot(0).getItem() == ModItems.FILM,
                "Shrine modern ItemStackHandler migration failed");
    }

    private static void validatePicnicAndBedStorage() {
        NBTTagCompound root=new NBTTagCompound(),forgeData=new NBTTagCompound(),center=new NBTTagCompound(),handler=new NBTTagCompound();
        center.setInteger("X",4);center.setInteger("Y",5);center.setInteger("Z",6);forgeData.setTag("CenterPos",center);
        NBTTagList items=new NBTTagList();NBTTagCompound apple=new NBTTagCompound();apple.setByte("Slot",(byte)2);apple.setString("id","minecraft:apple");apple.setByte("Count",(byte)3);apple.setShort("Damage",(short)0);items.appendTag(apple);handler.setTag("Items",items);handler.setInteger("Size",9);forgeData.setTag("StorageItem",handler);
        NBTTagList seats=new NBTTagList();for(int i=0;i<4;i++)seats.appendTag(new net.minecraft.nbt.NBTTagIntArray(new int[]{0,0,0,0}));forgeData.setTag("SitIds",seats);root.setTag("ForgeData",forgeData);
        com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityPicnicMat picnic=new com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityPicnicMat();picnic.readFromNBT(root);
        check(picnic.getCenterX()==4&&picnic.getCenterY()==5&&picnic.getCenterZ()==6&&picnic.getStackInSlot(2)!=null&&picnic.getStackInSlot(2).stackSize==3,"Picnic Mat modern state migration failed");

        NBTTagCompound modernBed=new NBTTagCompound(),modernBedData=new NBTTagCompound();modernBedData.setInteger("BedColor",6);modernBed.setTag("ForgeData",modernBedData);
        com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityMaidBed bed=new com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityMaidBed();bed.readFromNBT(modernBed);
        check(bed.getColor()==6,"Maid Bed modern pink DyeColor migration failed");
        NBTTagCompound oldBed=new NBTTagCompound();oldBed.setInteger("BedColor",9);bed.readFromNBT(oldBed);
        check(bed.getColor()==6,"Maid Bed legacy dye-damage migration failed");
    }

    private static void validateMaidModelSpawnCatalog() {
        java.util.List<String> ids = com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidModelIdCatalog.getModelIds();
        check(ids.size() > 1 && ids.contains(com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid.DEFAULT_MODEL_ID),
                "Cake Box/random maid model catalog was not loaded");
        java.util.Random first = new java.util.Random(12345L), second = new java.util.Random(12345L);
        check(com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidModelIdCatalog.random(first).equals(
                        com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidModelIdCatalog.random(second)),
                "Maid model spawn selection is not stable for the server RNG");
    }

    private static void validateMaidGuiResources() {
        String[] textures = {"maid_gui_main", "maid_gui_side", "maid_gui_button",
                "maid_gui_backpack", "maid_gui_bauble", "maid_gui_task", "maid_gui_config"};
        for (String name : textures) {
            check(resource("/assets/touhou_little_maid/textures/gui/" + name + ".png"),
                    "Maid GUI texture missing: " + name);
        }
    }

    private static void validateBackpacks() {
        Object[][] definitions = {
                {ModItems.MAID_BACKPACK_SMALL, 12, "small_backpack", "small_backpack"},
                {ModItems.MAID_BACKPACK_MIDDLE, 24, "middle_backpack", "middle_backpack"},
                {ModItems.MAID_BACKPACK_BIG, 36, "big_backpack", "big_backpack"},
                {ModItems.CRAFTING_TABLE_BACKPACK, 18, "crafting_table_backpack", "crafting_table_backpack"},
                {ModItems.ENDER_CHEST_BACKPACK, 6, "end_chest_backpack", "ender_chest_backpack"},
                {ModItems.FURNACE_BACKPACK, 18, "furnace_backpack", "furnace_backpack"},
                {ModItems.TANK_BACKPACK, 18, "tank_backpack", "tank_backpack"}
        };
        for (Object[] definition : definitions) {
            com.github.tartaricacid.touhoulittlemaid.item.ItemMaidBackpack item =
                    (com.github.tartaricacid.touhoulittlemaid.item.ItemMaidBackpack) definition[0];
            check(item.getCapacity() == (Integer) definition[1],
                    "backpack capacity mismatch: " + item.getBackpackType());
            validateBedrockGeometry("/assets/touhou_little_maid/models/bedrock/entity/backpack/"
                    + definition[2] + ".json", "backpack " + definition[2]);
            check(resource("/assets/touhou_little_maid/textures/bedrock/entity/backpack/"
                            + definition[3] + ".png"),
                    "backpack texture missing: " + definition[3]);
        }
    }

    private static void validateBedrockGeometry(String path, String label) {
        InputStream stream = LegacyPortSelfTest.class.getResourceAsStream(path);
        check(stream != null, label + " model missing");
        try {
            JsonObject root = new JsonParser().parse(new InputStreamReader(stream, "UTF-8")).getAsJsonObject();
            JsonArray geometries = root.getAsJsonArray("minecraft:geometry");
            check(geometries != null && geometries.size() > 0, "invalid geometry: " + label);
            JsonArray bones = geometries.get(0).getAsJsonObject().getAsJsonArray("bones");
            check(bones != null && bones.size() > 0, "geometry has no bones: " + label);
        } catch (Exception e) {
            throw new IllegalStateException("cannot validate " + label, e);
        } finally {
            try { stream.close(); } catch (Exception ignored) { }
        }
    }

    private static int[] uuidArray(java.util.UUID id){long most=id.getMostSignificantBits(),least=id.getLeastSignificantBits();return new int[]{(int)(most>>32),(int)most,(int)(least>>32),(int)least};}

    private static boolean resource(String path) {
        java.io.InputStream in = LegacyPortSelfTest.class.getResourceAsStream(path);
        if (in == null) return false;
        try {
            in.close();
        } catch (Exception ignored) {
        }
        return true;
    }

    private static void check(boolean ok, String message) {
        if (!ok) throw new IllegalStateException("TLM legacy self-test: " + message);
    }
}

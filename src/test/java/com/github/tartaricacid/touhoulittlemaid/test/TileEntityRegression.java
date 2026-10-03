package com.github.tartaricacid.touhoulittlemaid.test;

import com.github.tartaricacid.touhoulittlemaid.tileentity.*;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

/** Standalone tests against the actual Forge classes; no running game or world files. */
public final class TileEntityRegression {
    private static int checks;
    private static void check(boolean ok, String message) { checks++; if (!ok) throw new AssertionError(message); }
    private static NBTTagCompound save(TileEntity tile) { NBTTagCompound n=new NBTTagCompound();tile.writeToNBT(n);return n; }
    private static NBTTagCompound nested(NBTTagCompound data) { NBTTagCompound n=new NBTTagCompound(); n.setTag("ForgeData",data.copy());return n; }
    public static void main(String[] args) throws Exception {
        Bootstrap.func_151354_b();
        Class<?>[] types={TileEntityAltar.class,TileEntityBookshelf.class,TileEntityCChess.class,
            TileEntityComputer.class,TileEntityGarageKit.class,TileEntityGomoku.class,TileEntityKeyboard.class,
            TileEntityMaidBeacon.class,TileEntityMaidBed.class,TileEntityModelSwitcher.class,
            TileEntityPicnicMat.class,TileEntityScarecrow.class,TileEntityShrine.class,
            TileEntitySnackCabinet.class,TileEntityStatue.class,TileEntityWChess.class};
        java.lang.reflect.Method register=TileEntity.class.getDeclaredMethod("addMapping",Class.class,String.class);
        register.setAccessible(true);
        for(Class<?> type:types) register.invoke(null,type,"audit:"+type.getSimpleName());
        for(Class<?> type:types) {
            TileEntity a=(TileEntity)type.newInstance(), b=(TileEntity)type.newInstance();
            a.xCoord=13;a.yCoord=70;a.zCoord=-9;
            NBTTagCompound data=save(a); b.readFromNBT(data);
            check(data.equals(save(b)),type.getSimpleName()+" round trip");
            b.readFromNBT(data); check(data.equals(save(b)),type.getSimpleName()+" repeated update");
            net.minecraft.network.Packet packet=a.getDescriptionPacket();
            if (packet instanceof net.minecraft.network.play.server.S35PacketUpdateTileEntity) {
                b.onDataPacket(null,(net.minecraft.network.play.server.S35PacketUpdateTileEntity)packet);
                check(data.equals(save(b)),type.getSimpleName()+" description packet");
            }
            if (type!=TileEntityAltar.class && type!=TileEntitySnackCabinet.class && type!=TileEntityScarecrow.class) {
                NBTTagCompound modern=nested(data);
                modern.setInteger("x",a.xCoord);modern.setInteger("y",a.yCoord);modern.setInteger("z",a.zCoord);
                b.readFromNBT(modern);
                check(data.equals(LegacyTileNbt.data(save(b))),type.getSimpleName()+" wrapped migration");
            }
        }
        TileEntitySnackCabinet cabinet=new TileEntitySnackCabinet();
        ItemStack stack=new ItemStack(Items.diamond,64); stack.setTagCompound(new NBTTagCompound());stack.getTagCompound().setString("keep","yes");
        cabinet.setInventorySlotContents(0,stack);
        check(cabinet.decrStackSize(0,-4)==null && stack.stackSize==64,"negative extraction duplicated items");
        NBTTagCompound saved=save(cabinet), original=(NBTTagCompound)saved.copy();
        cabinet.readFromNBT(saved);check(original.equals(saved),"NBT input mutated");
        cabinet.readFromNBT(new NBTTagCompound());check(cabinet.getStackInSlot(0)==null,"ghost inventory");
        TileEntityAltar altar=new TileEntityAltar();altar.setInventorySlotContents(0,stack.copy());
        altar.setInventorySlotContents(1,new ItemStack(Items.coal));altar.consumeOfferings();
        check(altar.getStackInSlot(0).stackSize==63 && altar.getStackInSlot(1)==null,"offering quantity");
        check("yes".equals(altar.getStackInSlot(0).getTagCompound().getString("keep")),"offering NBT");
        TileEntityModelSwitcher model=new TileEntityModelSwitcher();model.addMode("touhou_little_maid:hakurei_reimu");model.setPowered(true);
        NBTTagCompound modelData=save(model);TileEntityModelSwitcher copy=new TileEntityModelSwitcher();copy.readFromNBT(modelData);
        check(copy.isPowered(),"redstone latch lost");
        NBTTagCompound carried=new NBTTagCompound();copy.writeStorage(carried);copy.readStorage(carried);
        check(!copy.isPowered(),"carried switcher kept old redstone input");
        copy.rotateMode(0,Float.NaN);check(!Float.isNaN(copy.getInfoList().get(0).yaw),"invalid rotation");
        NBTTagCompound wrapped=nested(modelData);model.readFromNBT(wrapped);model.renameMode(0,"edited");
        copy.readFromNBT(save(model));check("edited".equals(copy.getInfoList().get(0).name),"stale ForgeData reverted model edit");
        NBTTagCompound bedData=new NBTTagCompound();bedData.setInteger("BedColor",3);
        TileEntityMaidBed bed=new TileEntityMaidBed();bed.readFromNBT(nested(bedData));check(bed.getColor()==3,"modern dye color");
        bed.setColor(7);bed.readFromNBT(save(bed));check(bed.getColor()==7,"stale bed color");
        TileEntityMaidBeacon beacon=new TileEntityMaidBeacon();beacon.readFromNBT(new NBTTagCompound());check(beacon.getPotionIndex()==-1,"empty beacon enabled effect");
        NBTTagCompound beaconData=new NBTTagCompound();beaconData.setInteger("PotionIndex",2);beaconData.setFloat("StoragePower",10);
        beacon.readFromNBT(nested(beaconData));check(beacon.getPotionIndex()==2 && beacon.getStoragePower()==10,"beacon ForgeData");
        TileEntityGarageKit kit=new TileEntityGarageKit();NBTTagCompound kitData=new NBTTagCompound();kitData.setString("GarageKitFacing","east");
        kit.readFromNBT(nested(kitData));check(kit.getFacing()==1,"garage facing migration");
        TileEntityStatue statue=new TileEntityStatue();NBTTagCompound statueData=new NBTTagCompound();statueData.setInteger("StatueSize",3);statueData.setBoolean("CoreBlock",true);
        statue.readFromNBT(nested(statueData));check(statue.getStatueSize()==3 && statue.isCoreBlock(),"statue migration");
        TileEntityGomoku game=new TileEntityGomoku();game.place(7,7);NBTTagCompound gameData=save(game);game.readFromNBT(nested(gameData));check(game.get(7,7)==1 && game.getMoves()==1,"gomoku migration");
        game.readFromNBT(new NBTTagCompound());check(game.getLatest()==-1,"empty gomoku latest");
        TileEntityCChess cc=new TileEntityCChess();cc.select(Integer.MAX_VALUE);check(!cc.move(-1),"xiangqi bounds");
        TileEntityWChess wc=new TileEntityWChess();wc.select(Integer.MAX_VALUE);check(!wc.move(-1),"chess bounds");
        NBTTagCompound item=new NBTTagCompound();new ItemStack(Items.apple,3).writeToNBT(item);item.setByte("Slot",(byte)0);
        NBTTagList items=new NBTTagList();items.appendTag(item);NBTTagCompound storage=new NBTTagCompound();storage.setTag("Items",items);
        NBTTagCompound picnicData=new NBTTagCompound();picnicData.setTag("StorageItem",storage);
        TileEntityPicnicMat picnic=new TileEntityPicnicMat();picnic.readFromNBT(nested(picnicData));check(picnic.getStackInSlot(0).stackSize==3,"picnic storage migration");
        picnic.setInventorySlotContents(0,null);picnic.readFromNBT(save(picnic));check(picnic.getStackInSlot(0)==null,"removed food resurrected from ForgeData");
        System.out.println("TileEntity regression passed: "+checks+" checks, "+types.length+" concrete tile types");
        MultiblockRegression.run();
        String root = new java.io.File("../..").getCanonicalPath();
        Class.forName("AnimationChecks").getMethod("main",String[].class).invoke(null,(Object)new String[]{root});
        Class.forName("CorpusChecks").getMethod("main",String[].class).invoke(null,(Object)new String[]{root, "../actual-gson-corpus.tsv"});
        Class.forName("TimelineChecks").getMethod("main",String[].class).invoke(null,(Object)new String[]{root});
        AnimationSpawnProbe.run();
        MaidRenderRegression.run(root);
        MaidBedRegression.run();
        MaidAIRegression.run();
        MaidFarmRegression.run();
        MaidFeedRegression.run();
        MaidFurnaceRegression.run();
        MaidTankRegression.run();
    }
}

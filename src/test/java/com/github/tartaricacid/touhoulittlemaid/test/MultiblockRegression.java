package com.github.tartaricacid.touhoulittlemaid.test;

import com.github.tartaricacid.touhoulittlemaid.block.*;
import com.github.tartaricacid.touhoulittlemaid.init.ModBlocks;
import com.github.tartaricacid.touhoulittlemaid.item.ItemBlockPicnicMat;
import com.github.tartaricacid.touhoulittlemaid.tileentity.*;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.util.ForgeDirection;
import java.util.*;

/** Deterministic in-memory world fixture: real block/TE methods, no game-loop claims. */
public final class MultiblockRegression {
    static int checks;
    static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    public static void run() throws Exception {
        java.lang.reflect.Field controller=cpw.mods.fml.common.Loader.class.getDeclaredField("modController");controller.setAccessible(true);
        controller.set(cpw.mods.fml.common.Loader.instance(),new cpw.mods.fml.common.LoadController(cpw.mods.fml.common.Loader.instance()));
        cpw.mods.fml.common.registry.GameRegistry.registerBlock(ModBlocks.ALTAR,"audit_altar");
        cpw.mods.fml.common.registry.GameRegistry.registerBlock(ModBlocks.PICNIC_MAT,ItemBlockPicnicMat.class,"audit_picnic");
        for(int facing=0;facing<4;facing++){
            MemoryWorld w=world();TestPlayer player=player();int ox=-20,oy=64,oz=-20;
            List<int[]> layout=LegacyAltarStructure.template(facing);int pedestals=0;
            check(layout.size()==38,"template block count");
            for(int[] p:layout){w.put(ox+p[0],oy+p[1],oz+p[2],p[3]==0?Blocks.log:Blocks.wool,p[3]==0?1:14);if(p[3]==0&&p[1]==2)pedestals++;}
            check(pedestals==6,"template pedestal count");
            int[] off=LegacyAltarStructure.offset(facing);int cx=ox-off[0],cy=oy-off[1],cz=oz-off[2];
            check(LegacyAltarStructure.tryBuild(w,player,new ItemStack(Items.stick),cx,cy,cz,1),"build orientation "+facing);
            TileEntityAltar core=(TileEntityAltar)w.getTileEntity(cx,cy,cz);
            check(core.isRenderCore()&&core.getStructureTiles().size()==38,"core and complete structure");
            int cores=0;TileEntityAltar offering=null;
            for(TileEntityAltar t:core.getStructureTiles()){if(t.isRenderCore())cores++;if(t.isPedestal())offering=t;}
            check(cores==1,"only one model renderer");
            offering.setInventorySlotContents(0,new ItemStack(Items.diamond));
            w.setBlock(cx,cy,cz,Blocks.air,0,3);
            check(w.drops.size()==2,"one original block plus offering on dismantle");
            for(int[] p:layout){if(ox+p[0]==cx&&oy+p[1]==cy&&oz+p[2]==cz)continue;check(w.getBlock(ox+p[0],oy+p[1],oz+p[2])==(p[3]==0?Blocks.log:Blocks.wool),"restored source block");}
            w=world();for(int[] p:layout)w.put(ox+p[0],oy+p[1],oz+p[2],p[3]==0?Blocks.log:Blocks.wool,p[3]==0?1:14);
            w.refuseAt=8;
            check(!LegacyAltarStructure.tryBuild(w,player,new ItemStack(Items.stick),cx,cy,cz,1),"failed build rejected");
            for(int[] p:layout)check(w.getBlock(ox+p[0],oy+p[1],oz+p[2])==(p[3]==0?Blocks.log:Blocks.wool),"rollback preserved originals");
            check(w.drops.isEmpty(),"rollback generated drops");
        }
        MemoryWorld w=world();TestPlayer player=player();
        ItemBlockPicnicMat basket=new ItemBlockPicnicMat(ModBlocks.PICNIC_MAT);ItemStack carried=new ItemStack(Item.getItemFromBlock(ModBlocks.PICNIC_MAT));
        check(basket.placeBlockAt(carried,player,w,10,64,10,1,0,0,0,0),"picnic placement");
        TileEntityPicnicMat center=(TileEntityPicnicMat)w.getTileEntity(10,64,10);center.setInventorySlotContents(0,new ItemStack(Items.apple,23));
        int parts=0,centers=0;for(TileEntity t:w.tiles.values())if(t instanceof TileEntityPicnicMat){parts++;if(((TileEntityPicnicMat)t).isCenter())centers++;}
        check(parts==25&&centers==1,"picnic 25 parts, one center");
        w.setBlock(8,64,8,Blocks.air,0,3);
        check(w.tiles.isEmpty()&&w.drops.size()==1,"picnic dismantle once");
        ItemStack drop=w.drops.get(0).getEntityItem();
        check(drop.getTagCompound().getCompoundTag("PicnicBasketContainer").getTagList("Items",10).getCompoundTagAt(0).getByte("Count")==23,"basket kept food");
        check(basket.placeBlockAt(drop,player,w,20,64,20,1,0,0,0,0),"basket replacement");
        check(((TileEntityPicnicMat)w.getTileEntity(20,64,20)).getStackInSlot(0).stackSize==23,"food restored on placement");
        TileEntityPicnicMat edge=(TileEntityPicnicMat)w.getTileEntity(18,64,18);
        check(!edge.isItemValidForSlot(0,new ItemStack(Items.apple)),"side rejects independent hopper inventory");
        check(edge.insertFood(new ItemStack(Items.apple))==0,"side rejects direct food insertion");
        // Simulate a missing part after chunk reload, bypassing the normal break callback.
        w.put(18,64,18,Blocks.air,0);w.removeTileEntity(18,64,18);
        TileEntityPicnicMat restored=(TileEntityPicnicMat)w.getTileEntity(20,64,20);
        for(int tick=0;tick<20;tick++)restored.updateEntity();
        check(w.tiles.isEmpty()&&w.drops.size()==2,"incomplete reloaded picnic dismantles once");
        w=world();w.refuseAt=8;
        check(!basket.placeBlockAt(carried,player,w,10,64,10,1,0,0,0,0),"picnic failure");
        check(w.tiles.isEmpty()&&w.drops.isEmpty(),"picnic rollback clean");
        w=world();w.put(8,64,8,Blocks.stone,0);
        check(!basket.placeBlockAt(carried,player,w,10,64,10,1,0,0,0,0),"occupied area rejected");
        check(w.getBlock(8,64,8)==Blocks.stone,"occupied block preserved");
        System.out.println("Multiblock regression passed: "+checks+" checks (in-memory world fixture)");
    }
    static sun.misc.Unsafe unsafe() throws Exception{java.lang.reflect.Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);return(sun.misc.Unsafe)f.get(null);}
    static MemoryWorld world() throws Exception{MemoryWorld w=(MemoryWorld)unsafe().allocateInstance(MemoryWorld.class);w.states=new HashMap<String,State>();w.tiles=new HashMap<String,TileEntity>();w.drops=new ArrayList<EntityItem>();w.refuseAt=-1;w.rand=new Random(1);java.lang.reflect.Field entities=World.class.getDeclaredField("loadedEntityList");entities.setAccessible(true);entities.set(w,new ArrayList<Entity>());java.lang.reflect.Field provider=World.class.getDeclaredField("provider");provider.setAccessible(true);provider.set(w,new net.minecraft.world.WorldProviderSurface());return w;}
    static TestPlayer player() throws Exception{TestPlayer p=(TestPlayer)unsafe().allocateInstance(TestPlayer.class);p.capabilities=new PlayerCapabilities();return p;}
    static final class State{Block block;int meta;State(Block b,int m){block=b;meta=m;}}
    public static final class TestPlayer extends EntityPlayer{
        private TestPlayer(){super(null,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"audit"));}
        @Override public boolean canPlayerEdit(int x,int y,int z,int side,ItemStack item){return true;}
        @Override public net.minecraft.util.ChunkCoordinates getPlayerCoordinates(){return new net.minecraft.util.ChunkCoordinates(0,64,0);}
        @Override public boolean canCommandSenderUseCommand(int level,String command){return true;}
        @Override public void addChatMessage(net.minecraft.util.IChatComponent message){}
    }
    public static final class MemoryWorld extends World{
        boolean refuseFarmChange;
        @Override public boolean isAirBlock(int x,int y,int z){return getBlock(x,y,z)==Blocks.air;}
        @Override public int getFullBlockLightValue(int x,int y,int z){return 15;}
        @Override public boolean canBlockSeeTheSky(int x,int y,int z){return true;}
        @Override public boolean setBlockMetadataWithNotify(int x,int y,int z,int meta,int flags){if(refuseFarmChange)return false;put(x,y,z,getBlock(x,y,z),meta);return true;}
        @Override public void playAuxSFX(int id,int x,int y,int z,int data){}
        Map<String,State> states;Map<String,TileEntity> tiles;List<EntityItem> drops;int placed,refuseAt;long testTime;boolean testCollision,testEntityCollision,testLiquid,testUnloaded;
        @Override public java.util.List getCollidingBoundingBoxes(Entity e,net.minecraft.util.AxisAlignedBB box){return testCollision?java.util.Collections.singletonList(box):java.util.Collections.emptyList();}
        @Override public boolean checkNoEntityCollision(net.minecraft.util.AxisAlignedBB box,Entity e){return !testEntityCollision;}
        @Override public boolean isAnyLiquid(net.minecraft.util.AxisAlignedBB box){return testLiquid;}
        @Override public long getWorldTime(){return testTime;}
        private MemoryWorld(){super(null,"audit",(net.minecraft.world.WorldSettings)null,(net.minecraft.world.WorldProvider)null,null);}
        String key(int x,int y,int z){return x+":"+y+":"+z;}
        void put(int x,int y,int z,Block b,int m){states.put(key(x,y,z),new State(b,m));}
        @Override public Block getBlock(int x,int y,int z){State s=states.get(key(x,y,z));return s==null?Blocks.air:s.block;}
        @Override public int getBlockMetadata(int x,int y,int z){State s=states.get(key(x,y,z));return s==null?0:s.meta;}
        @Override public TileEntity getTileEntity(int x,int y,int z){return tiles.get(key(x,y,z));}
        @Override public void removeTileEntity(int x,int y,int z){tiles.remove(key(x,y,z));}
        @Override public boolean blockExists(int x,int y,int z){return !testUnloaded&&y>=0&&y<256;}
        @Override public boolean checkChunksExist(int x1,int y1,int z1,int x2,int y2,int z2){return false;}
        @Override public boolean canMineBlock(EntityPlayer p,int x,int y,int z){return true;}
        @Override public boolean isSideSolid(int x,int y,int z,ForgeDirection side){return true;}
        @Override public boolean setBlock(int x,int y,int z,Block b,int m,int flags){
            if(refuseFarmChange)return false;
            if((b==ModBlocks.ALTAR||b==ModBlocks.PICNIC_MAT)&&placed++==refuseAt)return false;
            Block old=getBlock(x,y,z);int meta=getBlockMetadata(x,y,z);put(x,y,z,b,m);
            if(old!=b)old.breakBlock(this,x,y,z,old,meta);
            tiles.remove(key(x,y,z));
            if(b.hasTileEntity(m)){TileEntity t=b.createTileEntity(this,m);t.xCoord=x;t.yCoord=y;t.zCoord=z;t.setWorldObj(this);tiles.put(key(x,y,z),t);}
            return true;
        }
        @Override public boolean spawnEntityInWorld(Entity e){if(e instanceof EntityItem)drops.add((EntityItem)e);return true;}
        @Override public void notifyBlocksOfNeighborChange(int x,int y,int z,Block b){}
        @Override public void markBlockForUpdate(int x,int y,int z){}
        @Override public void markTileEntityChunkModified(int x,int y,int z,TileEntity t){}
        @Override public void playSoundEffect(double x,double y,double z,String sound,float v,float p){}
        @Override public void playSoundAtEntity(Entity entity,String sound,float volume,float pitch){}
        @Override protected IChunkProvider createChunkProvider(){return null;}
        @Override protected int func_152379_p(){return 0;}
        @Override public Entity getEntityByID(int id){return null;}
    }
}

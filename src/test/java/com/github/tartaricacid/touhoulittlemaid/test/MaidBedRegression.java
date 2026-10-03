package com.github.tartaricacid.touhoulittlemaid.test;

import com.github.tartaricacid.touhoulittlemaid.block.BlockMaidBed;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.*;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.*;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityMaidBed;
import net.minecraft.entity.*;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.world.World;
import java.lang.reflect.*;
import java.util.*;

public final class MaidBedRegression {
    static int checks;
    static void check(boolean value,String text){checks++;if(!value)throw new AssertionError(text);}
    static void set(Class<?> type,Object o,String name,Object value)throws Exception{Field f=type.getDeclaredField(name);f.setAccessible(true);f.set(o,value);}
    public static void run()throws Exception{
        BlockMaidBed block=new BlockMaidBed();
        Method seek=EntityMaid.class.getDeclaredMethod("seekBedAndRest");seek.setAccessible(true);
        for(int facing=0;facing<4;facing++){
            MultiblockRegression.MemoryWorld world=MultiblockRegression.world();world.testTime=16000;
            TileEntityMaidBed bed=new TileEntityMaidBed();bed.xCoord=0;bed.yCoord=64;bed.zCoord=0;bed.setWorldObj(world);
            int dx=facing==1?-1:facing==3?1:0,dz=facing==0?1:facing==2?-1:0;
            world.put(0,64,0,block,8|facing);world.put(-dx,64,-dz,block,facing);
            world.tiles.put(world.key(0,64,0),bed);
            set(World.class,world,"loadedTileEntityList",new ArrayList<net.minecraft.tileentity.TileEntity>(Arrays.asList(bed)));
            check(bed.isComplete(),"two halves facing "+facing);
            world.put(-dx,64,-dz,block,(facing+1)&3);check(!bed.isComplete(),"mismatched foot rejected");world.put(-dx,64,-dz,block,facing);
            world.testUnloaded=true;check(!bed.isComplete(),"unloaded bed rejected");world.testUnloaded=false;
            Maid maid=(Maid)MultiblockRegression.unsafe().allocateInstance(Maid.class);maid.worldObj=world;maid.home=true;maid.mode=MaidSchedule.DAY;
            maid.posX=.5;maid.posY=64;maid.posZ=.5;
            DataWatcher watcher=new DataWatcher(maid);watcher.addObject(24,(byte)0);set(Entity.class,maid,"dataWatcher",watcher);
            SchedulePos schedule=new SchedulePos();set(EntityMaid.class,maid,"schedulePos",schedule);schedule.setAll(maid);
            maid.nav=(Navigation)MultiblockRegression.unsafe().allocateInstance(Navigation.class);
            check(!maid.canRunIdleMovement(),"rest path cannot be overwritten by wandering/cake");
            maid.mode=MaidSchedule.ALL;check(maid.canRunIdleMovement(),"all-day schedule does not invent sleep");maid.mode=MaidSchedule.DAY;
            maid.home=false;check(maid.canRunIdleMovement(),"follow mode retains idle movement");maid.home=true;
            seek.invoke(maid);check(maid.ridingEntity instanceof EntitySit&&maid.isMaidSleeping(),"nearby bed enters sleep");
            EntitySit seat=(EntitySit)maid.ridingEntity;
            check(seat.rotationYaw==facing*90F,"bed direction preserved");
            check(Math.abs(seat.posY+seat.getMountedYOffset()-64.8)<.00001,"sleep height matches SRC");
            maid.mountEntity(null);world.loadedEntityList.add(seat);seek.invoke(maid);check(maid.ridingEntity==null,"occupied bed not stolen");
            world.loadedEntityList.clear();maid.posX=5.5;
            world.put(1,63,0,Blocks.stone,0);world.put(-1,63,0,Blocks.stone,0);world.put(0,63,1,Blocks.stone,0);world.put(0,63,-1,Blocks.stone,0);
            seek.invoke(maid);check(maid.nav.attempts>0&&maid.ridingEntity==null,"distant bed approached through adjacent air");
            check(world.isAirBlock((int)Math.floor(maid.nav.x),64,(int)Math.floor(maid.nav.z)),"navigator target is not the solid bed");
            world.put(-dx,64,-dz,Blocks.air,0);maid.posX=.5;seek.invoke(maid);check(maid.ridingEntity==null,"missing foot prevents sleep");
        }
        System.out.println("Maid bed regression: "+checks+" checks PASS (four facings, approach, occupancy, schedule; no game loop)");
    }
    public static final class Maid extends EntityMaid{
        Navigation nav;boolean home;MaidSchedule mode;
        private Maid(World world){super(world);}
        @Override public boolean isHomeMode(){return home;}
        @Override public boolean isSitting(){return false;}
        @Override public MaidSchedule getSchedule(){return mode;}
        @Override public MaidActivity getCurrentActivity(){return MaidActivity.REST;}
        @Override public void setRestriction(int x,int y,int z,int radius){}
        @Override public boolean isPositionWithinRestriction(double x,double y,double z){return true;}
        @Override public PathNavigate getNavigator(){return nav;}
        @Override public void mountEntity(Entity seat){ridingEntity=seat;if(seat!=null)seat.riddenByEntity=this;}
    }
    public static final class Navigation extends PathNavigate{
        int attempts;double x,z;
        private Navigation(EntityLiving e,World w){super(e,w);}
        @Override public boolean tryMoveToXYZ(double x,double y,double z,double speed){attempts++;this.x=x;this.z=z;return true;}
        @Override public void clearPathEntity(){}
    }
}

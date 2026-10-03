package com.github.tartaricacid.touhoulittlemaid.test;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.*;
import com.github.tartaricacid.touhoulittlemaid.entity.projectile.EntityMaidFishingHook;
import com.github.tartaricacid.touhoulittlemaid.entity.animation.MaidActionState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.ai.EntityLookHelper;
import net.minecraft.init.Items;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.util.AxisAlignedBB;
import java.lang.reflect.Method;

/** Runs actual profession selection/transactions against an in-memory world. */
public final class MaidProfessionRegression {
    private static int checks;
    private static void check(boolean ok,String label){checks++;if(!ok)throw new AssertionError(label);}
    private static void box(Entity e,AxisAlignedBB box) throws Exception {java.lang.reflect.Field f=Entity.class.getDeclaredField("boundingBox");f.setAccessible(true);f.set(e,box);}
    public static void run() throws Exception {
        MultiblockRegression.MemoryWorld w=MultiblockRegression.world();
        Maid m=(Maid)MultiblockRegression.unsafe().allocateInstance(Maid.class);
        m.worldObj=w;m.bag=new InventoryBasic("bag",false,6);m.task=new InventoryBasic("task",false,6);m.equipment=new InventoryBasic("equip",false,2);
        m.visible=true;m.working=true;m.width=.6F;m.height=1.5F;m.posY=65;box(m,AxisAlignedBB.getBoundingBox(0,65,0,.6,66.5,.6));
        m.look=new EntityLookHelper(m);
        Cow cow=(Cow)MultiblockRegression.unsafe().allocateInstance(Cow.class);cow.worldObj=w;cow.posY=65;cow.posX=1;box(cow,AxisAlignedBB.getBoundingBox(1,65,0,2,67,1));w.loadedEntityList.add(cow);
        TaskMilk milk=new TaskMilk();m.bag.setInventorySlotContents(0,new ItemStack(Items.bucket,3));
        m.visible=false;milk.tick(m);check(m.outputs==0 && m.bag.getStackInSlot(0).stackSize==3,"milk cannot cross walls");
        m.visible=true;cow.isDead=true;milk.tick(m);check(m.outputs==0,"dead cow excluded");
        cow.isDead=false;cow.child=true;milk.tick(m);check(m.outputs==0,"calf excluded");
        cow.child=false;m.working=false;milk.tick(m);check(m.outputs==0,"milk respects schedule");
        m.working=true;m.sleeping=true;milk.tick(m);check(m.outputs==0,"milk respects sleep");m.sleeping=false;
        for(int i=1;i<6;i++)m.bag.setInventorySlotContents(i,new ItemStack(Items.stick));
        milk.tick(m);check(m.outputs==0,"milk waits for output space");m.bag.setInventorySlotContents(5,null);
        m.failConsume=true;milk.tick(m);check(m.outputs==0,"failed bucket transaction produces no milk");m.failConsume=false;
        milk.tick(m);check(m.outputs==1 && m.bag.getStackInSlot(0).stackSize==2,"one bucket for one milk");
        check(m.workItem!=null && m.workItem.getItem()==Items.bucket,"milking displays bucket rather than weapon");
        m.bag.setInventorySlotContents(0,null);milk.tick(m);check(m.outputs==1,"no bucket no milk");
        Method spot=TaskFishing.class.getDeclaredMethod("findSpot",EntityMaid.class);spot.setAccessible(true);
        TaskFishing fishing=new TaskFishing();w.put(1,64,0,Blocks.water,0);w.put(0,64,0,Blocks.stone,0);
        Object found=spot.invoke(fishing,m);check(found!=null,"normal shore one block above water is found");
        java.lang.reflect.Field standY=found.getClass().getDeclaredField("standY");standY.setAccessible(true);check(standY.getInt(found)==65,"normal shore feet height");
        w.testCollision=true;check(spot.invoke(fishing,m)==null,"blocked body/headroom rejected");w.testCollision=false;
        w.testLiquid=true;check(spot.invoke(fishing,m)==null,"underwater standing position rejected");w.testLiquid=false;
        m.restricted=true;check(spot.invoke(fishing,m)==null,"fishing respects home area");m.restricted=false;
        w.testUnloaded=true;check(spot.invoke(fishing,m)==null,"unloaded water rejected");w.testUnloaded=false;
        w.put(0,64,0,Blocks.air,0);w.put(0,63,0,Blocks.stone,0);found=spot.invoke(fishing,m);check(found!=null && standY.getInt(found)==64,"low shore still supported");
        EntityMaidFishingHook hook=new EntityMaidFishingHook(w);
        Method rod=EntityMaidFishingHook.class.getDeclaredMethod("getRod",EntityMaid.class,int.class);rod.setAccessible(true);
        m.equipment.setInventorySlotContents(0,new ItemStack(Items.fishing_rod));check(rod.invoke(hook,m,-2)!=null,"rod accepted");
        m.equipment.setInventorySlotContents(0,new ItemStack(Items.iron_sword));check(rod.invoke(hook,m,-2)==null,"swapped sword immediately invalidates rod");
        m.equipment.setInventorySlotContents(0,new ItemStack(Items.fishing_rod,0));check(rod.invoke(hook,m,-2)==null,"empty rod stack rejected");
        m.equipment.setInventorySlotContents(0,new ItemStack(Items.fishing_rod));m.setEntityId(900);w.loadedEntityList.add(m);
        EntityMaidFishingHook owned=new EntityMaidFishingHook(w,m,-2,1.5,64.85,.5);w.loadedEntityList.add(owned);
        Method active=TaskFishing.class.getDeclaredMethod("hasActiveHook",EntityMaid.class);active.setAccessible(true);
        check((Boolean)active.invoke(fishing,m),"live hook prevents duplicate cast");
        fishing.onDeselected(m);check(owned.isDead && !m.fishing,"switching profession removes hook and synchronized state");
        check(!(Boolean)active.invoke(fishing,m),"dead hook does not prevent next cast");
        MaidActionState server=new MaidActionState(),client=new MaidActionState();ItemStack bucket=new ItemStack(Items.bucket,3);
        server.beginUse(bucket,false,"work",10,false,100);server.swing(false,6,100);client.accept(server.snapshot(102),500);
        check("work".equals(client.kind(500)) && client.displayItem(500).getItem()==Items.bucket,"work item survives network snapshot");
        check(bucket.stackSize==3 && client.displayItem(500).stackSize==1,"presentation does not consume inventory");
        check(client.swinging(500) && !client.isRanged() && client.bowPullStage(500,false)==-1,"work swing does not trigger bow draw");
        check(client.expire(508) && client.displayItem(508)==null,"work presentation expires");
        System.out.println("Maid profession regression: "+checks+" checks PASS (in-memory world, no game loop)");
    }
    public static final class Cow extends EntityCow {
        boolean child;
        private Cow(World w){super(w);}
        @Override public boolean isEntityAlive(){return !isDead;}
        @Override public boolean isChild(){return child;}
    }
    public static final class Maid extends EntityMaid {
        InventoryBasic bag,task,equipment;EntityLookHelper look;boolean visible,working,sleeping,restricted,failConsume,fishing;int outputs;ItemStack workItem;
        private Maid(World w){super(w);}
        @Override public java.util.UUID getUniqueID(){return new java.util.UUID(0,900);}
        @Override public void setFishingHookActive(boolean value){fishing=value;}
        @Override public boolean isEntityAlive(){return !isDead;}
        @Override public boolean isSitting(){return false;}
        @Override public boolean isMaidSleeping(){return sleeping;}
        @Override public boolean isWorkingNow(){return working;}
        @Override public boolean isPeriodicTick(int interval){return true;}
        @Override public boolean canEntityBeSeen(Entity e){return visible;}
        @Override public boolean isPositionWithinRestriction(double x,double y,double z){return !restricted;}
        @Override public EntityLookHelper getLookHelper(){return look;}
        @Override public InventoryBasic getMaidInventory(){return bag;}
        @Override public InventoryBasic getMaidTaskInventory(){return task;}
        @Override public InventoryBasic getMaidEquipmentInventory(){return equipment;}
        @Override public int getBackpackCapacity(){return 6;}
        @Override public ItemStack takeOneFromSlot(int slot){return failConsume?null:(slot>=200?task:bag).decrStackSize(slot>=200?slot-200:slot,1);}
        @Override public ItemStack addToMaidInventory(ItemStack stack){if(stack.getItem()==Items.milk_bucket)outputs++;return null;}
        @Override public void workSwing(ItemStack item){workItem=item.copy();}
    }
}

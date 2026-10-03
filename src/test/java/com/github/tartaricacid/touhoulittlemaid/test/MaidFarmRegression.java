package com.github.tartaricacid.touhoulittlemaid.test;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskFarm;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import java.lang.reflect.Method;
import java.util.ArrayList;

public final class MaidFarmRegression {
    private static int checks;
    private static void check(boolean ok,String name){checks++;if(!ok)throw new AssertionError(name);}
    public static void run() throws Exception {
        MultiblockRegression.MemoryWorld w=MultiblockRegression.world();
        Maid m=(Maid)MultiblockRegression.unsafe().allocateInstance(Maid.class);
        m.worldObj=w;m.equipment=new InventoryBasic("equipment",false,6);m.bag=new InventoryBasic("bag",false,6);
        m.task=new InventoryBasic("task",false,6);m.received=new ArrayList<ItemStack>();m.capacity=6;
        MultiblockRegression.TestPlayer actor=MultiblockRegression.player();actor.worldObj=w;actor.inventory=new InventoryPlayer(actor);
        Method method=TaskFarm.class.getDeclaredMethod("workAt",EntityMaid.class,int.class,int.class,int.class,net.minecraft.entity.player.EntityPlayer.class);
        method.setAccessible(true); TaskFarm farm=new TaskFarm();
        Guard guard=new Guard();
        java.lang.reflect.Field busField=cpw.mods.fml.common.eventhandler.EventBus.class.getDeclaredField("busID");
        busField.setAccessible(true);int busId=busField.getInt(MinecraftForge.EVENT_BUS);
        // Register on the parent listener list: no full mod loader/game startup needed.
        new cpw.mods.fml.common.eventhandler.Event().getListenerList().register(busId,cpw.mods.fml.common.eventhandler.EventPriority.NORMAL,guard);
        try {
            net.minecraft.item.Item[] seeds={Items.wheat_seeds,Items.carrot,Items.potato,Items.nether_wart,Items.melon_seeds,Items.pumpkin_seeds};
            net.minecraft.block.Block[] plants={Blocks.wheat,Blocks.carrots,Blocks.potatoes,Blocks.nether_wart,Blocks.melon_stem,Blocks.pumpkin_stem};
            for(int i=0;i<seeds.length;i++) {
                w.put(0,63,0,i==3?Blocks.soul_sand:Blocks.farmland,0);w.put(0,64,0,Blocks.air,0);
                m.bag.setInventorySlotContents(0,new ItemStack(seeds[i],2));
                check(work(method,farm,m,actor),"plant vanilla seed "+i);
                check(w.getBlock(0,64,0)==plants[i] && m.bag.getStackInSlot(0).stackSize==1,"plant and consume exactly one "+i);
            }
            w.put(0,63,0,Blocks.farmland,0);w.put(0,64,0,Blocks.air,0);m.bag.setInventorySlotContents(0,new ItemStack(Items.wheat_seeds,2));
            guard.place=true;check(!work(method,farm,m,actor),"cancel placement");
            check(w.getBlock(0,64,0)==Blocks.air && m.bag.getStackInSlot(0).stackSize==2,"cancel restores block and seed");guard.place=false;
            w.refuseFarmChange=true;check(!work(method,farm,m,actor),"placement refusal");check(m.bag.getStackInSlot(0).stackSize==2,"refusal keeps seeds");w.refuseFarmChange=false;
            m.capacity=0;check(!work(method,farm,m,actor),"locked bag seeds unavailable");m.capacity=6;
            w.put(0,63,0,Blocks.stone,0);check(!work(method,farm,m,actor),"wrong soil");
            w.put(0,63,0,Blocks.farmland,0);w.put(0,64,0,Blocks.water,0);check(!work(method,farm,m,actor),"no planting in liquid");
            w.put(0,64,0,Blocks.wheat,6);check(!work(method,farm,m,actor),"immature crop untouched");
            w.put(0,64,0,Blocks.wheat,7);guard.breaking=true;check(!work(method,farm,m,actor),"cancel harvesting");
            check(w.getBlockMetadata(0,64,0)==7 && m.received.isEmpty(),"cancel keeps crop and yields nothing");guard.breaking=false;
            w.refuseFarmChange=true;check(!work(method,farm,m,actor),"harvest mutation refusal");check(m.received.isEmpty(),"no drops from refused reset");w.refuseFarmChange=false;
            check(work(method,farm,m,actor),"bare hand harvest");check(w.getBlock(0,64,0)==Blocks.wheat && w.getBlockMetadata(0,64,0)==0,"reset mode keeps crop");
            check(count(m,Items.wheat)==1 && m.bag.getStackInSlot(0).stackSize==2,"reset harvest source drops and no replant charge");
            m.received.clear();w.put(0,63,0,Blocks.soul_sand,0);w.put(0,64,0,Blocks.nether_wart,3);
            check(work(method,farm,m,actor),"wart reset");check(count(m,Items.nether_wart)==1,"SRC wart reset yields exactly one");
            m.received.clear();m.equipment.setInventorySlotContents(0,new ItemStack(Items.iron_hoe));m.bag.setInventorySlotContents(0,null);
            w.put(0,63,0,Blocks.farmland,0);w.put(0,64,0,Blocks.wheat,7);
            check(work(method,farm,m,actor),"hoe harvest");check(w.getBlock(0,64,0)==Blocks.air && count(m,Items.wheat)==1,"hoe removes crop");
            m.bag.setInventorySlotContents(0,new ItemStack(Items.wheat_seeds,2));w.put(0,64,0,Blocks.wheat,7);
            check(work(method,farm,m,actor),"hoe harvest then plant");check(w.getBlock(0,64,0)==Blocks.wheat && w.getBlockMetadata(0,64,0)==0 && m.bag.getStackInSlot(0).stackSize==1,"replant consumes seed");
            w.put(0,64,0,Blocks.air,0);m.restricted=true;check(!work(method,farm,m,actor),"outside home");m.restricted=false;
            w.testUnloaded=true;check(!work(method,farm,m,actor),"unloaded farmland");w.testUnloaded=false;
            w.isRemote=true;check(!work(method,farm,m,actor),"client cannot farm");w.isRemote=false;
            w.captureBlockSnapshots=true;check(!work(method,farm,m,actor),"outer placement transaction not changed");w.captureBlockSnapshots=false;
        } finally {cpw.mods.fml.common.eventhandler.ListenerList.unregisterAll(busId,guard);}
        System.out.println("Maid farm regression: "+checks+" checks PASS (real Forge events, in-memory world)");
    }
    private static boolean work(Method method,TaskFarm farm,Maid m,MultiblockRegression.TestPlayer p)throws Exception{return (Boolean)method.invoke(farm,m,0,64,0,p);}
    private static int count(Maid m,net.minecraft.item.Item item){int n=0;for(ItemStack s:m.received)if(s.getItem()==item)n+=s.stackSize;return n;}
    public static final class Guard implements cpw.mods.fml.common.eventhandler.IEventListener {
        boolean place,breaking;
        @Override public void invoke(cpw.mods.fml.common.eventhandler.Event event){
            if(event instanceof BlockEvent.PlaceEvent) place((BlockEvent.PlaceEvent)event);
            if(event instanceof BlockEvent.BreakEvent) breaking((BlockEvent.BreakEvent)event);
        }
        @SubscribeEvent public void place(BlockEvent.PlaceEvent e){if(place)e.setCanceled(true);}
        @SubscribeEvent public void breaking(BlockEvent.BreakEvent e){if(breaking)e.setCanceled(true);}
    }
    public static final class Maid extends EntityMaid {
        InventoryBasic equipment,bag,task;ArrayList<ItemStack> received;int capacity;boolean restricted;
        private Maid(World world){super(world);}
        @Override public InventoryBasic getMaidEquipmentInventory(){return equipment;}
        @Override public InventoryBasic getMaidInventory(){return bag;}
        @Override public InventoryBasic getMaidTaskInventory(){return task;}
        @Override public int getBackpackCapacity(){return capacity;}
        @Override public ItemStack getHeldItem(){return equipment.getStackInSlot(0);}
        @Override public boolean isPositionWithinRestriction(double x,double y,double z){return !restricted;}
        @Override public ItemStack addToMaidInventory(ItemStack stack){received.add(stack.copy());return null;}
        @Override public void swingItem(){}
    }
}

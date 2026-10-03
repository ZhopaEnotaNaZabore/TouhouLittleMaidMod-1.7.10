package com.github.tartaricacid.touhoulittlemaid.test;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskFeedOwner;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.util.FoodStats;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import java.util.ArrayList;
import java.util.Collection;
import java.lang.reflect.Method;

public final class MaidFeedRegression {
    private static int checks;
    private static void check(boolean ok,String name){checks++;if(!ok)throw new AssertionError(name);}
    public static void run() throws Exception {
        Maid m=(Maid)MultiblockRegression.unsafe().allocateInstance(Maid.class);
        m.worldObj=MultiblockRegression.world();m.bag=new InventoryBasic("bag",false,12);m.task=new InventoryBasic("task",false,6);m.capacity=6;
        Player p=(Player)MultiblockRegression.unsafe().allocateInstance(Player.class);
        p.worldObj=m.worldObj;p.food=new FoodStats();p.effects=new ArrayList<PotionEffect>();p.initState();
        Method priority=TaskFeedOwner.class.getDeclaredMethod("priority",ItemStack.class,EntityPlayer.class);priority.setAccessible(true);
        Method feed=TaskFeedOwner.class.getDeclaredMethod("feedFrom",EntityMaid.class,EntityPlayer.class,IInventory.class,int.class);feed.setAccessible(true);
        p.food.setFoodLevel(10);
        for(ItemStack bad:new ItemStack[]{new ItemStack(Items.rotten_flesh),new ItemStack(Items.spider_eye),new ItemStack(Items.poisonous_potato),new ItemStack(Items.chicken),new ItemStack(Items.fish,1,3)})
            check((Integer)priority.invoke(null,bad,p)==0,"reject harmful food "+bad.getItem());
        check((Integer)priority.invoke(null,new ItemStack(Items.fish,1,0),p)>0,"ordinary fish allowed");
        p.food.setFoodLevel(20);check((Integer)priority.invoke(null,new ItemStack(Items.apple),p)==0,"healthy full owner not fed");
        check((Integer)priority.invoke(null,new ItemStack(Items.milk_bucket),p)==0,"no needless milk");
        p.effects.add(new PotionEffect(Potion.poison.id,100));check((Integer)priority.invoke(null,new ItemStack(Items.milk_bucket),p)==4,"milk priority");
        m.bag.setInventorySlotContents(0,new ItemStack(Items.milk_bucket));
        check((Boolean)feed.invoke(null,m,p,m.bag,0),"milk callback");
        check(p.cures==1 && p.effects.isEmpty(),"milk cures effects");
        check(m.bag.getStackInSlot(0).getItem()==Items.bucket,"empty bucket retained in source slot");
        p.setHealth(8);m.bag.setInventorySlotContents(0,new ItemStack(Items.golden_apple));
        check((Boolean)feed.invoke(null,m,p,m.bag,0),"golden apple while full and injured");
        check(!p.effects.isEmpty() && m.bag.getStackInSlot(0)==null,"golden apple effects and consumption");
        p.setHealth(20);p.food.setFoodLevel(5);m.bag.setInventorySlotContents(0,new ItemStack(Items.mushroom_stew));
        check((Boolean)feed.invoke(null,m,p,m.bag,0),"stew callback");
        check(m.bag.getStackInSlot(0).getItem()==Items.bowl && p.food.getFoodLevel()==11,"stew nutrition and bowl");
        p.food.setFoodLevel(5);m.bag.setInventorySlotContents(0,new ItemStack(Items.mushroom_stew,2));m.overflow=true;
        check((Boolean)feed.invoke(null,m,p,m.bag,0),"stacked container food");
        check(m.bag.getStackInSlot(0).stackSize==1 && m.dropped.getItem()==Items.bowl,"full inventory drops remainder without loss");
        m.bag.setInventorySlotContents(0,null);m.bag.setInventorySlotContents(9,new ItemStack(Items.wheat));
        check(m.findInventorySlot(Items.wheat)==-1,"closed bag slot excluded");
        m.task.setInventorySlotContents(2,new ItemStack(Items.wheat));check(m.findInventorySlot(Items.wheat)==202,"task slot available");
        m.bag.setInventorySlotContents(1,new ItemStack(Items.wheat));check(m.findInventorySlot(Items.wheat)==202,"task slot preferred");
        m.task.setInventorySlotContents(2,null);check(m.findInventorySlot(Items.wheat)==1,"open bag slot available");
        m.bag.setInventorySlotContents(1,new ItemStack(Items.wheat,0));check(m.findInventorySlot(Items.wheat)==-1,"empty stack excluded");
        m.bag.setInventorySlotContents(1,new ItemStack(Items.iron_pickaxe));check(m.findAvailableInventorySlot(s->s.getItem()==Items.iron_pickaxe)==1,"predicate tool lookup");
        System.out.println("Maid feed/inventory regression: "+checks+" checks PASS (real item callbacks, no game loop)");
    }
    public static final class Maid extends EntityMaid {
        InventoryBasic bag,task;int capacity;boolean overflow;ItemStack dropped;
        private Maid(World w){super(w);}
        @Override public InventoryBasic getMaidInventory(){return bag;}
        @Override public InventoryBasic getMaidTaskInventory(){return task;}
        @Override public int getBackpackCapacity(){return capacity;}
        @Override public ItemStack addToMaidInventory(ItemStack stack){return overflow?stack:null;}
        @Override public EntityItem entityDropItem(ItemStack stack,float offset){dropped=stack.copy();return null;}
    }
    public static final class Player extends EntityPlayer {
        FoodStats food;ArrayList<PotionEffect> effects;int cures;net.minecraft.entity.ai.attributes.BaseAttributeMap attributes;
        private Player(){super(null,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"feed-test"));}
        @Override public FoodStats getFoodStats(){return food;}
        void initState(){capabilities=new net.minecraft.entity.player.PlayerCapabilities();dataWatcher=new net.minecraft.entity.DataWatcher(this);dataWatcher.addObject(6,20F);attributes=new net.minecraft.entity.ai.attributes.ServersideAttributeMap();attributes.registerAttribute(net.minecraft.entity.SharedMonsterAttributes.maxHealth).setBaseValue(20);}
        @Override public net.minecraft.entity.ai.attributes.IAttributeInstance getEntityAttribute(net.minecraft.entity.ai.attributes.IAttribute attribute){return attributes.getAttributeInstance(attribute);}
        @Override public Collection getActivePotionEffects(){return effects;}
        @Override public void addPotionEffect(PotionEffect effect){effects.add(effect);}
        @Override public void curePotionEffects(ItemStack stack){cures++;effects.clear();}
        @Override public java.util.Random getRNG(){return worldObj.rand;}
        @Override public void playSound(String sound,float volume,float pitch){}
        @Override public void addStat(net.minecraft.stats.StatBase stat,int amount){}
        @Override public net.minecraft.util.ChunkCoordinates getPlayerCoordinates(){return new net.minecraft.util.ChunkCoordinates(0,64,0);}
        @Override public boolean canCommandSenderUseCommand(int level,String command){return false;}
        @Override public void addChatMessage(net.minecraft.util.IChatComponent message){}
    }
}



package com.github.tartaricacid.touhoulittlemaid.test;
import com.github.tartaricacid.touhoulittlemaid.inventory.MaidFurnaceInventory;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.ContainerMaidFurnace;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
public final class MaidFurnaceRegression {
    private static int checks;
    private static void check(boolean ok,String name){checks++;if(!ok)throw new AssertionError(name);}
    public static void run() throws Exception {
        MaidFurnaceInventory f=new MaidFurnaceInventory(null);
        f.setInventorySlotContents(0,new ItemStack(Blocks.iron_ore,8));f.setInventorySlotContents(1,new ItemStack(Items.coal));
        for(int i=0;i<199;i++)f.tick();
        check(f.getStackInSlot(2)==null && f.furnaceCookTime==199,"200 tick recipe boundary");
        f.tick();check(f.getStackInSlot(2).stackSize==1 && f.getStackInSlot(0).stackSize==7,"first smelt");
        check(f.getStackInSlot(1)==null && f.furnaceBurnTime>0,"coal remains burning after consumption");
        NBTTagCompound saved=new NBTTagCompound();f.writeToNBT(saved);
        MaidFurnaceInventory loaded=new MaidFurnaceInventory(null);loaded.readFromNBT(saved);
        check(loaded.furnaceBurnTime==f.furnaceBurnTime && loaded.getStackInSlot(0).stackSize==7,"inventory and burn persist");
        for(int i=0;i<1400;i++)loaded.tick();
        check(loaded.getStackInSlot(0)==null && loaded.getStackInSlot(2).stackSize==8,"one coal smelts eight items");
        loaded.tick();check(loaded.furnaceBurnTime==0,"coal expires");
        f.clear();f.setInventorySlotContents(0,new ItemStack(Blocks.iron_ore));f.setInventorySlotContents(1,new ItemStack(Items.lava_bucket));f.tick();
        check(f.getStackInSlot(1).getItem()==Items.bucket && f.furnaceBurnTime==20000,"lava duration and bucket");
        f.clear();f.setInventorySlotContents(0,new ItemStack(Blocks.iron_ore));f.setInventorySlotContents(1,new ItemStack(Items.coal));f.setInventorySlotContents(2,new ItemStack(Items.iron_ingot,64));f.tick();
        check(f.getStackInSlot(1).stackSize==1 && f.furnaceBurnTime==0,"full output consumes no fuel");
        f.setInventorySlotContents(2,new ItemStack(Items.gold_ingot));f.tick();check(f.furnaceBurnTime==0,"different output blocks cooking");
        ItemStack named=new ItemStack(Items.iron_ingot);named.setStackDisplayName("named");f.setInventorySlotContents(2,named);f.tick();check(f.furnaceBurnTime==0,"NBT output mismatch blocks cooking");
        f.setInventorySlotContents(2,null);for(int i=0;i<30;i++)f.tick();f.setInventorySlotContents(0,new ItemStack(Blocks.gold_ore));
        check(f.furnaceCookTime==0,"different input resets progress");
        for(int i=0;i<40;i++)f.tick();saved=new NBTTagCompound();f.writeToNBT(saved);loaded.readFromNBT(saved);
        check(loaded.furnaceCookTime==40,"partial progress round trip");
        loaded.clear();check(loaded.getStackInSlot(0)==null && loaded.getStackInSlot(1)==null && loaded.getStackInSlot(2)==null && loaded.furnaceBurnTime==0 && loaded.furnaceCookTime==0,"detach clears inventory and timers");
        saved.setInteger("BurnTime",-1);saved.setInteger("CookTime",999);loaded.readFromNBT(saved);check(loaded.furnaceBurnTime==0 && loaded.furnaceCookTime==199,"invalid timers clamped");
        loaded.readFromNBT(new NBTTagCompound());check(loaded.getStackInSlot(0)==null && loaded.furnaceCookTime==0,"repeated empty load clears old inventory");
        MultiblockRegression.TestPlayer player=MultiblockRegression.player();player.worldObj=MultiblockRegression.world();player.inventory=new net.minecraft.entity.player.InventoryPlayer(player);
        ContainerMaidFurnace container=new ContainerMaidFurnace(player.inventory,f);
        check(container.inventorySlots.size()==39,"three furnace and 36 player slots");
        check(!container.getSlot(2).isItemValid(new ItemStack(Items.apple)),"output rejects insertion");
        f.clear();player.inventory.setInventorySlotContents(9,new ItemStack(Blocks.iron_ore,3));container.transferStackInSlot(player,3);
        check(f.getStackInSlot(0).stackSize==3 && player.inventory.getStackInSlot(9)==null,"shift click recipe into input");
        player.inventory.setInventorySlotContents(9,new ItemStack(Items.coal,2));container.transferStackInSlot(player,3);
        check(f.getStackInSlot(1).stackSize==2,"shift click fuel");
        f.setInventorySlotContents(2,new ItemStack(Items.iron_ingot,2));container.transferStackInSlot(player,2);
        check(f.getStackInSlot(2)==null,"shift click output");
        Maid m=(Maid)MultiblockRegression.unsafe().allocateInstance(Maid.class);m.worldObj=player.worldObj;m.owner=player;m.alive=true;m.type="furnace_backpack";
        MaidFurnaceInventory owned=new MaidFurnaceInventory(m);
        java.lang.reflect.Field field=com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid.class.getDeclaredField("furnaceInventory");field.setAccessible(true);field.set(m,owned);
        check(owned.isUseableByPlayer(player),"owner access");
        m.type="empty";check(!owned.isUseableByPlayer(player),"removed backpack invalidates container");m.type="furnace_backpack";
        m.alive=false;check(!owned.isUseableByPlayer(player),"dead maid rejects access");m.alive=true;
        m.owner=null;check(!owned.isUseableByPlayer(player),"non owner rejected");m.owner=player;
        player.posX=9;check(!owned.isUseableByPlayer(player),"distance rejects access");player.posX=0;
        owned.setInventorySlotContents(0,new ItemStack(Blocks.iron_ore,4));owned.setInventorySlotContents(1,new ItemStack(Items.coal,2));owned.furnaceBurnTime=100;
        java.lang.reflect.Method detach=com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid.class.getDeclaredMethod("dropFurnaceContents");detach.setAccessible(true);detach.invoke(m);
        check(m.dropped==6 && owned.getStackInSlot(0)==null && owned.furnaceBurnTime==0,"detach drops contents and clears timers");
        detach.invoke(m);check(m.dropped==6,"repeat detach cannot duplicate contents");
        ItemStack film=com.github.tartaricacid.touhoulittlemaid.item.ItemFilm.maidToFilm(m);
        check(!film.getTagCompound().getCompoundTag("MaidInfo").hasKey("MaidFurnace") && !film.getTagCompound().getCompoundTag("MaidInfo").hasKey("MaidBackpackData"),"film excludes separately dropped furnace contents");
        NBTTagCompound modern=new NBTTagCompound(),entry=new NBTTagCompound();entry.setString("id","minecraft:coal");entry.setByte("Count",(byte)2);entry.setByte("Slot",(byte)1);
        net.minecraft.nbt.NBTTagList items=new net.minecraft.nbt.NBTTagList();items.appendTag(entry);modern.setTag("Items",items);owned.readFromNBT(modern);
        check(owned.getStackInSlot(1)!=null && owned.getStackInSlot(1).getItem()==Items.coal && owned.getStackInSlot(1).stackSize==2,"modern string item id migrated");
        check(entry.hasKey("id",8),"migration leaves source NBT unchanged");
        System.out.println("Maid furnace regression: "+checks+" checks PASS (real furnace inventory/container, no game loop)");
    }
    public static final class Maid extends com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid {
        net.minecraft.entity.EntityLivingBase owner;boolean alive;String type;int dropped;
        private Maid(net.minecraft.world.World world){super(world);}
        @Override public boolean isEntityAlive(){return alive;}
        @Override public net.minecraft.entity.EntityLivingBase getOwner(){return owner;}
        @Override public String getBackpackType(){return type;}
        @Override public net.minecraft.entity.item.EntityItem entityDropItem(ItemStack stack,float offset){dropped+=stack.stackSize;return null;}
        @Override public void writeToNBT(NBTTagCompound tag){tag.setTag("MaidFurnace",new NBTTagCompound());tag.setTag("MaidBackpackData",new NBTTagCompound());}
    }
}

package com.github.tartaricacid.touhoulittlemaid.test;
import com.github.tartaricacid.touhoulittlemaid.inventory.*;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.ContainerMaidTank;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
public final class MaidTankRegression {
    private static int checks;
    private static void check(boolean ok,String name){checks++;if(!ok)throw new AssertionError(name);}
    public static void run() throws Exception {
        for(net.minecraft.item.Item full:new net.minecraft.item.Item[]{Items.water_bucket,Items.lava_bucket,Items.milk_bucket}) {
            ItemStack source=new ItemStack(full);
            LegacyTankTransfer.Result r=LegacyTankTransfer.transfer(source,"",0,true);
            check(r!=null && r.amount==1000 && r.container.getItem()==Items.bucket,"fill "+full);
            check(source.getItem()==full && source.stackSize==1,"input not mutated");
            LegacyTankTransfer.Result reverse=LegacyTankTransfer.transfer(r.container,r.fluid,r.amount,false);
            check(reverse!=null && reverse.amount==0 && reverse.fluid.isEmpty() && reverse.container.getItem()==full,"round trip "+full);
        }
        check(LegacyTankTransfer.transfer(new ItemStack(Items.water_bucket),"lava",1000,true)==null,"mix rejected");
        check(LegacyTankTransfer.transfer(new ItemStack(Items.water_bucket),"water",9500,true)==null,"capacity rejection");
        check(LegacyTankTransfer.transfer(new ItemStack(Items.bucket),"water",999,false)==null,"no partial bucket");
        check(LegacyTankTransfer.transfer(new ItemStack(Items.bucket),"",0,false)==null,"empty tank");
        check(LegacyTankTransfer.transfer(new ItemStack(Items.bucket),"unknown_fluid",1000,false)==null,"unknown fluid preserved");
        check(LegacyTankTransfer.transfer(new ItemStack(Items.bucket,2),"water",1000,false)==null,"stack requires single-item transaction");
        check(LegacyTankTransfer.transfer(new ItemStack(Items.water_bucket),"water",9000,true).amount==10000,"exact capacity accepted");
        net.minecraftforge.fluids.Fluid juice=new net.minecraftforge.fluids.Fluid("tlm_regression_juice");net.minecraftforge.fluids.FluidRegistry.registerFluid(juice);
        net.minecraftforge.fluids.FluidContainerRegistry.registerFluidContainer(new net.minecraftforge.fluids.FluidStack(juice,250),new ItemStack(Items.potionitem,1,42),new ItemStack(Items.glass_bottle));
        LegacyTankTransfer.Result custom=LegacyTankTransfer.transfer(new ItemStack(Items.potionitem,1,42),"",0,true);
        check(custom!=null && custom.amount==250 && custom.container.getItem()==Items.glass_bottle,"registered container uses its own volume");
        check(LegacyTankTransfer.transfer(custom.container,custom.fluid,custom.amount,false).container.getItemDamage()==42,"registered custom container round trip");
        Maid m=(Maid)MultiblockRegression.unsafe().allocateInstance(Maid.class);m.worldObj=MultiblockRegression.world();m.fluid="";m.type="tank_backpack";
        m.tank=new MaidTankInventory(m);m.tank.setInventorySlotContents(0,new ItemStack(Items.water_bucket));m.tank.tick();
        check(m.amount==1000 && m.tank.getStackInSlot(0).getItem()==Items.bucket,"input slot fills tank");m.tank.tick();check(m.amount==1000,"empty input not refilled");
        m.tank.setInventorySlotContents(1,new ItemStack(Items.bucket));m.tank.tick();check(m.amount==0 && m.tank.getStackInSlot(1).getItem()==Items.water_bucket,"output slot drains");m.tank.tick();check(m.amount==0,"filled output not drained back");
        NBTTagCompound nbt=m.tank.save();m.tank.clear();m.tank.load(nbt);check(m.tank.getStackInSlot(1).getItem()==Items.water_bucket,"slots persist");
        m.tank.load(new NBTTagCompound());check(m.tank.getStackInSlot(0)==null&&m.tank.getStackInSlot(1)==null,"empty reload clears slots");
        m.tank.setInventorySlotContents(0,new ItemStack(Items.water_bucket));m.worldObj.isRemote=true;m.tank.tick();check(m.amount==0,"client cannot transfer");m.worldObj.isRemote=false;m.tank.clear();
        MultiblockRegression.TestPlayer player=MultiblockRegression.player();player.worldObj=m.worldObj;player.inventory=new net.minecraft.entity.player.InventoryPlayer(player);m.owner=player;
        ContainerMaidTank container=new ContainerMaidTank(player.inventory,m);check(container.canInteractWith(player),"owner access");m.type="empty";check(!container.canInteractWith(player),"removal closes access");m.type="tank_backpack";
        player.inventory.setInventorySlotContents(9,new ItemStack(Items.apple));check(container.transferStackInSlot(player,2)==null && player.inventory.getStackInSlot(9).getItem()==Items.apple,"non-container shift rejected without loss");
        player.inventory.setInventorySlotContents(9,new ItemStack(Items.bucket,16));container.transferStackInSlot(player,2);
        check(m.tank.getStackInSlot(1).stackSize==1 && player.inventory.getStackInSlot(9).stackSize==15,"shift empty bucket moves one");
        container.transferStackInSlot(player,2);check(player.inventory.getStackInSlot(9).stackSize==15,"occupied slot leaves stack unchanged");
        m.tank.clear();player.inventory.setInventorySlotContents(9,new ItemStack(Items.water_bucket));container.transferStackInSlot(player,2);
        check(m.tank.getStackInSlot(0).getItem()==Items.water_bucket,"shift filled bucket selects input");
        java.lang.reflect.Field field=EntityMaid.class.getDeclaredField("tankInventory");field.setAccessible(true);field.set(m,m.tank);
        java.lang.reflect.Method detach=EntityMaid.class.getDeclaredMethod("dropTankContents");detach.setAccessible(true);detach.invoke(m);detach.invoke(m);
        check(m.dropped==1 && m.tank.getStackInSlot(0)==null,"detach drops exactly once");
        System.out.println("Maid tank regression: "+checks+" checks PASS (Forge fluid containers and inventory, no game loop)");
    }
    public static final class Maid extends EntityMaid {
        String fluid,type;int amount,dropped;MaidTankInventory tank;net.minecraft.entity.EntityLivingBase owner;
        private Maid(net.minecraft.world.World world){super(world);}
        @Override public String getBackpackFluid(){return fluid;}
        @Override public int getBackpackFluidAmount(){return amount;}
        @Override public void setBackpackFluidState(String fluid,int amount){this.fluid=fluid;this.amount=amount;}
        @Override public MaidTankInventory getTankInventory(){return tank;}
        @Override public String getBackpackType(){return type;}
        @Override public boolean isEntityAlive(){return true;}
        @Override public net.minecraft.entity.EntityLivingBase getOwner(){return owner;}
        @Override public net.minecraft.entity.item.EntityItem entityDropItem(ItemStack stack,float offset){dropped+=stack.stackSize;return null;}
    }
}

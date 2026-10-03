package com.github.tartaricacid.touhoulittlemaid.inventory;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

/** All-or-nothing transfers of one registered 1.7.10 fluid container. */
public final class LegacyTankTransfer {
    public static final int CAPACITY=10000;
    private LegacyTankTransfer(){}
    public static Result transfer(ItemStack container,String fluid,int amount,boolean intoTank){
        if(container==null || container.stackSize!=1 || amount<0 || amount>CAPACITY)return null;
        if(intoTank){
            FluidStack content=FluidContainerRegistry.getFluidForFilledItem(container);
            boolean milk=container.getItem()==Items.milk_bucket;
            if(!milk && (content==null || content.tag!=null))return null;
            String name=milk?"milk":content.getFluid().getName();int volume=milk?1000:content.amount;
            if(volume<=0 || amount>CAPACITY-volume || (amount>0&&!name.equals(fluid)))return null;
            ItemStack empty=milk?new ItemStack(Items.bucket):FluidContainerRegistry.drainFluidContainer(container);
            if(empty==null)return null;
            return new Result(empty,name,amount+volume);
        }
        if(amount<=0)return null;
        ItemStack filled;int volume;
        if("milk".equals(fluid) && container.getItem()==Items.bucket){filled=new ItemStack(Items.milk_bucket);volume=1000;}
        else {
            Fluid registered=FluidRegistry.getFluid(fluid);if(registered==null)return null;
            filled=FluidContainerRegistry.fillFluidContainer(new FluidStack(registered,amount),container);
            if(filled==null)return null;
            FluidStack content=FluidContainerRegistry.getFluidForFilledItem(filled);
            if(content==null || content.tag!=null || !content.getFluid().getName().equals(fluid))return null;
            volume=content.amount;
        }
        if(volume<=0 || volume>amount)return null;
        return new Result(filled,amount==volume?"":fluid,amount-volume);
    }
    public static final class Result {
        public final ItemStack container;public final String fluid;public final int amount;
        private Result(ItemStack container,String fluid,int amount){this.container=container;this.fluid=fluid;this.amount=amount;}
    }
}

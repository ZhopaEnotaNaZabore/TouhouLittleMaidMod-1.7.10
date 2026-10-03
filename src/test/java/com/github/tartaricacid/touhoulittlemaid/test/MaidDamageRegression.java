package com.github.tartaricacid.touhoulittlemaid.test;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;

/** Exercises the actual Forge damage/armor pipeline, not a replacement damage implementation. */
public final class MaidDamageRegression {
    private static int checks;
    private static void check(boolean condition,String message) { checks++; if(!condition) throw new AssertionError(message); }
    public static void run() throws Exception {
        Maid m = new Maid(MultiblockRegression.world());
        check(!m.isMaidInvulnerable() && !m.isEntityInvulnerable(), "default maid is vulnerable");
        check(m.attackEntityFrom(DamageSource.generic, 4) && m.getHealth() == 16, "unarmored physical damage reaches health");
        m.hurtResistantTime=0;
        check(m.attackEntityFrom(DamageSource.causeMobDamage(new Maid(m.worldObj)), 3) && m.getHealth() == 13, "mob melee reaches health");
        m.hurtResistantTime=0;
        EntityPlayer player=MultiblockRegression.player();player.worldObj=m.worldObj;
        check(m.attackEntityFrom(DamageSource.causePlayerDamage(player), 2) && m.getHealth() == 11, "player melee reaches health");
        m.setMaidInvulnerable(true);
        check(m.isEntityInvulnerable() && !m.attackEntityFrom(DamageSource.generic, 2), "Jizo intentionally protects maid");
        java.lang.reflect.Field inv=net.minecraft.entity.Entity.class.getDeclaredField("invulnerable");inv.setAccessible(true);inv.setBoolean(m,true);
        m.setMaidInvulnerable(false);m.hurtResistantTime=0;
        check(!m.isEntityInvulnerable() && m.attackEntityFrom(DamageSource.generic, 1) && m.getHealth()==10, "cleared synced flag overrides stale vanilla NBT state");
        ItemStack chest=new ItemStack(Items.diamond_chestplate);
        m.getMaidEquipmentInventory().setInventorySlotContents(4,chest);
        check(m.getEquipmentInSlot(3)==chest,"armor reads authoritative inventory immediately");
        float reduced=m.armor(new DamageSource("physical"),10);
        check(Math.abs(reduced-6.8F)<.001F && chest.getItemDamage()>0,"vanilla armor absorbs and wears");
        int wear=chest.getItemDamage();
        check(m.armor(new DamageSource("piercing").setDamageBypassesArmor(),10)==10 && chest.getItemDamage()==wear,"bypass respects ordinary armor");
        Special special=new Special();ItemStack stack=new ItemStack(special);m.setCurrentItemOrArmor(3,stack);
        check(m.armor(new DamageSource("physical"),10)==5 && special.damageCalls==1,"ISpecialArmor protection and damage callback");
        check(m.armor(new DamageSource("piercing").setDamageBypassesArmor(),10)==10,"special armor chooses bypass policy");
        special.breakOnDamage=true;m.armor(new DamageSource("physical"),10);
        check(m.getMaidEquipmentInventory().getStackInSlot(4)==null,"broken special armor cleared from authoritative inventory");
        m.setTamed(true);m.setCurrentItemOrArmor(0,new ItemStack(Items.diamond_sword));
        CancelDeath listener = new CancelDeath();
        java.lang.reflect.Field busField=cpw.mods.fml.common.eventhandler.EventBus.class.getDeclaredField("busID");
        busField.setAccessible(true);int busId=busField.getInt(net.minecraftforge.common.MinecraftForge.EVENT_BUS);
        new cpw.mods.fml.common.eventhandler.Event().getListenerList().register(busId,cpw.mods.fml.common.eventhandler.EventPriority.NORMAL,listener);
        try {
            m.setHealth(0);m.onDeath(DamageSource.generic);
            check(listener.calls==1 && m.getHeldItem()!=null && !m.isDead,"Forge death cancellation runs once before tombstone/inventory transfer");
        } finally {cpw.mods.fml.common.eventhandler.ListenerList.unregisterAll(busId,listener);}
        System.out.println("Maid damage regression: "+checks+" checks PASS (real Forge pipeline, memory world)");
    }
    public static final class Maid extends EntityMaid {
        public Maid(World world){super(world);}
        float armor(DamageSource source,float amount){return applyArmorCalculations(source,amount);}
        @Override public void playMaidVoice(String event) { }
    }
    public static final class CancelDeath implements cpw.mods.fml.common.eventhandler.IEventListener {
        int calls;
        public void invoke(cpw.mods.fml.common.eventhandler.Event event){if(event instanceof net.minecraftforge.event.entity.living.LivingDeathEvent){calls++;event.setCanceled(true);}}
    }
    private static final class Special extends ItemArmor implements ISpecialArmor {
        int damageCalls;boolean breakOnDamage;
        Special(){super(ItemArmor.ArmorMaterial.DIAMOND,0,1);}
        public ArmorProperties getProperties(EntityLivingBase wearer,ItemStack stack,DamageSource source,double damage,int slot){return new ArmorProperties(1,source.isUnblockable()?0:.5,1000);}
        public int getArmorDisplay(EntityPlayer wearer,ItemStack stack,int slot){return 10;}
        public void damageArmor(EntityLivingBase wearer,ItemStack stack,DamageSource source,int damage,int slot){damageCalls++;if(breakOnDamage)stack.stackSize=0;}
    }
}

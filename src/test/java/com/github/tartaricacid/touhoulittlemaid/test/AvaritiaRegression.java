package com.github.tartaricacid.touhoulittlemaid.test;
import com.github.tartaricacid.touhoulittlemaid.compat.LegacyAvaritia;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.item.*;
import net.minecraft.util.DamageSource;
import net.minecraft.potion.*;
import net.minecraft.entity.projectile.EntityArrow;
public final class AvaritiaRegression {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void run() throws Exception {
        net.minecraft.init.Bootstrap.func_151354_b();
        java.lang.reflect.Field mods=cpw.mods.fml.common.Loader.class.getDeclaredField("namedMods");mods.setAccessible(true);mods.set(cpw.mods.fml.common.Loader.instance(),new java.util.HashMap());
        MultiblockRegression.MemoryWorld world=MultiblockRegression.world();
        MaidDamageRegression.Maid maid=new MaidDamageRegression.Maid(world);
        for(int slot=1;slot<=4;slot++) {
            Item armor=(Item)Class.forName("fox.spiteful.avaritia.items.ItemArmorInfinity").getConstructor(int.class).newInstance(4-slot);
            ItemStack stack=new ItemStack(armor);maid.setCurrentItemOrArmor(slot,stack);
            check(LegacyAvaritia.armor(stack,4-slot),"native Infinity armor slot "+slot);
        }
        check(LegacyAvaritia.fullArmor(maid),"full Infinity set recognized");
        check(!maid.attackEntityFrom(new DamageSource("physical"),100) && maid.getHealth()==20,"Infinity armor native immunity policy");
        check(!LegacyAvaritia.protects(maid,new DamageSource("infinity")),"Infinity damage exception preserved");
        ItemStack boots=maid.getEquipmentInSlot(1);maid.setCurrentItemOrArmor(1,null);
        check(!LegacyAvaritia.fullArmor(maid),"incomplete set not invulnerable");maid.setCurrentItemOrArmor(1,boots);
        maid.setAir(1);maid.setHunger(10);maid.setFire(10);maid.addPotionEffect(new PotionEffect(Potion.poison.id,200));maid.addPotionEffect(new PotionEffect(Potion.moveSpeed.id,200));
        Class.forName("fox.spiteful.avaritia.PotionHelper").getMethod("healthInspection").invoke(null);
        LegacyAvaritia.tickArmor(maid);
        check(maid.getAir()==300 && maid.getHunger()==100,"helmet breathing and maid hunger");
        check(!maid.isBurning(),"leggings extinguish fire");
        check(!maid.isPotionActive(Potion.poison) && maid.isPotionActive(Potion.moveSpeed),"native bad potion filter retains positive effects");
        maid.onGround=true;maid.moveForward=1;double motionBefore=maid.motionZ;
        java.lang.reflect.Field fast=Class.forName("fox.spiteful.avaritia.Config").getField("fast");fast.setBoolean(null,true);
        LegacyAvaritia.tickArmor(maid);check(maid.motionZ>motionBefore,"Infinity boots accelerate moving maid");
        motionBefore=maid.motionZ;fast.setBoolean(null,false);LegacyAvaritia.tickArmor(maid);
        check(maid.motionZ==motionBefore,"Infinity boots obey fast config");fast.setBoolean(null,true);
        Item sword=(Item)Class.forName("fox.spiteful.avaritia.items.tools.ItemSwordInfinity").newInstance();
        ItemStack weapon=new ItemStack(sword);maid.setCurrentItemOrArmor(0,weapon);
        check(LegacyAvaritia.isSword(weapon),"native sword recognized");
        Victim victim=new Victim(world);victim.setPosition(6,64,0);maid.setPosition(0,64,0);
        check(LegacyAvaritia.attack(maid,victim) && victim.getHealth()==0 && victim.died,"native sword callback executes kill effect");
        Item bow=(Item)Class.forName("fox.spiteful.avaritia.items.tools.ItemBowInfinity").newInstance();
        cpw.mods.fml.common.registry.GameRegistry.registerItem(bow,"probe_infinity_bow","avaritia_probe");
        weapon=new ItemStack(bow);maid.setCurrentItemOrArmor(0,weapon);
        check(LegacyAvaritia.isBow(weapon) && maid.hasBowAndArrow(),"Infinity bow ready without vanilla ammo");
        check(com.github.tartaricacid.touhoulittlemaid.item.ItemAnimationGun.isBowWeapon(bow),"ranged task and animation recognize Infinity bow");
        maid.getActionState().beginUse(weapon,false,"bow",72000,true,0);
        maid.ticksExisted=12;check(!LegacyAvaritia.fire(maid,victim),"no shot before native 13 tick draw");
        maid.ticksExisted=13;check(LegacyAvaritia.fire(maid,victim),"native Heaven arrow spawns");
        EntityArrow arrow=(EntityArrow)world.lastSpawn;
        check(arrow.getClass().getName().equals("fox.spiteful.avaritia.entity.EntityHeavenArrow") && arrow.shootingEntity==maid,"native barrage projectile and actual maid shooter");
        check(arrow.getDamage()==60 && arrow.getIsCritical() && arrow.canBePickedUp==2,"native damage, critical and no ammo duplication");
        arrow.getClass().getMethod("barrage").invoke(arrow);
        check(world.lastSpawn.getClass().getName().equals("fox.spiteful.avaritia.entity.EntityHeavenSubArrow"),"native barrage callback executes");
        System.out.println("Avaritia 1.13 regression: "+checks+" checks PASS (real mod jar, memory world)");
    }
    private static final class Victim extends EntityMaid {
        boolean died;
        Victim(net.minecraft.world.World world){super(world);}
        @Override public void onDeath(DamageSource source){died=true;}
    }
}

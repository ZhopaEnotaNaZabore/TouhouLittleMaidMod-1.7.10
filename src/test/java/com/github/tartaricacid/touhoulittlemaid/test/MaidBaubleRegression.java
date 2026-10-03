package com.github.tartaricacid.touhoulittlemaid.test;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.ModItems;
import com.github.tartaricacid.touhoulittlemaid.item.ItemAnimationGun;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.entity.*;
import net.minecraft.potion.Potion;
public final class MaidBaubleRegression {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void run() throws Exception {
        Maid m=(Maid)MultiblockRegression.unsafe().allocateInstance(Maid.class);
        m.worldObj=MultiblockRegression.world();m.init();
        InventoryBasic bag=new InventoryBasic("baubles",false,30);
        java.lang.reflect.Field field=EntityMaid.class.getDeclaredField("maidBaubleInventory");field.setAccessible(true);field.set(m,bag);
        ItemStack fall=new ItemStack(ModItems.FALL_PROTECT_BAUBLE);bag.setInventorySlotContents(0,fall);
        check(!m.attackEntityFrom(DamageSource.fall,10)&&fall.getItemDamage()==1,"fall blocked and charged once");
        m.worldObj.isRemote=true;m.attackEntityFrom(DamageSource.fall,10);check(fall.getItemDamage()==1,"no client accessory mutation");m.worldObj.isRemote=false;
        m.attackEntityFrom(DamageSource.fall,0);check(fall.getItemDamage()==1,"zero damage not charged");
        fall.setItemDamage(fall.getMaxDamage()-1);m.attackEntityFrom(DamageSource.fall,2);
        check(bag.getStackInSlot(0)==null,"last charge breaks at SRC durability boundary");
        bag.setInventorySlotContents(0,new ItemStack(ModItems.DROWN_PROTECT_BAUBLE));
        check(!m.attackEntityFrom(DamageSource.drown,2)&&m.getAir()==300,"drowning restores air");
        bag.setInventorySlotContents(0,new ItemStack(ModItems.NIMBLE_FABRIC));
        check(!m.attackEntityFrom(new DamageSource("test").setProjectile(),3),"nimble cancels even when no safe destination exists");
        check(bag.getStackInSlot(0).getItemDamage()==1,"failed teleport still charges fabric as SRC");
        bag.setInventorySlotContents(0,new ItemStack(ModItems.MUTE_BAUBLE,0));check(!m.isMuted(),"zero count bauble inactive");
        bag.setInventorySlotContents(0,null);bag.setInventorySlotContents(20,new ItemStack(ModItems.MUTE_BAUBLE));check(!m.isMuted(),"locked slot inactive");
        bag.setInventorySlotContents(0,new ItemStack(ModItems.MUTE_BAUBLE));check(m.isMuted(),"open slot active");
        ItemStack life=new ItemStack(ModItems.ULTRAMARINE_ORB_ELIXIR);bag.setInventorySlotContents(0,life);m.setHealth(0);m.onDeath(DamageSource.generic);
        check(m.getHealth()==m.getMaxHealth()&&life.getItemDamage()==1&&!m.isDead,"elixir intercepts actual death");
        check(ItemAnimationGun.isBowWeapon(ModItems.ANIMATION_RIFLE)&&ItemAnimationGun.isBowWeapon(ModItems.ANIMATION_PISTOL)&&ItemAnimationGun.isBowWeapon(ModItems.ANIMATION_RPG),"triggers accepted as bow weapons");
        check(!ItemAnimationGun.isBowWeapon(Items.diamond_sword),"sword not accepted as gun");
        System.out.println("Maid bauble/gun regression: "+checks+" checks PASS (in-memory world)");
    }
    public static final class Maid extends EntityMaid {
        net.minecraft.entity.ai.attributes.BaseAttributeMap attributes;
        private Maid(World world){super(world);}
        void init(){dataWatcher=new DataWatcher(this);dataWatcher.addObject(6,20F);dataWatcher.addObject(1,(short)0);attributes=new net.minecraft.entity.ai.attributes.ServersideAttributeMap();attributes.registerAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20);rand=new java.util.Random(1);width=.6F;height=1.5F;}
        @Override public net.minecraft.entity.ai.attributes.IAttributeInstance getEntityAttribute(net.minecraft.entity.ai.attributes.IAttribute a){return attributes.getAttributeInstance(a);}
        @Override public net.minecraft.item.ItemStack getEquipmentInSlot(int slot){return null;}
        @Override public boolean isMaidInvulnerable(){return false;}
        @Override public boolean isEntityAlive(){return true;}
        @Override public boolean isPotionActive(Potion p){return false;}
        @Override public int getBaubleCapacity(){return 10;}
        @Override public java.util.Random getRNG(){return rand;}
    }
}

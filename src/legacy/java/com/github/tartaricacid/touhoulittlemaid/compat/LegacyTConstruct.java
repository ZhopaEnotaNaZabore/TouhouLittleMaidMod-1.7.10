package com.github.tartaricacid.touhoulittlemaid.compat;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.*;
import net.minecraftforge.common.util.FakePlayer;
import java.lang.reflect.Method;

/** Optional TiC 1.8.x / Minecraft 1.7.10 adapter. No TiC classes enter our linkage. */
public final class LegacyTConstruct {
    public enum Kind { NONE, BOW, CROSSBOW, JAVELIN, THROWING }
    private LegacyTConstruct() { }
    public static boolean installed(){
        // Registry probes also run before FML has populated its mod index.
        try{return cpw.mods.fml.common.Loader.isModLoaded("TConstruct");}
        catch(NullPointerException notInitialized){return false;}
    }
    public static boolean type(ItemStack stack,String name){
        if(stack==null || stack.getItem()==null)return false;
        for(Class<?> c=stack.getItem().getClass();c!=null;c=c.getSuperclass())if(c.getName().equals(name))return true;
        return false;
    }
    public static boolean tool(ItemStack s){return type(s,"tconstruct.library.tools.ToolCore");}
    public static boolean usable(ItemStack s){return s!=null && s.stackSize>0 && s.hasTagCompound()
            && s.getTagCompound().hasKey("InfiTool",10) && !tags(s).getBoolean("Broken");}
    private static NBTTagCompound tags(ItemStack s){return s.getTagCompound().getCompoundTag("InfiTool");}
    public static Kind kind(ItemStack s){
        if(type(s,"tconstruct.weaponry.weapons.Crossbow"))return Kind.CROSSBOW;
        if(type(s,"tconstruct.library.weaponry.BowBaseAmmo"))return Kind.BOW;
        if(type(s,"tconstruct.weaponry.weapons.Javelin"))return Kind.JAVELIN;
        if(type(s,"tconstruct.library.weaponry.AmmoWeapon"))return Kind.THROWING;
        return Kind.NONE;
    }
    public static boolean matchesTask(ItemStack s,String task){
        Kind k=kind(s);
        return usable(s) && (TaskManager.CROSSBOW_ATTACK_ID.equals(task)?k==Kind.CROSSBOW:
                TaskManager.TRIDENT_ATTACK_ID.equals(task)?k==Kind.JAVELIN:
                TaskManager.RANGED_ATTACK_ID.equals(task)&&(k==Kind.BOW||k==Kind.JAVELIN||k==Kind.THROWING));
    }
    public static boolean melee(ItemStack s){
        if(!tool(s)||!usable(s)||kind(s)!=Kind.NONE)return false;
        try {for(String trait:(String[])call(s.getItem(),"getTraits",new Class[0]))if("melee".equals(trait))return true;}
        catch(Exception | LinkageError e){warn(e);}
        return type(s,"tconstruct.library.tools.Weapon");
    }
    public static int ammo(ItemStack s){
        if(!usable(s))return 0;
        try{return ((Number)call(s.getItem(),"getAmmoCount",new Class[]{ItemStack.class},s)).intValue();}
        catch(Exception | LinkageError e){return 0;}
    }
    public static int ammoSlot(EntityMaid maid,Kind k){
        int slot=maid.findAvailableInventorySlot(s->ammo(s)>0 && type(s,k==Kind.CROSSBOW?
                "tconstruct.weaponry.ammo.BoltAmmo":"tconstruct.weaponry.ammo.ArrowAmmo"));
        return slot>=0?slot:k==Kind.BOW?maid.findInventorySlot(Items.arrow):-1;
    }
    public static boolean ready(EntityMaid maid){
        ItemStack s=maid.getHeldItem();if(!matchesTask(s,maid.getTaskId()))return false;
        Kind k=kind(s);
        if(k==Kind.THROWING||k==Kind.JAVELIN)return ammo(s)>0;
        if(k==Kind.CROSSBOW && loaded(s)!=null)return true;
        return ammoSlot(maid,k)>=0;
    }
    private static ItemStack loaded(ItemStack s){
        if(!usable(s)||!tags(s).getBoolean("Loaded"))return null;
        ItemStack loaded=ItemStack.loadItemStackFromNBT(tags(s).getCompoundTag("LoadedItem"));
        return type(loaded,"tconstruct.weaponry.ammo.BoltAmmo")?loaded:null;
    }
    public static int windup(ItemStack s){
        try{return Math.max(1,Math.min(72000,((Number)call(s.getItem(),"getWindupTime",new Class[]{ItemStack.class},s)).intValue()));}
        catch(Exception | LinkageError e){return 20;}
    }
    public static String animation(ItemStack s){Kind k=kind(s);return k==Kind.CROSSBOW?"crossbow":k==Kind.JAVELIN||k==Kind.THROWING?"spear":k==Kind.BOW?"bow":"";}
    public static String category(ItemStack s){
        String a=animation(s);if(!a.isEmpty())return "crossbow".equals(a)&&loaded(s)!=null?"charged_crossbow":a;
        if(melee(s))return mining(s)?"pickaxe":"sword";return "";
    }
    public static boolean mining(ItemStack s){return usable(s) && (type(s,"tconstruct.items.tools.Pickaxe")||type(s,"tconstruct.items.tools.Hammer"));}
    /** Use TC's actual projectile factory; charge ammo/durability only after a successful spawn. */
    public static boolean fire(EntityMaid maid,EntityLivingBase target){
        ItemStack weapon=maid.getHeldItem();
        if(maid.worldObj.isRemote || !(maid.worldObj instanceof WorldServer) || !ready(maid))return false;
        Kind kind=kind(weapon);int draw=windup(weapon);
        if(maid.getActionState().useElapsed(maid.ticksExisted)<draw && !(kind==Kind.CROSSBOW&&loaded(weapon)!=null))return false;
        try {
            boolean thrown=kind==Kind.THROWING||kind==Kind.JAVELIN;
            ItemStack charged=kind==Kind.CROSSBOW?loaded(weapon):null;
            int slot=thrown||charged!=null?-1:ammoSlot(maid,kind);
            ItemStack ammunition=thrown?weapon:charged!=null?charged:maid.getStackInLogicalSlot(slot);
            if(ammunition==null)return false;
            Actor actor=new Actor(maid);
            double dx=target.posX-maid.posX,dz=target.posZ-maid.posZ;
            double dy=target.boundingBox.minY+target.height*.5-(maid.posY+maid.getEyeHeight());
            actor.setLocationAndAngles(maid.posX,maid.posY,maid.posZ,(float)(Math.atan2(dz,dx)*180/Math.PI)-90,
                    (float)(-Math.atan2(dy+Math.sqrt(dx*dx+dz*dz)*.1D,Math.sqrt(dx*dx+dz*dz))*180/Math.PI));
            actor.inventory.mainInventory[0]=weapon;
            float accuracy=((Number)call(weapon.getItem(),"getAccuracy",new Class[]{ItemStack.class,int.class},weapon,draw)).floatValue();
            ItemStack reference=ammunition.copy();reference.stackSize=1;
            if(tool(reference))call(reference.getItem(),"setAmmo",new Class[]{int.class,ItemStack.class},1,reference);
            Entity projectile;
            float progress=1F;
            if(thrown){
                projectile=(Entity)call(weapon.getItem(),"createProjectile",new Class[]{ItemStack.class,World.class,EntityPlayer.class,float.class,int.class},reference,maid.worldObj,actor,accuracy,draw);
            } else {
                float speed=((Number)call(weapon.getItem(),"getProjectileSpeed",new Class[]{ItemStack.class},weapon)).floatValue();
                if(kind==Kind.BOW){
                    net.minecraftforge.event.entity.player.ArrowLooseEvent event=new net.minecraftforge.event.entity.player.ArrowLooseEvent(actor,weapon,draw);
                    if(net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event))return false;
                    progress=((Number)call(weapon.getItem(),"getWindupProgress",new Class[]{ItemStack.class,int.class},weapon,event.charge)).floatValue();
                    float minimum=((Number)call(weapon.getItem(),"getMinWindupProgress",new Class[]{ItemStack.class},weapon)).floatValue();
                    if(progress<minimum)return false;speed*=progress;
                }
                projectile=(Entity)call(weapon.getItem(),"createProjectile",new Class[]{ItemStack.class,World.class,EntityPlayer.class,float.class,float.class,float.class},reference,maid.worldObj,actor,speed,accuracy,progress);
            }
            if(!(projectile instanceof EntityArrow))return false;
            EntityArrow arrow=(EntityArrow)projectile;
            // TC only runs active projectile modifiers for an EntityPlayer shooter.
            // Keep the isolated actor and start outside the maid's collision box.
            double horizontal=Math.max(.001,Math.sqrt(dx*dx+dz*dz)),offset=maid.width*.5D+.5D;
            arrow.setPosition(maid.posX+dx/horizontal*offset,maid.posY+maid.getEyeHeight(),maid.posZ+dz/horizontal*offset);
            double velocity=Math.sqrt(arrow.motionX*arrow.motionX+arrow.motionY*arrow.motionY+arrow.motionZ*arrow.motionZ);
            if(!Double.isFinite(velocity)||velocity<=0)return false;
            // Keep the factory trajectory: TC applies material-dependent spread here.
            if(!maid.worldObj.spawnEntityInWorld(arrow))return false;
            if(charged!=null){tags(weapon).setBoolean("Loaded",false);tags(weapon).removeTag("LoadedItem");tags(weapon).removeTag("Reloading");}
            else if(tool(ammunition))call(ammunition.getItem(),"consumeAmmo",new Class[]{int.class,ItemStack.class},1,ammunition);
            else maid.takeOneFromSlot(slot);
            if(!thrown && maid.getRNG().nextInt(10)<10-tags(weapon).getInteger("Unbreaking"))damage(weapon,maid);
            if(slot>=0)maid.markLogicalInventoryDirty(slot);
            maid.getMaidEquipmentInventory().markDirty();
            maid.playSound("random.bow",1,.7F);
            return true;
        } catch(Exception | LinkageError e){warn(e);return false;}
    }
    public static boolean attack(EntityMaid maid,EntityLivingBase target){
        ItemStack weapon=maid.getHeldItem();if(!melee(weapon)||!(maid.worldObj instanceof WorldServer))return false;
        // Native callback preserves rapier armor piercing, cleaver recovery and active modifiers.
        Actor actor=new Actor(maid);actor.inventory.mainInventory[0]=weapon;
        actor.setLocationAndAngles(maid.posX,maid.posY,maid.posZ,maid.rotationYaw,maid.rotationPitch);
        float health=target.getHealth(),absorption=target.getAbsorptionAmount();
        weapon.getItem().onLeftClickEntity(weapon,actor,target);
        maid.getMaidEquipmentInventory().markDirty();maid.setLastAttacker(target);
        return target.getHealth()<health || target.getAbsorptionAmount()<absorption;
    }
    /** Returns true for a handled TC projectile, including a full-inventory failure. */
    public static boolean pickup(EntityMaid maid,EntityArrow arrow){
        boolean tc=false;for(Class<?> c=arrow.getClass();c!=null;c=c.getSuperclass())if(c.getName().equals("tconstruct.library.entity.ProjectileBase"))tc=true;
        if(!tc)return false;
        try {
            ItemStack reference=(ItemStack)call(arrow,"getEntityItem",new Class[0]);if(reference==null)return true;
            ItemStack candidate=maid.getHeldItem();
            if(!mergeAmmo(reference,candidate)) {
                final ItemStack ref=reference;
                int slot=maid.findAvailableInventorySlot(s->{try{return canMergeAmmo(ref,s);}catch(Exception | LinkageError e){return false;}});
                if(slot>=0){candidate=maid.getStackInLogicalSlot(slot);if(mergeAmmo(reference,candidate)){maid.markLogicalInventoryDirty(slot);arrow.setDead();}}
                else {ItemStack remaining=maid.addToMaidInventory(reference.copy());if(remaining==null||remaining.stackSize<=0)arrow.setDead();}
            } else {maid.getMaidEquipmentInventory().markDirty();arrow.setDead();}
        } catch(Exception | LinkageError e){warn(e);}
        return true;
    }
    private static boolean canMergeAmmo(ItemStack ref,ItemStack target)throws Exception {
        return target!=null && Boolean.TRUE.equals(call(ref.getItem(),"testIfAmmoMatches",new Class[]{ItemStack.class,ItemStack.class},ref,target))
                && ammo(ref)>0 && ammo(ref)<=((Number)call(target.getItem(),"getMaxAmmo",new Class[]{ItemStack.class},target)).intValue()-ammo(target);
    }
    private static boolean mergeAmmo(ItemStack ref,ItemStack target)throws Exception {
        if(!canMergeAmmo(ref,target))return false;
        int count=ammo(ref);return count>0 && ((Number)call(target.getItem(),"addAmmo",new Class[]{int.class,ItemStack.class},count,target)).intValue()==0;
    }
    private static void damage(ItemStack s,EntityLivingBase maid)throws Exception{
        Class<?> helper=Class.forName("tconstruct.library.tools.AbilityHelper");
        helper.getMethod("damageTool",ItemStack.class,int.class,EntityLivingBase.class,boolean.class).invoke(null,s,1,maid,false);
    }
    private static Object call(Object receiver,String name,Class<?>[] signature,Object...args)throws Exception{
        for(Class<?> c=receiver.getClass();c!=null;c=c.getSuperclass())try{Method method=c.getDeclaredMethod(name,signature);method.setAccessible(true);return method.invoke(receiver,args);}catch(NoSuchMethodException ignored){}
        throw new NoSuchMethodException(receiver.getClass().getName()+"."+name);
    }
    private static boolean warned;
    private static void warn(Throwable error){if(!warned){warned=true;org.apache.logging.log4j.LogManager.getLogger("TLM-TConstruct").warn("Unsupported TConstruct API",error);}}
    /** Never registered in the world or borrowed from a real player's inventory. */
    private static final class Actor extends FakePlayer {
        private EntityMaid maid;
        Actor(EntityMaid maid){super((WorldServer)maid.worldObj,new GameProfile(maid.getUniqueID(),"[TLM-Maid]"));this.maid=maid;capabilities.isCreativeMode=false;onGround=maid.onGround;fallDistance=maid.fallDistance;setSprinting(maid.isSprinting());
            for(Object effect:maid.getActivePotionEffects())super.addPotionEffect(new net.minecraft.potion.PotionEffect((net.minecraft.potion.PotionEffect)effect));}
        @Override public float getEyeHeight(){return maid==null?1.62F:maid.getEyeHeight();}
        @Override public void heal(float amount){if(maid!=null)maid.heal(amount);else super.heal(amount);}
        @Override public void addPotionEffect(net.minecraft.potion.PotionEffect effect){if(maid!=null)maid.addPotionEffect(effect);else super.addPotionEffect(effect);}
    }
}

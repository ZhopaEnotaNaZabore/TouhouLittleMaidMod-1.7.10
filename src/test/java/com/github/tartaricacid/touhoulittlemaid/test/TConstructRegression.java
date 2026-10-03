package com.github.tartaricacid.touhoulittlemaid.test;
import com.github.tartaricacid.touhoulittlemaid.compat.LegacyTConstruct;
import com.github.tartaricacid.touhoulittlemaid.compat.miner.LegacyMiningToolCompat;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.init.*;
/** Executed only with a separately supplied real TiC dev jar. */
public final class TConstructRegression {
    static int checks;
    static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
    static ItemStack stack(String name)throws Exception{
        Item item=(Item)Class.forName(name).newInstance();
        cpw.mods.fml.common.registry.GameRegistry.registerItem(item,"probe_"+name.replace('.','_'),"tic_probe");
        ItemStack stack=new ItemStack(item);NBTTagCompound root=new NBTTagCompound(),tool=new NBTTagCompound();
        tool.setInteger("TotalDurability",100);tool.setInteger("Ammo",5);tool.setInteger("DrawSpeed",30);tool.setFloat("FlightSpeed",3);tool.setFloat("Mass",1.8F);tool.setFloat("Accuracy",100F);tool.setInteger("HarvestLevel",1);tool.setInteger("MiningSpeed",600);
        root.setTag("InfiTool",tool);stack.setTagCompound(root);return stack;
    }
    public static void run()throws Exception{
        Bootstrap.func_151354_b();
        Blocks.diamond_ore.setHarvestLevel("pickaxe",2);
        ItemStack arrows=stack("tconstruct.weaponry.ammo.ArrowAmmo"),bolts=stack("tconstruct.weaponry.ammo.BoltAmmo");
        MultiblockRegression.TestPlayer player=MultiblockRegression.player();player.worldObj=MultiblockRegression.world();
        java.lang.reflect.Method invoke=LegacyTConstruct.class.getDeclaredMethod("call",Object.class,String.class,Class[].class,Object[].class);invoke.setAccessible(true);
        for(String name:new String[]{"ShortBow","LongBow","Crossbow","Javelin","ThrowingKnife","Shuriken"}){
            ItemStack s=stack("tconstruct.weaponry.weapons."+name);
            LegacyTConstruct.Kind k=LegacyTConstruct.kind(s);check(k!=LegacyTConstruct.Kind.NONE,name+" recognized");
            String task=name.equals("Crossbow")?TaskManager.CROSSBOW_ATTACK_ID:TaskManager.RANGED_ATTACK_ID;
            check(LegacyTConstruct.matchesTask(s,task),name+" profession");
            check(!LegacyTConstruct.melee(s),name+" not selected as melee");
            check(LegacyTConstruct.windup(s)>0,name+" valid windup");
            if(name.equals("Javelin"))check(LegacyTConstruct.matchesTask(s,TaskManager.TRIDENT_ATTACK_ID),"javelin trident profession");
            if(k==LegacyTConstruct.Kind.JAVELIN||k==LegacyTConstruct.Kind.THROWING){
                check(LegacyTConstruct.ammo(s)==5,name+" native ammo count");
                s.getItem().getClass().getMethod("consumeAmmo",int.class,ItemStack.class).invoke(s.getItem(),1,s);
                check(LegacyTConstruct.ammo(s)==4 && s.stackSize==1,name+" native ammo consumption keeps item");
            }
            boolean thrown=k==LegacyTConstruct.Kind.JAVELIN||k==LegacyTConstruct.Kind.THROWING;
            ItemStack reference=(thrown?s:k==LegacyTConstruct.Kind.CROSSBOW?bolts:arrows).copy();reference.getTagCompound().getCompoundTag("InfiTool").setInteger("Ammo",1);
            Class[] signature=thrown?new Class[]{ItemStack.class,net.minecraft.world.World.class,net.minecraft.entity.player.EntityPlayer.class,float.class,int.class}:
                    new Class[]{ItemStack.class,net.minecraft.world.World.class,net.minecraft.entity.player.EntityPlayer.class,float.class,float.class,float.class};
            Object[] values=thrown?new Object[]{reference,player.worldObj,player,.5F,30}:new Object[]{reference,player.worldObj,player,3F,.5F,1F};
            net.minecraft.entity.Entity projectile=(net.minecraft.entity.Entity)invoke.invoke(null,s.getItem(),"createProjectile",signature,values);
            check(projectile instanceof net.minecraft.entity.projectile.EntityArrow,name+" native projectile factory");
            check(projectile.getClass().getName().startsWith("tconstruct.weaponry.entity."),name+" native projectile retained");
            check(Double.isFinite(projectile.motionX+projectile.motionY+projectile.motionZ),name+" finite trajectory");
            s.getTagCompound().getCompoundTag("InfiTool").setBoolean("Broken",true);check(!LegacyTConstruct.matchesTask(s,task),name+" broken rejected");
        }
        for(String name:new String[]{"Rapier","Cleaver","Longsword","Broadsword","FryingPan","Battleaxe"}){
            ItemStack s=stack("tconstruct.items.tools."+name);check(LegacyTConstruct.melee(s),name+" melee");
        }
        for(String name:new String[]{"Pickaxe","Hammer"}){
            ItemStack s=stack("tconstruct.items.tools."+name);check(LegacyMiningToolCompat.isMiningTool(s),name+" mining");
            check(!LegacyMiningToolCompat.canHarvest(s,Blocks.diamond_ore,0),name+" low harvest level rejected");
            s.getTagCompound().getCompoundTag("InfiTool").setInteger("HarvestLevel",3);
            check(LegacyMiningToolCompat.canHarvest(s,Blocks.diamond_ore,0),name+" high harvest level accepted");
            s.getTagCompound().getCompoundTag("InfiTool").setBoolean("Broken",true);check(!LegacyMiningToolCompat.isMiningTool(s),name+" broken rejected");
        }
        check(LegacyTConstruct.kind(new ItemStack(Items.bow))==LegacyTConstruct.Kind.NONE,"vanilla remains independent");
        System.out.println("Real TConstruct build991 API regression: "+checks+" checks PASS (no live world)");
    }
}

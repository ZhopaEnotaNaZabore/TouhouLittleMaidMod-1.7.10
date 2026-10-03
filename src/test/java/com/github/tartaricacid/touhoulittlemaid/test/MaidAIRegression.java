package com.github.tartaricacid.touhoulittlemaid.test;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.*;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import net.minecraft.world.World;
public final class MaidAIRegression {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void run() throws Exception {
        MultiblockRegression.MemoryWorld world=MultiblockRegression.world();
        Maid maid=(Maid)MultiblockRegression.unsafe().allocateInstance(Maid.class);
        maid.worldObj=world;maid.alive=true;maid.healthy=true;maid.schedule=MaidSchedule.DAY;
        world.testTime=11999;check(maid.canRunCombatAI(),"last work tick");
        world.testTime=12000;check(!maid.canRunCombatAI(),"schedule boundary before activity watcher update");
        check(!maid.attackEntityAsMob(null),"no melee after work");
        maid.attackEntityWithRangedAttack(null,1);checks++;
        world.testTime=16000;check(!maid.canRunCombatAI(),"rest blocks combat");
        world.testTime=0;maid.sitting=true;check(!maid.canRunCombatAI(),"waiting blocks combat");maid.sitting=false;
        maid.sleeping=true;check(!maid.canRunCombatAI(),"sleeping blocks combat");maid.sleeping=false;
        maid.healthy=false;check(!maid.canRunCombatAI(),"retreat health blocks combat");maid.healthy=true;
        maid.alive=false;check(!maid.canRunCombatAI(),"dead blocks combat");maid.alive=true;
        world.isRemote=true;check(!maid.canRunCombatAI(),"client cannot deal AI damage");world.isRemote=false;
        maid.schedule=MaidSchedule.NIGHT;world.testTime=11999;check(!maid.canRunCombatAI(),"night idle");
        world.testTime=12000;check(maid.canRunCombatAI(),"night work starts");
        maid.schedule=MaidSchedule.ALL;world.testTime=18000;check(maid.canRunCombatAI(),"all day work");
        for(String task:new String[]{TaskManager.HONEY_ID,TaskManager.CROSSBOW_ATTACK_ID,TaskManager.TRIDENT_ATTACK_ID}){
            check(!TaskManager.isCombatTask(task),"stub not combat: "+task);
        }
        MultiblockRegression.TestPlayer owner=MultiblockRegression.player();owner.worldObj=world;owner.dimension=1;
        check(!maid.safeTeleportNear(owner),"other dimension coordinates cannot be used for teleport");
        owner.dimension=0;owner.worldObj=MultiblockRegression.world();check(!maid.safeTeleportNear(owner),"other world rejected");
        maid.width=.6F;maid.height=1.5F;
        world.put(0,63,0,net.minecraft.init.Blocks.stone,0);
        check(MaidTeleportSafety.canStand(world,maid,.5,64,.5),"clear supported teleport");
        world.testCollision=true;check(!MaidTeleportSafety.canStand(world,maid,.5,64,.5),"body collision rejected");world.testCollision=false;
        world.testEntityCollision=true;check(!MaidTeleportSafety.canStand(world,maid,.5,64,.5),"occupied landing rejected");world.testEntityCollision=false;
        world.testLiquid=true;check(!MaidTeleportSafety.canStand(world,maid,.5,64,.5),"liquid rejected");world.testLiquid=false;
        world.testUnloaded=true;check(!MaidTeleportSafety.canStand(world,maid,.5,64,.5),"unloaded surroundings rejected");world.testUnloaded=false;
        check(!MaidTeleportSafety.canStand(world,maid,Double.NaN,64,.5),"invalid event destination rejected");
        check(!MaidTeleportSafety.canStand(world,maid,Double.MAX_VALUE,64,.5),"out of world destination rejected before integer conversion");
        world.put(0,64,0,net.minecraft.init.Blocks.fire,0);check(!MaidTeleportSafety.canStand(world,maid,.5,64,.5),"fire rejected");world.put(0,64,0,net.minecraft.init.Blocks.air,0);
        world.put(0,63,0,net.minecraft.init.Blocks.air,0);check(!MaidTeleportSafety.canStand(world,maid,.5,64,.5),"unsupported landing rejected");
        check(!MaidTeleportSafety.canStand(world,maid,.5,255,.5),"height boundary rejected");
        System.out.println("Maid AI regression: "+checks+" checks PASS (behavior gates, no game loop)");
    }
    public static final class Maid extends EntityMaid {
        boolean alive,healthy,sitting,sleeping;MaidSchedule schedule;
        private Maid(World world){super(world);}
        @Override public boolean isHomeMode(){return false;}
        @Override public boolean isEntityAlive(){return alive;}
        @Override public boolean canEngageCombat(){return healthy;}
        @Override public boolean isSitting(){return sitting;}
        @Override public boolean isMaidSleeping(){return sleeping;}
        @Override public MaidSchedule getSchedule(){return schedule;}
    }
}

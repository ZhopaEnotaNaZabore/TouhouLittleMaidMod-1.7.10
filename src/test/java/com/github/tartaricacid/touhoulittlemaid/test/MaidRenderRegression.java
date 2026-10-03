package com.github.tartaricacid.touhoulittlemaid.test;

import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.LegacyBedrockModel;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.animation.*;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.LegacyMaidPreviewContext;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.gson.*;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.util.*;

/** Production pose/state checks without an OpenGL context; not a visual approval. */
public final class MaidRenderRegression {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static void near(double value,double expected,String message){check(Double.isFinite(value)&&Math.abs(value-expected)<.0001,message+": "+value);}
    private static JsonObject read(Path path)throws IOException{try(Reader in=Files.newBufferedReader(path)){return new JsonParser().parse(in).getAsJsonObject();}}
    public static void run(String root)throws Exception{
        EntityMaid maid=(EntityMaid)MultiblockRegression.unsafe().allocateInstance(EntityMaid.class);
        maid.renderYawOffset=1;maid.prevRenderYawOffset=2;maid.rotationYaw=3;maid.prevRotationYaw=4;
        maid.rotationPitch=5;maid.prevRotationPitch=6;maid.rotationYawHead=7;maid.prevRotationYawHead=8;
        LegacyBedrockModel model=new LegacyBedrockModel();
        Method getPlayer=LegacyBedrockModel.class.getDeclaredMethod("animationPlayer",net.minecraft.entity.Entity.class);getPlayer.setAccessible(true);
        Object world=getPlayer.invoke(model,maid);
        try(LegacyMaidPreviewContext outer=LegacyMaidPreviewContext.enter(maid,.25F)){
            near(LegacyMaidPreviewContext.partialTicks(1),.25,"portrait keeps real partial tick");
            Object preview=getPlayer.invoke(model,maid);check(world!=preview,"portrait cannot rewind world's player");
            check(preview==getPlayer.invoke(model,maid),"portrait retains its own transition state");
            maid.rotationYaw=90;
            try(LegacyMaidPreviewContext inner=LegacyMaidPreviewContext.enter(maid,.75F)){maid.rotationYaw=180;}
            near(maid.rotationYaw,90,"nested preview restores outer pose");near(LegacyMaidPreviewContext.partialTicks(1),.25,"nested clock restored");
            maid.renderYawOffset=maid.prevRenderYawOffset=maid.rotationYaw=maid.prevRotationYaw=99;
            maid.rotationPitch=maid.prevRotationPitch=maid.rotationYawHead=maid.prevRotationYawHead=99;
        }
        check(!LegacyMaidPreviewContext.active(),"preview scope released");
        check(world==getPlayer.invoke(model,maid),"world player restored after portrait");
        float[] restored={maid.renderYawOffset,maid.prevRenderYawOffset,maid.rotationYaw,maid.prevRotationYaw,maid.rotationPitch,maid.prevRotationPitch,maid.rotationYawHead,maid.prevRotationYawHead};
        for(int i=0;i<restored.length;i++)near(restored[i],i+1,"live entity rotation restored "+i);
        Path resources=Paths.get(root,"src/main/resources/assets");
        Path pack=resources.resolve("touhou_little_maid/tlm_custom_pack/touhou_little_maid-1.0.0/assets/geckolib");
        JsonObject defaults=read(resources.resolve("touhou_little_maid/animation/maid.animation.json"));
        for(String name:new String[]{"zhiban","zhiban_hanfu","zhiban_new_year"}){
            JsonObject geometry=read(pack.resolve("models/entity/"+name+".json"));
            LegacyAnimationLibrary library=new LegacyAnimationLibrary();
            library.merge(read(pack.resolve("animation/"+(name.equals("zhiban")?name:"zhiban_hanfu")+".animation.json")),name,false);
            library.merge(defaults,"SRC defaults",true);
            for(String state:new String[]{"idle","sit","chair","run","jump","swing_hand","swing:sword"})
                check(library.get(state)!=null,name+" loads "+state+": "+library.diagnostics().get(state));
            LegacyAnimationPose pose=new LegacyAnimationPose();
            for(JsonElement b:geometry.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones"))
                pose.bones.put(b.getAsJsonObject().get("name").getAsString(),new LegacyAnimationPose.Bone(0,0,0,true));
            LegacyAnimationFrame frame=new LegacyAnimationFrame();frame.sitting=true;
            LegacyKeyframePlayer player=new LegacyKeyframePlayer(library);
            player.apply(pose,frame);frame.age=3;pose.reset();player.apply(pose,frame);
            String sittingRoot=name.equals("zhiban")?"AllBody":"Root";
            double[][] rootTrack=library.get("sit").sample(.15,frame,null).get(sittingRoot);
            near(pose.get(sittingRoot).offsetY,-rootTrack[1][1]/16,name+" authored sitting root");
            check(Math.abs(pose.get("LeftLeg").x)>.5,name+" sitting bends legs");
            check(!player.runtimeFailures().contains("sit"),name+" sitting survives expressions/timeline");
            frame.sitting=false;frame.mainCategory="sword";frame.mainId="minecraft:diamond_sword";
            frame.swingSequence=1;frame.swing=.5F;frame.swingTicks=3;frame.age=10;pose.reset();player.apply(pose,frame);
            float attackX=pose.get("RightArm").x;
            check(Math.abs(attackX)>.5,name+" real sword clip moves arm");
            frame.age=12;frame.swingTicks=5;pose.reset();player.apply(pose,frame);
            check(Math.abs(pose.get("RightArm").x-attackX)>.01,name+" attack advances instead of restarting");
            check(!player.runtimeFailures().contains("swing:sword"),name+" sword clip remains valid");
            try(InputStream in=Files.newInputStream(pack.resolve("models/entity/"+name+".json"))){
                LegacyBedrockModel parsed=new LegacyBedrockModel(in);
                check(parsed.hasBone("Head")&&parsed.hasBone("BackpackLocator")&&parsed.hasBone("RightHandLocator"),name+" geometry imports attachment bones");
            }
        }
        LegacyAnimationFrame frame=new LegacyAnimationFrame();frame.mainId="minecraft:stick";frame.groundSpeed=4;frame.verticalDisplacement=-.25F;frame.yawSpeed=60;
        LegacyMolang.Context context=new LegacyMolang.Context(frame,0);
        near(LegacyMolang.compile("ysm.has_mainhand?2:0;").eval(context),2,"terminated equipped-hand expression");
        near(LegacyMolang.compile("q.ground_speed").eval(context),4,"horizontal blocks per second");
        near(LegacyMolang.compile("q.vertical_speed").eval(context),-5,"vertical displacement per second");
        near(LegacyMolang.compile("q.yaw_speed").eval(context),60,"yaw degrees per second");
        near(LegacyMolang.compile("q.vertical_speed / math.abs(q.vertical_speed)").eval(new LegacyMolang.Context(new LegacyAnimationFrame(),0)),0,"stationary physics division matches SRC");
        near(LegacyMolang.compile("1;2").eval(context),2,"all expression statements execute");
        com.github.tartaricacid.touhoulittlemaid.entity.animation.MaidActionState bow=new com.github.tartaricacid.touhoulittlemaid.entity.animation.MaidActionState();
        bow.beginUse(new net.minecraft.item.ItemStack(net.minecraft.init.Items.bow),false,"bow",40,true,100);
        for(int[] sample:new int[][]{{100,-1},{101,0},{113,0},{114,1},{117,1},{118,2},{139,2},{140,-1}})
            check(bow.bowPullStage(sample[0],false)==sample[1],"1.7 bow pull threshold at "+sample[0]);
        check(bow.bowPullStage(120,true)==-1,"other hand cannot inherit drawn bow texture");
        bow.stopUse();check(bow.bowPullStage(120,false)==-1,"release restores resting bow texture");
        checkMelee();
        System.out.println("Maid render/real Zhib animation regression: "+checks+" checks PASS (no OpenGL/game loop)");
    }

    private static void checkMelee()throws Exception{
        AttackMaid maid=(AttackMaid)MultiblockRegression.unsafe().allocateInstance(AttackMaid.class);
        maid.worldObj=MultiblockRegression.world();maid.width=.6F;maid.enabled=true;maid.visible=true;
        Target target=(Target)MultiblockRegression.unsafe().allocateInstance(Target.class);
        target.worldObj=maid.worldObj;target.width=.6F;
        java.lang.reflect.Field box=net.minecraft.entity.Entity.class.getDeclaredField("boundingBox");box.setAccessible(true);
        box.set(target,net.minecraft.util.AxisAlignedBB.getBoundingBox(0,0,0,.6,1.8,.6));
        maid.senses=new net.minecraft.entity.ai.EntitySenses(maid){@Override public boolean canSee(net.minecraft.entity.Entity entity){return maid.visible;}};
        com.github.tartaricacid.touhoulittlemaid.entity.ai.EntityAIMaidMeleeAttack ai=new com.github.tartaricacid.touhoulittlemaid.entity.ai.EntityAIMaidMeleeAttack(maid);
        Method attempt=ai.getClass().getDeclaredMethod("tryMeleeAttack",net.minecraft.entity.EntityLivingBase.class);attempt.setAccessible(true);
        for(int tick=0;tick<20;tick++){maid.ticksExisted=tick;attempt.invoke(ai,target);}
        check(maid.swings==1&&maid.hits==1,"bare-hand melee emits one swing/hit, not a restart every tick");
        maid.ticksExisted=20;attempt.invoke(ai,target);check(maid.swings==2&&maid.hits==2,"next attack after cooldown");
        maid.ticksExisted=40;maid.visible=false;attempt.invoke(ai,target);check(maid.hits==2,"wall blocks melee presentation and damage");
        maid.visible=true;maid.enabled=false;attempt.invoke(ai,target);check(maid.hits==2,"sitting/rest gate blocks swing");
        maid.enabled=true;target.posX=8;attempt.invoke(ai,target);check(maid.hits==2,"outside melee reach cannot swing");
    }
    public static final class AttackMaid extends EntityMaid{
        int swings,hits;boolean enabled,visible;net.minecraft.entity.ai.EntitySenses senses;
        private AttackMaid(net.minecraft.world.World world){super(world);}
        @Override public boolean canRunCombatAI(){return enabled;}
        @Override public String getTaskId(){return com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager.ATTACK_ID;}
        @Override public net.minecraft.entity.EntityLivingBase getOwner(){return null;}
        @Override public boolean isPositionWithinRestriction(double x,double y,double z){return true;}
        @Override public boolean isOnSameTeam(net.minecraft.entity.EntityLivingBase other){return false;}
        @Override public net.minecraft.entity.ai.EntitySenses getEntitySenses(){return senses;}
        @Override public void swingItem(){swings++;}
        @Override public boolean attackEntityAsMob(net.minecraft.entity.Entity other){hits++;return true;}
    }
    public static final class Target extends net.minecraft.entity.monster.EntityZombie{
        private Target(net.minecraft.world.World world){super(world);}
        @Override public boolean isEntityAlive(){return true;}
    }
}

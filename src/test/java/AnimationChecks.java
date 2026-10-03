import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.animation.*;
import com.github.tartaricacid.touhoulittlemaid.entity.animation.MaidActionState;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.LegacyMaidItemContext;
import com.google.gson.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import java.nio.file.*;
import java.io.*;
import java.util.*;

/** Hand-derived formula/track assertions; real production classes, explicit platform doubles. */
public final class AnimationChecks {
    static int checks;
    static void check(boolean v, String text) { checks++; if (!v) throw new AssertionError(text); }
    static void near(double actual, double expected, String text) { check(Double.isFinite(actual) && Math.abs(actual - expected) < 2E-5, text + ": " + actual + " != " + expected); }
    static void reject(Runnable r, String text) { boolean thrown=false; try { r.run(); } catch (RuntimeException e) { thrown=true; } check(thrown,text); }
    static JsonObject json(String s) { return new JsonParser().parse(s).getAsJsonObject(); }
    static LegacyAnimationPose pose(String... names) { LegacyAnimationPose p=new LegacyAnimationPose();for(String n:names)p.bones.put(n,new LegacyAnimationPose.Bone(.2F,.3F,.4F,true));return p; }
    static void animation(LegacyAnimationPose p, LegacyAnimationFrame f, String path) { p.reset();check(LegacyMaidAnimations.applyOne(p,path.indexOf(':')<0?LegacyAnimationProfile.ROOT+path:path,f),"known animation "+path); }
    static LegacyKeyframeClip clip(String body) { return new LegacyKeyframeClip("test",json(body)); }
    static double[] rotation(LegacyKeyframeClip c,double time,LegacyAnimationFrame f) { return c.sample(time,f,null).get("B")[0]; }
    public static void main(String[] args) throws Exception {
        LegacyAnimationFrame f=new LegacyAnimationFrame();
        LegacyAnimationPose p=pose("armLeft","armRight","armLeftVertical","legLeft","legRight","head","tail","sinFloat","cosFloat","_sinFloat","_cosFloat","hat","blink","blink2","sittingRotationSkirt","xReciprocate");
        LegacyAnimationProfile custom=new LegacyAnimationProfile(false,Arrays.asList(LegacyAnimationProfile.ROOT+"head/default.js"));
        f.limbAmount=1; f.limbSwing=2; f.pitch=30; f.yaw=-45;
        p.reset();LegacyMaidAnimations.apply(p,custom,f);
        near(p.get("armLeft").x,.2,"head-only profile cannot force walk");near(p.get("tail").z,.4,"head-only profile cannot force tail");near(p.get("head").x,Math.PI/6,"head X absolute");near(p.get("head").y,-Math.PI/4,"head Y absolute");
        animation(p,f,"arm/default.js");near(p.get("armLeft").x,-Math.cos(2*.67)*.7,"left walk absolute not rest-additive");near(p.get("armRight").x,Math.cos(2*.67)*.7,"right phase");near(p.get("armLeft").z,.45,"cos at age zero not sin");near(p.get("armRight").z,.35,"opposite cos phase");near(p.get("armLeft").y,.3,"arm baseY");
        f.age=(float)(Math.PI/.05);animation(p,f,"arm/default.js");near(p.get("armLeft").z,.35,"half-period cos");
        f.swing=.5F; f.swingLeft=true;animation(p,f,"arm/default.js");
        double swing=Math.sin((1-Math.pow(.5,4))*Math.PI)*1.2-Math.sin(.5*Math.PI)*.525;
        near(p.get("armLeft").x,-Math.cos(2*.67)*.7-swing,"left attack curve");near(p.get("armRight").x,Math.cos(2*.67)*.7,"right unaffected by left attack");
        near(p.get("armLeft").z,-.05,"left attack Z sign");
        f.swingLeft=false;animation(p,f,"arm/default.js");near(p.get("armRight").x,Math.cos(2*.67)*.7-swing,"right attack curve");
        f.use="eat";f.useLeft=true;animation(p,f,"arm/default.js");near(p.get("armLeft").x,.2-Math.toRadians(80),"use left baseX");near(p.get("armLeft").y,.3+Math.toRadians(25),"use left baseY");
        f.useLeft=false;animation(p,f,"arm/default.js");near(p.get("armRight").x,.2-Math.toRadians(80),"use right baseX");near(p.get("armRight").y,.3-Math.toRadians(20),"use right baseY");
        f.use="";f.swing=0;f.ranged=true;f.mainId="minecraft:bow";animation(p,f,"arm/swing.js");near(p.get("armLeft").x,-1.396,"ranged arms");near(p.get("armLeft").y,.785,"ranged left yaw");near(p.get("armRight").y,-.174,"ranged right yaw");
        f.ranged=false;f.sitting=true;animation(p,f,"sit/default.js");near(p.get("armLeft").x,-.798,"sitting arm absolute");near(p.get("legLeft").x,-1.134,"sitting leg absolute");near(p.transforms.get(0)[2],.3,"sitting global translation");
        f.sitting=false;f.riding=true;animation(p,f,"sit/default.js");near(p.get("armLeft").x,.2,"riding does not force sitting arms");near(p.get("legRight").z,.262,"riding leg Z");
        f.riding=false;f.sitting=true;animation(p,f,"sit/no_leg.js");near(p.get("legLeft").x,.2,"no_leg keeps authored legs");check(p.transforms.isEmpty(),"no_leg has no forced root translate");
        f.sitting=false;f.sleeping=true;animation(p,f,"head/default.js");near(p.get("head").x,Math.toRadians(15),"sleep head");check(!p.get("hat").visible,"sleep hat hidden");
        animation(p,f,"head/blink.js");check(p.get("blink").visible&&p.get("blink2").visible,"both sleeping blink bones");
        f.sleeping=false;animation(p,f,"head/default.js");check(p.get("hat").visible,"wake reset hat");
        f.age=0;animation(p,f,"touhou_little_maid:animation/base/float/default.js");near(p.get("sinFloat").offsetY,0,"sin float zero");near(p.get("cosFloat").offsetY,.05,"float units are blocks");near(p.get("_cosFloat").offsetY,-.05,"negative float counterpart");
        animation(p,f,"touhou_little_maid:animation/base/rotation/reciprocate.js");near(p.get("xReciprocate").x,.2,"reciprocate cos");
        f.task="farm";f.swing=.25F;animation(p,f,"leg/default.js");near(p.get("legLeft").x,Math.cos(2*.67)*.3-.3927,"farm leg tilt");check(p.transforms.size()==2,"farm global transforms");near(p.transforms.get(0)[2],.0713625,"farm translation Y");near(p.transforms.get(1)[0],22.5,"farm rotation");
        f.task="idle";f.swing=0;animation(p,f,"leg/default.js");check(p.transforms.isEmpty(),"farm global transform reset");
        LegacyAnimationProfile ordered=new LegacyAnimationProfile(false,Arrays.asList(LegacyAnimationProfile.ROOT+"arm/default.js",LegacyAnimationProfile.ROOT+"arm/vertical.js"));p.reset();LegacyMaidAnimations.apply(p,ordered,f);near(p.get("armLeftVertical").x,-p.get("armLeft").x,"authored order vertical counterrotation");
        check(!LegacyMaidAnimations.applyOne(p,"unknown:animation.js",f),"unknown script explicit false");
        check(new LegacyAnimationProfile(true,Collections.<String>emptyList()).animations.get(0).equals(LegacyAnimationProfile.DEFAULT_JSON),"gecko default file");
        check(new LegacyAnimationProfile(true,Arrays.asList("test:skip.js","test:arbitrary.json")).animations.equals(Arrays.asList("test:arbitrary.json")),"SRC Gecko decorator filters .json, not only .animation.json");
        p.reset();LegacyMaidAnimations.apply(p,new LegacyAnimationProfile(true,Collections.<String>emptyList()),f);near(p.get("armLeft").x,.2,"no JS motion on Gecko");

        LegacyMolang.Context c=new LegacyMolang.Context(f,.25);
        near(LegacyMolang.compile("math.sin(query.anim_time * 360)").eval(c),1,"Molang degrees");
        f.pitch=20;f.yaw=150;near(LegacyMolang.compile("ysm.head_pitch").eval(c),-20,"SRC negative Gecko pitch");near(LegacyMolang.compile("ysm.head_yaw").eval(c),-85,"clamped negative Gecko yaw");
        f.age=87;near(LegacyMolang.compile("ysm.is_close_eyes").eval(c),1,"Gecko 90-tick blink");
        near(LegacyMolang.compile("2+3*4==14 ? math.clamp(4,0,2) : 0").eval(c),2,"precedence and ternary");
        near(LegacyMolang.compile("0 && (1 / 0)").eval(c),0,"short circuit");
        near(LegacyMolang.compile("v.physics = 1; return v.physics").eval(c),1,"bounded Molang assignments");
        reject(()->LegacyMolang.compile("query.unported_state"),"unknown query rejected");
        check(LegacyMolang.compile("math.random(0,1)").eval(new LegacyMolang.Context(f,0))>=0,"bounded random supported");
        LegacyKeyframeClip linear=clip("{\"animation_length\":1,\"bones\":{\"B\":{\"rotation\":{\"0\":[0,0,0],\"1\":[20,40,60]}}}}");
        near(rotation(linear,.25,f)[0],5,"linear interpolation");near(rotation(linear,5,f)[2],60,"once clamp");
        near(linear.sampleTime(1.25,LegacyKeyframeClip.Loop.LOOP),.25,"forced loop");near(linear.sampleTime(1.25,LegacyKeyframeClip.Loop.HOLD),1,"hold last frame");
        LegacyKeyframeClip step=clip("{\"bones\":{\"B\":{\"rotation\":{\"0\":[0,0,0],\"1\":{\"pre\":[10,0,0],\"post\":[30,0,0]},\"2\":[40,0,0]}}}}");
        near(rotation(step,.5,f)[0],5,"interpolate to pre");near(rotation(step,1,f)[0],30,"exact timestamp uses post");near(rotation(step,1.5,f)[0],35,"interpolate from post");
        LegacyKeyframeClip late=clip("{\"bones\":{\"B\":{\"rotation\":{\"1\":{\"pre\":[10,0,0],\"post\":[30,0,0]}}}}}");near(rotation(late,0,f)[0],10,"before first key holds pre");
        near(LegacyKeyframeClip.catmull(.5,0,10,20,0),16.875,"CatmullRom independent polynomial");
        LegacyKeyframeClip cat=clip("{\"bones\":{\"B\":{\"rotation\":{\"0\":[0,0,0],\"1\":[10,0,0],\"2\":{\"vector\":[20,0,0],\"lerp_mode\":\"catmullrom\"},\"3\":[0,0,0]}}}}");near(rotation(cat,1.5,f)[0],16.875,"actual catmull destination track");
        LegacyKeyframeClip scale=clip("{\"bones\":{\"B\":{\"scale\":2}}}");near(scale.sample(0,f,null).get("B")[2][1],2,"scalar scale broadcast");
        reject(()->clip("{\"bones\":{\"B\":{\"rotation\":{\"0\":{\"vector\":[0,0,0],\"easing\":\"easeInElastic\"}}}}}"),"unknown easing rejected");
        reject(()->clip("{\"timeline\":{\"0\":\"do stuff\"}}"),"unsupported channel explicit");
        LegacyKeyframeClip divide=clip("{\"bones\":{\"B\":{\"rotation\":[\"1/0\",0,0]}}}");near(rotation(divide,0,f)[0],0,"SRC Molang division by zero returns zero");
        LegacyAnimationLibrary lib=new LegacyAnimationLibrary();lib.merge(json("{\"animations\":{\"idle\":{\"bones\":{\"B\":{\"rotation\":[\"query.unsupported\",0,0]}}}}}"),"custom",false);
        lib.merge(json("{\"animations\":{\"idle\":{\"bones\":{\"B\":{\"rotation\":[10,0,0]}}},\"sit\":{}}}"),"defaults",true);
        check(lib.get("idle")==null&&lib.diagnostics().containsKey("idle"),"unsupported authored clip blocks default substitution");check(lib.get("sit")!=null,"missing names receive SRC default");
        lib.merge(json("{\"animations\":{\"idle\":{}}}"),"later override",false);check(lib.get("idle")!=null&&!lib.diagnostics().containsKey("idle"),"ordered override replaces rejection");

        // Layer names and priorities from AnimationRegister / GeckoMaidEntity.
        LegacyAnimationLibrary layers=new LegacyAnimationLibrary();layers.merge(json("{\"animations\":{\"idle\":{\"bones\":{\"B\":{\"rotation\":[10,0,0]}}},\"pre_parallel0\":{\"bones\":{\"B\":{\"rotation\":[5,0,0]}}},\"parallel0\":{\"bones\":{\"B\":{\"rotation\":[2,0,0]}}},\"sit\":{\"bones\":{\"B\":{\"rotation\":[40,0,0]}}},\"swing:sword\":{\"animation_length\":1,\"bones\":{\"R\":{\"rotation\":[50,0,0]}}},\"swing_offhand:sword\":{\"animation_length\":1,\"bones\":{\"L\":{\"rotation\":[60,0,0]}}}}}"),"test",false);
        LegacyKeyframePlayer player=new LegacyKeyframePlayer(layers);LegacyAnimationPose layerPose=pose("B","L","R");LegacyAnimationFrame lf=new LegacyAnimationFrame();
        player.apply(layerPose,lf);lf.age=2;layerPose.reset();player.apply(layerPose,lf);near(layerPose.get("B").x,.2+Math.toRadians(17),"pre/main/parallel ordered rotation accumulation");
        lf.sitting=true;lf.age=10;layerPose.reset();player.apply(layerPose,lf);near(layerPose.get("B").x,.2+Math.toRadians(17),"transition starts at prior channel");lf.age=12;layerPose.reset();player.apply(layerPose,lf);near(layerPose.get("B").x,.2+Math.toRadians(47),"transition reaches new channel");
        lf.swing=.5F;lf.swingTicks=3;lf.swingSequence=1;lf.mainId="minecraft:iron_sword";lf.mainCategory="sword";lf.age=20;layerPose.reset();player.apply(layerPose,lf);near(layerPose.get("R").x,.2+Math.toRadians(50),"swing:sword conditional prefix");near(layerPose.get("L").x,.2,"right swing leaves left rest");
        lf.swingLeft=true;lf.offId="minecraft:iron_sword";lf.offCategory="sword";lf.swingSequence=2;lf.age=24;layerPose.reset();player.apply(layerPose,lf);near(layerPose.get("L").x,.2+Math.toRadians(60),"offhand conditional clip");
        lf.swing=0;lf.cancelSwing=true;lf.age=25;layerPose.reset();player.apply(layerPose,lf);lf.age=26;layerPose.reset();player.apply(layerPose,lf);near(layerPose.get("L").x,.2,"cancelled action resets to rest");
        LegacyKeyframePlayer lateSwing=new LegacyKeyframePlayer(layers);LegacyAnimationFrame lateFrame=new LegacyAnimationFrame();
        lateFrame.swingSequence=7;lateFrame.swingTicks=10;lateFrame.age=100;lateFrame.mainId="minecraft:iron_sword";lateFrame.mainCategory="sword";
        LegacyAnimationPose latePose=pose("R");lateSwing.apply(latePose,lateFrame);near(latePose.get("R").x,.2+Math.toRadians(50),"lateFrame tracker resumes authored swing tail beyond six vanilla ticks");
        lateFrame.swingTicks=30;lateFrame.swingSequence=8;latePose.reset();new LegacyKeyframePlayer(layers).apply(latePose,lateFrame);near(latePose.get("R").x,.2,"expired snapshot cannot replay stale swing");
        lf.sleeping=true;check("sleep".equals(player.mainState(lf)),"sleep beats sitting");lf.dead=true;check("death".equals(player.mainState(lf)),"death highest");

        MaidActionState server=new MaidActionState();Item apple=net.minecraft.init.Items.apple;ItemStack stack=new ItemStack(apple);stack.stackSize=3;
        server.beginUse(stack,true,"eat",32,false,100);stack.stackSize=0;check(server.displayItem(100).stackSize==1,"presentation copy independent of consumed inventory");check(server.useLeft(),"use hand");
        MaidActionState client=new MaidActionState();client.accept(server.snapshot(110),5);near(client.useElapsed(5),10,"late tracker elapsed snapshot");near(client.useElapsed(6.5F),11.5,"partial tick progression");check(client.using(26)&&!client.using(27),"same remaining use duration");
        server.swing(true,6,112);client.accept(server.snapshot(114),9);check(client.swingLeft(),"swing hand snapshot");near(client.swingProgress(9),2D/6,"swing normalized snapshot");
        NBTTagCompound stale=server.snapshot(114);server.stopUse();client.accept(server.snapshot(115),10);client.accept(stale,11);check(!client.using(11),"out of order snapshot cannot resurrect use");
        server.clear();client.accept(server.snapshot(116),12);check(!client.hasSwingEvent(),"task clear cancels swing event");
        check(!LegacyMaidItemContext.isLeft(),"default right item context");try(LegacyMaidItemContext l=LegacyMaidItemContext.enter(true)){check(LegacyMaidItemContext.isLeft(),"left render context");try(LegacyMaidItemContext r=LegacyMaidItemContext.enter(false)){check(!LegacyMaidItemContext.isLeft(),"nested right context");}check(LegacyMaidItemContext.isLeft(),"restore left context");}check(!LegacyMaidItemContext.isLeft(),"restore caller context");

        // Real archived default animation resource, not a replacement fixture.
        Path root=Paths.get(args[0]);LegacyAnimationLibrary defaults=new LegacyAnimationLibrary();
        try(Reader reader=Files.newBufferedReader(root.resolve("src/main/resources/assets/touhou_little_maid/animation/maid.animation.json"))){defaults.merge(new JsonParser().parse(reader).getAsJsonObject(),"SRC maid.animation.json",false);}
        for(String key:Arrays.asList("idle","walk","run","sit","chair","sleep","swing_hand","swing_offhand","use_mainhand:eat","use_offhand:eat","use_mainhand:drink","use_mainhand:gohei","use_mainhand:bow","hold_mainhand:fishing"))check(defaults.get(key)!=null,"default clip supported: "+key+" "+defaults.diagnostics().get(key));
        double[][] fishing=defaults.get("hold_mainhand:fishing").sample(0,f,null).get("LeftArm");near(fishing[0][0],-56.72671,"authored fishing rotation X");near(fishing[0][1],19.95595,"authored fishing rotation Y");near(fishing[0][2],11.62581,"authored fishing rotation Z");
        near(defaults.get("swing_hand").sample(.125,f,null).get("RightArm")[0][0],-7.5,"authored actual swing keyframe");
        LegacyAnimationFrame authorFrame=new LegacyAnimationFrame();authorFrame.limbAmount=1;authorFrame.age=0;
        LegacyAnimationPose author=pose("armLeft","armRight","floatLeg","left","right");
        animation(author,authorFrame,"authors_and_credits:animation/arm.js");
        near(author.get("armLeft").x,.2,"author arm keeps rest X despite walking");near(author.get("armLeft").y,0,"author arm absolute Y");
        near(author.get("armLeft").z,-.35,"author left arm cos and fixed offset");near(author.get("armRight").z,.35,"author right arm opposite offset");
        authorFrame.swing=.5F;authorFrame.swingLeft=true;animation(author,authorFrame,"authors_and_credits:animation/arm.js");
        near(author.get("armLeft").x,.2-swing,"author selected left attack");near(author.get("armRight").x,.2,"author opposite arm stays rest");
        animation(author,authorFrame,"authors_and_credits:animation/float.js");near(author.get("floatLeg").x,.2+Math.atan(1)*1.25,"author float leg atan");
        near(author.get("left").z,.4+Math.atan(1)*.75,"author float left roll");near(author.transforms.get(0)[2],-.1,"author global float units");
        authorFrame.sitting=true;animation(author,authorFrame,"authors_and_credits:animation/float.js");near(author.get("armLeft").x,-.8,"author seated arm");near(author.get("right").z,-.1,"author seated right");
        authorFrame.sitting=false;authorFrame.sleeping=true;animation(author,authorFrame,"authors_and_credits:animation/float.js");near(author.transforms.get(0)[2],.5,"author sleeping float");
        check(defaults.get("swing:sword")!=null,"authored timeline-dependent sword supported");
        LegacyAnimationLibrary badRuntime=new LegacyAnimationLibrary();badRuntime.merge(json("{\"animations\":{\"idle\":{\"bones\":{\"B\":{\"rotation\":[\"math.exp(10000)\",0,0]}}}}}"),"runtime-test",false);
        LegacyKeyframePlayer badPlayer=new LegacyKeyframePlayer(badRuntime);badPlayer.apply(pose("B"),new LegacyAnimationFrame());
        check(badPlayer.runtimeDiagnostics().containsKey("idle"),"runtime non-finite failure is recorded with reason");
        System.out.println("Animation/formula/state assertions: "+checks+" PASS");
    }
}

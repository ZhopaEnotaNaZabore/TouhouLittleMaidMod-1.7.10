import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.animation.*;
import com.google.gson.*;
import java.nio.file.*;
import java.io.*;
import java.util.*;
public final class TimelineChecks {
    static int checks;
    static void check(boolean b,String message){checks++;if(!b)throw new AssertionError(message);}
    static void near(double a,double b,String message){check(Math.abs(a-b)<1e-5,message+": "+a+" != "+b);}
    static LegacyKeyframeClip clip(String s){return new LegacyKeyframeClip("test",new JsonParser().parse(s).getAsJsonObject());}
    static double x(LegacyKeyframeClip c,LegacyTimeline.Playback p,double t,LegacyAnimationFrame f){return c.sample(t,f,null,p).get("B")[0][0];}
    public static void main(String[] args)throws Exception{
        LegacyAnimationFrame f=new LegacyAnimationFrame();
        LegacyKeyframeClip c=clip("{\"loop\":true,\"animation_length\":1,\"timeline\":{\"0\":[\"v.x=1;\"],\"0.5\":[\"v.x=v.x+2;\"]},\"bones\":{\"B\":{\"rotation\":[\"v.x\",0,0]}}}");
        LegacyTimeline.Playback p=c.timeline.playback(3);
        near(x(c,p,0,f),1,"zero event precedes pose");near(x(c,p,.5,f),3,"event at exact boundary");
        near(x(c,p,.5,f),3,"same frame does not repeat event");near(x(c,p,.9,f),3,"later frame does not repeat event");
        near(x(c,p,1,f),1,"loop restarts zero event");near(x(c,p,2.75,f),3,"skipped loops processed");
        near(x(c,p,.25,f),1,"rewind resets playback");near(x(c,c.timeline.playback(3),.75,f),3,"late first sample catches up");
        LegacyKeyframeClip r=clip("{\"animation_length\":1,\"timeline\":{\"0\":[\"v.x=math.random(-30,30);\"]},\"bones\":{\"B\":{\"rotation\":[\"v.x\",0,0]}}}");
        LegacyTimeline.Playback a=r.timeline.playback(1),b=r.timeline.playback(2);double first=x(r,a,0,f);
        near(x(r,a,.5,f),first,"event random stable across frames");check(x(r,b,0,f)!=first,"entity playback independent");
        near(x(r,r.timeline.playback(1),0,f),first,"same seed reproducible");
        near(LegacyMolang.compile("q.is_jumping").eval(new LegacyMolang.Context(f,0)),0,"grounded");
        f.onGround=false;near(LegacyMolang.compile("q.is_jumping").eval(new LegacyMolang.Context(f,0)),1,"airborne");
        f.water=true;near(LegacyMolang.compile("q.is_jumping").eval(new LegacyMolang.Context(f,0)),0,"water excluded");f.water=false;f.riding=true;
        near(LegacyMolang.compile("q.is_jumping").eval(new LegacyMolang.Context(f,0)),0,"passenger excluded");f.riding=false;
        near(LegacyMolang.compile("v.external").eval(new LegacyMolang.Context(f,0)),0,"SRC undefined numeric variable");
        LegacyMolang.Context ctx=new LegacyMolang.Context(f,0);
        near(LegacyMolang.compile("v.a=2;v.b=v.a*3;return v.b+1;").eval(ctx),7,"assignment sequence and return");
        near(LegacyMolang.compile("v.missing??4").eval(ctx),4,"coalesce undefined");
        near(LegacyMolang.compile("v.zero=0;return v.zero??4").eval(ctx),0,"coalesce preserves defined zero");
        near(LegacyMolang.compile("1?v.branch=3:v.branch=9").eval(ctx),3,"conditional assignment");
        near(LegacyMolang.compile("v.branch").eval(ctx),3,"unchosen branch has no side effect");
        LegacyMolang.Expression spring=LegacyMolang.compile("ysm.second_order('hair',q.vertical_speed,2,0.6,0)");
        f.age=0;f.verticalDisplacement=0;near(spring.eval(ctx),0,"spring starts at rest");
        f.verticalDisplacement=.1F;double prior=0;
        for(int tick=1;tick<=100;tick++){f.age=tick;double v=spring.eval(ctx);check(Double.isFinite(v)&&Math.abs(v)<10,"bounded spring response");near(spring.eval(ctx),v,"duplicate frame cannot integrate twice");prior=v;}
        near(prior,2,"spring settles on input");f.age=0;f.verticalDisplacement=0;
        near(spring.eval(ctx),0,"spring rewind resets");
        LegacyKeyframeClip weighted=clip("{\"blend_weight\":0.5,\"bones\":{\"B\":{\"rotation\":[20,0,0],\"scale\":[3,1,1]}}}");
        near(weighted.sample(0,f,null).get("B")[0][0],10,"weighted rotation");near(weighted.sample(0,f,null).get("B")[2][0],2,"weighted scale around one");
        LegacyKeyframeClip late=clip("{\"loop\":true,\"animation_length\":1,\"timeline\":{\"0\":\"v.x=2;\",\"1.01\":\"v.x=99;\"},\"bones\":{\"B\":{\"rotation\":[\"v.x\",0,0]}}}");
        near(x(late,late.timeline.playback(0),1.5,f),2,"event beyond explicit duration is unreachable");
        near(x(late,late.timeline.playback(0),5000.5,f),2,"large visibility gap resynchronizes without rejecting clip");
        JsonObject sharedClips=new JsonObject();
        sharedClips.add("pre_parallel0",new JsonParser().parse("{\"bones\":{\"B\":{\"rotation\":[\"v.shared=25\",0,0]}}}"));
        sharedClips.add("idle",new JsonParser().parse("{\"bones\":{\"B\":{\"rotation\":[\"v.shared\",0,0]}}}"));
        JsonObject sharedRoot=new JsonObject();sharedRoot.add("animations",sharedClips);
        LegacyAnimationLibrary sharedLib=new LegacyAnimationLibrary();sharedLib.merge(sharedRoot,"shared test",false);
        LegacyKeyframePlayer sharedPlayer=new LegacyKeyframePlayer(sharedLib);LegacyAnimationPose sharedPose=new LegacyAnimationPose();sharedPose.bones.put("B",new LegacyAnimationPose.Bone(0,0,0,true));
        f.age=0;sharedPlayer.apply(sharedPose,f);f.age=3;sharedPose.reset();sharedPlayer.apply(sharedPose,f);
        near(sharedPose.get("B").x,Math.toRadians(25),"pre layer variables are visible to main layer");
        try{clip("{\"timeline\":{\"0\":\"host.execute();\"}}");throw new AssertionError("unsupported instruction accepted");}catch(IllegalArgumentException expected){checks++;}
        JsonObject json;try(Reader reader=Files.newBufferedReader(Paths.get(args[0],"src/main/resources/assets/touhou_little_maid/animation/maid.animation.json"))){json=new JsonParser().parse(reader).getAsJsonObject();}
        LegacyAnimationLibrary library=new LegacyAnimationLibrary();library.merge(json,"SRC",false);
        LegacyKeyframeClip sword=library.get("swing:sword");check(sword!=null,"actual SRC sword accepted");
        LegacyTimeline.Playback swordState=sword.timeline.playback(42);double airborne=sword.sample(0,f,null,swordState).get("RightArm")[0][2];
        near(airborne,-33.2615,"SRC jumping sword pose");f.onGround=true;
        near(sword.sample(0,f,null,swordState).get("RightArm")[0][2],airborne,"jump choice retained for event");
        double ground=sword.sample(0,f,null,sword.timeline.playback(42)).get("RightArm")[0][2];check(ground>=-120&&ground<=-60,"new grounded swing random angle");
        LegacyKeyframePlayer player=new LegacyKeyframePlayer(library);LegacyAnimationPose pose=new LegacyAnimationPose();pose.bones.put("RightArm",new LegacyAnimationPose.Bone(0,0,0,true));
        f.mainId="minecraft:iron_sword";f.mainCategory="sword";f.swingSequence=1;f.swing=.1F;f.swingTicks=0;f.age=0;player.apply(pose,f);
        f.age=3;f.swingTicks=3;pose.reset();player.apply(pose,f);check(player.runtimeFailures().isEmpty(),"sword state machine samples without error");
        check(Math.abs(pose.get("RightArm").x)>.1,"sword changes real pose");
        f.swingSequence=2;f.swingTicks=0;f.age=4;f.onGround=false;pose.reset();player.apply(pose,f);
        f.age=7;f.swingTicks=3;pose.reset();player.apply(pose,f);check(player.runtimeFailures().isEmpty(),"new swing sequence resets timeline");
        System.out.println("Timeline regression: "+checks+" checks PASS");
    }
}

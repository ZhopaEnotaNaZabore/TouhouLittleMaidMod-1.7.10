import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.LegacyBedrockModel;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.animation.*;
import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.lang.reflect.Field;
import java.util.*;

/** Exercises every bundled catalog entry with its own geometry and animation profile. */
public final class AllMaidModelAudit {
    static Path assets, pack;
    static JsonObject read(Path p)throws Exception {try(Reader r=Files.newBufferedReader(p)){return new JsonParser().parse(r).getAsJsonObject();}}
    static Path resolve(String id){String[] s=id.split(":",2);Path legacy=assets.getParent().getParent().getParent().resolve("legacy/resources/assets").resolve(s[0]).resolve(s[1]);if(Files.exists(legacy))return legacy;Path p=pack.resolve(s[0]).resolve(s[1]);return Files.exists(p)?p:assets.resolve(s[0]).resolve(s[1]);}
    public static void main(String[] args)throws Exception {
        assets=Paths.get(args[0],"src/main/resources/assets");pack=assets.resolve("touhou_little_maid/tlm_custom_pack/touhou_little_maid-1.0.0/assets");
        Field field=LegacyBedrockModel.class.getDeclaredField("pose");field.setAccessible(true);
        List<String> report=new ArrayList<String>();report.add("model\ttype\tstate\tchanged_bones\truntime_errors\trejected_clips");
        List<String> blocked=new ArrayList<String>();blocked.add("model\tclip\treason");
        int models=0,gecko=0,samples=0;Set<String> geometry=new HashSet<String>();
        try(DirectoryStream<Path> domains=Files.newDirectoryStream(pack)) {for(Path domain:domains){Path catalog=domain.resolve("maid_model.json");if(!Files.exists(catalog))continue;
            for(JsonElement element:read(catalog).getAsJsonArray("model_list")) {
                JsonObject entry=element.getAsJsonObject();String id=entry.get("model_id").getAsString();String[] parts=id.split(":",2);
                String model=entry.has("model")?entry.get("model").getAsString():parts[0]+":models/entity/"+parts[1]+".json";
                List<String> scripts=new ArrayList<String>();if(entry.has("animation"))for(JsonElement script:entry.getAsJsonArray("animation"))scripts.add(script.getAsString());
                LegacyAnimationProfile profile=new LegacyAnimationProfile(entry.has("is_gecko")&&entry.get("is_gecko").getAsBoolean(),scripts);
                LegacyBedrockModel parsed;try(InputStream in=Files.newInputStream(resolve(model))){parsed=new LegacyBedrockModel(in);}
                LegacyAnimationPose pose=(LegacyAnimationPose)field.get(parsed);LegacyAnimationLibrary library=new LegacyAnimationLibrary();
                if(profile.gecko){gecko++;for(String script:profile.animations)library.merge(read(resolve(script)),script,false);if(!profile.animations.contains(LegacyAnimationProfile.DEFAULT_JSON))library.merge(read(resolve(LegacyAnimationProfile.DEFAULT_JSON)),"defaults",true);}
                else for(String script:profile.animations)if(!LegacyMaidAnimations.applyOne(new LegacyAnimationPose(),script,new LegacyAnimationFrame()))throw new AssertionError(id+" unsupported script "+script);
                models++;geometry.add(model);
                for(Map.Entry<String,String> rejection:library.diagnostics().entrySet())blocked.add(id+"\t"+rejection.getKey()+"\t"+rejection.getValue());
                for(String state:new String[]{"idle","walk","run","sit","chair","sleep","attack","bow","eat","gun_hold","gun_aim","gun_fire","gun_sleep","fishing","milk","shears","farm","feed","torch","miner","crossbow","spear"}){
                    LegacyKeyframePlayer player=new LegacyKeyframePlayer(library);Set<String> changed=new HashSet<String>();
                    for(int tick=0;tick<=60;tick++){
                        LegacyAnimationFrame f=new LegacyAnimationFrame();f.age=tick;f.onGround=true;f.limbSwing=tick*.6F;
                        f.limbAmount=state.equals("walk")||state.equals("run")?1:0;f.sprinting=state.equals("run");f.sitting=state.equals("sit");f.riding=state.equals("chair");f.sleeping=state.equals("sleep");
                        if(state.equals("attack")){f.mainId="minecraft:diamond_sword";f.mainCategory="sword";f.swing=.5F;f.swingSequence=1;f.swingTicks=tick;}
                        if(state.equals("bow")||state.equals("eat")){f.use=state.equals("bow")?"bow":"eat";f.mainId=state.equals("bow")?"minecraft:bow":"minecraft:apple";f.mainCategory=f.use;f.useTicks=tick;f.useSequence=1;f.ranged=state.equals("bow");}
                        if(state.startsWith("gun_")){f.gunType="rifle";f.mainCategory="bow";f.mainId="touhou_little_maid:animation_rifle";
                            if(state.equals("gun_aim")){f.use="bow";f.ranged=true;f.useTicks=tick;}
                            if(state.equals("gun_fire")){f.swing=.5F;f.swingSequence=1;f.swingTicks=tick;}
                            f.sleeping=state.equals("gun_sleep");}
                        if(state.equals("crossbow")||state.equals("spear")){f.mainId="tic_probe:"+state;f.mainCategory=state;f.use=state;f.ranged=true;f.useTicks=tick;f.useSequence=1;}
                        if(state.equals("fishing")){f.task="fishing";f.fishing=true;f.riding=true;f.joy="fishing";f.mainId="minecraft:fishing_rod";}
                        if(state.equals("milk")||state.equals("shears")||state.equals("farm")||state.equals("feed")||state.equals("torch")||state.equals("miner")){
                            f.task=state;f.mainId="minecraft:"+(state.equals("milk")?"bucket":state.equals("shears")?"shears":state.equals("farm")?"iron_hoe":state.equals("feed")?"wheat":state.equals("miner")?"iron_pickaxe":"torch");
                            f.mainCategory=state.equals("farm")?"hoe":state.equals("miner")?"pickaxe":"";
                            f.swing=(tick%20)<6?(tick%20)/6F:0;f.swingTicks=tick%20;f.swingSequence=tick/20+1;
                        }
                        f.groundSpeed=f.limbAmount*4;f.inputVertical=f.limbAmount;f.yaw=(float)Math.sin(tick*.1)*40;f.pitch=(float)Math.cos(tick*.1)*20;
                        f.verticalDisplacement=(float)Math.sin(tick*.2)*.02F;f.positionDelta[0]=f.limbAmount*.05;f.positionDelta[1]=f.verticalDisplacement;f.position[0]=tick*f.limbAmount*.1;
                        pose.reset();if(profile.gecko)player.apply(pose,f);else LegacyMaidAnimations.apply(pose,profile,f);
                        if(id.equals("geckolib:sta")){
                            if(state.startsWith("gun_")&&!state.equals("gun_sleep") && pose.get("74m").scaleX<=0)throw new AssertionError("Sta rifle hidden "+state);
                            if(state.equals("gun_sleep") && pose.get("74m").scaleX!=0)throw new AssertionError("Sta rifle shown asleep");
                            if(state.equals("attack")&&tick>=2&&pose.get("dao1").scaleX<=0)throw new AssertionError("Sta knife hidden");
                        }
                        if(id.equals("geckolib:winefox_survivor")&&state.equals("bow")&&tick>=2){
                            LegacyAnimationPose.Bone locator=pose.get("RightHandLocator"),gun=pose.get("bow2");
                            if(locator.scaleX!=0||locator.scaleY!=0||locator.scaleZ!=0)throw new AssertionError("Survivor vanilla bow must be hidden");
                            if(gun==null||gun.scaleX<=0||gun.scaleY<=0||gun.scaleZ<=0)throw new AssertionError("Survivor built-in weapon must remain visible");
                        }
                        if(id.equals("geckolib:winefox_magical")&&state.equals("bow")&&tick>=2){
                            LegacyAnimationPose.Bone locator=pose.get("RightHandLocator"),staff=pose.get("mofazhang");
                            if(locator.scaleX!=0||locator.scaleY!=0||locator.scaleZ!=0)throw new AssertionError("Magical bow locator not hidden at "+tick);
                            if(staff==null||staff.scaleX<=0||staff.scaleY<=0||staff.scaleZ<=0)throw new AssertionError("Magical staff not visible at "+tick);
                        }
                        for(Map.Entry<String,LegacyAnimationPose.Bone> b:pose.bones.entrySet()){
                            LegacyAnimationPose.Bone v=b.getValue();for(float n:new float[]{v.x,v.y,v.z,v.offsetX,v.offsetY,v.offsetZ,v.scaleX,v.scaleY,v.scaleZ})if(!Float.isFinite(n))throw new AssertionError(id+" "+state+" nonfinite "+b.getKey());
                            if(Math.abs(v.x-v.baseX)+Math.abs(v.y-v.baseY)+Math.abs(v.z-v.baseZ)+Math.abs(v.offsetX)+Math.abs(v.offsetY)+Math.abs(v.offsetZ)+Math.abs(v.scaleX-1)+Math.abs(v.scaleY-1)+Math.abs(v.scaleZ-1)>.001||v.visible!=v.baseVisible)changed.add(b.getKey());
                        }samples++;
                    }
                    report.add(id+"\t"+(profile.gecko?"gecko":"basic")+"\t"+state+"\t"+changed.size()+"\t"+player.runtimeDiagnostics()+"\t"+(profile.gecko?library.diagnostics():"{}"));
                    if(!player.runtimeFailures().isEmpty())throw new AssertionError(id+" "+state+" "+player.runtimeDiagnostics());
                }
            }
        }}
        Path out=Paths.get(args[0],"build/render-review/all-model-animation-audit.tsv");Files.createDirectories(out.getParent());Files.write(out,report,java.nio.charset.StandardCharsets.UTF_8);
        Files.write(out.resolveSibling("all-model-rejected-clips.tsv"),blocked,java.nio.charset.StandardCharsets.UTF_8);
        System.out.println("All model audit: "+models+" entries, "+geometry.size()+" geometries, "+gecko+" Gecko entries, "+samples+" finite pose frames PASS; "+(blocked.size()-1)+" rejected model/clip pairs remain (not visual acceptance). Report: "+out);
    }
}

import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.animation.*;
import com.google.gson.*;
import java.nio.file.*;
import java.io.*;
import java.util.*;
/** Inventories actual source animation files; rejection is recorded, not counted as support. */
public final class CorpusChecks {
    public static void main(String[] args)throws Exception {
        Path root=Paths.get(args[0]),out=Paths.get(args[1]);List<Path> paths=new ArrayList<Path>();
        try(java.util.stream.Stream<Path> stream=Files.walk(root.resolve("src/main/resources"))){stream.filter(p->p.toString().endsWith(".animation.json")).forEach(paths::add);}
        Collections.sort(paths);int supported=0,rejected=0,samples=0,runtime=0;
        try(PrintWriter report=new PrintWriter(Files.newBufferedWriter(out))){
            report.println("path\tclip\tstatus\tdetails");
            for(Path path:paths){LegacyAnimationLibrary library=new LegacyAnimationLibrary();JsonObject json;
                try(Reader reader=Files.newBufferedReader(path)){json=new JsonParser().parse(reader).getAsJsonObject();}
                library.merge(json,root.relativize(path).toString(),false);
                for(Map.Entry<String,String> entry:library.diagnostics().entrySet()){rejected++;report.println(root.relativize(path)+"\t"+entry.getKey()+"\tREJECTED\t"+entry.getValue().replace('\t',' ').replace('\n',' '));}
                for(LegacyKeyframeClip clip:library.clips().values()){
                    boolean failed=false;String reason="";LegacyAnimationFrame f=new LegacyAnimationFrame();f.age=87;f.yaw=23;f.pitch=-14;f.limbAmount=.8F;
                    for(double time:new double[]{0,.025,.05,.125,.25,.375,.5,.75,1,1.5,2,5,60})try{clip.sample(time,f,null);samples++;}catch(RuntimeException e){failed=true;reason=e.toString();break;}
                    if(failed)runtime++;else supported++;
                    report.println(root.relativize(path)+"\t"+clip.name+"\t"+(failed?"RUNTIME_REJECTED":"SUPPORTED_SUBSET")+"\t"+reason.replace('\t',' ').replace('\n',' '));
                }
            }
        }
        System.out.println("Animation corpus: "+paths.size()+" files; "+supported+" accepted clips, "+rejected+" load-time rejections, "+runtime+" runtime rejections; "+samples+" finite samples (real Gson, numerical sampling without game loop)");
    }
}

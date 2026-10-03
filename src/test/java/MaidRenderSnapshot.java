import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.LegacyBedrockModel;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.animation.*;
import com.google.gson.*;
import net.minecraft.client.model.ModelRenderer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;

/** Optional offscreen visual probe. Renders production geometry/GL bone traversal;
 * intentionally separate from headless regressions and from the running client. */
public final class MaidRenderSnapshot {
    static final int SIZE=384;
    static Object field(Object o,String name)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}
    static void value(Object o,String name,float v)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);f.setFloat(o,v);}
    static JsonObject read(Path p)throws Exception{try(Reader r=Files.newBufferedReader(p)){return new JsonParser().parse(r).getAsJsonObject();}}
    public static void main(String[] args)throws Exception{
        Path root=Paths.get(args[0]),output=Paths.get(args[1]);Files.createDirectories(output);
        Path assets=root.resolve("src/main/resources/assets"),pack=assets.resolve("touhou_little_maid/tlm_custom_pack/touhou_little_maid-1.0.0/assets/geckolib");
        Pbuffer buffer=new Pbuffer(SIZE,SIZE,new PixelFormat(8,24,0),null,null);
        try{
            buffer.makeCurrent();
            float viewAngle=args.length>2?Float.parseFloat(args[2]):200;
            BufferedImage sheet=new BufferedImage(SIZE*5,SIZE*4,BufferedImage.TYPE_INT_RGB);
            LegacyBedrockModel backpack;
            try(InputStream in=Files.newInputStream(assets.resolve("touhou_little_maid/models/bedrock/entity/backpack/big_backpack.json"))){backpack=new LegacyBedrockModel(in);}
            int backpackTexture=upload(ImageIO.read(assets.resolve("touhou_little_maid/textures/bedrock/entity/backpack/big_backpack.png").toFile()));
            int bowTexture, swordTexture;
            Path clientJar=args.length>3?Paths.get(args[3]):Paths.get(System.getProperty("user.home"),"AppData/Roaming/.minecraft/versions/1.7.10/1.7.10.jar");
            try(java.util.zip.ZipFile zip=new java.util.zip.ZipFile(clientJar.toFile())){
                try(InputStream in=zip.getInputStream(zip.getEntry("assets/minecraft/textures/items/bow_pulling_2.png"))){bowTexture=upload(ImageIO.read(in));}
                try(InputStream in=zip.getInputStream(zip.getEntry("assets/minecraft/textures/items/diamond_sword.png"))){swordTexture=upload(ImageIO.read(in));}
            }
            boolean all=Boolean.getBoolean("maid.snapshot.all");
            List<JsonObject> entries=new ArrayList<JsonObject>();
            if(all)for(JsonElement e:read(pack.resolve("maid_model.json")).getAsJsonArray("model_list"))entries.add(e.getAsJsonObject());
            else for(String n:new String[]{"zhiban","zhiban_hanfu","zhiban_new_year","hakurei_reimu"}){JsonObject e=new JsonObject();e.addProperty("model_id",(n.equals("hakurei_reimu")?"touhou_little_maid:":"geckolib:")+n);e.addProperty("is_gecko",!n.equals("hakurei_reimu"));if(!n.equals("hakurei_reimu")){JsonArray a=new JsonArray();a.add(new JsonPrimitive("geckolib:animation/"+(n.equals("zhiban")?n:"zhiban_hanfu")+".animation.json"));e.add("animation",a);e.addProperty("render_entity_scale",.65);}entries.add(e);}
            String[] names=new String[entries.size()];for(int i=0;i<names.length;i++)names[i]=entries.get(i).get("model_id").getAsString().split(":")[1];
            for(int row=0;row<names.length;row++){
                String name=names[row];JsonObject entry=entries.get(row);boolean gecko=entry.get("is_gecko").getAsBoolean();LegacyBedrockModel model;
                if(row%4==0)sheet=new BufferedImage(SIZE*5,SIZE*4,BufferedImage.TYPE_INT_RGB);
                Path modelPack=gecko?pack:pack.resolveSibling("touhou_little_maid");
                try(InputStream in=Files.newInputStream((entry.has("model")?asset(pack,assets,entry.get("model").getAsString()):modelPack.resolve("models/entity/"+name+".json")))){model=new LegacyBedrockModel(in);}
                LegacyAnimationLibrary library=new LegacyAnimationLibrary();
                if(gecko&&entry.has("animation"))for(JsonElement animation:entry.getAsJsonArray("animation"))library.merge(read(asset(pack,assets,animation.getAsString())),name,false);
                library.merge(read(assets.resolve("touhou_little_maid/animation/maid.animation.json")),"default",true);
                LegacyAnimationProfile profile=new LegacyAnimationProfile(gecko,Collections.<String>emptyList());model.configureAnimations(profile,library);
                int texture=upload(ImageIO.read((entry.has("texture")?asset(pack,assets,entry.get("texture").getAsString()):modelPack.resolve("textures/entity/"+name+".png")).toFile()));
                for(int column=0;column<5;column++){
                    LegacyAnimationPose pose=(LegacyAnimationPose)field(model,"pose");LegacyAnimationFrame frame=new LegacyAnimationFrame();
                    frame.sitting=column==1;frame.mainId=column==2?"minecraft:diamond_sword":"";frame.mainCategory=column==2?"sword":"";
                    if(column>=3){frame.mainId="minecraft:bow";frame.mainCategory="bow";frame.use="bow";frame.useSequence=1;frame.ranged=true;}
                    LegacyKeyframePlayer player=new LegacyKeyframePlayer(library);
                    for(int tick=0;tick<=(column==3?10:column==4?19:3);tick++){frame.age=tick;frame.useTicks=tick;if(column==2){frame.swingSequence=1;frame.swingTicks=tick;frame.swing=tick/6F;}pose.reset();if(gecko)player.apply(pose,frame);else LegacyMaidAnimations.apply(pose,profile,frame);}
                    Map<?,?> bones=(Map<?,?>)field(model,"bones");
                    for(Map.Entry<String,LegacyAnimationPose.Bone> e:pose.bones.entrySet()){
                        ModelRenderer r=(ModelRenderer)field(bones.get(e.getKey()),"renderer");LegacyAnimationPose.Bone p=e.getValue();
                        r.rotateAngleX=p.x;r.rotateAngleY=p.y;r.rotateAngleZ=p.z;r.offsetX=p.offsetX;r.offsetY=p.offsetY;r.offsetZ=p.offsetZ;
                        value(r,"scaleX",p.scaleX);value(r,"scaleY",p.scaleY);value(r,"scaleZ",p.scaleZ);r.showModel=p.visible;
                    }
                    GL11.glViewport(0,0,SIZE,SIZE);GL11.glClearColor(.12F,.12F,.14F,1);GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
                    GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glLoadIdentity();GL11.glOrtho(-1.25,1.25,-.15,2.35,-10,10);
                    float modelScale=entry.has("render_entity_scale")?entry.get("render_entity_scale").getAsFloat():1;
                    GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();GL11.glRotatef(viewAngle,0,1,0);GL11.glScalef(modelScale,-modelScale,modelScale);GL11.glTranslatef(0,-1.5F,0);
                    GL11.glEnable(GL11.GL_TEXTURE_2D);GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture);GL11.glEnable(GL11.GL_DEPTH_TEST);GL11.glDisable(GL11.GL_CULL_FACE);
                    GL11.glEnable(GL11.GL_ALPHA_TEST);GL11.glAlphaFunc(GL11.GL_GREATER,.1F);GL11.glColor4f(1,1,1,1);
                    GL11.glPushMatrix();
                    java.lang.reflect.Method global=LegacyBedrockModel.class.getDeclaredMethod("applyGlobalTransforms");global.setAccessible(true);global.invoke(model);
                    for(Object r:(List<?>)field(model,"roots"))((ModelRenderer)r).render(.0625F);
                    GL11.glPopMatrix();
                    GL11.glPushMatrix();
                    if(com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.LegacyMaidAccessoryModels.postRenderBackpack(model)){
                        GL11.glBindTexture(GL11.GL_TEXTURE_2D,backpackTexture);backpack.renderStatic(.0625F,null,null);
                    }
                    GL11.glPopMatrix();
                    if(column>=2){
                        GL11.glPushMatrix();
                        boolean showItem=model.postRenderHand(gecko?null:"armRight",gecko?"RightHandLocator":null,.0625F);
                        if((name.equals("winefox_magical")||name.equals("winefox_survivor"))&&column>=3&&showItem)throw new AssertionError("Magical staff must hide vanilla bow");
                        if(showItem){
                        if(gecko){GL11.glRotatef(180,0,0,1);GL11.glTranslatef(0,-.0625F,-.1F);GL11.glRotatef(-90,1,0,0);}
                        else{GL11.glRotatef(-90,1,0,0);GL11.glRotatef(180,0,1,0);GL11.glTranslatef(.0625F,.125F,-.525F);}
                        com.github.tartaricacid.touhoulittlemaid.client.renderer.LegacyVanillaHandTransform.apply(com.github.tartaricacid.touhoulittlemaid.client.renderer.LegacyVanillaHandTransform.Kind.valueOf(column==2?"HANDHELD":"BOW"),false);
                        GL11.glTranslatef(0,-.3F,0);GL11.glScalef(1.5F,1.5F,1.5F);GL11.glRotatef(50,0,1,0);GL11.glRotatef(335,0,0,1);GL11.glTranslatef(-.9375F,-.0625F,0);
                        GL11.glBindTexture(GL11.GL_TEXTURE_2D,column==2?swordTexture:bowTexture);
                        net.minecraft.client.renderer.ItemRenderer.renderItemIn2D(net.minecraft.client.renderer.Tessellator.instance,1,0,0,1,16,16,.0625F);
                        }
                        GL11.glPopMatrix();
                    }
                    ByteBuffer pixels=BufferUtils.createByteBuffer(SIZE*SIZE*4);GL11.glReadPixels(0,0,SIZE,SIZE,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,pixels);
                    for(int y=0;y<SIZE;y++)for(int x=0;x<SIZE;x++){int i=(y*SIZE+x)*4;int rgb=((pixels.get(i)&255)<<16)|((pixels.get(i+1)&255)<<8)|(pixels.get(i+2)&255);sheet.setRGB(column*SIZE+x,(row%4)*SIZE+SIZE-1-y,rgb);}
                    System.out.println(name+" "+new String[]{"idle","sit","sword","bow10","bow19"}[column]+" runtime failures: "+player.runtimeDiagnostics());
                }
                if(all&&(row%4==3||row==names.length-1))ImageIO.write(sheet,"png",output.resolve("all-gecko-"+(row/4)+".png").toFile());
                GL11.glDeleteTextures(texture);
            }
            String filename=viewAngle==200?"maid-poses-front.png":"maid-poses-back.png";
            if(!all)ImageIO.write(sheet,"png",output.resolve(filename).toFile());
            System.out.println("Offscreen snapshots: "+output.resolve(filename));
        }finally{buffer.destroy();}
    }
    static Path asset(Path pack,Path assets,String id){String[] s=id.split(":",2);Path legacy=assets.getParent().getParent().getParent().resolve("legacy/resources/assets").resolve(s[0]).resolve(s[1]);if(Files.exists(legacy))return legacy;Path p=pack.resolveSibling(s[0]).resolve(s[1]);return Files.exists(p)?p:assets.resolve(s[0]).resolve(s[1]);}
    static int upload(BufferedImage image){
        ByteBuffer rgba=BufferUtils.createByteBuffer(image.getWidth()*image.getHeight()*4);
        for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++){int c=image.getRGB(x,y);rgba.put((byte)(c>>16)).put((byte)(c>>8)).put((byte)c).put((byte)(c>>24));}rgba.flip();
        int id=GL11.glGenTextures();GL11.glBindTexture(GL11.GL_TEXTURE_2D,id);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_NEAREST);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_NEAREST);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL11.GL_RGBA,image.getWidth(),image.getHeight(),0,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,rgba);return id;
    }
}

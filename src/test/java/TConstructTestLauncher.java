import java.io.File;
import java.net.URLClassLoader;
import java.util.HashMap;
import net.minecraft.launchwrapper.*;
/** Optional real-TiC probe; the production mod never bundles its dependencies. */
public final class TConstructTestLauncher {
    public static void main(String[] args)throws Exception {
        Launch.classLoader=new LaunchClassLoader(((URLClassLoader)TConstructTestLauncher.class.getClassLoader()).getURLs());
        Launch.blackboard=new HashMap<String,Object>();Launch.minecraftHome=new File(".");
        Launch.classLoader.registerTransformer("cpw.mods.fml.common.asm.transformers.EventSubscriptionTransformer");
        Launch.classLoader.registerTransformer("TConstructTestLauncher$OptionalInterfaces");
        Thread.currentThread().setContextClassLoader(Launch.classLoader);
        Launch.classLoader.loadClass("cpw.mods.fml.common.Loader").getMethod("injectData",Object[].class).invoke(null,(Object)new Object[]{"7","10","99","99","1.7.10","9.05",new File("."),new java.util.ArrayList<String>()});
        try{Launch.classLoader.loadClass("com.github.tartaricacid.touhoulittlemaid.test.TConstructRegression").getMethod("run").invoke(null);}
        catch(java.lang.reflect.InvocationTargetException e){e.getCause().printStackTrace();System.exit(1);}
    }
    /** Equivalent to FML optional-interface stripping for the five absent integrations. */
    public static final class OptionalInterfaces implements IClassTransformer {
        public byte[] transform(String name,String mapped,byte[] bytes){
            if(bytes==null || !name.startsWith("tconstruct."))return bytes;
            org.objectweb.asm.tree.ClassNode node=new org.objectweb.asm.tree.ClassNode();new org.objectweb.asm.ClassReader(bytes).accept(node,0);
            java.util.Iterator<String> i=node.interfaces.iterator();while(i.hasNext()){String n=i.next();if(n.startsWith("cofh/")||n.startsWith("mods/battlegear2/")||n.startsWith("zeldaswordskills/")||n.startsWith("dynamicswordskills/"))i.remove();}
            java.util.Iterator<org.objectweb.asm.tree.MethodNode> methods=node.methods.iterator();
            while(methods.hasNext()){
                org.objectweb.asm.tree.MethodNode method=methods.next();boolean optional=false;
                for(java.util.List<org.objectweb.asm.tree.AnnotationNode> annotations:java.util.Arrays.asList(method.visibleAnnotations,method.invisibleAnnotations))
                    if(annotations!=null)for(org.objectweb.asm.tree.AnnotationNode a:annotations)if(a.desc.equals("Lcpw/mods/fml/common/Optional$Method;"))optional=true;
                if(optional)methods.remove();
            }
            org.objectweb.asm.ClassWriter writer=new org.objectweb.asm.ClassWriter(0);node.accept(writer);return writer.toByteArray();
        }
    }
}

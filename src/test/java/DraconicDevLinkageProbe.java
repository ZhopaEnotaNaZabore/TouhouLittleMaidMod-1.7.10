import java.io.File;
import java.net.URLClassLoader;
import java.util.HashMap;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
/** Optional dev-mod linkage check; no client or world is started. */
public final class DraconicDevLinkageProbe {
    public static void main(String[] args)throws Exception {
        Launch.classLoader=new LaunchClassLoader(((URLClassLoader)DraconicDevLinkageProbe.class.getClassLoader()).getURLs());
        Launch.blackboard=new HashMap<String,Object>();Launch.minecraftHome=new File(".");
        Launch.classLoader.registerTransformer("cpw.mods.fml.common.asm.transformers.EventSubscriptionTransformer");
        Thread.currentThread().setContextClassLoader(Launch.classLoader);
        Launch.classLoader.loadClass("cpw.mods.fml.common.Loader").getMethod("injectData",Object[].class).invoke(null,(Object)new Object[]{"7","10","99","99","1.7.10","9.05",new File("."),new java.util.ArrayList<String>()});
        Launch.classLoader.loadClass("net.minecraft.init.Bootstrap").getMethod("func_151354_b").invoke(null);
        Class<?> block=Launch.classLoader.loadClass("com.brandon3055.draconicevolution.common.blocks.ChaosShardAtmos");
        Object instance=block.newInstance();
        if(!((Boolean)block.getMethod("getTickRandomly").invoke(instance)))throw new AssertionError("Tick flag lost");
        System.out.println("Draconic ChaosShardAtmos constructor and setTickRandomly linkage PASS");
    }
}

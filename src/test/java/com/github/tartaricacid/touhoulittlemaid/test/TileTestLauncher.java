package com.github.tartaricacid.touhoulittlemaid.test;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

/** FML registries require LaunchClassLoader even in tests without a game loop. */
public final class TileTestLauncher {
    public static void main(String[] args) throws Exception {
        URL[] urls=((URLClassLoader)TileTestLauncher.class.getClassLoader()).getURLs();
        Launch.classLoader=new LaunchClassLoader(urls);
        Launch.blackboard=new HashMap<String,Object>();
        Launch.minecraftHome=new File(".");
        Launch.classLoader.registerTransformer("cpw.mods.fml.common.asm.transformers.EventSubscriptionTransformer");
        Thread.currentThread().setContextClassLoader(Launch.classLoader);
        Class<?> loader=Launch.classLoader.loadClass("cpw.mods.fml.common.Loader");
        loader.getMethod("injectData",Object[].class).invoke(null,(Object)new Object[]{"7","10","99","99","1.7.10","9.05",new File("."),new java.util.ArrayList<String>()});
        try {
            Launch.classLoader.loadClass("com.github.tartaricacid.touhoulittlemaid.test.TileEntityRegression")
                    .getMethod("main",String[].class).invoke(null,(Object)args);
        } catch(java.lang.reflect.InvocationTargetException failure) {
            failure.getCause().printStackTrace(); System.exit(1);
        }
    }
}

package com.github.tartaricacid.touhoulittlemaid;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityPowerPoint;
import com.github.tartaricacid.touhoulittlemaid.entity.projectile.EntityDanmaku;
import com.github.tartaricacid.touhoulittlemaid.entity.monster.EntityFairy;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityExtinguishingAgent;
import com.github.tartaricacid.touhoulittlemaid.entity.projectile.EntityMaidFishingHook;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityChair;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityBroom;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityBox;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityTombstone;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityMaidPainting;
import com.github.tartaricacid.touhoulittlemaid.entity.projectile.EntityThrowPowerPoint;
import com.github.tartaricacid.touhoulittlemaid.command.CommandTlmMaid;
import com.github.tartaricacid.touhoulittlemaid.command.CommandTlmChat;
import com.github.tartaricacid.touhoulittlemaid.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.proxy.CommonProxy;
import com.github.tartaricacid.touhoulittlemaid.init.ModItems;
import com.github.tartaricacid.touhoulittlemaid.init.ModBlocks;
import com.github.tartaricacid.touhoulittlemaid.config.LegacyConfig;
import com.github.tartaricacid.touhoulittlemaid.init.LegacyRecipes;
import com.github.tartaricacid.touhoulittlemaid.init.LegacyLoot;
import com.github.tartaricacid.touhoulittlemaid.init.ModEnchantments;
import com.github.tartaricacid.touhoulittlemaid.compat.LegacyCompatibility;
import com.github.tartaricacid.touhoulittlemaid.init.ModAchievements;
import com.github.tartaricacid.touhoulittlemaid.test.LegacyPortSelfTest;
import com.github.tartaricacid.touhoulittlemaid.capability.LegacyPlayerPowerEvents;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import com.github.tartaricacid.touhoulittlemaid.network.ServerThreadDispatcher;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = TouhouLittleMaid.MOD_ID, name = TouhouLittleMaid.NAME, version = TouhouLittleMaid.VERSION)
public final class TouhouLittleMaid {
    public static final String MOD_ID = "touhou_little_maid";
    public static final String NAME = "Touhou Little Maid";
    public static final String VERSION = "0.1.0-port";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Mod.Instance(MOD_ID)
    public static TouhouLittleMaid instance;

    @SidedProxy(
            clientSide = "com.github.tartaricacid.touhoulittlemaid.proxy.ClientProxy",
            serverSide = "com.github.tartaricacid.touhoulittlemaid.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        FMLCommonHandler.instance().bus().register(ServerThreadDispatcher.INSTANCE);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(LegacyPlayerPowerEvents.INSTANCE);
        LegacyConfig.load(event.getSuggestedConfigurationFile());
        ModEnchantments.init();
        ModItems.init();
        ModBlocks.init();
        ModAchievements.init();
        LegacyRecipes.init(); LegacyLoot.init();
        NetworkHandler.init();
        EntityRegistry.registerModEntity(EntityMaid.class, "maid", 0, this, 80, 3, true);
        EntityRegistry.registerModEntity(EntityPowerPoint.class, "power_point", 1, this, 64, 10, true);
        EntityRegistry.registerModEntity(EntityDanmaku.class, "danmaku", 2, this, 64, 10, true);
        EntityRegistry.registerModEntity(EntityFairy.class, "fairy", 3, this, 80, 3, true);
        EntityRegistry.registerModEntity(EntityExtinguishingAgent.class, "extinguishing_agent", 4, this, 64, 3, false);
        EntityRegistry.registerModEntity(EntityMaidFishingHook.class, "maid_fishing_hook", 5, this, 64, 5, true);
        EntityRegistry.registerModEntity(EntityChair.class, "chair", 6, this, 80, 3, true);
        EntityRegistry.registerModEntity(EntitySit.class, "sit", 7, this, 64, 3, false);
        EntityRegistry.registerModEntity(EntityBroom.class, "broom", 8, this, 80, 3, true);
        EntityRegistry.registerModEntity(EntityBox.class, "box", 9, this, 80, 3, true);
        EntityRegistry.registerModEntity(EntityTombstone.class, "tombstone", 10, this, 80, 3, true);
        EntityRegistry.registerModEntity(EntityMaidPainting.class, "wine_fox_painting", 11, this, 80, Integer.MAX_VALUE, false);
        EntityRegistry.registerModEntity(EntityThrowPowerPoint.class, "throw_power_point", 12, this, 64, 10, true);
        if (LegacyConfig.fairySpawnWeight > 0) EntityRegistry.addSpawn(EntityFairy.class, LegacyConfig.fairySpawnWeight, 1, 2, EnumCreatureType.monster,
                BiomeGenBase.plains, BiomeGenBase.forest, BiomeGenBase.taiga,
                BiomeGenBase.swampland, BiomeGenBase.extremeHills);
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LegacyCompatibility.init();
        LegacyPortSelfTest.run();
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandTlmMaid());
        event.registerServerCommand(new CommandTlmChat());
    }
}

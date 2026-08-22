package com.github.tartaricacid.touhoulittlemaid.config;

import net.minecraftforge.common.config.Configuration;
import java.io.File;

/** Forge 1.7 Configuration replacement for the core modern ForgeConfigSpec values. */
public final class LegacyConfig {
    public static int fairySpawnWeight=8, maxMaidPerPlayer=20, backupIntervalTicks=6000;
    public static int shrineLampMaxRange=6, scarecrowRange=48;
    public static int minerSearchRadius=8, minerVerticalRange=4, minerScanBudget=384;
    public static float shrineLampEffectCost=0.9F, shrineLampMaxStorage=100.0F;
    public static boolean boardOwnerOnly=true, crossDimensionFollow=true, enableMaidChat=false, enableTts=false;
    public static boolean replaceSlimeModel=true, replaceXpTexture=true;
    public static boolean minerAreaMining=true;
    public static String[] minerOreAllowList=new String[0], minerOreDenyList=new String[0];
    public static String chatServiceUrl="", chatApiKey="", chatModel="gpt-4o-mini", ttsServiceUrl="";
    public static int chatTimeoutSeconds=30, chatCooldownSeconds=10, chatHistorySize=16;
    public static int impedingEnchantmentId=125, speedyEnchantmentId=126, endersEnderEnchantmentId=127;
    private LegacyConfig(){}
    public static void load(File file){Configuration c=new Configuration(file);try{c.load();
        fairySpawnWeight=c.getInt("fairySpawnWeight","spawn",8,0,100,"Fairy spawn weight; 0 disables natural spawning");
        maxMaidPerPlayer=c.getInt("maxMaidPerPlayer","maid",20,1,1000,"Maximum tamed maids per owner");
        backupIntervalTicks=c.getInt("backupIntervalTicks","maid",6000,200,72000,"Ticks between rolling maid backups");
        shrineLampEffectCost=c.getFloat("ShrineLampEffectCost","gameplay",0.9F,0,Float.MAX_VALUE,"Shrine Lamp displayed Power cost per hour");
        shrineLampMaxStorage=c.getFloat("ShrineLampMaxStorage","gameplay",100.0F,0,Float.MAX_VALUE,"Maid Beacon maximum Power storage");
        shrineLampMaxRange=c.getInt("ShrineLampMaxRange","gameplay",6,0,256,"Maid Beacon Power Point absorption range");
        scarecrowRange=c.getInt("ScarecrowRange","gameplay",48,0,256,"Square range in which scarecrows prevent fairy spawning");
        minerSearchRadius=c.getInt("minerSearchRadius","extras.miner",8,1,32,"Horizontal ore-search radius");
        minerVerticalRange=c.getInt("minerVerticalRange","extras.miner",4,1,16,"Vertical ore-search radius");
        minerScanBudget=c.getInt("minerScanBudget","extras.miner",384,32,8192,"Maximum block states checked per Maid work tick");
        minerAreaMining=c.getBoolean("minerAreaMining","extras.miner",true,"Allow supported hammer-like tools to mine a protected 3x3 plane");
        minerOreAllowList=c.getStringList("oreAllowList","extras.miner",new String[0],"Extra ore states: modid:block or modid:block@metadata");
        minerOreDenyList=c.getStringList("oreDenyList","extras.miner",new String[0],"Denied states; evaluated before adapters and OreDictionary");
        boardOwnerOnly=c.getBoolean("boardOwnerOnly","gameplay",true,"Only an owner may play against their maid");
        crossDimensionFollow=c.getBoolean("crossDimensionFollow","maid",true,"Allow following an owner between dimensions");
        replaceSlimeModel=c.getBoolean("ReplaceSlimeModel","vanilla",true,"Replace vanilla slime/magma cube with bundled Yukkuri models");
        replaceXpTexture=c.getBoolean("ReplaceXPTexture","vanilla",true,"Replace vanilla experience orbs with point items");
        enableMaidChat=c.getBoolean("enableMaidChat","ai",false,"Enable external AI chat service");
        enableTts=c.getBoolean("enableTts","ai",false,"Enable external TTS service");
        chatServiceUrl=c.getString("chatServiceUrl","ai","","HTTP endpoint; leave blank to disable");
        chatApiKey=c.getString("chatApiKey","ai","","Secret API key (server-side config only)");
        chatModel=c.getString("chatModel","ai","gpt-4o-mini","OpenAI-compatible model name");
        chatTimeoutSeconds=c.getInt("chatTimeoutSeconds","ai",30,3,60,"External service connect/read timeout");
        chatCooldownSeconds=c.getInt("chatCooldownSeconds","ai",10,1,300,"Per-player chat cooldown");
        chatHistorySize=c.getInt("chatHistorySize","ai",16,2,64,"Recent user/assistant messages kept per maid");
        ttsServiceUrl=c.getString("ttsServiceUrl","ai","","TTS HTTP endpoint");
        impedingEnchantmentId=c.getInt("impedingEnchantmentId","ids",125,0,255,"Legacy enchantment ID");
        speedyEnchantmentId=c.getInt("speedyEnchantmentId","ids",126,0,255,"Legacy enchantment ID");
        endersEnderEnchantmentId=c.getInt("endersEnderEnchantmentId","ids",127,0,255,"Legacy enchantment ID");
    }finally{if(c.hasChanged())c.save();}}
}

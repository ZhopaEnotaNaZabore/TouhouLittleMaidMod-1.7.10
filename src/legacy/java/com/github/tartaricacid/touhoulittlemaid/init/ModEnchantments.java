package com.github.tartaricacid.touhoulittlemaid.init;
import com.github.tartaricacid.touhoulittlemaid.item.enchantment.EnchantmentGohei;
import com.github.tartaricacid.touhoulittlemaid.config.LegacyConfig;
public final class ModEnchantments{public static EnchantmentGohei IMPEDING,SPEEDY,ENDERS_ENDER;private ModEnchantments(){}public static void init(){IMPEDING=new EnchantmentGohei(LegacyConfig.impedingEnchantmentId,"impeding",EnchantmentGohei.Type.IMPEDING,5);SPEEDY=new EnchantmentGohei(LegacyConfig.speedyEnchantmentId,"speedy",EnchantmentGohei.Type.SPEEDY,2);ENDERS_ENDER=new EnchantmentGohei(LegacyConfig.endersEnderEnchantmentId,"enders_ender",EnchantmentGohei.Type.ENDERS_ENDER,1);}}

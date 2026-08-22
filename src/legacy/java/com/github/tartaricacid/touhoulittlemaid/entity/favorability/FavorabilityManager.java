package com.github.tartaricacid.touhoulittlemaid.entity.favorability;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import com.github.tartaricacid.touhoulittlemaid.init.ModAchievements;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Cooldown-aware favorability levels and attribute scaling, using modern NBT keys. */
public final class FavorabilityManager {
    public static final String TAG_NAME = "FavorabilityManagerCounter";
    private static final int[] POINTS={0,64,192,384}, HEALTH={20,30,40,80}, ATTACK={2,3,4,6};
    private final EntityMaid maid; private final Map<String,Integer> cooldowns=new HashMap<String,Integer>();
    public FavorabilityManager(EntityMaid maid){this.maid=maid;}
    public void tick(){for(Map.Entry<String,Integer> e:cooldowns.entrySet())if(e.getValue()>0)e.setValue(e.getValue()-1);}
    public boolean apply(String event,int points,int cooldown){Integer left=cooldowns.get(event);if(left!=null&&left>0)return false;add(points);cooldowns.put(event,cooldown);return true;}
    public void add(int amount){int before=getLevel();maid.setFavorability(clamp(maid.getFavorability()+amount,0,384));int after=getLevel();if(before!=after){applyAttributes();if(before<3&&after>=3){Entity owner=maid.getOwner();if(owner instanceof EntityPlayer)((EntityPlayer)owner).triggerAchievement(ModAchievements.DEVOTED);}}}
    public void reduceWithoutLevel(int amount){add(-amount);}
    public int getLevel(){int p=maid.getFavorability();return p<64?0:p<192?1:p<384?2:3;}
    public int nextLevelPoint(){int level=getLevel();return level>=3?0:POINTS[level+1]-maid.getFavorability();}
    public void applyAttributes(){int level=getLevel();maid.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(HEALTH[level]);maid.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(ATTACK[level]);if(maid.getHealth()>maid.getMaxHealth())maid.setHealth(maid.getMaxHealth());}
    public void writeToNBT(NBTTagCompound root){NBTTagCompound data=new NBTTagCompound();for(Map.Entry<String,Integer> e:cooldowns.entrySet())data.setInteger(e.getKey(),e.getValue());root.setTag(TAG_NAME,data);}
    @SuppressWarnings("unchecked") public void readFromNBT(NBTTagCompound root){cooldowns.clear();if(root.hasKey(TAG_NAME,10)){NBTTagCompound data=root.getCompoundTag(TAG_NAME);for(String key:(Set<String>)data.func_150296_c())cooldowns.put(key,data.getInteger(key));}applyAttributes();}
    private static int clamp(int v,int min,int max){return Math.max(min,Math.min(max,v));}
}

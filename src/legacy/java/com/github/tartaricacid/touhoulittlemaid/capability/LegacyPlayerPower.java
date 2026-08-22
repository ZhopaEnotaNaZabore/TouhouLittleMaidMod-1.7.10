package com.github.tartaricacid.touhoulittlemaid.capability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

/** Java 8/1.7 counterpart of the source player's bounded Power capability. */
public final class LegacyPlayerPower implements IExtendedEntityProperties {
    public static final String KEY="TouhouLittleMaidPower";
    public static final float MAX=5.0F;
    private float value;

    public static void register(EntityPlayer player){if(player.getExtendedProperties(KEY)==null)player.registerExtendedProperties(KEY,new LegacyPlayerPower());}
    public static LegacyPlayerPower get(EntityPlayer player){register(player);return (LegacyPlayerPower)player.getExtendedProperties(KEY);}
    public float get(){return value;}
    public void set(float amount){value=Float.isNaN(amount)||Float.isInfinite(amount)?0:Math.max(0,Math.min(MAX,amount));}
    public float add(float amount){float before=value;set(value+Math.max(0,amount));return value-before;}
    public float take(float amount){float moved=Math.min(value,Math.max(0,amount));value-=moved;return moved;}
    @Override public void saveNBTData(NBTTagCompound root){root.setFloat(KEY,value);}
    @Override public void loadNBTData(NBTTagCompound root){set(root.getFloat(KEY));}
    @Override public void init(Entity entity,World world){}
}

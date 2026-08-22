package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityTombstone;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

/** Chat-based 1.7 replacement for the modern tracking screen. */
public final class ItemFoxScroll extends Item {
    public enum Type { MAIDS, TOMBSTONES }
    private final Type type;
    public ItemFoxScroll(String name,Type type){this.type=type;setUnlocalizedName(TouhouLittleMaid.MOD_ID+"."+name);setTextureName(TouhouLittleMaid.MOD_ID+":"+name);setMaxStackSize(1);}
    @Override public ItemStack onItemRightClick(ItemStack stack,World world,EntityPlayer player){if(!world.isRemote&&MinecraftServer.getServer()!=null){int count=0;player.addChatMessage(new ChatComponentText(type==Type.MAIDS?"Owned maids:":"Owned tombstones:"));for(WorldServer server:MinecraftServer.getServer().worldServers){for(Object value:server.loadedEntityList){Entity entity=(Entity)value;if(type==Type.MAIDS&&entity instanceof EntityMaid&&((EntityMaid)entity).getOwnerId().equals(player.getUniqueID().toString())){player.addChatMessage(line(entity.dimension,entity.posX,entity.posY,entity.posZ,entity.getCommandSenderName()));count++;}else if(type==Type.TOMBSTONES&&entity instanceof EntityTombstone&&((EntityTombstone)entity).isOwnedBy(player)){player.addChatMessage(line(entity.dimension,entity.posX,entity.posY,entity.posZ,((EntityTombstone)entity).getMaidName()));count++;}}}if(count==0)player.addChatMessage(new ChatComponentText("  none currently loaded"));}return stack;}
    private ChatComponentText line(int dim,double x,double y,double z,String name){return new ChatComponentText("  "+name+" dim "+dim+" @ "+(int)x+", "+(int)y+", "+(int)z);}
}

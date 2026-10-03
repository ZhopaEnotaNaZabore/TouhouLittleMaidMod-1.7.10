package com.github.tartaricacid.touhoulittlemaid.network.message;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.network.ServerThreadDispatcher;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;

/** Owner-only cosmetic change; custom resource-pack IDs are permitted as in SRC. */
public final class MessageMaidSkin implements IMessage {
    private int entityId; private UUID uuid; private String model="",sound="";
    public MessageMaidSkin() { }
    public MessageMaidSkin(EntityMaid maid,String model,String sound){entityId=maid.getEntityId();uuid=maid.getUniqueID();this.model=model;this.sound=sound;}
    public static boolean validId(String id){return id!=null && id.length()<=192 && id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+");}
    private static String read(ByteBuf b){int n=b.readUnsignedShort();if(n>192||n>b.readableBytes())throw new IllegalArgumentException("Invalid skin ID length");byte[] bytes=new byte[n];b.readBytes(bytes);return new String(bytes,StandardCharsets.UTF_8);}
    private static void write(ByteBuf b,String s){byte[] bytes=s.getBytes(StandardCharsets.UTF_8);if(bytes.length>192)throw new IllegalArgumentException("Skin ID too long");b.writeShort(bytes.length);b.writeBytes(bytes);}
    @Override public void fromBytes(ByteBuf b){entityId=b.readInt();uuid=new UUID(b.readLong(),b.readLong());model=read(b);sound=read(b);}
    @Override public void toBytes(ByteBuf b){b.writeInt(entityId);b.writeLong(uuid.getMostSignificantBits());b.writeLong(uuid.getLeastSignificantBits());write(b,model);write(b,sound);}
    public boolean apply(EntityMaid maid,EntityPlayerMP player){
        if(!validId(model)||(!sound.isEmpty()&&!validId(sound))||maid.getEntityId()!=entityId||!maid.getUniqueID().equals(uuid)
                ||!maid.isEntityAlive()||maid.worldObj!=player.worldObj||maid.getOwner()!=player||maid.getDistanceSqToEntity(player)>=64)return false;
        maid.setModelId(model);if(!sound.isEmpty())maid.setSoundPackId(sound);return true;
    }
    public static final class Handler implements IMessageHandler<MessageMaidSkin,IMessage>{
        @Override public IMessage onMessage(final MessageMaidSkin m,MessageContext context){final EntityPlayerMP player=context.getServerHandler().playerEntity;
            ServerThreadDispatcher.enqueue(new Runnable(){public void run(){Entity e=player.worldObj.getEntityByID(m.entityId);if(e instanceof EntityMaid)m.apply((EntityMaid)e,player);}});return null;}
    }
}

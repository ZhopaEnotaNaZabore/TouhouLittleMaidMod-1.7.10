package com.github.tartaricacid.touhoulittlemaid.network.message;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import cpw.mods.fml.common.network.simpleimpl.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import com.github.tartaricacid.touhoulittlemaid.network.ServerThreadDispatcher;

public final class MessageOpenMaidGui implements IMessage {
    private int entityId,tab;
    public MessageOpenMaidGui(){} public MessageOpenMaidGui(int entityId,int tab){this.entityId=entityId;this.tab=tab;}
    @Override public void fromBytes(ByteBuf b){entityId=b.readInt();tab=b.readUnsignedByte();}
    @Override public void toBytes(ByteBuf b){b.writeInt(entityId);b.writeByte(tab);}
    public static final class Handler implements IMessageHandler<MessageOpenMaidGui,IMessage>{
        @Override public IMessage onMessage(final MessageOpenMaidGui m,MessageContext c){final EntityPlayerMP player=c.getServerHandler().playerEntity;ServerThreadDispatcher.enqueue(new Runnable(){public void run(){Entity e=player.worldObj.getEntityByID(m.entityId);if(e instanceof EntityMaid&&((EntityMaid)e).getOwner()==player&&e.getDistanceSqToEntity(player)<64)player.openGui(TouhouLittleMaid.instance,m.tab>=0&&m.tab<=3?m.tab:0,e.worldObj,e.getEntityId(),0,0);}});return null;}
    }
}

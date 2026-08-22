package com.github.tartaricacid.touhoulittlemaid.network.message;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.MaidActivity;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.SchedulePos;
import com.github.tartaricacid.touhoulittlemaid.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.network.ServerThreadDispatcher;
import cpw.mods.fml.common.network.simpleimpl.*;import io.netty.buffer.ByteBuf;import net.minecraft.entity.Entity;import net.minecraft.entity.player.EntityPlayerMP;

public final class MessageRequestMaidHome implements IMessage{
    private int entityId;public MessageRequestMaidHome(){}public MessageRequestMaidHome(int id){entityId=id;}
    public void fromBytes(ByteBuf b){entityId=b.readInt();}public void toBytes(ByteBuf b){b.writeInt(entityId);}
    public static final class Handler implements IMessageHandler<MessageRequestMaidHome,IMessage>{public IMessage onMessage(final MessageRequestMaidHome m,MessageContext c){final EntityPlayerMP p=c.getServerHandler().playerEntity;ServerThreadDispatcher.enqueue(new Runnable(){public void run(){Entity e=p.worldObj.getEntityByID(m.entityId);if(!(e instanceof EntityMaid)||((EntityMaid)e).getOwner()!=p||e.getDistanceSqToEntity(p)>=64)return;EntityMaid maid=(EntityMaid)e;SchedulePos s=maid.getSchedulePos();NetworkHandler.channel.sendTo(new MessageMaidHome(maid.getEntityId(),point(s.getForActivity(MaidActivity.WORK)),point(s.getForActivity(MaidActivity.IDLE)),point(s.getForActivity(MaidActivity.REST)),s.getDimension(),s.isConfigured()),p);}});return null;}private static int[] point(SchedulePos.Point p){return new int[]{p.x,p.y,p.z};}}
}

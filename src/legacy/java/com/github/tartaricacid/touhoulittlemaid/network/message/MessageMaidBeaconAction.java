package com.github.tartaricacid.touhoulittlemaid.network.message;

import com.github.tartaricacid.touhoulittlemaid.capability.LegacyPlayerPower;
import com.github.tartaricacid.touhoulittlemaid.init.ModBlocks;
import com.github.tartaricacid.touhoulittlemaid.network.ServerThreadDispatcher;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityMaidBeacon;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

public final class MessageMaidBeaconAction implements IMessage {
    public static final int EFFECT=0,DEPOSIT=1,TAKE=2,OVERFLOW=3;
    private int x,y,z,action,value;
    public MessageMaidBeaconAction(){}
    public MessageMaidBeaconAction(TileEntityMaidBeacon beacon,int action,int value){x=beacon.xCoord;y=beacon.yCoord;z=beacon.zCoord;this.action=action;this.value=value;}
    @Override public void fromBytes(ByteBuf b){x=b.readInt();y=b.readInt();z=b.readInt();action=b.readUnsignedByte();value=b.readInt();}
    @Override public void toBytes(ByteBuf b){b.writeInt(x);b.writeInt(y);b.writeInt(z);b.writeByte(action);b.writeInt(value);}
    public static final class Handler implements IMessageHandler<MessageMaidBeaconAction,IMessage>{
        @Override public IMessage onMessage(final MessageMaidBeaconAction m,MessageContext context){final EntityPlayerMP player=context.getServerHandler().playerEntity;ServerThreadDispatcher.enqueue(new Runnable(){@Override public void run(){
            if(m.action<0||m.action>OVERFLOW||player.getDistanceSq(m.x+.5D,m.y+.5D,m.z+.5D)>=64||player.worldObj.getBlock(m.x,m.y,m.z)!=ModBlocks.MAID_BEACON)return;
            TileEntity tile=player.worldObj.getTileEntity(m.x,m.y,m.z);if(!(tile instanceof TileEntityMaidBeacon))return;TileEntityMaidBeacon beacon=(TileEntityMaidBeacon)tile;
            LegacyPlayerPower power=LegacyPlayerPower.get(player);
            if(m.action==EFFECT)beacon.setPotionIndex(Math.max(-1,Math.min(4,m.value)));
            else if(m.action==OVERFLOW)beacon.setOverflowDelete(m.value!=0);
            else if(m.action==DEPOSIT){
                // Source requires the player to own the requested full unit;
                // only the destination's remaining capacity may reduce it.
                if(power.get()>=1.0F){float moved=Math.min(1.0F,beacon.getMaxStorage()-beacon.getStoragePower());if(moved>0){power.take(moved);beacon.setStoragePower(beacon.getStoragePower()+moved);}}
            }
            else {
                // Likewise, "Take One" does nothing unless the lamp contains
                // one complete unit, even when the player has less free room.
                if(beacon.getStoragePower()>=1.0F){float moved=Math.min(1.0F,LegacyPlayerPower.MAX-power.get());if(moved>0){beacon.setStoragePower(beacon.getStoragePower()-moved);power.add(moved);}}
            }
            com.github.tartaricacid.touhoulittlemaid.network.NetworkHandler.channel.sendTo(new MessagePlayerPower(power.get()),player);
        }});return null;}
    }
}

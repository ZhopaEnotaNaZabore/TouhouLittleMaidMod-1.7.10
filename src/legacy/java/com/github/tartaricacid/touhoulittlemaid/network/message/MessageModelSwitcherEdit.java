package com.github.tartaricacid.touhoulittlemaid.network.message;

import com.github.tartaricacid.touhoulittlemaid.init.ModBlocks;
import com.github.tartaricacid.touhoulittlemaid.network.ServerThreadDispatcher;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityModelSwitcher;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

import java.nio.charset.Charset;

public final class MessageModelSwitcherEdit implements IMessage {
    public static final int ADD=0, REMOVE=1, APPLY=2, ROTATE_LEFT=3, ROTATE_RIGHT=4, CAPTURE=5, RENAME=6;
    private int x,y,z,action,index; private String modelId="";
    public MessageModelSwitcherEdit() { }
    public MessageModelSwitcherEdit(TileEntityModelSwitcher tile,int action,int index,String modelId) {
        x=tile.xCoord;y=tile.yCoord;z=tile.zCoord;this.action=action;this.index=index;this.modelId=modelId==null?"":modelId;
    }
    @Override public void fromBytes(ByteBuf b){x=b.readInt();y=b.readInt();z=b.readInt();action=b.readUnsignedByte();index=b.readShort();int n=b.readUnsignedShort();if(n>384||n>b.readableBytes())throw new IllegalArgumentException("Invalid Model Switcher text");byte[] data=new byte[n];b.readBytes(data);modelId=new String(data,Charset.forName("UTF-8"));}
    @Override public void toBytes(ByteBuf b){byte[] data=modelId.getBytes(Charset.forName("UTF-8"));if(data.length>384)throw new IllegalArgumentException("Model Switcher text too long");b.writeInt(x);b.writeInt(y);b.writeInt(z);b.writeByte(action);b.writeShort(index);b.writeShort(data.length);b.writeBytes(data);}
    public static final class Handler implements IMessageHandler<MessageModelSwitcherEdit,IMessage>{
        @Override public IMessage onMessage(final MessageModelSwitcherEdit m,MessageContext context){final EntityPlayerMP p=context.getServerHandler().playerEntity;ServerThreadDispatcher.enqueue(new Runnable(){@Override public void run(){
            if(p.getDistanceSq(m.x+.5D,m.y+.5D,m.z+.5D)>=64||p.worldObj.getBlock(m.x,m.y,m.z)!=ModBlocks.MODEL_SWITCHER)return;
            TileEntity raw=p.worldObj.getTileEntity(m.x,m.y,m.z);if(!(raw instanceof TileEntityModelSwitcher))return;TileEntityModelSwitcher s=(TileEntityModelSwitcher)raw;if(!s.isOwnedBy(p))return;
            switch(m.action){case ADD:if(m.modelId.length()<=96&&m.modelId.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))s.addMode(m.modelId);break;case REMOVE:s.removeMode(m.index);break;case APPLY:s.applyMode(m.index);break;case ROTATE_LEFT:s.rotateMode(m.index,-90);break;case ROTATE_RIGHT:s.rotateMode(m.index,90);break;case CAPTURE:com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid maid=s.getBoundMaid();if(maid!=null&&maid.getOwner()==p)s.capture(maid);break;case RENAME:s.renameMode(m.index,m.modelId);break;default:break;}
        }});return null;}
    }
}

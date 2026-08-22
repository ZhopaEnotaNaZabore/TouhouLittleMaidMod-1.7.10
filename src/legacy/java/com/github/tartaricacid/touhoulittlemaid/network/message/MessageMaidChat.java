package com.github.tartaricacid.touhoulittlemaid.network.message;

import com.github.tartaricacid.touhoulittlemaid.client.chat.ClientChatBubbles;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import java.nio.charset.Charset;

public final class MessageMaidChat implements IMessage {
    private int entityId;private String text="";private boolean error;
    public MessageMaidChat(){}public MessageMaidChat(int entityId,String text,boolean error){this.entityId=entityId;this.text=text;this.error=error;}
    public void fromBytes(ByteBuf b){entityId=b.readInt();error=b.readBoolean();int length=Math.min(b.readUnsignedShort(),4096);text=b.readBytes(length).toString(Charset.forName("UTF-8"));}
    public void toBytes(ByteBuf b){byte[] data=text.getBytes(Charset.forName("UTF-8"));int length=Math.min(data.length,4096);b.writeInt(entityId);b.writeBoolean(error);b.writeShort(length);b.writeBytes(data,0,length);}
    public static final class Handler implements IMessageHandler<MessageMaidChat,IMessage>{public IMessage onMessage(final MessageMaidChat m,MessageContext c){Minecraft.getMinecraft().func_152344_a(new Runnable(){public void run(){ClientChatBubbles.put(m.entityId,m.text,m.error);if(Minecraft.getMinecraft().thePlayer!=null)Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText((m.error?"§c":"§d[Maid] §f")+m.text));}});return null;}}
}

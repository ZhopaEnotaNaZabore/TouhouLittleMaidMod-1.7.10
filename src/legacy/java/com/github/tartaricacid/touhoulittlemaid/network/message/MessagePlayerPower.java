package com.github.tartaricacid.touhoulittlemaid.network.message;

import com.github.tartaricacid.touhoulittlemaid.capability.LegacyPlayerPower;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

public final class MessagePlayerPower implements IMessage {
    private float value;
    public MessagePlayerPower(){}
    public MessagePlayerPower(float value){this.value=value;}
    @Override public void fromBytes(ByteBuf b){value=b.readFloat();}
    @Override public void toBytes(ByteBuf b){b.writeFloat(value);}
    public static final class Handler implements IMessageHandler<MessagePlayerPower,IMessage>{
        @Override public IMessage onMessage(final MessagePlayerPower message,MessageContext context){Minecraft.getMinecraft().func_152344_a(new Runnable(){@Override public void run(){if(Minecraft.getMinecraft().thePlayer!=null)LegacyPlayerPower.get(Minecraft.getMinecraft().thePlayer).set(message.value);}});return null;}
    }
}

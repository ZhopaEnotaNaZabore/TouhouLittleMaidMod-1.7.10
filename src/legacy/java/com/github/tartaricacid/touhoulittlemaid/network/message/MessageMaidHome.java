package com.github.tartaricacid.touhoulittlemaid.network.message;

import com.github.tartaricacid.touhoulittlemaid.client.gui.ClientMaidHomeData;import cpw.mods.fml.common.network.simpleimpl.*;import io.netty.buffer.ByteBuf;import net.minecraft.client.Minecraft;
public final class MessageMaidHome implements IMessage{
    private int id,dimension;private boolean configured;private int[] work=new int[3],idle=new int[3],sleep=new int[3];
    public MessageMaidHome(){}public MessageMaidHome(int id,int[] w,int[] i,int[] s,int d,boolean c){this.id=id;work=w;idle=i;sleep=s;dimension=d;configured=c;}
    public void fromBytes(ByteBuf b){id=b.readInt();read(b,work);read(b,idle);read(b,sleep);dimension=b.readInt();configured=b.readBoolean();}
    public void toBytes(ByteBuf b){b.writeInt(id);write(b,work);write(b,idle);write(b,sleep);b.writeInt(dimension);b.writeBoolean(configured);}
    private static void read(ByteBuf b,int[] p){for(int n=0;n<3;n++)p[n]=b.readInt();}private static void write(ByteBuf b,int[] p){for(int n:p)b.writeInt(n);}
    public static final class Handler implements IMessageHandler<MessageMaidHome,IMessage>{public IMessage onMessage(final MessageMaidHome m,MessageContext c){Minecraft.getMinecraft().func_152344_a(new Runnable(){public void run(){ClientMaidHomeData.put(m.id,new ClientMaidHomeData.Entry(m.work,m.idle,m.sleep,m.dimension,m.configured));}});return null;}}
}

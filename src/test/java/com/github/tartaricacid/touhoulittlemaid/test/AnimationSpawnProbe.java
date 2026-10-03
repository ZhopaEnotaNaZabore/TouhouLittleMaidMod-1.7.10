package com.github.tartaricacid.touhoulittlemaid.test;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.animation.MaidActionState;
import net.minecraft.entity.Entity;
import com.github.tartaricacid.touhoulittlemaid.network.message.MessageMaidAction;
import io.netty.buffer.Unpooled;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
public final class AnimationSpawnProbe {
 public static void run() throws Exception {
  EntityMaid server=(EntityMaid)MultiblockRegression.unsafe().allocateInstance(EntityMaid.class);
  EntityMaid client=(EntityMaid)MultiblockRegression.unsafe().allocateInstance(EntityMaid.class);
  java.lang.reflect.Field uuid=Entity.class.getDeclaredField("entityUniqueID");uuid.setAccessible(true);
  uuid.set(server,UUID.fromString("00000000-0000-0000-0000-000000000001"));
  uuid.set(client,UUID.fromString("00000000-0000-0000-0000-000000000002"));
  java.lang.reflect.Field state=EntityMaid.class.getDeclaredField("actionState");state.setAccessible(true);
  state.set(server,new MaidActionState());state.set(client,new MaidActionState());
  server.setEntityId(71);client.setEntityId(71);server.dimension=client.dimension=0;
  server.getActionState().beginUse(new net.minecraft.item.ItemStack(net.minecraft.init.Items.apple),true,"eat",32,false,0);
  ByteBuf buffer=Unpooled.buffer();try {server.writeSpawnData(buffer);client.readSpawnData(buffer);}finally{buffer.release();}
  check(server.getUniqueID().equals(client.getUniqueID()),"spawn UUID synchronized");
  check(client.getActionState().using(0)&&client.getActionState().useLeft(),"spawn snapshot restored");
  server.getActionState().swing(true,6,0);
  MessageMaidAction fresh=copy(new MessageMaidAction(server));
  check(fresh.applyTo(client)&&client.getActionState().swingLeft(),"subsequent action accepted");
  client.dimension=1;check(!fresh.applyTo(client),"wrong dimension rejected");client.dimension=0;
  client.setEntityId(72);check(!fresh.applyTo(client),"wrong entity ID rejected");client.setEntityId(71);
  uuid.set(client,UUID.fromString("00000000-0000-0000-0000-000000000003"));
  check(!fresh.applyTo(client),"reused entity ID with different UUID rejected");uuid.set(client,server.getUniqueID());
  server.getActionState().stopUse();copy(new MessageMaidAction(server)).applyTo(client);
  fresh.applyTo(client);check(!client.getActionState().using(0),"older revision cannot resurrect use");
  System.out.println("Animation spawn/network regression: 7 checks PASS (real Forge NBT/Netty, no game loop)");
 }
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 static MessageMaidAction copy(MessageMaidAction message){ByteBuf b=Unpooled.buffer();try{message.toBytes(b);MessageMaidAction copy=new MessageMaidAction();copy.fromBytes(b);return copy;}finally{b.release();}}
}

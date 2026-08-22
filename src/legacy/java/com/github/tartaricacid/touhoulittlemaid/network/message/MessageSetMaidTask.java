package com.github.tartaricacid.touhoulittlemaid.network.message;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.touhoulittlemaid.network.ServerThreadDispatcher;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;

import java.nio.charset.Charset;

/** Direct task selection used by the source-style task panel. */
public final class MessageSetMaidTask implements IMessage {
    private int entityId;
    private String taskId = TaskManager.IDLE_ID;

    public MessageSetMaidTask() { }
    public MessageSetMaidTask(int entityId, String taskId) { this.entityId = entityId; this.taskId = taskId; }

    @Override public void fromBytes(ByteBuf buffer) {
        entityId = buffer.readInt();
        int length = buffer.readUnsignedByte();
        if (length > 96 || length > buffer.readableBytes()) throw new IllegalArgumentException("Invalid task id");
        byte[] bytes = new byte[length]; buffer.readBytes(bytes);
        taskId = new String(bytes, Charset.forName("UTF-8"));
    }

    @Override public void toBytes(ByteBuf buffer) {
        byte[] bytes = taskId.getBytes(Charset.forName("UTF-8"));
        if (bytes.length > 96) throw new IllegalArgumentException("Task id is too long");
        buffer.writeInt(entityId); buffer.writeByte(bytes.length); buffer.writeBytes(bytes);
    }

    public static final class Handler implements IMessageHandler<MessageSetMaidTask, IMessage> {
        @Override public IMessage onMessage(final MessageSetMaidTask message, MessageContext context) {
            final EntityPlayerMP player = context.getServerHandler().playerEntity;
            ServerThreadDispatcher.enqueue(new Runnable() { @Override public void run() {
                Entity entity = player.worldObj.getEntityByID(message.entityId);
                if (entity instanceof EntityMaid && ((EntityMaid) entity).getOwner() == player
                        && entity.getDistanceSqToEntity(player) < 64.0D
                        && TaskManager.getTasks().containsKey(message.taskId))
                    ((EntityMaid) entity).setTaskId(message.taskId);
            }});
            return null;
        }
    }
}

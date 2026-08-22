package com.github.tartaricacid.touhoulittlemaid.network.message;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import com.github.tartaricacid.touhoulittlemaid.network.ServerThreadDispatcher;

public final class MessageCycleMaidTask implements IMessage {
    private int entityId;
    private int direction;

    public MessageCycleMaidTask() {
    }

    public MessageCycleMaidTask(int entityId, int direction) {
        this.entityId = entityId;
        this.direction = direction < 0 ? -1 : 1;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        entityId = buffer.readInt();
        direction = buffer.readByte() < 0 ? -1 : 1;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(entityId);
        buffer.writeByte(direction);
    }

    public static final class Handler implements IMessageHandler<MessageCycleMaidTask, IMessage> {
        @Override
        public IMessage onMessage(final MessageCycleMaidTask message, MessageContext context) {
            final EntityPlayerMP player=context.getServerHandler().playerEntity;
            ServerThreadDispatcher.enqueue(new Runnable(){public void run(){Entity entity = player.worldObj.getEntityByID(message.entityId);
            if (entity instanceof EntityMaid) {
                EntityMaid maid = (EntityMaid) entity;
                if (maid.getOwner() == player && maid.getDistanceSqToEntity(player) < 64.0D) {
                    int index = TaskManager.indexOf(maid.getTaskId());
                    maid.setTaskId(TaskManager.getByIndex(index + message.direction).getId());
                }
            }
            }});
            return null;
        }
    }
}

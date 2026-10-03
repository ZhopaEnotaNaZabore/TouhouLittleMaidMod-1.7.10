package com.github.tartaricacid.touhoulittlemaid.network.message;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import java.util.UUID;

/** Server -> client only. Dimension and UUID prevent stale entity-ID reuse. */
public final class MessageMaidAction implements IMessage {
    private int id, dimension;
    private UUID uuid;
    private NBTTagCompound state;
    public MessageMaidAction() { }
    public MessageMaidAction(EntityMaid maid) {
        id = maid.getEntityId(); dimension = maid.dimension; uuid = maid.getUniqueID();
        state = maid.getActionState().snapshot(maid.ticksExisted);
    }
    public void toBytes(ByteBuf b) {
        b.writeInt(id); b.writeInt(dimension); b.writeLong(uuid.getMostSignificantBits()); b.writeLong(uuid.getLeastSignificantBits());
        ByteBufUtils.writeTag(b, state);
    }
    public void fromBytes(ByteBuf b) {
        id = b.readInt(); dimension = b.readInt(); uuid = new UUID(b.readLong(), b.readLong());
        state = ByteBufUtils.readTag(b);
    }
    /** Called on the client thread only, after resolving the current world entity. */
    public boolean applyTo(EntityMaid maid) {
        if (maid == null || maid.getEntityId() != id || maid.dimension != dimension
                || !maid.getUniqueID().equals(uuid)) return false;
        maid.getActionState().accept(state, maid.ticksExisted);
        return true;
    }
    public static final class Handler implements IMessageHandler<MessageMaidAction, IMessage> {
        public IMessage onMessage(final MessageMaidAction message, MessageContext context) {
            Minecraft.getMinecraft().func_152344_a(new Runnable() { public void run() {
                Minecraft mc = Minecraft.getMinecraft();
                if (mc.theWorld == null || mc.theWorld.provider.dimensionId != message.dimension) return;
                Entity entity = mc.theWorld.getEntityByID(message.id);
                if (entity instanceof EntityMaid) message.applyTo((EntityMaid) entity);
            } });
            return null;
        }
    }
}

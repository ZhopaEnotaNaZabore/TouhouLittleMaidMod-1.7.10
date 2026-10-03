package com.github.tartaricacid.touhoulittlemaid.network.message;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.MaidSchedule;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import com.github.tartaricacid.touhoulittlemaid.network.ServerThreadDispatcher;

public final class MessageMaidConfig implements IMessage {
    public static final int CYCLE_SCHEDULE = 0;
    public static final int TOGGLE_HOME = 1;
    public static final int TOGGLE_PICKUP = 2;
    public static final int TOGGLE_SITTING = 3;
    public static final int OPEN_BACKPACK = 4;

    private int entityId;
    private int action;

    public MessageMaidConfig() {
    }

    public MessageMaidConfig(int entityId, int action) {
        this.entityId = entityId;
        this.action = action;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        entityId = buffer.readInt();
        action = buffer.readUnsignedByte();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(entityId);
        buffer.writeByte(action);
    }

    public static final class Handler implements IMessageHandler<MessageMaidConfig, IMessage> {
        @Override
        public IMessage onMessage(final MessageMaidConfig message, MessageContext context) {
            final EntityPlayerMP player=context.getServerHandler().playerEntity;
            ServerThreadDispatcher.enqueue(new Runnable(){public void run(){Entity entity = player.worldObj.getEntityByID(message.entityId);
            if (!(entity instanceof EntityMaid)) return;
            EntityMaid maid = (EntityMaid) entity;
            if (!maid.isEntityAlive() || maid.getOwner() != player || maid.getDistanceSqToEntity(player) >= 64.0D) return;

            switch (message.action) {
                case CYCLE_SCHEDULE:
                    maid.setSchedule(MaidSchedule.byOrdinal(maid.getSchedule().ordinal() + 1));
                    break;
                case TOGGLE_HOME:
                    boolean enableHome = !maid.isHomeMode();
                    maid.setHomeMode(enableHome);
                    com.github.tartaricacid.touhoulittlemaid.entity.passive.SchedulePos.Point home =
                            maid.getSchedulePos().getForActivity(maid.getCurrentActivity());
                    player.addChatMessage(new net.minecraft.util.ChatComponentTranslation(
                            enableHome ? "message.touhou_little_maid.home.enabled"
                                    : "message.touhou_little_maid.home.disabled",
                            home.x, home.y, home.z));
                    break;
                case TOGGLE_PICKUP:
                    maid.setPickupEnabled(!maid.isPickupEnabled());
                    break;
                case TOGGLE_SITTING:
                    maid.setMaidSitting(!maid.isSitting());
                    break;
                case OPEN_BACKPACK:
                    String type=maid.getBackpackType();
                    if("crafting_table_backpack".equals(type))player.openGui(com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid.instance, com.github.tartaricacid.touhoulittlemaid.proxy.CommonProxy.MAID_CRAFTING_GUI_ID, maid.worldObj, maid.getEntityId(), 0, 0);
                    else if("ender_chest_backpack".equals(type))player.displayGUIChest(player.getInventoryEnderChest());
                    else if("tank_backpack".equals(type))player.openGui(com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid.instance,com.github.tartaricacid.touhoulittlemaid.proxy.CommonProxy.MAID_TANK_GUI_ID,maid.worldObj,maid.getEntityId(),0,0);
                    else if("furnace_backpack".equals(type))player.openGui(com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid.instance,com.github.tartaricacid.touhoulittlemaid.proxy.CommonProxy.MAID_FURNACE_GUI_ID,maid.worldObj,maid.getEntityId(),0,0);
                    else if (!"empty".equals(type) && (!(player.openContainer instanceof com.github.tartaricacid.touhoulittlemaid.inventory.container.ContainerMaid)
                            || ((com.github.tartaricacid.touhoulittlemaid.inventory.container.ContainerMaid)player.openContainer).getMaid()!=maid))
                        player.openGui(com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid.instance,com.github.tartaricacid.touhoulittlemaid.proxy.CommonProxy.MAID_GUI_ID,maid.worldObj,maid.getEntityId(),0,0);
                    break;
                default:
                    break;
            }
            }});
            return null;
        }
    }
}

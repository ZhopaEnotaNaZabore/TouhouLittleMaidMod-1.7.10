package com.github.tartaricacid.touhoulittlemaid.network;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import com.github.tartaricacid.touhoulittlemaid.network.message.MessageCycleMaidTask;
import com.github.tartaricacid.touhoulittlemaid.network.message.MessageMaidConfig;
import com.github.tartaricacid.touhoulittlemaid.network.message.MessageOpenMaidGui;
import com.github.tartaricacid.touhoulittlemaid.network.message.MessageMaidChat;
import com.github.tartaricacid.touhoulittlemaid.network.message.MessageMaidTts;
import com.github.tartaricacid.touhoulittlemaid.network.message.MessageRequestMaidHome;
import com.github.tartaricacid.touhoulittlemaid.network.message.MessageMaidHome;
import com.github.tartaricacid.touhoulittlemaid.network.message.MessageSetMaidTask;
import com.github.tartaricacid.touhoulittlemaid.network.message.MessageModelSwitcherEdit;
import com.github.tartaricacid.touhoulittlemaid.network.message.MessageMaidBeaconAction;
import com.github.tartaricacid.touhoulittlemaid.network.message.MessagePlayerPower;

public final class NetworkHandler {
    public static SimpleNetworkWrapper channel;

    private NetworkHandler() {
    }

    public static void init() {
        channel = NetworkRegistry.INSTANCE.newSimpleChannel(TouhouLittleMaid.MOD_ID);
        channel.registerMessage(MessageCycleMaidTask.Handler.class, MessageCycleMaidTask.class, 0, Side.SERVER);
        channel.registerMessage(MessageMaidConfig.Handler.class, MessageMaidConfig.class, 1, Side.SERVER);
        channel.registerMessage(MessageOpenMaidGui.Handler.class, MessageOpenMaidGui.class, 2, Side.SERVER);
        channel.registerMessage(MessageMaidChat.Handler.class, MessageMaidChat.class, 3, Side.CLIENT);
        channel.registerMessage(MessageMaidTts.Handler.class, MessageMaidTts.class, 4, Side.CLIENT);
        channel.registerMessage(MessageRequestMaidHome.Handler.class, MessageRequestMaidHome.class, 5, Side.SERVER);
        channel.registerMessage(MessageMaidHome.Handler.class, MessageMaidHome.class, 6, Side.CLIENT);
        channel.registerMessage(MessageSetMaidTask.Handler.class, MessageSetMaidTask.class, 7, Side.SERVER);
        channel.registerMessage(MessageModelSwitcherEdit.Handler.class, MessageModelSwitcherEdit.class, 8, Side.SERVER);
        channel.registerMessage(MessageMaidBeaconAction.Handler.class, MessageMaidBeaconAction.class, 9, Side.SERVER);
        channel.registerMessage(MessagePlayerPower.Handler.class, MessagePlayerPower.class, 10, Side.CLIENT);
    }
}

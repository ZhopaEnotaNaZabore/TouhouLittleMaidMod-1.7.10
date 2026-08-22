package com.github.tartaricacid.touhoulittlemaid.network;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/** 1.7.10 has no server addScheduledTask API, so network/worker callbacks rendezvous here. */
public final class ServerThreadDispatcher {
    public static final ServerThreadDispatcher INSTANCE = new ServerThreadDispatcher();
    private final Queue<Runnable> pending = new ConcurrentLinkedQueue<Runnable>();

    private ServerThreadDispatcher() {
    }

    public static void enqueue(Runnable action) {
        if (action != null) INSTANCE.pending.add(action);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Runnable action;
        int budget = 1024;
        while (budget-- > 0 && (action = pending.poll()) != null) {
            try {
                action.run();
            } catch (Throwable error) {
                TouhouLittleMaid.LOGGER.error("Scheduled server action failed", error);
            }
        }
    }
}

package com.github.tartaricacid.touhoulittlemaid.capability;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

public final class LegacyPlayerPowerEvents {
    public static final LegacyPlayerPowerEvents INSTANCE=new LegacyPlayerPowerEvents();
    private LegacyPlayerPowerEvents(){}
    @SubscribeEvent public void construct(EntityEvent.EntityConstructing event){if(event.entity instanceof EntityPlayer)LegacyPlayerPower.register((EntityPlayer)event.entity);}
    @SubscribeEvent public void clone(PlayerEvent.Clone event){LegacyPlayerPower.get(event.entityPlayer).set(LegacyPlayerPower.get(event.original).get());}
}

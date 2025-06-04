package org.mcteampotato.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.mcteampotato.SOLValpotato;
import org.mcteampotato.network.SyncFoodDataPacket;

@EventBusSubscriber(modid = SOLValpotato.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEvent {
    @SubscribeEvent
    public static void registerNetWork(RegisterPayloadHandlersEvent event){
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(SyncFoodDataPacket.TYPE, SyncFoodDataPacket.STREAM_CODEC, SyncFoodDataPacket::handle);
    }
}

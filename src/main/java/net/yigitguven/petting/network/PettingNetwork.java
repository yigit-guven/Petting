package net.yigitguven.petting.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.yigitguven.petting.Petting;

public class PettingNetwork {

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                OpenPetInventoryPayload.TYPE,
                OpenPetInventoryPayload.STREAM_CODEC,
                OpenPetInventoryPayload::handle
        );
    }
}

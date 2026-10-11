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
        registrar.playToServer(
                UpdateCollarSettingsPayload.TYPE,
                UpdateCollarSettingsPayload.STREAM_CODEC,
                UpdateCollarSettingsPayload::handle
        );
        registrar.playToServer(
                TogglePetSitPayload.TYPE,
                TogglePetSitPayload.STREAM_CODEC,
                TogglePetSitPayload::handle
        );
        registrar.playToServer(
                MountPetPayload.TYPE,
                MountPetPayload.STREAM_CODEC,
                MountPetPayload::handle
        );
        registrar.playToServer(
                UpdateRidingSettingsPayload.TYPE,
                UpdateRidingSettingsPayload.STREAM_CODEC,
                UpdateRidingSettingsPayload::handle
        );
        registrar.playToServer(
                RiddenPetAttackPayload.TYPE,
                RiddenPetAttackPayload.STREAM_CODEC,
                RiddenPetAttackPayload::handle
        );
    }
}

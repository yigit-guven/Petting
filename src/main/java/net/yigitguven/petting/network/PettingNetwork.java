package net.yigitguven.petting.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class PettingNetwork {

    public static void registerPayloads() {
        // C2S Payload Types
        PayloadTypeRegistry.serverboundPlay().register(OpenPetInventoryPayload.TYPE, OpenPetInventoryPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(UpdateCollarSettingsPayload.TYPE, UpdateCollarSettingsPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(TogglePetSitPayload.TYPE, TogglePetSitPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(MountPetPayload.TYPE, MountPetPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(UpdateRidingSettingsPayload.TYPE, UpdateRidingSettingsPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(RiddenPetAttackPayload.TYPE, RiddenPetAttackPayload.STREAM_CODEC);

        // S2C Payload Types
        PayloadTypeRegistry.clientboundPlay().register(DismountPetPayload.TYPE, DismountPetPayload.STREAM_CODEC);

        // Server Receivers
        ServerPlayNetworking.registerGlobalReceiver(OpenPetInventoryPayload.TYPE, OpenPetInventoryPayload::handle);
        ServerPlayNetworking.registerGlobalReceiver(UpdateCollarSettingsPayload.TYPE, UpdateCollarSettingsPayload::handle);
        ServerPlayNetworking.registerGlobalReceiver(TogglePetSitPayload.TYPE, TogglePetSitPayload::handle);
        ServerPlayNetworking.registerGlobalReceiver(MountPetPayload.TYPE, MountPetPayload::handle);
        ServerPlayNetworking.registerGlobalReceiver(UpdateRidingSettingsPayload.TYPE, UpdateRidingSettingsPayload::handle);
        ServerPlayNetworking.registerGlobalReceiver(RiddenPetAttackPayload.TYPE, RiddenPetAttackPayload::handle);
    }
}

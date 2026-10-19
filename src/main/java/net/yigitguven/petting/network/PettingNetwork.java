package net.yigitguven.petting.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.yigitguven.petting.Petting;

public class PettingNetwork {

    public static final Channel<CustomPacketPayload> CHANNEL = ChannelBuilder.named(Identifier.fromNamespaceAndPath(Petting.MODID, "network"))
            .networkProtocolVersion(1)
            .payloadChannel()
            .play()
                .serverbound()
                    .add(OpenPetInventoryPayload.TYPE, OpenPetInventoryPayload.STREAM_CODEC.cast(), OpenPetInventoryPayload::handle)
                    .add(UpdateCollarSettingsPayload.TYPE, UpdateCollarSettingsPayload.STREAM_CODEC, UpdateCollarSettingsPayload::handle)
                    .add(TogglePetSitPayload.TYPE, TogglePetSitPayload.STREAM_CODEC.cast(), TogglePetSitPayload::handle)
                    .add(MountPetPayload.TYPE, MountPetPayload.STREAM_CODEC.cast(), MountPetPayload::handle)
                    .add(UpdateRidingSettingsPayload.TYPE, UpdateRidingSettingsPayload.STREAM_CODEC.cast(), UpdateRidingSettingsPayload::handle)
                    .add(RiddenPetAttackPayload.TYPE, RiddenPetAttackPayload.STREAM_CODEC.cast(), RiddenPetAttackPayload::handle)
                .clientbound()
                    .add(DismountPetPayload.TYPE, DismountPetPayload.STREAM_CODEC.cast(), DismountPetPayload::handle)
                    .add(SyncPetDataPayload.TYPE, SyncPetDataPayload.STREAM_CODEC, SyncPetDataPayload::handle)
            .build();

    public static void init() {
        // Triggers class loading and channel registration
    }

    public static void sendToServer(CustomPacketPayload payload) {
        CHANNEL.send(payload, PacketDistributor.SERVER.noArg());
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        CHANNEL.send(payload, PacketDistributor.PLAYER.with(player));
    }

    public static void sendToTrackingAndSelf(Entity entity, CustomPacketPayload payload) {
        CHANNEL.send(payload, PacketDistributor.TRACKING_ENTITY_AND_SELF.with(entity));
    }
}

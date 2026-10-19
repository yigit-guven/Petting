package net.yigitguven.petting.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.yigitguven.petting.Petting;

public class PettingNetwork {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Petting.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int nextId() {
        return packetId++;
    }

    public static void init() {
        CHANNEL.messageBuilder(OpenPetInventoryPayload.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(OpenPetInventoryPayload::new)
                .encoder(OpenPetInventoryPayload::encode)
                .consumerMainThread(OpenPetInventoryPayload::handle)
                .add();

        CHANNEL.messageBuilder(UpdateCollarSettingsPayload.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(UpdateCollarSettingsPayload::new)
                .encoder(UpdateCollarSettingsPayload::encode)
                .consumerMainThread(UpdateCollarSettingsPayload::handle)
                .add();

        CHANNEL.messageBuilder(TogglePetSitPayload.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(TogglePetSitPayload::new)
                .encoder(TogglePetSitPayload::encode)
                .consumerMainThread(TogglePetSitPayload::handle)
                .add();

        CHANNEL.messageBuilder(MountPetPayload.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(MountPetPayload::new)
                .encoder(MountPetPayload::encode)
                .consumerMainThread(MountPetPayload::handle)
                .add();

        CHANNEL.messageBuilder(UpdateRidingSettingsPayload.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(UpdateRidingSettingsPayload::new)
                .encoder(UpdateRidingSettingsPayload::encode)
                .consumerMainThread(UpdateRidingSettingsPayload::handle)
                .add();

        CHANNEL.messageBuilder(RiddenPetAttackPayload.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(RiddenPetAttackPayload::new)
                .encoder(RiddenPetAttackPayload::encode)
                .consumerMainThread(RiddenPetAttackPayload::handle)
                .add();

        CHANNEL.messageBuilder(DismountPetPayload.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(DismountPetPayload::new)
                .encoder(DismountPetPayload::encode)
                .consumerMainThread(DismountPetPayload::handle)
                .add();

        CHANNEL.messageBuilder(SyncPetDataPayload.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncPetDataPayload::new)
                .encoder(SyncPetDataPayload::encode)
                .consumerMainThread(SyncPetDataPayload::handle)
                .add();
    }

    public static void sendToServer(Object msg) {
        CHANNEL.sendToServer(msg);
    }

    public static void sendToPlayer(ServerPlayer player, Object msg) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public static void sendToTrackingAndSelf(Entity entity, Object msg) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), msg);
    }
}

package net.yigitguven.petting.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.data.PetData;

public record SyncPetDataPayload(int entityId, PetData petData) implements CustomPacketPayload {
    public static final Type<SyncPetDataPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Petting.MODID, "sync_pet_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPetDataPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    SyncPetDataPayload::entityId,
                    PetData.STREAM_CODEC,
                    SyncPetDataPayload::petData,
                    SyncPetDataPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncPetDataPayload payload, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            if (context.isClientSide()) {
                net.yigitguven.petting.client.PettingClientPacketHandler.handleSyncData(payload);
            }
        });
        context.setPacketHandled(true);
    }
}

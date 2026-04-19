package net.yigitguven.petting.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.yigitguven.petting.PettingMod;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncPetStatusPayload(int entityId, boolean isTamed) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncPetStatusPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PettingMod.MODID, "sync_pet_status"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPetStatusPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.entityId());
                buf.writeBoolean(payload.isTamed());
            },
            buf -> new SyncPetStatusPayload(buf.readInt(), buf.readBoolean())
    );

    @Override
    public CustomPacketPayload.Type<SyncPetStatusPayload> type() {
        return TYPE;
    }

    public static void handle(final SyncPetStatusPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            net.yigitguven.petting.client.PettingClientPayloadHandler.handleSyncPetStatus(payload, context);
        });
    }
}

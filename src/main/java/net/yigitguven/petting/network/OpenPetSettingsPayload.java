package net.yigitguven.petting.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.yigitguven.petting.PettingMod;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenPetSettingsPayload(int entityId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenPetSettingsPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PettingMod.MODID, "open_pet_settings"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenPetSettingsPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeInt(payload.entityId()),
            buf -> new OpenPetSettingsPayload(buf.readInt())
    );

    @Override
    public CustomPacketPayload.Type<OpenPetSettingsPayload> type() {
        return TYPE;
    }

    public static void handle(final OpenPetSettingsPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                EntitySettingsServer.handleOpenRequest(player, payload.entityId());
            }
        });
    }
}

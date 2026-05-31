package net.yigitguven.petting.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.yigitguven.petting.PettingMod;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdatePetSettingPayload(int entityId, String key, String value) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdatePetSettingPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PettingMod.MODID, "update_pet_setting"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdatePetSettingPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.entityId());
                buf.writeUtf(payload.key());
                buf.writeUtf(payload.value());
            },
            buf -> new UpdatePetSettingPayload(buf.readInt(), buf.readUtf(32767), buf.readUtf(32767))
    );

    @Override
    public CustomPacketPayload.Type<UpdatePetSettingPayload> type() {
        return TYPE;
    }

    public static void handle(final UpdatePetSettingPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                EntitySettingsServer.applyUpdate(player, payload.entityId(), payload.key(), payload.value());
            }
        });
    }
}

package net.yigitguven.petting.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.yigitguven.petting.PettingMod;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SendPetSettingsPayload(int entityId,
                                      boolean sitStill,
                                      boolean waiting,
                                      boolean isTamed,
                                      boolean attackIfOwnerAttacks,
                                      boolean attackIfOwnerAttacked,
                                      boolean attackIfSelfAttacked,
                                      boolean damageOwner,
                                      boolean ignoreWhistle,
                                      int followDistance,
                                      int teleportDistance,
                                      boolean openScreen,
                                      String controlRightClick,
                                      String controlShiftRightClick) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SendPetSettingsPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PettingMod.MODID, "send_pet_settings"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SendPetSettingsPayload> STREAM_CODEC = StreamCodec.of(
                (buf, payload) -> {
                buf.writeInt(payload.entityId());
                buf.writeBoolean(payload.sitStill());
                buf.writeBoolean(payload.waiting());
                buf.writeBoolean(payload.isTamed());
                buf.writeBoolean(payload.attackIfOwnerAttacks());
                buf.writeBoolean(payload.attackIfOwnerAttacked());
                buf.writeBoolean(payload.attackIfSelfAttacked());
                buf.writeBoolean(payload.damageOwner());
                buf.writeBoolean(payload.ignoreWhistle());
                buf.writeInt(payload.followDistance());
                buf.writeInt(payload.teleportDistance());
                buf.writeBoolean(payload.openScreen());
                buf.writeUtf(payload.controlRightClick());
                buf.writeUtf(payload.controlShiftRightClick());
                },
                buf -> new SendPetSettingsPayload(
                    buf.readInt(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readBoolean(),
                    buf.readUtf(32767),
                    buf.readUtf(32767)
                )
    );

    @Override
    public CustomPacketPayload.Type<SendPetSettingsPayload> type() {
        return TYPE;
    }

    public static void handle(final SendPetSettingsPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> net.yigitguven.petting.client.PettingClientPayloadHandler.handleSendPetSettings(payload, context));
    }
}

package net.yigitguven.petting.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.yigitguven.petting.PettingMod;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SavePetControlDefaultsPayload(String rightClick, String shiftRightClick) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SavePetControlDefaultsPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PettingMod.MODID, "save_pet_control_defaults"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SavePetControlDefaultsPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeUtf(payload.rightClick());
                buf.writeUtf(payload.shiftRightClick());
            },
            buf -> new SavePetControlDefaultsPayload(buf.readUtf(32767), buf.readUtf(32767))
    );

    @Override
    public CustomPacketPayload.Type<SavePetControlDefaultsPayload> type() {
        return TYPE;
    }

    public static void handle(final SavePetControlDefaultsPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                net.yigitguven.petting.network.EntitySettingsServer.saveControlDefaults(player, payload.rightClick(), payload.shiftRightClick());
            }
        });
    }
}
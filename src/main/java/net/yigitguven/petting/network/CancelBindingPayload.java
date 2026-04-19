package net.yigitguven.petting.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.yigitguven.petting.PettingMod;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yigitguven.petting.procedures.PetBedBindingHandler;

public record CancelBindingPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CancelBindingPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PettingMod.MODID, "cancel_binding"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CancelBindingPayload> STREAM_CODEC = StreamCodec.unit(new CancelBindingPayload());

    @Override
    public CustomPacketPayload.Type<CancelBindingPayload> type() {
        return TYPE;
    }

    public static void handle(final CancelBindingPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            PetBedBindingHandler.handleMenuCancellation(context.player());
        });
    }
}

package net.yigitguven.petting.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.yigitguven.petting.Petting;

public record DismountPetPayload(int entityId) implements CustomPacketPayload {
    public static final Type<DismountPetPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Petting.MODID, "dismount_pet"));

    public static final StreamCodec<ByteBuf, DismountPetPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    DismountPetPayload::entityId,
                    DismountPetPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DismountPetPayload payload, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            if (context.isClientSide()) {
                net.yigitguven.petting.client.PettingClientPacketHandler.handleDismount();
            }
        });
        context.setPacketHandled(true);
    }
}

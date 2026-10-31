package net.yigitguven.petting.network;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.entity.PetRidingHandler;

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

    public static void handle(DismountPetPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            Player player = context.player();
            if (player != null) {
                PetRidingHandler.allowForcedDismount(player);
                player.stopRiding();
                player.setPose(Pose.STANDING);
                player.resetFallDistance();
            }
        });
    }
}

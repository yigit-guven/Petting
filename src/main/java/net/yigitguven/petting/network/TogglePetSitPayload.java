package net.yigitguven.petting.network;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.util.PetHelper;

public record TogglePetSitPayload(int entityId) implements CustomPacketPayload {
    public static final Type<TogglePetSitPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Petting.MODID, "toggle_pet_sit"));

    public static final StreamCodec<ByteBuf, TogglePetSitPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    TogglePetSitPayload::entityId,
                    TogglePetSitPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TogglePetSitPayload payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            ServerPlayer player = context.player();
            Entity entity = player.level().getEntity(payload.entityId());
            if (entity instanceof net.minecraft.world.entity.LivingEntity mob && mob.isAlive()) {
                if (player.getVehicle() == mob || mob.hasPassenger(player) || mob.isVehicle() || mob.isPassenger()) {
                    return;
                }
                if (PetHelper.isTamed(mob) && PetHelper.isOwner(mob, player) && (player.isWithinEntityInteractionRange(mob, 3.0D) || player.distanceTo(mob) <= 16.0D)) {
                    PetHelper.toggleSit(mob, player);
                }
            }
        });
    }
}

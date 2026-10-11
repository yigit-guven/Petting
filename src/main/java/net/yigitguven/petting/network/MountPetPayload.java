package net.yigitguven.petting.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.entity.PetRidingHandler;
import net.yigitguven.petting.util.PetHelper;

public record MountPetPayload(int entityId) implements CustomPacketPayload {
    public static final Type<MountPetPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Petting.MODID, "mount_pet"));

    public static final StreamCodec<ByteBuf, MountPetPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    MountPetPayload::entityId,
                    MountPetPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MountPetPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                // If player is already riding a pet, pressing ride triggers dismount
                if (player.getVehicle() instanceof Mob riddenMob && PetHelper.isTamed(riddenMob) && PetHelper.isOwner(riddenMob, player)) {
                    PetRidingHandler.allowForcedDismount(player);
                    player.stopRiding();
                    return;
                }

                Entity entity = player.level().getEntity(payload.entityId());
                if (entity instanceof Mob mob && mob.isAlive()) {
                    if (PetHelper.isTamed(mob) && PetHelper.isOwner(mob, player) && player.distanceTo(mob) <= 6.0D) {
                        if (!PetHelper.isPetRidingEnabledFor(mob)) {
                            return;
                        }

                        boolean hasSaddle = PetHelper.hasSaddle(mob);
                        if (!hasSaddle && !PettingServerConfig.isAllowRidingWithoutSaddle()) {
                            player.sendSystemMessage(Component.translatable("petting.action.saddle_required"), true);
                            return;
                        }

                        if (PetHelper.isSitting(mob)) {
                            PetHelper.setOrder(mob, net.yigitguven.petting.data.PetOrder.FOLLOW);
                        }

                        player.startRiding(mob, true, false);

                        if (hasSaddle) {
                            player.sendSystemMessage(Component.translatable("petting.action.mount_controlled", mob.getDisplayName()), true);
                        } else {
                            player.sendSystemMessage(Component.translatable("petting.action.mount_uncontrolled", mob.getDisplayName()), true);
                        }
                    }
                }
            }
        });
    }
}

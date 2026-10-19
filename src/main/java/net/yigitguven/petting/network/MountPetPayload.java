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
import net.minecraftforge.event.network.CustomPayloadEvent;
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

    public static void handle(MountPetPayload payload, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            if (!PettingServerConfig.isPetInventoryEnabled()) {
                return;
            }
            ServerPlayer player = context.getSender();
            if (player != null) {
                // If player is already riding a pet, pressing ride triggers dismount
                if (player.getVehicle() instanceof net.minecraft.world.entity.LivingEntity riddenMob && PetHelper.isTamed(riddenMob) && PetHelper.isOwner(riddenMob, player)) {
                    PetRidingHandler.dismountPlayer(player, riddenMob);
                    return;
                }

                Entity entity = player.level().getEntity(payload.entityId());
                if (entity instanceof net.minecraft.world.entity.LivingEntity mob && mob.isAlive()) {
                    double maxDist = (mob instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon) ? 18.0D : 6.0D;
                    if (PetHelper.isTamed(mob) && PetHelper.isOwner(mob, player) && (player.isWithinEntityInteractionRange(mob, 3.0D) || player.distanceTo(mob) <= maxDist)) {
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
                        if (mob instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon) {
                            player.setYRot(net.minecraft.util.Mth.wrapDegrees(mob.getYRot() + 180.0F));
                        }

                        if (hasSaddle) {
                            player.sendSystemMessage(Component.translatable("petting.action.mount_controlled", mob.getDisplayName()), true);
                        } else {
                            player.sendSystemMessage(Component.translatable("petting.action.mount_uncontrolled", mob.getDisplayName()), true);
                        }
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}

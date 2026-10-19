package net.yigitguven.petting.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.entity.PetRidingHandler;
import net.yigitguven.petting.util.PetHelper;

import java.util.function.Supplier;

public record MountPetPayload(int entityId) {

    public MountPetPayload(FriendlyByteBuf buf) {
        this(buf.readVarInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entityId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context context = ctxSupplier.get();
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

                Entity entity = player.level().getEntity(this.entityId);
                if (entity instanceof net.minecraft.world.entity.LivingEntity mob && mob.isAlive()) {
                    double maxDist = (mob instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon) ? 18.0D : 6.0D;
                    if (PetHelper.isTamed(mob) && PetHelper.isOwner(mob, player) && player.distanceTo(mob) <= maxDist) {
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

                        player.startRiding(mob, true);
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

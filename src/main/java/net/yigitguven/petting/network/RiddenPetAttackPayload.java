package net.yigitguven.petting.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.entity.PetRidingHandler;
import net.yigitguven.petting.util.PetHelper;

public record RiddenPetAttackPayload(int entityId) implements CustomPacketPayload {
    public static final Type<RiddenPetAttackPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Petting.MODID, "ridden_pet_attack"));

    public static final StreamCodec<ByteBuf, RiddenPetAttackPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    RiddenPetAttackPayload::entityId,
                    RiddenPetAttackPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RiddenPetAttackPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!net.yigitguven.petting.config.PettingServerConfig.isPetInventoryEnabled()
                    || !net.yigitguven.petting.config.PettingServerConfig.isRiddenPetsAttackOnClick()) {
                return;
            }
            if (context.player() instanceof ServerPlayer player) {
                Entity entity = player.level().getEntity(payload.entityId());
                if (entity instanceof Mob mob && mob.isAlive()) {
                    if (player.getVehicle() == mob && PetHelper.isTamed(mob) && PetHelper.isOwner(mob, player)) {
                        PetRidingHandler.performRiddenAttack(player, mob);
                    }
                }
            }
        });
    }
}

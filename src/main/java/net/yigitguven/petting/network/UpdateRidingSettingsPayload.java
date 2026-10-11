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
import net.yigitguven.petting.util.PetHelper;

public record UpdateRidingSettingsPayload(int entityId, boolean attackOnClick, boolean autoAttackHostiles) implements CustomPacketPayload {
    public static final Type<UpdateRidingSettingsPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Petting.MODID, "update_riding_settings"));

    public static final StreamCodec<ByteBuf, UpdateRidingSettingsPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    UpdateRidingSettingsPayload::entityId,
                    ByteBufCodecs.BOOL,
                    UpdateRidingSettingsPayload::attackOnClick,
                    ByteBufCodecs.BOOL,
                    UpdateRidingSettingsPayload::autoAttackHostiles,
                    UpdateRidingSettingsPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateRidingSettingsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Entity entity = player.level().getEntity(payload.entityId());
                if (entity instanceof Mob mob && mob.isAlive()) {
                    if (PetHelper.isTamed(mob) && PetHelper.isOwner(mob, player)) {
                        PetHelper.setRideAttackOnClick(mob, payload.attackOnClick());
                        PetHelper.setRideAutoAttackHostiles(mob, payload.autoAttackHostiles());
                    }
                }
            }
        });
    }
}

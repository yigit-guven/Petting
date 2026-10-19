package net.yigitguven.petting.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.data.CollarSettings;
import net.yigitguven.petting.util.PetHelper;

public record UpdateCollarSettingsPayload(int entityId, CollarSettings settings) implements CustomPacketPayload {
    public static final Type<UpdateCollarSettingsPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Petting.MODID, "update_collar_settings"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateCollarSettingsPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    UpdateCollarSettingsPayload::entityId,
                    CollarSettings.STREAM_CODEC,
                    UpdateCollarSettingsPayload::settings,
                    UpdateCollarSettingsPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateCollarSettingsPayload payload, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            if (!net.yigitguven.petting.config.PettingServerConfig.isPetInventoryEnabled()) {
                return;
            }
            ServerPlayer player = context.getSender();
            if (player != null) {
                Entity entity = player.level().getEntity(payload.entityId());
                if (entity instanceof net.minecraft.world.entity.LivingEntity mob && mob.isAlive()) {
                    if (PetHelper.isTamed(mob) && PetHelper.isOwner(mob, player) && player.distanceTo(mob) <= 12.0D) {
                        PetHelper.setCollarSettings(mob, payload.settings());
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}

package net.yigitguven.petting.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.yigitguven.petting.data.CollarSettings;
import net.yigitguven.petting.util.PetHelper;

import java.util.function.Supplier;

public record UpdateCollarSettingsPayload(int entityId, CollarSettings settings) {

    public UpdateCollarSettingsPayload(FriendlyByteBuf buf) {
        this(buf.readVarInt(), CollarSettings.fromBytes(buf));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entityId);
        this.settings.toBytes(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context context = ctxSupplier.get();
        context.enqueueWork(() -> {
            if (!net.yigitguven.petting.config.PettingServerConfig.isPetInventoryEnabled()) {
                return;
            }
            ServerPlayer player = context.getSender();
            if (player != null) {
                Entity entity = player.level().getEntity(this.entityId);
                if (entity instanceof net.minecraft.world.entity.LivingEntity mob && mob.isAlive()) {
                    if (PetHelper.isTamed(mob) && PetHelper.isOwner(mob, player) && player.distanceToSqr(mob) <= 144.0D) {
                        PetHelper.setCollarSettings(mob, this.settings);
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}

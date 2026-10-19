package net.yigitguven.petting.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.yigitguven.petting.util.PetHelper;

import java.util.function.Supplier;

public record UpdateRidingSettingsPayload(int entityId, boolean attackOnClick, boolean autoAttackHostiles) {

    public UpdateRidingSettingsPayload(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readBoolean(), buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entityId);
        buf.writeBoolean(this.attackOnClick);
        buf.writeBoolean(this.autoAttackHostiles);
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
                    if (PetHelper.isTamed(mob) && PetHelper.isOwner(mob, player)) {
                        PetHelper.setRideAttackOnClick(mob, this.attackOnClick);
                        PetHelper.setRideAutoAttackHostiles(mob, this.autoAttackHostiles);
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}

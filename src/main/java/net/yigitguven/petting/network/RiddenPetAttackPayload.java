package net.yigitguven.petting.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.yigitguven.petting.entity.PetRidingHandler;
import net.yigitguven.petting.util.PetHelper;

import java.util.function.Supplier;

public record RiddenPetAttackPayload(int entityId) {

    public RiddenPetAttackPayload(FriendlyByteBuf buf) {
        this(buf.readVarInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entityId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context context = ctxSupplier.get();
        context.enqueueWork(() -> {
            if (!net.yigitguven.petting.config.PettingServerConfig.isPetInventoryEnabled()
                    || !net.yigitguven.petting.config.PettingServerConfig.isRiddenPetsAttackOnClick()) {
                return;
            }
            ServerPlayer player = context.getSender();
            if (player != null) {
                Entity entity = player.level().getEntity(this.entityId);
                if (entity instanceof net.minecraft.world.entity.LivingEntity mob && mob.isAlive()) {
                    if (player.getVehicle() == mob && PetHelper.isTamed(mob) && PetHelper.isOwner(mob, player)) {
                        PetRidingHandler.performRiddenAttack(player, mob);
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}

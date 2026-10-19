package net.yigitguven.petting.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.yigitguven.petting.inventory.PetInventoryMenu;
import net.yigitguven.petting.util.PetHelper;

import java.util.function.Supplier;

public record OpenPetInventoryPayload(int entityId) {

    public OpenPetInventoryPayload(FriendlyByteBuf buf) {
        this(buf.readVarInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entityId);
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
                    if (PetHelper.isTamed(mob) && PetHelper.isOwner(mob, player) && player.distanceToSqr(mob) <= 256.0D) {
                        PetInventoryMenu.open(player, mob);
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}

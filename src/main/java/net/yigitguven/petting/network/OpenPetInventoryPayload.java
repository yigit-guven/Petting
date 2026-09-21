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
import net.yigitguven.petting.inventory.PetInventoryMenu;
import net.yigitguven.petting.util.PetHelper;

public record OpenPetInventoryPayload(int entityId) implements CustomPacketPayload {
    public static final Type<OpenPetInventoryPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Petting.MODID, "open_pet_inventory"));

    public static final StreamCodec<ByteBuf, OpenPetInventoryPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    OpenPetInventoryPayload::entityId,
                    OpenPetInventoryPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenPetInventoryPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Entity entity = player.level().getEntity(payload.entityId());
                if (entity instanceof Mob mob && mob.isAlive()) {
                    if (PetHelper.isTamed(mob) && PetHelper.isOwner(mob, player) && player.distanceTo(mob) <= 8.0D) {
                        PetInventoryMenu.open(player, mob);
                    }
                }
            }
        });
    }
}

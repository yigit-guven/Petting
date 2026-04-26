package net.yigitguven.petting.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.yigitguven.petting.PettingMod;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.yigitguven.petting.world.inventory.PetInventoryMenu;

public record OpenPetInventoryPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenPetInventoryPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PettingMod.MODID, "open_pet_inventory"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenPetInventoryPayload> STREAM_CODEC = StreamCodec.unit(new OpenPetInventoryPayload());

    @Override
    public CustomPacketPayload.Type<OpenPetInventoryPayload> type() {
        return TYPE;
    }

    public static void handle(final OpenPetInventoryPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.getVehicle() != null) {
                    Entity pet = player.getVehicle();
                    boolean isOwner = false;
                    if (pet.getPersistentData().contains("ownerUUID") && pet.getPersistentData().getString("ownerUUID").equals(player.getStringUUID())) {
                        isOwner = true;
                    } else if (pet instanceof net.minecraft.world.entity.TamableAnimal tamable) {
                        if (tamable.isOwnedBy(player)) isOwner = true;
                    }

                    if (isOwner || pet.getPersistentData().getBoolean("pettingtamed")) {
                        player.openMenu(new SimpleMenuProvider(
                            (id, inv, p) -> new PetInventoryMenu(id, inv, pet),
                            pet.getDisplayName()
                        ), buf -> buf.writeInt(pet.getId()));
                    }
                }
            }
        });
    }
}

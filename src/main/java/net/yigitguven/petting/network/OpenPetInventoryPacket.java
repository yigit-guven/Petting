package net.yigitguven.petting.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraftforge.network.NetworkEvent;
import net.yigitguven.petting.world.inventory.PetInventoryMenu;

import java.util.function.Supplier;

public class OpenPetInventoryPacket {
    public OpenPetInventoryPacket() {
    }

    public OpenPetInventoryPacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.getVehicle() != null) {
                Entity pet = player.getVehicle();
                boolean isOwner = false;
                if (pet.getPersistentData().contains("ownerUUID") && pet.getPersistentData().getString("ownerUUID").equals(player.getStringUUID())) {
                    isOwner = true;
                } else if (pet instanceof net.minecraft.world.entity.TamableAnimal tamable) {
                    if (tamable.isOwnedBy(player)) isOwner = true;
                }

                if (isOwner || pet.getPersistentData().contains("pettingtamed")) {
                    net.minecraftforge.network.NetworkHooks.openScreen(player, new SimpleMenuProvider(
                        (id, inv, p) -> new PetInventoryMenu(id, inv, pet),
                        pet.getDisplayName()
                    ), buf -> buf.writeInt(pet.getId()));
                }
            }
        });
        context.setPacketHandled(true);
    }
}

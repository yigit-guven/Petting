package net.yigitguven.petting.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import net.yigitguven.petting.procedures.PetBedBindingHandler;

import java.util.function.Supplier;

public class CancelBindingPacket {
    public CancelBindingPacket() {
    }

    public CancelBindingPacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                // We use a public helper in PetBedBindingHandler to do the actual clearing
                PetBedBindingHandler.handleMenuCancellation(player);
            }
        });
        context.setPacketHandled(true);
    }
}

package net.yigitguven.petting.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class OpenPetSettingsPacket {
    public int entityId;

    public OpenPetSettingsPacket(int entityId) {
        this.entityId = entityId;
    }

    public OpenPetSettingsPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
    }

    public static void encode(OpenPetSettingsPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
    }

    public static void handle(OpenPetSettingsPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                EntitySettingsServer.handleOpenRequest(player, msg.entityId);
            }
        });
        context.setPacketHandled(true);
    }
}

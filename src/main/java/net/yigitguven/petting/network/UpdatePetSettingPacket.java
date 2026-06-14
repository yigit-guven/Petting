package net.yigitguven.petting.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class UpdatePetSettingPacket {
    public int entityId;
    public String key;
    public String value;

    public UpdatePetSettingPacket(int entityId, String key, String value) {
        this.entityId = entityId;
        this.key = key;
        this.value = value;
    }

    public UpdatePetSettingPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.key = buf.readUtf(32767);
        this.value = buf.readUtf(32767);
    }

    public static void encode(UpdatePetSettingPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeUtf(msg.key);
        buf.writeUtf(msg.value);
    }

    public static void handle(UpdatePetSettingPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                EntitySettingsServer.applyUpdate(player, msg.entityId, msg.key, msg.value);
            }
        });
        context.setPacketHandled(true);
    }
}

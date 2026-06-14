package net.yigitguven.petting.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class SavePetControlDefaultsPacket {
    public String controlRightClick;
    public String controlShiftRightClick;

    public SavePetControlDefaultsPacket(String controlRightClick, String controlShiftRightClick) {
        this.controlRightClick = controlRightClick;
        this.controlShiftRightClick = controlShiftRightClick;
    }

    public SavePetControlDefaultsPacket(FriendlyByteBuf buf) {
        this.controlRightClick = buf.readUtf(32767);
        this.controlShiftRightClick = buf.readUtf(32767);
    }

    public static void encode(SavePetControlDefaultsPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.controlRightClick);
        buf.writeUtf(msg.controlShiftRightClick);
    }

    public static void handle(SavePetControlDefaultsPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                EntitySettingsServer.saveControlDefaults(player, msg.controlRightClick, msg.controlShiftRightClick);
            }
        });
        context.setPacketHandled(true);
    }
}

package net.yigitguven.petting.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

import java.util.function.Supplier;

public class SyncPetStatusPacket {
    private final int entityId;
    private final boolean isTamed;

    public SyncPetStatusPacket(int entityId, boolean isTamed) {
        this.entityId = entityId;
        this.isTamed = isTamed;
    }

    public SyncPetStatusPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.isTamed = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeBoolean(isTamed);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Client-side handling
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                Entity entity = mc.level.getEntity(entityId);
                if (entity != null) {
                    if (isTamed) {
                        entity.getPersistentData().putBoolean("pettingtamed", true);
                    } else {
                        entity.getPersistentData().remove("pettingtamed");
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}

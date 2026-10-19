package net.yigitguven.petting.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.yigitguven.petting.data.PetData;

import java.util.function.Supplier;

public record SyncPetDataPayload(int entityId, PetData petData) {

    public SyncPetDataPayload(FriendlyByteBuf buf) {
        this(buf.readVarInt(), PetData.fromBytes(buf));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entityId);
        this.petData.toBytes(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context context = ctxSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                net.yigitguven.petting.client.PettingClientPacketHandler.handleSyncData(this);
            }
        });
        context.setPacketHandled(true);
    }
}

package net.yigitguven.petting.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.yigitguven.petting.network.SyncPetStatusPayload;

public class PettingClientPayloadHandler {
    public static void handleSyncPetStatus(final SyncPetStatusPayload payload, final IPayloadContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Entity entity = mc.level.getEntity(payload.entityId());
            if (entity != null) {
                if (payload.isTamed()) {
                    entity.getPersistentData().putBoolean("pettingtamed", true);
                } else {
                    entity.getPersistentData().remove("pettingtamed");
                }
            }
        }
    }
}

package net.yigitguven.petting.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.yigitguven.petting.data.PetData;
import net.yigitguven.petting.entity.PetRidingHandler;
import net.yigitguven.petting.network.SyncPetDataPayload;
import net.yigitguven.petting.util.PetHelper;

public class PettingClientPacketHandler {
    public static void handleDismount() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            PetRidingHandler.allowForcedDismount(player);
            player.stopRiding();
            player.setPose(Pose.STANDING);
            player.resetFallDistance();
        }
    }

    public static void handleSyncData(SyncPetDataPayload payload) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Entity entity = mc.level.getEntity(payload.entityId());
            if (entity != null) {
                PetData data = PetHelper.getPetData(entity);
                if (data != null) {
                    data.copyFrom(payload.petData());
                }
            }
        }
    }
}

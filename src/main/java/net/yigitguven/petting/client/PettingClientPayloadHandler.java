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

    public static void handleSendPetSettings(final net.yigitguven.petting.network.SendPetSettingsPayload payload, final IPayloadContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.execute(() -> {
                    net.minecraft.world.entity.Entity entity = mc.level != null ? mc.level.getEntity(payload.entityId()) : null;
                    if (entity != null) {
                        entity.getPersistentData().putBoolean("sitstill", payload.sitStill());
                        entity.getPersistentData().putBoolean("waiting", payload.waiting());
                        if (payload.isTamed()) entity.getPersistentData().putBoolean("pettingtamed", true); else entity.getPersistentData().remove("pettingtamed");
                        entity.getPersistentData().putBoolean("attackifownerattacks", payload.attackIfOwnerAttacks());
                        entity.getPersistentData().putBoolean("attackifownerattacked", payload.attackIfOwnerAttacked());
                        entity.getPersistentData().putBoolean("attackifselfattacked", payload.attackIfSelfAttacked());
                        entity.getPersistentData().putBoolean("damageOwner", payload.damageOwner());
                        entity.getPersistentData().putBoolean("ignoreWhistle", payload.ignoreWhistle());
                        entity.getPersistentData().putInt("followdistance", payload.followDistance());
                        entity.getPersistentData().putInt("teleportdistance", payload.teleportDistance());
                        entity.getPersistentData().putString("control_right_click", payload.controlRightClick());
                        entity.getPersistentData().putString("control_shift_right_click", payload.controlShiftRightClick());
                    }
                    // Do not steal focus when user is editing key mappings.
                    if (!(mc.screen instanceof net.yigitguven.petting.client.gui.PetControlMappingsScreen)
                            && !(mc.screen instanceof net.yigitguven.petting.client.gui.MappingSelectionScreen)
                            && !(mc.screen instanceof net.yigitguven.petting.client.gui.CommandInputScreen)) {
                        net.yigitguven.petting.client.gui.PetSettingsScreen.open(payload.entityId(), payload.sitStill(), payload.waiting(), payload.isTamed(), payload.attackIfOwnerAttacks(), payload.attackIfOwnerAttacked(), payload.attackIfSelfAttacked(), payload.damageOwner(), payload.ignoreWhistle(), payload.followDistance(), payload.teleportDistance(), payload.controlRightClick(), payload.controlShiftRightClick());
                    }
            });
        }
    }
}

package net.yigitguven.petting.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.yigitguven.petting.network.SendPetSettingsPacket;

import java.util.function.Supplier;

public class PettingClientPacketHandler {

    public static void handleSendPetSettings(SendPetSettingsPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.level != null) {
                Entity entity = mc.level.getEntity(msg.entityId);
                if (entity != null) {
                    entity.getPersistentData().putBoolean("sitstill", msg.sitStill);
                    entity.getPersistentData().putBoolean("waiting", msg.waiting);
                    if (msg.isTamed) {
                        entity.getPersistentData().putBoolean("pettingtamed", true);
                    } else {
                        entity.getPersistentData().remove("pettingtamed");
                    }
                    entity.getPersistentData().putBoolean("attackifownerattacks", msg.attackIfOwnerAttacks);
                    entity.getPersistentData().putBoolean("attackifownerattacked", msg.attackIfOwnerAttacked);
                    entity.getPersistentData().putBoolean("attackifselfattacked", msg.attackIfSelfAttacked);
                    entity.getPersistentData().putBoolean("damageOwner", msg.damageOwner);
                    entity.getPersistentData().putBoolean("ignoreWhistle", msg.ignoreWhistle);
                    entity.getPersistentData().putInt("followdistance", msg.followDistance);
                    entity.getPersistentData().putInt("teleportdistance", msg.teleportDistance);
                    entity.getPersistentData().putString("control_right_click", msg.controlRightClick);
                    entity.getPersistentData().putString("control_shift_right_click", msg.controlShiftRightClick);
                }
                
                if (msg.openScreen
                    && !(mc.screen instanceof net.yigitguven.petting.client.gui.PetControlMappingsScreen)
                    && !(mc.screen instanceof net.yigitguven.petting.client.gui.MappingSelectionScreen)
                    && !(mc.screen instanceof net.yigitguven.petting.client.gui.CommandInputScreen)) {
                    
                    net.yigitguven.petting.client.gui.PetSettingsScreen.open(
                        msg.entityId, msg.sitStill, msg.waiting, msg.isTamed, 
                        msg.attackIfOwnerAttacks, msg.attackIfOwnerAttacked, msg.attackIfSelfAttacked, 
                        msg.damageOwner, msg.ignoreWhistle, msg.followDistance, msg.teleportDistance, 
                        msg.controlRightClick, msg.controlShiftRightClick
                    );
                }
            }
        });
        context.setPacketHandled(true);
    }
}

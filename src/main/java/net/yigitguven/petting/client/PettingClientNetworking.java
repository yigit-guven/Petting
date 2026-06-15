package net.yigitguven.petting.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.yigitguven.petting.PettingMod;

public class PettingClientNetworking {
    public static final ResourceLocation SEND_PET_SETTINGS = new ResourceLocation(PettingMod.MODID, "send_pet_settings");
    public static final ResourceLocation SYNC_PET_STATUS = new ResourceLocation(PettingMod.MODID, "sync_pet_status");

    public static void registerReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(SYNC_PET_STATUS, (client, handler, buf, responseSender) -> {
            int entityId = buf.readInt();
            boolean isTamed = buf.readBoolean();
            client.execute(() -> {
                if (client.level != null) {
                    Entity entity = client.level.getEntity(entityId);
                    if (entity != null) {
                        if (isTamed) {
                            ((net.yigitguven.petting.IEntityData)entity).getPersistentData().putBoolean("pettingtamed", true);
                        } else {
                            ((net.yigitguven.petting.IEntityData)entity).getPersistentData().remove("pettingtamed");
                        }
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SEND_PET_SETTINGS, (client, handler, buf, responseSender) -> {
            int entityId = buf.readInt();
            boolean sitStill = buf.readBoolean();
            boolean waiting = buf.readBoolean();
            boolean isTamed = buf.readBoolean();
            boolean attackIfOwnerAttacks = buf.readBoolean();
            boolean attackIfOwnerAttacked = buf.readBoolean();
            boolean attackIfSelfAttacked = buf.readBoolean();
            boolean damageOwner = buf.readBoolean();
            boolean ignoreWhistle = buf.readBoolean();
            int followDistance = buf.readInt();
            int teleportDistance = buf.readInt();
            boolean openScreen = buf.readBoolean();
            String controlRightClick = buf.readUtf(32767);
            String controlShiftRightClick = buf.readUtf(32767);

            client.execute(() -> {
                Entity entity = client.level != null ? client.level.getEntity(entityId) : null;
                if (entity != null) {
                    ((net.yigitguven.petting.IEntityData)entity).getPersistentData().putBoolean("sitstill", sitStill);
                    ((net.yigitguven.petting.IEntityData)entity).getPersistentData().putBoolean("waiting", waiting);
                    if (isTamed) ((net.yigitguven.petting.IEntityData)entity).getPersistentData().putBoolean("pettingtamed", true); else ((net.yigitguven.petting.IEntityData)entity).getPersistentData().remove("pettingtamed");
                    ((net.yigitguven.petting.IEntityData)entity).getPersistentData().putBoolean("attackifownerattacks", attackIfOwnerAttacks);
                    ((net.yigitguven.petting.IEntityData)entity).getPersistentData().putBoolean("attackifownerattacked", attackIfOwnerAttacked);
                    ((net.yigitguven.petting.IEntityData)entity).getPersistentData().putBoolean("attackifselfattacked", attackIfSelfAttacked);
                    ((net.yigitguven.petting.IEntityData)entity).getPersistentData().putBoolean("damageOwner", damageOwner);
                    ((net.yigitguven.petting.IEntityData)entity).getPersistentData().putBoolean("ignoreWhistle", ignoreWhistle);
                    ((net.yigitguven.petting.IEntityData)entity).getPersistentData().putInt("followdistance", followDistance);
                    ((net.yigitguven.petting.IEntityData)entity).getPersistentData().putInt("teleportdistance", teleportDistance);
                    ((net.yigitguven.petting.IEntityData)entity).getPersistentData().putString("control_right_click", controlRightClick);
                    ((net.yigitguven.petting.IEntityData)entity).getPersistentData().putString("control_shift_right_click", controlShiftRightClick);
                }
                
                if (openScreen
                    && !(client.screen instanceof net.yigitguven.petting.client.gui.PetControlMappingsScreen)
                    && !(client.screen instanceof net.yigitguven.petting.client.gui.MappingSelectionScreen)
                    && !(client.screen instanceof net.yigitguven.petting.client.gui.CommandInputScreen)) {
                    net.yigitguven.petting.client.gui.PetSettingsScreen.open(entityId, sitStill, waiting, isTamed, attackIfOwnerAttacks, attackIfOwnerAttacked, attackIfSelfAttacked, damageOwner, ignoreWhistle, followDistance, teleportDistance, controlRightClick, controlShiftRightClick);
                }
            });
        });
    }

    public static void sendUpdatePetSetting(int entityId, String key, String value) {
        net.minecraft.network.FriendlyByteBuf buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
        buf.writeInt(entityId);
        buf.writeUtf(key);
        buf.writeUtf(value);
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(net.yigitguven.petting.network.PettingServerNetworking.UPDATE_PET_SETTING, buf);
    }

    public static void sendSavePetControlDefaults(String rightClick, String shiftRightClick) {
        net.minecraft.network.FriendlyByteBuf buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
        buf.writeUtf(rightClick);
        buf.writeUtf(shiftRightClick);
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(net.yigitguven.petting.network.PettingServerNetworking.SAVE_PET_CONTROL_DEFAULTS, buf);
    }

    public static void sendPetAttack() {
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(net.yigitguven.petting.network.PettingServerNetworking.PET_ATTACK, net.fabricmc.fabric.api.networking.v1.PacketByteBufs.empty());
    }

    public static void sendOpenPetSettings(int entityId) {
        net.minecraft.network.FriendlyByteBuf buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
        buf.writeInt(entityId);
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(net.yigitguven.petting.network.PettingServerNetworking.OPEN_PET_SETTINGS, buf);
    }
}

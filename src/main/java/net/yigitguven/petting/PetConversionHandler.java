package net.yigitguven.petting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class PetConversionHandler {

    @SubscribeEvent
    public static void onLivingConversion(LivingConversionEvent.Post event) {
        Entity original = event.getEntity();
        Entity converted = event.getOutcome();

        if (original == null || converted == null) {
            return;
        }
        
        if (net.yigitguven.petting.util.PetInventoryUtil.isBlacklisted(original) || 
            net.yigitguven.petting.util.PetInventoryUtil.isBlacklisted(converted)) return;

        CompoundTag originalData = original.getPersistentData();
        if (originalData.getBoolean("pettingtamed")) {
            CompoundTag convertedData = converted.getPersistentData();
            
            convertedData.putBoolean("pettingtamed", true);
            if (originalData.contains("ownerUUID")) {
                convertedData.putString("ownerUUID", originalData.getString("ownerUUID"));
            }
            if (originalData.contains("isNameGenerated")) {
                convertedData.putBoolean("isNameGenerated", originalData.getBoolean("isNameGenerated"));
            }

            copyBoolean(originalData, convertedData, "attackifownerattacks");
            copyBoolean(originalData, convertedData, "attackifownerattacked");
            copyBoolean(originalData, convertedData, "attackifselfattacked");
            copyBoolean(originalData, convertedData, "damageOwner");
            copyBoolean(originalData, convertedData, "sitstill");
            copyBoolean(originalData, convertedData, "waiting");
            copyBoolean(originalData, convertedData, "ignoreWhistle");

            copyInt(originalData, convertedData, "followdistance");
            copyInt(originalData, convertedData, "teleportdistance");

            copyDouble(originalData, convertedData, "pet_bed_loc_x");
            copyDouble(originalData, convertedData, "pet_bed_loc_y");
            copyDouble(originalData, convertedData, "pet_bed_loc_z");
            copyString(originalData, convertedData, "pet_bed_dim");

            copyBoolean(originalData, convertedData, "pettingbound");
            copyDouble(originalData, convertedData, "boundX");
            copyDouble(originalData, convertedData, "boundY");
            copyDouble(originalData, convertedData, "boundZ");

            copyString(originalData, convertedData, "control_right_click");
            copyString(originalData, convertedData, "control_shift_right_click");

            // Transfer pet inventory capability contents if present
            original.getCapability(net.yigitguven.petting.capability.PetInventoryCapability.PET_INVENTORY).ifPresent(origInv -> {
                converted.getCapability(net.yigitguven.petting.capability.PetInventoryCapability.PET_INVENTORY).ifPresent(convInv -> {
                    for (int slot = 0; slot < origInv.getSlots() && slot < convInv.getSlots(); slot++) {
                        convInv.setStackInSlot(slot, origInv.getStackInSlot(slot).copy());
                    }
                });
            });

            if (converted instanceof Mob convertedMob) {
                convertedMob.setPersistenceRequired();
            }
            
            if (original.hasCustomName() && !converted.hasCustomName()) {
                converted.setCustomName(original.getCustomName());
                converted.setCustomNameVisible(original.isCustomNameVisible());
            }

            // NeoForge payload sync
            if (converted.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                net.yigitguven.petting.PettingMod.PACKET_HANDLER.send(net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY.with(() -> converted), new net.yigitguven.petting.network.SyncPetStatusPacket(converted.getId(), true));
            }
        }
    }

    private static void copyBoolean(CompoundTag from, CompoundTag to, String key) {
        if (from.contains(key)) {
            to.putBoolean(key, from.getBoolean(key));
        }
    }

    private static void copyInt(CompoundTag from, CompoundTag to, String key) {
        if (from.contains(key)) {
            to.putInt(key, from.getInt(key));
        }
    }

    private static void copyDouble(CompoundTag from, CompoundTag to, String key) {
        if (from.contains(key)) {
            to.putDouble(key, from.getDouble(key));
        }
    }

    private static void copyString(CompoundTag from, CompoundTag to, String key) {
        if (from.contains(key)) {
            to.putString(key, from.getString(key));
        }
    }
}

package net.yigitguven.petting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PetConversionHandler {

    @SubscribeEvent
    public static void onLivingConversion(LivingConversionEvent.Post event) {
        Entity original = event.getEntity();
        Entity converted = event.getOutcome();

        if (original == null || converted == null) {
            return;
        }

        CompoundTag originalData = original.getPersistentData();
        if (originalData.getBoolean("pettingtamed")) {
            CompoundTag convertedData = converted.getPersistentData();
            
            // Core pet data
            convertedData.putBoolean("pettingtamed", true);
            if (originalData.contains("ownerUUID")) {
                convertedData.putString("ownerUUID", originalData.getString("ownerUUID"));
            }
            if (originalData.contains("isNameGenerated")) {
                convertedData.putBoolean("isNameGenerated", originalData.getBoolean("isNameGenerated"));
            }

            // Behavior settings
            copyBoolean(originalData, convertedData, "attackifownerattacks");
            copyBoolean(originalData, convertedData, "attackifownerattacked");
            copyBoolean(originalData, convertedData, "attackifselfattacked");
            copyBoolean(originalData, convertedData, "damageOwner");
            copyBoolean(originalData, convertedData, "sitstill");
            copyBoolean(originalData, convertedData, "waiting");

            // Distances
            copyInt(originalData, convertedData, "followdistance");
            copyInt(originalData, convertedData, "teleportdistance");

            // Pet Bed
            copyDouble(originalData, convertedData, "pet_bed_loc_x");
            copyDouble(originalData, convertedData, "pet_bed_loc_y");
            copyDouble(originalData, convertedData, "pet_bed_loc_z");

            // Bound roaming
            copyBoolean(originalData, convertedData, "pettingbound");
            copyDouble(originalData, convertedData, "boundX");
            copyDouble(originalData, convertedData, "boundY");
            copyDouble(originalData, convertedData, "boundZ");

            // Ensure the new entity is persistent
            if (converted instanceof Mob convertedMob) {
                convertedMob.setPersistenceRequired();
            }
            
            // Transfer custom name if it exists and wasn't automatically transferred
            if (original.hasCustomName() && !converted.hasCustomName()) {
                converted.setCustomName(original.getCustomName());
                converted.setCustomNameVisible(original.isCustomNameVisible());
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
}

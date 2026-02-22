package net.yigitguven.petting.procedures;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

import java.util.UUID;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class PetBedBindingHandler {

    private static final String TAG_BINDING_MODE = "PettingBindingMode";
    private static final String TAG_BED_X = "PettingTempBedX";
    private static final String TAG_BED_Y = "PettingTempBedY";
    private static final String TAG_BED_Z = "PettingTempBedZ";

    /**
     * EVENT 1: BLOCK CLICK (Trigger Binding Mode OR Cancel)
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Player player = event.getEntity();
        Level world = event.getLevel();
        BlockPos pos = event.getPos();
        
        // Check what block was clicked
        ResourceLocation blockRegistryName = BuiltInRegistries.BLOCK.getKey(world.getBlockState(pos).getBlock());
        String blockId = (blockRegistryName != null) ? blockRegistryName.toString() : "";

        CompoundTag playerNBT = player.getPersistentData();
        boolean isBinding = playerNBT.getBoolean(TAG_BINDING_MODE);

        // SCENARIO A: Clicking a Pet Bed (Start Binding)
        if (blockId.contains("pet_bed")) {
            // Save Coordinates to Player
            playerNBT.putBoolean(TAG_BINDING_MODE, true);
            playerNBT.putDouble(TAG_BED_X, pos.getX());
            playerNBT.putDouble(TAG_BED_Y, pos.getY());
            playerNBT.putDouble(TAG_BED_Z, pos.getZ());

            player.displayClientMessage(Component.literal("§a[Petting] §fBinding Mode Active!"), true);
            player.sendSystemMessage(Component.literal("§eRight-click a tamed pet to bind it to this bed."));
            player.sendSystemMessage(Component.literal("§7(Right-click air or ground to cancel)"));
            
            // Play a "click" sound
            world.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.5f, 1.0f);
            
            // Cancel event so we don't open the bed GUI immediately if it exists
            event.setCanceled(true); 
            return;
        }

        // SCENARIO B: Clicking any OTHER block while Binding is active (Cancel)
        if (isBinding) {
            clearBindingState(player);
            player.displayClientMessage(Component.literal("§c[Petting] Binding Cancelled."), true);
        }
    }

    /**
     * EVENT 2: AIR CLICK (Cancel)
     */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide()) return;
        Player player = event.getEntity();
        
        // If clicking air while binding, cancel it
        if (player.getPersistentData().getBoolean(TAG_BINDING_MODE)) {
            clearBindingState(player);
            player.displayClientMessage(Component.literal("§c[Petting] Binding Cancelled."), true);
        }
    }

    /**
     * EVENT 3: ENTITY CLICK (The Binding Logic)
     */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Player player = event.getEntity();
        Entity target = event.getTarget();
        CompoundTag playerNBT = player.getPersistentData();

        // Check if player is trying to bind
        if (!playerNBT.getBoolean(TAG_BINDING_MODE)) {
            return;
        }

        // 1. Validate Target (Must be a Custom Tamed Pet)
        boolean validPet = false;
        
        if (target instanceof Mob mob) {
            CompoundTag petNBT = mob.getPersistentData();
            
            // Check 1: Is it from Petting Mod?
            if (petNBT.getBoolean("pettingtamed")) {
                // Check 2: Is the clicker the owner?
                String ownerUUIDStr = petNBT.getString("ownerUUID");
                if (!ownerUUIDStr.isEmpty() && ownerUUIDStr.equals(player.getStringUUID())) {
                    validPet = true;
                }
            }
        }

        if (validPet) {
            // --- SUCCESSFUL BINDING ---
            
            // 1. Transfer Coords from Player -> Pet
            double bedX = playerNBT.getDouble(TAG_BED_X);
            double bedY = playerNBT.getDouble(TAG_BED_Y);
            double bedZ = playerNBT.getDouble(TAG_BED_Z);

            CompoundTag petNBT = target.getPersistentData();
            petNBT.putDouble("pet_bed_loc_x", bedX);
            petNBT.putDouble("pet_bed_loc_y", bedY);
            petNBT.putDouble("pet_bed_loc_z", bedZ);

            // 2. Feedback
            player.sendSystemMessage(Component.literal("§a[Petting] §fSuccessfully bound " + target.getName().getString() + " to the bed!"));
            player.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);

            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, 
                    target.getX(), target.getY() + 0.5, target.getZ(), 
                    10, 0.5, 0.5, 0.5, 0.1);
            }
        } else {
            // --- INVALID TARGET (Cancel) ---
            player.sendSystemMessage(Component.literal("§c[Petting] Cancelled. That is not your custom pet!"));
        }

        // Always clear state and consume the click so we don't sit on the pet
        clearBindingState(player);
        event.setCanceled(true); 
    }

    private static void clearBindingState(Player player) {
        CompoundTag nbt = player.getPersistentData();
        nbt.remove(TAG_BINDING_MODE);
        nbt.remove(TAG_BED_X);
        nbt.remove(TAG_BED_Y);
        nbt.remove(TAG_BED_Z);
    }
}




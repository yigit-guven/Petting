package net.yigitguven.petting.procedures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.registries.ForgeRegistries;

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

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.yigitguven.petting.PettingMod;
import net.yigitguven.petting.network.CancelBindingPacket;

import java.util.UUID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
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
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Player player = event.getEntity();
        if (player.isSecondaryUseActive()) return;

        Level world = event.getLevel();
        BlockPos pos = event.getPos();
        
        // Check what block was clicked
        ResourceLocation blockRegistryName = ForgeRegistries.BLOCKS.getKey(world.getBlockState(pos).getBlock());
        String blockId = (blockRegistryName != null) ? blockRegistryName.toString() : "";

        CompoundTag playerNBT = player.getPersistentData();
        boolean isBinding = playerNBT.getBoolean(TAG_BINDING_MODE);

        // SCENARIO A: Clicking a Pet Bed (Start Binding)
        if (blockId.contains("pet_bed")) {
            // Cancel and deny on both sides to prevent placement prediction
            event.setCanceled(true); 
            event.setUseItem(Event.Result.DENY);
            event.setUseBlock(Event.Result.DENY);

            if (world.isClientSide()) return;
            
            // Force inventory sync to be safe
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.containerMenu.broadcastFullState();
            }

            // Save Coordinates to Player
            playerNBT.putBoolean(TAG_BINDING_MODE, true);
            playerNBT.putDouble(TAG_BED_X, pos.getX());
            playerNBT.putDouble(TAG_BED_Y, pos.getY());
            playerNBT.putDouble(TAG_BED_Z, pos.getZ());

            player.displayClientMessage(Component.literal("§a[Petting] §fBinding Mode Active!"), true);
            player.sendSystemMessage(Component.literal("§eRight-click a tamed pet to bind it to this bed."));
            player.sendSystemMessage(Component.literal("§7(Right-click air or ground to cancel)"));
            
            // Play a "click" sound
            world.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.get(), SoundSource.PLAYERS, 0.5f, 1.0f);
            
            // Cancel event so we don't open the bed GUI immediately if it exists
            event.setCanceled(true); 
            return;
        }

        if (isBinding) {
            // Cancel and deny on both sides to prevent placement prediction
            event.setCanceled(true); 
            event.setUseItem(Event.Result.DENY);
            event.setUseBlock(Event.Result.DENY);

            if (world.isClientSide()) return;

            // Force immediate inventory sync to clear ghost items on client
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.containerMenu.broadcastFullState();
            }

            clearBindingState(player);
            player.displayClientMessage(Component.literal("§c[Petting] Binding Cancelled."), true);
        }
    }

    /**
     * EVENT 2: AIR CLICK (Item in hand)
     */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (player.isSecondaryUseActive()) return;
        
        // If clicking air while binding, cancel it
        if (event.getLevel().isClientSide()) {
            // Client sends packet because it can't see the binding NBT
            PettingMod.PACKET_HANDLER.sendToServer(new CancelBindingPacket());
        } else if (player.getPersistentData().getBoolean(TAG_BINDING_MODE)) {
            // Server handles it directly if it's the one receiving the event
            handleMenuCancellation(player);
        }
    }

    /**
     * EVENT 4: EMPTY AIR CLICK (Empty hand, Client Side Only)
     */
    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        Player player = event.getEntity();
        if (player.isSecondaryUseActive()) return;

        // Since we can't easily check NBT on Client, we just send the packet 
        // whenever the player right-clicks air. The server will ignore it if not in binding mode.
        PettingMod.PACKET_HANDLER.sendToServer(new CancelBindingPacket());
    }

    /**
     * Public helper to safely clear state and sync from both Packet and Event handlers
     */
    public static void handleMenuCancellation(Player player) {
        if (player.getPersistentData().getBoolean(TAG_BINDING_MODE)) {
            clearBindingState(player);
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(Component.literal("§c[Petting] Binding Cancelled."), true);
                serverPlayer.containerMenu.broadcastFullState();
            }
        }
    }

    /**
     * EVENT 3: ENTITY CLICK (The Binding Logic)
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
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

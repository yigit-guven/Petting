package net.yigitguven.petting.handler;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yigitguven.petting.util.PetInventoryUtil;

import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import java.lang.reflect.Field;

@Mod.EventBusSubscriber
public class PetRidingHandler {
    private static final Field JUMPING_FIELD = ObfuscationReflectionHelper.findField(net.minecraft.world.entity.LivingEntity.class, "f_20899_"); // official name: jumping
    private static final Field XXA_FIELD = ObfuscationReflectionHelper.findField(net.minecraft.world.entity.LivingEntity.class, "f_20900_"); // official name: xxa
    private static final Field YYA_FIELD = ObfuscationReflectionHelper.findField(net.minecraft.world.entity.LivingEntity.class, "f_20901_"); // official name: yya
    private static final Field ZZA_FIELD = ObfuscationReflectionHelper.findField(net.minecraft.world.entity.LivingEntity.class, "f_20902_"); // official name: zza

    static {
        JUMPING_FIELD.setAccessible(true);
        XXA_FIELD.setAccessible(true);
        YYA_FIELD.setAccessible(true);
        ZZA_FIELD.setAccessible(true);
    }

    private static boolean isJumping(LivingEntity entity) {
        try {
            return JUMPING_FIELD.getBoolean(entity);
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    private static float getXxa(LivingEntity entity) {
        try {
            return XXA_FIELD.getFloat(entity);
        } catch (IllegalAccessException e) {
            return 0;
        }
    }

    private static float getZza(LivingEntity entity) {
        try {
            return ZZA_FIELD.getFloat(entity);
        } catch (IllegalAccessException e) {
            return 0;
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity pet = event.getEntity();
        if (pet.level().isClientSide()) return;

        // Check if there is a player passenger
        if (pet.getPassengers().isEmpty()) return;
        
        Player player = null;
        for (net.minecraft.world.entity.Entity passenger : pet.getPassengers()) {
            if (passenger instanceof Player p) {
                player = p;
                break;
            }
        }

        if (player != null) {
            // Check for saddle
            if (PetInventoryUtil.hasSaddle(pet)) {
                handleRidingControl(pet, player);
            }
        }
    }

    private static void handleRidingControl(LivingEntity pet, Player player) {
        // Clear sitting/waiting states when mounting
        net.minecraft.nbt.CompoundTag data = pet.getPersistentData();
        if (data.getBoolean("sitstill") || data.getBoolean("waiting") || pet.isShiftKeyDown()) {
            data.putBoolean("sitstill", false);
            data.putBoolean("waiting", false);
            pet.setShiftKeyDown(false);
        }

        // 1. Sync Rotation
        pet.setYRot(player.getYRot());
        pet.yRotO = pet.getYRot();
        pet.setXRot(player.getXRot() * 0.5F); // Look up/down slightly
        pet.setYBodyRot(pet.getYRot());
        pet.setYHeadRot(pet.getYRot());

        // 2. Movement Inputs
        float forward = getZza(player); // Forward/Backward (W/S)
        float strafe = getXxa(player);  // Left/Right (A/D)
        
        // Use a multiplier for speed (standardized)
        float speed = 0.1F; // Base speed fallback
        if (pet instanceof Mob mob) {
            speed = (float) mob.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        }

        boolean isFlying = PetInventoryUtil.isFlyingMob(pet);
        
        if (isFlying) {
            handleFlightMovement(pet, player, forward, strafe, speed);
        } else {
            handleLandMovement(pet, player, forward, strafe, speed);
        }
    }

    private static void handleFlightMovement(LivingEntity pet, Player player, float forward, float strafe, float speed) {
        double vx = 0;
        double vy = 0;
        double vz = 0;

        // Vertical Movement
        if (isJumping(player)) {
            vy = 0.25; // Gentle climb
        } else if (forward < 0) { // S key to Fly Down
            vy = -0.25;
        } else {
            // Neutral buoyancy for flyers
            vy = 0;
            pet.setNoGravity(true);
        }

        // Horizontal Movement (Only apply forward if W is pressed, or if only strafing)
        float horizontalForward = Math.max(0, forward); 
        if (horizontalForward != 0 || strafe != 0) {
            // Flight speed multiplier (1.5x feels smooth and controllable)
            Vec3 moveVec = new Vec3(strafe, 0, horizontalForward).yRot(-player.getYRot() * ((float)Math.PI / 180F)).normalize().scale(speed * 1.5);
            vx = moveVec.x;
            vz = moveVec.z;
        }

        // In flight, we use DeltaMovement for full 3D freedom
        pet.setDeltaMovement(vx, vy, vz);
        
        // Ensure it doesn't just fall when we stop riding
        if (pet.getPassengers().isEmpty()) {
            pet.setNoGravity(false);
        }
    }

    private static void handleLandMovement(LivingEntity pet, Player player, float forward, float strafe, float speed) {
        pet.setNoGravity(false);
        
        if (pet instanceof Mob mob) {
            mob.getNavigation().stop();
        }

        // Apply WASD movement via DeltaMovement
        float moveSpeed = speed;
        if (forward < 0) moveSpeed *= 0.5F; // Slower backing up
        if (player.isSprinting()) moveSpeed *= 1.3F;

        if (forward != 0 || strafe != 0) {
            Vec3 moveVec = new Vec3(strafe, 0, forward).yRot(-player.getYRot() * ((float)Math.PI / 180F)).normalize().scale(moveSpeed);
            pet.setDeltaMovement(moveVec.x, pet.getDeltaMovement().y, moveVec.z);
        }

        // Jumping logic
        if (isJumping(player) && pet.onGround()) {
            pet.setDeltaMovement(pet.getDeltaMovement().x, 0.42D, pet.getDeltaMovement().z);
        }
    }
}

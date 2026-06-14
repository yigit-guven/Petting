package net.yigitguven.petting.handler;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.yigitguven.petting.util.PetInventoryUtil;
import net.yigitguven.petting.config.PettingConfig;

import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import java.lang.reflect.Field;

@EventBusSubscriber
public class PetRidingHandler {
    private static final Field JUMPING_FIELD = ObfuscationReflectionHelper.findField(net.minecraft.world.entity.LivingEntity.class, "jumping");
    private static final Field XXA_FIELD = ObfuscationReflectionHelper.findField(net.minecraft.world.entity.LivingEntity.class, "xxa");
    private static final Field YYA_FIELD = ObfuscationReflectionHelper.findField(net.minecraft.world.entity.LivingEntity.class, "yya");
    private static final Field ZZA_FIELD = ObfuscationReflectionHelper.findField(net.minecraft.world.entity.LivingEntity.class, "zza");

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
    public static void onEntityTick(net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent event) {
        LivingEntity pet = event.getEntity();
        if (pet.level().isClientSide()) return;

        if (PetInventoryUtil.isBlacklisted(pet)) return;

        if (pet.getPassengers().isEmpty()) return;
        
        Player player = null;
        for (net.minecraft.world.entity.Entity passenger : pet.getPassengers()) {
            if (passenger instanceof Player p) {
                player = p;
                break;
            }
        }

        if (player != null) {
            if (PetInventoryUtil.hasSaddle(pet) && PetInventoryUtil.isRidingAllowed(pet)) {
                handleRidingControl(pet, player);
            }
        }
    }

    private static void handleRidingControl(LivingEntity pet, Player player) {
        net.minecraft.nbt.CompoundTag data = pet.getPersistentData();
        if (data.getBoolean("sitstill") || data.getBoolean("waiting") || pet.isShiftKeyDown()) {
            data.putBoolean("sitstill", false);
            data.putBoolean("waiting", false);
            pet.setShiftKeyDown(false);
        }

        float yaw = player.getYRot();
        // In 1.21.1 Ender Dragon id check
        net.minecraft.resources.ResourceLocation key = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(pet.getType());
        if (key != null && key.toString().equals("minecraft:ender_dragon")) {
            yaw += 180.0F;
        }
        
        pet.setYRot(yaw);
        pet.yRotO = pet.getYRot();
        pet.setXRot(player.getXRot() * 0.5F);
        pet.setYBodyRot(pet.getYRot());
        pet.setYHeadRot(pet.getYRot());

        float forward = getZza(player);
        float strafe = getXxa(player);
        
        float speed = 0.1F;
        if (pet instanceof Mob mob) {
            speed = (float) mob.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        }

        boolean isFlying = PetInventoryUtil.isFlyingMob(pet);
        boolean isSwimming = PetInventoryUtil.isSwimmingMob(pet) && pet.isInWater();
        
        if (isFlying) {
            handleFlightMovement(pet, player, forward, strafe, speed);
        } else if (isSwimming) {
            handleSwimmingMovement(pet, player, forward, strafe, speed);
        } else {
            handleLandMovement(pet, player, forward, strafe, speed);
        }
    }

    private static void handleSwimmingMovement(LivingEntity pet, Player player, float forward, float strafe, float speed) {
        double vx = 0;
        double vy = 0;
        double vz = 0;

        if (isJumping(player)) {
            vy = 0.2;
        } else if (forward < 0) {
            vy = -0.2;
        } else {
            vy = pet.getDeltaMovement().y * 0.5;
        }

        float horizontalForward = Math.max(0, forward); 
        if (horizontalForward != 0 || strafe != 0) {
            Vec3 moveVec = new Vec3(strafe, 0, horizontalForward).yRot(-player.getYRot() * ((float)Math.PI / 180F)).normalize().scale(speed * PettingConfig.SWIMMING_RIDING_SPEED_MULTIPLIER.get());
            vx = moveVec.x;
            vz = moveVec.z;
        } else {
            vx = pet.getDeltaMovement().x * 0.8;
            vz = pet.getDeltaMovement().z * 0.8;
        }

        pet.setDeltaMovement(vx, vy, vz);
    }

    private static void handleFlightMovement(LivingEntity pet, Player player, float forward, float strafe, float speed) {
        double vx = 0;
        double vy = 0;
        double vz = 0;

        if (isJumping(player)) {
            vy = 0.25;
        } else if (forward < 0) {
            vy = -0.25;
        } else {
            vy = 0;
            pet.setNoGravity(true);
        }

        float horizontalForward = Math.max(0, forward); 
        if (horizontalForward != 0 || strafe != 0) {
            Vec3 moveVec = new Vec3(strafe, 0, horizontalForward).yRot(-player.getYRot() * ((float)Math.PI / 180F)).normalize().scale(speed * PettingConfig.FLYING_RIDING_SPEED_MULTIPLIER.get());
            vx = moveVec.x;
            vz = moveVec.z;
        }

        pet.setDeltaMovement(vx, vy, vz);
        
        if (pet.getPassengers().isEmpty()) {
            pet.setNoGravity(false);
        }
    }

    private static void handleLandMovement(LivingEntity pet, Player player, float forward, float strafe, float speed) {
        pet.setNoGravity(false);
        
        if (pet instanceof Mob mob) {
            mob.getNavigation().stop();
        }

        float moveSpeed = speed * PettingConfig.LAND_RIDING_SPEED_MULTIPLIER.get().floatValue();
        if (forward < 0) moveSpeed *= 0.5F;
        if (player.isSprinting()) moveSpeed *= 1.3F;

        if (forward != 0 || strafe != 0) {
            Vec3 moveVec = new Vec3(strafe, 0, forward).yRot(-player.getYRot() * ((float)Math.PI / 180F)).normalize().scale(moveSpeed);
            pet.setDeltaMovement(moveVec.x, pet.getDeltaMovement().y, moveVec.z);
        }

        if (isJumping(player) && pet.onGround()) {
            pet.setDeltaMovement(pet.getDeltaMovement().x, 0.42D, pet.getDeltaMovement().z);
        }
    }
}

package net.yigitguven.petting;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.yigitguven.petting.config.PettingConfig;

import java.util.UUID;

public class FollowOwnerOrTeleport {
    private static final UUID PETTING_STOP_UUID = UUID.fromString("11111111-2222-3333-4444-555555555555");

    public static void register() {
        // We use the mixin logic now for ticking, but we might keep this for initialization if needed.
        // Fabric doesn't have a direct equivalent to LivingTickEvent without Mixins.
        // The ticking logic is already integrated into PetAttackLogic.tick() via MobMixin.
    }

    public static void tick(Mob mob) {
        Level world = mob.level();
        if (world.isClientSide()) return;

        IEntityData dataAccess = (IEntityData) mob;
        CompoundTag data = dataAccess.getPersistentData();

        if (!data.contains("pettingtamed") || !data.getBoolean("pettingtamed")) {
            return;
        }

        // --- BOUND ROAM LOGIC ---
        if (data.getBoolean("pettingbound")) {
            double bX = data.getDouble("boundX");
            double bY = data.getDouble("boundY");
            double bZ = data.getDouble("boundZ");
            Vec3 boundPos = new Vec3(bX, bY, bZ);
            double distToBound = mob.distanceToSqr(boundPos);
            double roamRadius = PettingConfig.boundRoamRadius;
            
            if (distToBound > 400.0) {
                mob.teleportTo(bX, bY, bZ);
                mob.getNavigation().stop();
            } else if (distToBound > roamRadius * roamRadius) {
                if (mob.tickCount % 20 == 0) {
                    mob.getNavigation().moveTo(bX, bY, bZ, 1.0D);
                }
            }
            return;
        }

        AttributeInstance speedAttribute = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance flyingSpeedAttribute = mob.getAttribute(Attributes.FLYING_SPEED);
        
        AttributeModifier stopModifier = new AttributeModifier(
            PETTING_STOP_UUID, "Petting Stop", -1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // --- SIT LOGIC ---
        if (data.contains("sitstill") && data.getBoolean("sitstill")) {
            mob.getNavigation().stop();
            mob.getMoveControl().setWantedPosition(mob.getX(), mob.getY(), mob.getZ(), 0.0);
            
            mob.setZza(0.0f);
            mob.setXxa(0.0f);
            mob.setYya(0.0f);
            mob.setSpeed(0.0f);

            if (speedAttribute != null && !speedAttribute.hasModifier(stopModifier)) {
                speedAttribute.addTransientModifier(stopModifier);
            }
            if (flyingSpeedAttribute != null && !flyingSpeedAttribute.hasModifier(stopModifier)) {
                flyingSpeedAttribute.addTransientModifier(stopModifier);
            }

            if (isUniversalFlyingMob(mob, data)) {
                Vec3 currentVel = mob.getDeltaMovement();
                double newY = (currentVel.y < 0) ? 0.0 : currentVel.y * 0.8;
                mob.setDeltaMovement(currentVel.x * 0.8, newY, currentVel.z * 0.8);
            } else {
                mob.setDeltaMovement(0, mob.getDeltaMovement().y, 0); 
            }

            int healInterval = PettingConfig.sitHealInterval;
            if (healInterval > 0 && mob.tickCount % healInterval == 0 && mob.getHealth() < mob.getMaxHealth()) {
                mob.heal((float) PettingConfig.sitHealAmount);
            }
            return;
        }

        // --- WAITING LOGIC ---
        if (data.contains("waiting") && data.getBoolean("waiting")) {
            mob.getNavigation().stop();
            mob.getMoveControl().setWantedPosition(mob.getX(), mob.getY(), mob.getZ(), 0.0);

            mob.setZza(0.0f); 
            mob.setXxa(0.0f); 
            mob.setYya(0.0f); 
            mob.setSpeed(0.0f);

            if (speedAttribute != null && !speedAttribute.hasModifier(stopModifier)) {
                speedAttribute.addTransientModifier(stopModifier);
            }
            if (flyingSpeedAttribute != null && !flyingSpeedAttribute.hasModifier(stopModifier)) {
                flyingSpeedAttribute.addTransientModifier(stopModifier);
            }

            if (isUniversalFlyingMob(mob, data)) {
                Vec3 currentVel = mob.getDeltaMovement();
                double newY = (currentVel.y < 0) ? 0.0 : currentVel.y * 0.8;
                mob.setDeltaMovement(currentVel.x * 0.8, newY, currentVel.z * 0.8);
            } else {
                mob.setDeltaMovement(0, mob.getDeltaMovement().y, 0); 
            }

            Player nearest = world.getNearestPlayer(mob, 10.0D);
            if (nearest != null) {
                mob.getLookControl().setLookAt(nearest, 10.0F, (float)mob.getMaxHeadXRot());
            }
            return;
        }

        // --- WANDERING RESTORE LOGIC ---
        if (speedAttribute != null && speedAttribute.hasModifier(stopModifier)) {
            speedAttribute.removeModifier(PETTING_STOP_UUID);
        }
        if (flyingSpeedAttribute != null && flyingSpeedAttribute.hasModifier(stopModifier)) {
            flyingSpeedAttribute.removeModifier(PETTING_STOP_UUID);
        }

        if (!data.contains("ownerUUID")) return;
        
        String ownerUUIDStr = data.getString("ownerUUID");
        if (ownerUUIDStr.isEmpty()) return;

        UUID ownerUUID;
        try {
            ownerUUID = UUID.fromString(ownerUUIDStr);
        } catch (IllegalArgumentException e) { return; }

        Player owner = world.getPlayerByUUID(ownerUUID);
        if (owner == null) return;

        double distanceToOwner = mob.distanceTo(owner);
        
        double followDist = data.contains("followdistance") ? data.getInt("followdistance") : PettingConfig.followDistance;
        double teleportDist = data.contains("teleportdistance") ? data.getInt("teleportdistance") : PettingConfig.teleportDistance;
        if (followDist == 0) followDist = PettingConfig.followDistance;
        if (teleportDist == 0) teleportDist = PettingConfig.teleportDistance;

        boolean isFlyer = isUniversalFlyingMob(mob, data);

        if (distanceToOwner >= teleportDist) {
            teleportToOwner(mob, owner, isFlyer);
            mob.getNavigation().stop();
            mob.setDeltaMovement(0, 0, 0); 
        } else if (distanceToOwner > followDist) {
            if (mob.getTarget() == null) {
                if (isFlyer) {
                    mob.getLookControl().setLookAt(owner, 10.0F, (float)mob.getMaxHeadXRot());
                    double targetX = owner.getX();
                    double targetY = owner.getY() + 1.0D; 
                    double targetZ = owner.getZ();
                    mob.getMoveControl().setWantedPosition(targetX, targetY, targetZ, 1.0D);
                    
                    if (mob.tickCount % 20 == 0) { 
                         mob.getNavigation().moveTo(owner, 1.2D);
                    }
                } else {
                    if (mob.tickCount % 10 == 0) {
                        mob.getNavigation().moveTo(owner, 1.25D);
                    }
                }
            }
        }
    }

    private static boolean isUniversalFlyingMob(Mob mob, CompoundTag data) {
        if (data.contains("force_flying") && data.getBoolean("force_flying")) return true;
        if (mob instanceof FlyingAnimal) return true;
        if (mob.getNavigation() instanceof FlyingPathNavigation) return true;
        if (mob.isNoGravity()) return true;

        ResourceLocation registryName = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        if (registryName != null) {
            String lowerName = registryName.toString().toLowerCase();
            if (lowerName.contains("ghast") || lowerName.contains("blaze") || 
                lowerName.contains("phantom") || lowerName.contains("bat") ||
                lowerName.contains("vex") || lowerName.contains("allay")) {
                return true;
            }
        }
        return false;
    }

    private static void teleportToOwner(Mob pet, Player owner, boolean isFlyer) {
        BlockPos ownerPos = owner.blockPosition();
        Level level = pet.level();
        for (int i = 0; i < 10; ++i) {
            int randomX = getRandomNumber(-3, 3);
            int randomY = getRandomNumber(-2, 2); 
            int randomZ = getRandomNumber(-3, 3);
            BlockPos targetPos = ownerPos.offset(randomX, randomY, randomZ);
            if (canTeleportTo(targetPos, level, isFlyer)) {
                pet.teleportTo(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);
                pet.getNavigation().stop();
                return;
            }
        }
    }

    private static boolean canTeleportTo(BlockPos pos, Level level, boolean isFlyer) {
        if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) return false;
        if (!level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()) return false;
        if (isFlyer) return true;
        BlockState blockBelow = level.getBlockState(pos.below());
        return !blockBelow.getCollisionShape(level, pos.below()).isEmpty(); 
    }

    private static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }
}

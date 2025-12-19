package net.ryukazan.petting;

import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.FlyingAnimal; 
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation; 
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

@EventBusSubscriber
public class FollowOwnerOrTeleport {
    public FollowOwnerOrTeleport() {
    }

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        new FollowOwnerOrTeleport();
    }

    @SubscribeEvent
    public static void clientLoad(FMLClientSetupEvent event) {
    }

    @EventBusSubscriber
    private static class FollowOwnerOrTeleportForgeBusEvents {
        
        @SubscribeEvent
        public static void serverLoad(ServerStartingEvent event) {
        }

        @SubscribeEvent
        public static void onEntityTick(EntityTickEvent.Pre event) {
            Entity entity = event.getEntity();
            Level world = entity.level();

            if (world.isClientSide() || !(entity instanceof Mob mob)) {
                return;
            }

            CompoundTag data = entity.getPersistentData();

            // Check Tamed Status
            if (!data.contains("pettingtamed") || !data.getBoolean("pettingtamed")) {
                return;
            }

            // --- SITTING LOGIC ---
            if (data.contains("sitstill") && data.getBoolean("sitstill")) {
                mob.getNavigation().stop();
                mob.getMoveControl().setWantedPosition(mob.getX(), mob.getY(), mob.getZ(), 0.0);

                if (isUniversalFlyingMob(mob, data)) {
                    Vec3 currentVel = mob.getDeltaMovement();
                    double newY = (currentVel.y < 0) ? 0.0 : currentVel.y * 0.8;
                    mob.setDeltaMovement(currentVel.x * 0.8, newY, currentVel.z * 0.8);
                } 
                else {
                    mob.setDeltaMovement(0, mob.getDeltaMovement().y, 0); 
                }
                return;
            }

            // Get Owner
            if (!data.contains("ownerUUID")) {
                return;
            }
            
            String ownerUUIDStr = data.getString("ownerUUID");
            if (ownerUUIDStr.isEmpty()) return;

            UUID ownerUUID;
            try {
                ownerUUID = UUID.fromString(ownerUUIDStr);
            } catch (IllegalArgumentException e) {
                return; 
            }

            Player owner = world.getPlayerByUUID(ownerUUID);
            if (owner == null) return;

            // --- DISTANCE SETTINGS ---
            // CHANGED: Default follow distance reduced from 15 to 6 so they actually follow.
            int startFollowDist = data.contains("followdistance") ? data.getInt("followdistance") : 6;
            int stopFollowDist = 2; // Stop when very close
            int teleportDist = data.contains("teleportdistance") ? data.getInt("teleportdistance") : 20;

            double distanceToOwner = mob.distanceTo(owner);
            boolean isFlyer = isUniversalFlyingMob(mob, data);

            // 1. TELEPORT (Too far)
            if (distanceToOwner >= teleportDist) {
                teleportToOwner(mob, owner, isFlyer);
                mob.getNavigation().stop();
                mob.setDeltaMovement(0, 0, 0); 
            } 
            // 2. STOP WALKING (Too close)
            else if (distanceToOwner < stopFollowDist) {
                mob.getNavigation().stop();
                mob.getMoveControl().setWantedPosition(mob.getX(), mob.getY(), mob.getZ(), 0.0);
            }
            // 3. WALK/FLY TO OWNER (In between)
            else if (distanceToOwner > startFollowDist) {
                
                // PERFORMANCE FIX: Only update path every 10 ticks (0.5 seconds) 
                // Updating every tick causes stuttering and lag.
                if (mob.tickCount % 10 == 0 || mob.getNavigation().isDone()) {

                    if (isFlyer) {
                        // --- FLYING LOGIC ---
                        mob.getLookControl().setLookAt(owner, 10.0F, (float)mob.getMaxHeadXRot());
                        
                        double targetX = owner.getX();
                        double targetY = owner.getY() + 1.5D; // Aim slightly above head
                        double targetZ = owner.getZ();

                        mob.getMoveControl().setWantedPosition(targetX, targetY, targetZ, 1.2D);
                        
                        // Fallback navigation
                        mob.getNavigation().moveTo(owner, 1.2D);

                    } else {
                        // --- GROUND LOGIC ---
                        // Speed 1.2D is a nice trotting speed
                        mob.getNavigation().moveTo(owner, 1.2D);
                    }
                }
            }
        }

        private static boolean isUniversalFlyingMob(Mob mob, CompoundTag data) {
            if (data.contains("force_flying") && data.getBoolean("force_flying")) {
                return true;
            }

            if (mob instanceof FlyingAnimal) return true;
            if (mob.getNavigation() instanceof FlyingPathNavigation) return true;
            if (mob.isNoGravity()) return true;

            String registryName = mob.getEncodeId(); 
            if (registryName == null) registryName = mob.getType().getDescriptionId(); 
            
            if (registryName != null) {
                String lowerName = registryName.toLowerCase();
                if (lowerName.contains("ghast") || 
                    lowerName.contains("blaze") || 
                    lowerName.contains("phantom") || 
                    lowerName.contains("bat") ||
                    lowerName.contains("vex") ||
                    lowerName.contains("allay")) {
                    return true;
                }
            }

            return false;
        }

        private static void teleportToOwner(Mob pet, Player owner, boolean isFlyer) {
            BlockPos ownerPos = owner.blockPosition();
            Level level = pet.level();
            
            for (int i = 0; i < 10; ++i) {
                int randomX = getRandomNumber(-2, 2); // Reduced range for closer teleport
                int randomY = getRandomNumber(-1, 1); 
                int randomZ = getRandomNumber(-2, 2);

                BlockPos targetPos = ownerPos.offset(randomX, randomY, randomZ);

                if (canTeleportTo(targetPos, level, isFlyer)) {
                    pet.teleportTo(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);
                    pet.getNavigation().stop();
                    
                    // Reset fall distance so they don't take damage if they were falling before teleport
                    pet.fallDistance = 0; 
                    return;
                }
            }
        }

        private static boolean canTeleportTo(BlockPos pos, Level level, boolean isFlyer) {
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                return false;
            }
            if (!level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()) {
                return false;
            }
            if (isFlyer) {
                return true;
            }
            BlockState blockBelow = level.getBlockState(pos.below());
            // Must have solid ground below
            return !blockBelow.getCollisionShape(level, pos.below()).isEmpty(); 
        }

        private static int getRandomNumber(int min, int max) {
            return (int) ((Math.random() * (max - min)) + min);
        }
    }
}
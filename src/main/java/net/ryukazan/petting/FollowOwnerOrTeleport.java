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
import net.minecraft.world.entity.ai.control.MoveControl;
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

            // 1. Logic only runs on Server Side and only for Mobs
            if (world.isClientSide() || !(entity instanceof Mob mob)) {
                return;
            }

            // 2. Retrieve Custom Data
            CompoundTag data = entity.getPersistentData();

            // FIXED: Removed .orElse(false)
            if (!data.contains("pettingtamed") || !data.getBoolean("pettingtamed")) {
                return;
            }

            // --- SITTING LOGIC START ---
            // FIXED: Removed .orElse(false)
            if (data.contains("sitstill") && data.getBoolean("sitstill")) {
                
                // A. Stop Pathfinding (Brains)
                mob.getNavigation().stop();
                
                // B. Stop Movement Control (The Engine)
                mob.getMoveControl().setWantedPosition(mob.getX(), mob.getY(), mob.getZ(), 0.0);

                // C. Physics Stabilization (The Body)
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
            // --- SITTING LOGIC END ---

            // 3. Get Owner
            if (!data.contains("ownerUUID")) {
                return;
            }
            
            // FIXED: Removed .orElse("")
            String ownerUUIDStr = data.getString("ownerUUID");
            if (ownerUUIDStr.isEmpty()) return;

            UUID ownerUUID;
            try {
                ownerUUID = UUID.fromString(ownerUUIDStr);
            } catch (IllegalArgumentException e) {
                return; 
            }

            Player owner = world.getPlayerByUUID(ownerUUID);

            if (owner == null) {
                return;
            }

            // 4. Calculate Distances
            double distanceToOwner = mob.distanceTo(owner);
            
            // FIXED: Removed .orElse(15) and .orElse(20)
            int followDist = data.contains("followdistance") ? data.getInt("followdistance") : 10;
            int teleportDist = data.contains("teleportdistance") ? data.getInt("teleportdistance") : 20;

            // Determine if this specific mob should behave like a flyer
            boolean isFlyer = isUniversalFlyingMob(mob, data);

            // 5. Teleport Logic (Priority)
            if (distanceToOwner >= teleportDist) {
                teleportToOwner(mob, owner, isFlyer);
                mob.getNavigation().stop();
                mob.setDeltaMovement(0, 0, 0); 
            } 
            // 6. Follow Logic
            else if (distanceToOwner > followDist) {
                
                if (isFlyer) {
                    // --- FLYING LOGIC ---
                    mob.getLookControl().setLookAt(owner, 10.0F, (float)mob.getMaxHeadXRot());
                    
                    double targetX = owner.getX();
                    double targetY = owner.getY() + 1.0D; 
                    double targetZ = owner.getZ();

                    mob.getMoveControl().setWantedPosition(targetX, targetY, targetZ, 1.0D);
                    
                    if (mob.tickCount % 20 == 0) { 
                         mob.getNavigation().moveTo(owner, 1.2D);
                    }

                } else {
                    // --- GROUND LOGIC ---
                    mob.getNavigation().moveTo(owner, 1.2D);
                }
            }
        }

        /**
         * A Universal check to see if a mob should be treated as a flying entity.
         */
        private static boolean isUniversalFlyingMob(Mob mob, CompoundTag data) {
            // FIXED: Removed .orElse(false)
            if (data.contains("force_flying") && data.getBoolean("force_flying")) {
                return true;
            }

            // 2. Standard Flying Interfaces
            if (mob instanceof FlyingAnimal) return true;
            if (mob.getNavigation() instanceof FlyingPathNavigation) return true;

            // 3. Physics Check
            if (mob.isNoGravity()) return true;

            // 4. String/ID Check
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

        /**
         * Helper function to teleport the pet safely near the owner.
         */
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

        /**
         * Checks if a block position is safe.
         */
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
            return !blockBelow.getCollisionShape(level, pos.below()).isEmpty(); 
        }

        private static int getRandomNumber(int min, int max) {
            return (int) ((Math.random() * (max - min)) + min);
        }
    }
}
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
import net.minecraft.world.entity.animal.FlyingAnimal; // Generic interface, safe to keep
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation; // Generic navigation, safe to keep
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;

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

            if (!data.contains("pettingtamed") || !data.getBoolean("pettingtamed").orElse(false)) {
                return;
            }

            // Check if sitting
            if (data.contains("sitstill") && data.getBoolean("sitstill").orElse(false)) {
                mob.getNavigation().stop();
                // Force stop for flyers who might drift
                mob.getMoveControl().setWantedPosition(mob.getX(), mob.getY(), mob.getZ(), 0.0);
                return;
            }

            // 3. Get Owner
            if (!data.contains("ownerUUID")) {
                return;
            }
            
            String ownerUUIDStr = data.getString("ownerUUID").orElse("");
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
            
            int followDist = data.contains("followdistance") ? data.getInt("followdistance").orElse(15) : 15;
            int teleportDist = data.contains("teleportdistance") ? data.getInt("teleportdistance").orElse(20) : 20;

            // Determine if this specific mob should behave like a flyer
            boolean isFlyer = isUniversalFlyingMob(mob, data);

            // 5. Teleport Logic (Priority)
            if (distanceToOwner >= teleportDist) {
                teleportToOwner(mob, owner, isFlyer);
                mob.getNavigation().stop();
                mob.setDeltaMovement(0, 0, 0); // Kill momentum
            } 
            // 6. Follow Logic
            else if (distanceToOwner > followDist) {
                
                if (isFlyer) {
                    // --- FLYING LOGIC ---
                    // Used for Mobs that don't walk (Ghasts, Blazes, Modded Dragons)
                    
                    // 1. Look at the owner
                    mob.getLookControl().setLookAt(owner, 10.0F, (float)mob.getMaxHeadXRot());
                    
                    // 2. Direct Velocity Control
                    // Pathfinding often fails for flyers in open air. We use MoveControl to "push" them.
                    // Target: 1 block above owner's feet (approx head height)
                    double targetX = owner.getX();
                    double targetY = owner.getY() + 1.0D; 
                    double targetZ = owner.getZ();

                    // Speed 1.0 is standard movement speed
                    mob.getMoveControl().setWantedPosition(targetX, targetY, targetZ, 1.0D);
                    
                    // Fallback: If they are stuck using MoveControl, try old nav just in case
                    if (mob.tickCount % 20 == 0) { // Every second
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
         * Works for Vanilla and Modded mobs without hardcoding imports.
         */
        private static boolean isUniversalFlyingMob(Mob mob, CompoundTag data) {
            // 1. NBT Override: You can set "force_flying: true" on any entity to force this logic
            if (data.contains("force_flying") && data.getBoolean("force_flying").orElse(false)) {
                return true;
            }

            // 2. Standard Flying Interfaces (e.g., Parrots, Bees, Modded Birds)
            if (mob instanceof FlyingAnimal) return true;
            if (mob.getNavigation() instanceof FlyingPathNavigation) return true;

            // 3. Physics Check: If gravity is disabled, it's likely a flyer or hoverer
            if (mob.isNoGravity()) return true;

            // 4. String/ID Check (The "Catch-All" for Blazes, Ghasts, and Modded variants)
            // We check the registry name string. This catches "minecraft:ghast", "some_mod:fire_ghast", "some_mod:blaze_knight"
            String registryName = mob.getEncodeId(); 
            if (registryName == null) registryName = mob.getType().getDescriptionId(); // Fallback
            
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
            
            // Try 10 times to find a valid spot
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
            // 1. The block we are teleporting INTO must be non-collidable (air)
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                return false;
            }
            
            // 2. The block ABOVE must also be non-collidable (head space)
            if (!level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()) {
                return false;
            }

            // 3. SPECIAL LOGIC FOR FLYING:
            // If the mob is a flyer, we ignore the ground check.
            if (isFlyer) {
                return true;
            }

            // 4. Standard Logic (Ground pets need a solid block below)
            BlockState blockBelow = level.getBlockState(pos.below());
            return !blockBelow.getCollisionShape(level, pos.below()).isEmpty(); 
        }

        private static int getRandomNumber(int min, int max) {
            return (int) ((Math.random() * (max - min)) + min);
        }
    }
}
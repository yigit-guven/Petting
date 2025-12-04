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

            // 1. Logic only runs on Server Side and only for Mobs (entities with AI)
            if (world.isClientSide() || !(entity instanceof Mob mob)) {
                return;
            }

            // 2. Retrieve Custom Data
            CompoundTag data = entity.getPersistentData();

            // FIX: Using .orElse(false) because your environment returns Optional<Boolean>
            if (!data.contains("pettingtamed") || !data.getBoolean("pettingtamed").orElse(false)) {
                return;
            }

            // Check if sitting (SitStill logic)
            if (data.contains("sitstill") && data.getBoolean("sitstill").orElse(false)) {
                // If sitting, ensure we stop moving and return
                mob.getNavigation().stop();
                return;
            }

            // 3. Get Owner
            if (!data.contains("ownerUUID")) {
                return;
            }
            
            // FIX: Using .orElse("") for Optional<String>
            String ownerUUIDStr = data.getString("ownerUUID").orElse("");
            if (ownerUUIDStr.isEmpty()) return;

            UUID ownerUUID;
            try {
                ownerUUID = UUID.fromString(ownerUUIDStr);
            } catch (IllegalArgumentException e) {
                return; // Invalid UUID string
            }

            // Find the owner in the world
            Player owner = world.getPlayerByUUID(ownerUUID);

            // If owner is null (offline or different dimension), stop logic
            if (owner == null) {
                return;
            }

            // 4. Calculate Distances
            double distanceToOwner = mob.distanceTo(owner);
            
            // FIX: Using .orElse(15) for Optional<Integer>
            int followDist = data.contains("followdistance") ? data.getInt("followdistance").orElse(15) : 15;
            int teleportDist = data.contains("teleportdistance") ? data.getInt("teleportdistance").orElse(20) : 20;

            // 5. Teleport Logic (Priority over walking)
            if (distanceToOwner >= teleportDist) {
                teleportToOwner(mob, owner);
                // Stop navigation immediately after teleport so they don't walk back to old target
                mob.getNavigation().stop();
            } 
            // 6. Follow Logic
            else if (distanceToOwner > followDist) {
                // Move to owner at speed 1.0 (standard run speed)
                mob.getNavigation().moveTo(owner, 1.2D);
            }
        }

        /**
         * Helper function to teleport the pet safely near the owner.
         */
        private static void teleportToOwner(Mob pet, Player owner) {
            BlockPos ownerPos = owner.blockPosition();
            Level level = pet.level();
            
            // Try 10 times to find a valid spot around the owner
            for (int i = 0; i < 10; ++i) {
                int randomX = getRandomNumber(-3, 3);
                int randomY = getRandomNumber(-1, 1);
                int randomZ = getRandomNumber(-3, 3);

                BlockPos targetPos = ownerPos.offset(randomX, randomY, randomZ);

                if (canTeleportTo(targetPos, level)) {
                    // Center the entity in the block
                    pet.teleportTo(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);
                    // Reset pathfinding so it doesn't try to walk back
                    pet.getNavigation().stop();
                    return;
                }
            }
        }

        /**
         * Checks if a block position is safe for the pet to stand on.
         * Uses manual block checks instead of internal Pathfinding classes to avoid version conflicts.
         */
        private static boolean canTeleportTo(BlockPos pos, Level level) {
            // 1. The block we are teleporting INTO must be non-collidable (usually air or grass)
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                return false;
            }
            
            // 2. The block ABOVE must also be non-collidable (head space)
            if (!level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()) {
                return false;
            }

            // 3. The block BELOW must be solid (something to stand on)
            BlockState blockBelow = level.getBlockState(pos.below());
            
            // FIX: Use collision shape check instead of isSolidRender with arguments
            // If the collision shape is NOT empty, it means there is something solid to stand on.
            return !blockBelow.getCollisionShape(level, pos.below()).isEmpty(); 
        }

        private static int getRandomNumber(int min, int max) {
            return (int) ((Math.random() * (max - min)) + min);
        }
    }
}
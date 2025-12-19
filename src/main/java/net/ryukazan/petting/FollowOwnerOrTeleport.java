package net.ryukazan.petting;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.FlyingAnimal; 
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation; 
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class FollowOwnerOrTeleport {
    public FollowOwnerOrTeleport() {}

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        new FollowOwnerOrTeleport();
    }

    @SubscribeEvent
    public static void clientLoad(FMLClientSetupEvent event) {}

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    private static class FollowOwnerOrTeleportForgeBusEvents {
        
        @SubscribeEvent
        public static void serverLoad(ServerStartingEvent event) {}

        @SubscribeEvent
        public static void onEntityTick(LivingEvent.LivingTickEvent event) {
            Entity entity = event.getEntity();
            Level world = entity.level();

            // Safety checks
            if (world.isClientSide() || !(entity instanceof Mob mob)) return;

            CompoundTag data = entity.getPersistentData();

            if (!data.contains("pettingtamed") || !data.getBoolean("pettingtamed")) {
                return;
            }

            // --- SIT LOGIC ---
            if (data.contains("sitstill") && data.getBoolean("sitstill")) {
                mob.getNavigation().stop();
                mob.getMoveControl().setWantedPosition(mob.getX(), mob.getY(), mob.getZ(), 0.0);

                if (isUniversalFlyingMob(mob, data)) {
                    Vec3 currentVel = mob.getDeltaMovement();
                    double newY = (currentVel.y < 0) ? 0.0 : currentVel.y * 0.8;
                    mob.setDeltaMovement(currentVel.x * 0.8, newY, currentVel.z * 0.8);
                } else {
                    mob.setDeltaMovement(0, mob.getDeltaMovement().y, 0); 
                }
                return;
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
            
            int followDist = data.contains("followdistance") ? data.getInt("followdistance") : 15;
            int teleportDist = data.contains("teleportdistance") ? data.getInt("teleportdistance") : 20;

            boolean isFlyer = isUniversalFlyingMob(mob, data);

            // 1. TELEPORT (Priority High)
            if (distanceToOwner >= teleportDist) {
                teleportToOwner(mob, owner, isFlyer);
                mob.getNavigation().stop();
                mob.setDeltaMovement(0, 0, 0); 
            } 
            // 2. FOLLOW
            else if (distanceToOwner > followDist) {
                if (isFlyer) {
                    // Flying Logic (Can be run every tick safely)
                    mob.getLookControl().setLookAt(owner, 10.0F, (float)mob.getMaxHeadXRot());
                    double targetX = owner.getX();
                    double targetY = owner.getY() + 1.0D; 
                    double targetZ = owner.getZ();
                    mob.getMoveControl().setWantedPosition(targetX, targetY, targetZ, 1.0D);
                    
                    if (mob.tickCount % 20 == 0) { 
                         mob.getNavigation().moveTo(owner, 1.2D);
                    }
                } else {
                    // GROUND LOGIC (The Fix)
                    // Only tell it to walk once every 10 ticks (0.5 seconds)
                    // This prevents pathfinding resets and lag
                    if (mob.tickCount % 10 == 0) {
                        mob.getNavigation().moveTo(owner, 1.25D); // Slightly faster speed (1.25)
                    }
                }
            }
        }

        private static boolean isUniversalFlyingMob(Mob mob, CompoundTag data) {
            if (data.contains("force_flying") && data.getBoolean("force_flying")) return true;
            if (mob instanceof FlyingAnimal) return true;
            if (mob.getNavigation() instanceof FlyingPathNavigation) return true;
            if (mob.isNoGravity()) return true;

            String registryName = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType()).toString();
            if (registryName != null) {
                String lowerName = registryName.toLowerCase();
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
}
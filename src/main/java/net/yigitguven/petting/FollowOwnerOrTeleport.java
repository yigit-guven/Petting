package net.yigitguven.petting;

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
            
            // --- GLOBAL BLACKLIST CHECK ---
            if (net.yigitguven.petting.util.PetInventoryUtil.isBlacklisted(entity)) return;

            CompoundTag data = entity.getPersistentData();

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
                // --- DYNAMIC TELEPORT THRESHOLD ---
                // If extremely far (e.g. owner teleported it away), teleport back
                double teleportThresholdSq = Math.max(900.0, Math.pow(roamRadius * 1.5, 2)); // Minimum 30 blocks squared
                if (distToBound > teleportThresholdSq) {
                    mob.teleportTo(bX, bY, bZ);
                    mob.getNavigation().stop();
                } 
                // If outside roaming radius, walk back
                else if (distToBound > roamRadius * roamRadius) {
                    if (mob.tickCount % 20 == 0) {
                        mob.getNavigation().moveTo(bX, bY, bZ, 1.0D);
                    }
                }
                
                // Skip the follow owner logic while bound
                return;
            }

            // Fetch Movement Speed Attribute
            net.minecraft.world.entity.ai.attributes.AttributeInstance speedAttribute = mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
            net.minecraft.world.entity.ai.attributes.AttributeInstance flyingSpeedAttribute = mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.FLYING_SPEED);
            
            // Define a unique modifier ID for Petting Stop
            final UUID PETTING_STOP_UUID = UUID.fromString("11111111-2222-3333-4444-555555555555");
            net.minecraft.world.entity.ai.attributes.AttributeModifier stopModifier = new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                PETTING_STOP_UUID, "Petting Stop", -1.0D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL);

            // --- SIT LOGIC ---
            if (data.contains("sitstill") && data.getBoolean("sitstill") && !mob.isVehicle()) {
                mob.getNavigation().stop();
                mob.getMoveControl().setWantedPosition(mob.getX(), mob.getY(), mob.getZ(), 0.0);
                
                // SPIDER/CLIMBER FIX: Physically zero out movement inputs so they can't shimmy or climb walls
                mob.setZza(0.0f); // Forward
                mob.setXxa(0.0f); // Strafe
                mob.setYya(0.0f); // Up/Jump/Climb
                mob.setSpeed(0.0f);

                // Apply Stop Modifier if not present
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

                // Configurable Healing while sitting
                int healInterval = net.yigitguven.petting.config.PettingConfig.SIT_HEAL_INTERVAL.get();
                if (healInterval > 0 && mob.tickCount % healInterval == 0 && mob.getHealth() < mob.getMaxHealth()) {
                    mob.heal(net.yigitguven.petting.config.PettingConfig.SIT_HEAL_AMOUNT.get().floatValue());
                }
                return;
            }

            // --- WAITING LOGIC ---
            if (data.contains("waiting") && data.getBoolean("waiting") && !mob.isVehicle()) {
                mob.getNavigation().stop();
                mob.getMoveControl().setWantedPosition(mob.getX(), mob.getY(), mob.getZ(), 0.0);

                // SPIDER/CLIMBER FIX: 
                mob.setZza(0.0f); 
                mob.setXxa(0.0f); 
                mob.setYya(0.0f); 
                mob.setSpeed(0.0f);

                // Apply Stop Modifier if not present
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

                // Head track owner or nearest player if waiting
                Player nearest = world.getNearestPlayer(mob, 10.0D);
                if (nearest != null) {
                    mob.getLookControl().setLookAt(nearest, 10.0F, (float)mob.getMaxHeadXRot());
                }
                return;
            }

            // --- WANDERING RESTORE LOGIC ---
            // Remove the stop modifier if it exists so the pet can walk again
            if (speedAttribute != null && speedAttribute.hasModifier(stopModifier)) {
                speedAttribute.removeModifier(PETTING_STOP_UUID);
            }
            if (flyingSpeedAttribute != null && flyingSpeedAttribute.hasModifier(stopModifier)) {
                flyingSpeedAttribute.removeModifier(PETTING_STOP_UUID);
            }

            // --- FREE WANDER LOGIC ---
            if (data.getBoolean("freewander")) {
                return; // Skip follow logic and let the natural AI wander
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
            
            double followDist = data.contains("followdistance") ? data.getInt("followdistance") : net.yigitguven.petting.config.PettingConfig.FOLLOW_DISTANCE.get();
            double teleportDist = data.contains("teleportdistance") ? data.getInt("teleportdistance") : net.yigitguven.petting.config.PettingConfig.TELEPORT_DISTANCE.get();
            if (followDist == 0) followDist = net.yigitguven.petting.config.PettingConfig.FOLLOW_DISTANCE.get();
            if (teleportDist == 0) teleportDist = net.yigitguven.petting.config.PettingConfig.TELEPORT_DISTANCE.get();

            boolean isFlyer = isUniversalFlyingMob(mob, data);

            // 1. TELEPORT (Priority High)
            if (distanceToOwner >= teleportDist) {
                teleportToOwner(mob, owner, isFlyer);
                mob.getNavigation().stop();
                mob.setDeltaMovement(0, 0, 0); 
            } 
            // 2. FOLLOW (Only if not actively fighting a target!)
            else if (distanceToOwner > followDist) {
                if (mob.getTarget() == null) {
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
                        // GROUND LOGIC 
                        // Only tell it to walk once every 10 ticks (0.5 seconds)
                        if (mob.tickCount % 10 == 0) {
                            mob.getNavigation().moveTo(owner, 1.25D); // Slightly faster speed
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

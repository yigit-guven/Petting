package net.yigitguven.petting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.yigitguven.petting.config.PettingConfig;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

public class PetAttackLogic {

    private static final UUID PETTING_STOP_UUID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final AttributeModifier STOP_MODIFIER = new AttributeModifier(
        PETTING_STOP_UUID, "Petting Stop", -1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);

    public static void tick(Mob pet) {
        if (pet.level().isClientSide()) return;

        if (isCustomPet(pet)) {
            Player owner = getOwner(pet);
            if (owner == null) return;

            IEntityData dataAccess = (IEntityData) pet;
            CompoundTag data = dataAccess.getPersistentData();

            // --- BOUND ROAM LOGIC ---
            if (data.getBoolean("pettingbound")) {
                handleBoundRoam(pet, data);
            } else {
                handleAI(pet, owner, data);
            }
        }
    }

    private static void handleBoundRoam(Mob pet, CompoundTag data) {
        double bX = data.getDouble("boundX");
        double bY = data.getDouble("boundY");
        double bZ = data.getDouble("boundZ");
        Vec3 boundPos = new Vec3(bX, bY, bZ);
        double distToBound = pet.distanceToSqr(boundPos);
        double roamRadius = PettingConfig.boundRoamRadius;
        
        if (distToBound > 400.0) {
            pet.teleportTo(bX, bY, bZ);
            pet.getNavigation().stop();
        } else if (distToBound > roamRadius * roamRadius) {
            if (pet.tickCount % 20 == 0) {
                pet.getNavigation().moveTo(bX, bY, bZ, 1.0D);
            }
        }
    }

    private static void handleAI(Mob pet, Player owner, CompoundTag data) {
        LivingEntity currentTarget = pet.getTarget();
        if (currentTarget != null) {
            if (currentTarget == pet || currentTarget == owner || isOwnerOf(currentTarget, owner)) {
                pet.setTarget(null);
            }
        }

        // Vanilla Pacifism
        List<Mob> nearbyHostiles = pet.level().getEntitiesOfClass(Mob.class, pet.getBoundingBox().inflate(16.0D), 
            mob -> mob.getTarget() == pet);
        for (Mob aggressor : nearbyHostiles) {
            if (isOwnerOf(aggressor, owner)) {
                aggressor.setTarget(null);
            }
        }

        AttributeInstance speed = pet.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance flySpeed = pet.getAttribute(Attributes.FLYING_SPEED);

        // SIT/WAIT logic
        boolean isSitting = data.getBoolean("sitstill");
        boolean isWaiting = data.getBoolean("waiting");

        if (isSitting || isWaiting) {
            pet.getNavigation().stop();
            pet.getMoveControl().setWantedPosition(pet.getX(), pet.getY(), pet.getZ(), 0.0);
            pet.setZza(0.0f);
            pet.setXxa(0.0f);
            pet.setYya(0.0f);
            pet.setSpeed(0.0f);

            if (speed != null && !speed.hasModifier(STOP_MODIFIER)) speed.addTransientModifier(STOP_MODIFIER);
            if (flySpeed != null && !flySpeed.hasModifier(STOP_MODIFIER)) flySpeed.addTransientModifier(STOP_MODIFIER);

            if (isUniversalFlyingMob(pet, data)) {
                Vec3 vel = pet.getDeltaMovement();
                pet.setDeltaMovement(vel.x * 0.8, (vel.y < 0) ? 0.0 : vel.y * 0.8, vel.z * 0.8);
            } else {
                pet.setDeltaMovement(0, pet.getDeltaMovement().y, 0); 
            }

            if (isSitting) {
                int healInterval = PettingConfig.sitHealInterval;
                if (healInterval > 0 && pet.tickCount % healInterval == 0 && pet.getHealth() < pet.getMaxHealth()) {
                    pet.heal((float) PettingConfig.sitHealAmount);
                }
            } else { // Waiting
                Player nearest = pet.level().getNearestPlayer(pet, 10.0D);
                if (nearest != null) pet.getLookControl().setLookAt(nearest, 10.0F, (float)pet.getMaxHeadXRot());
            }
            return;
        }

        // Restore movement
        if (speed != null && speed.hasModifier(STOP_MODIFIER)) speed.removeModifier(PETTING_STOP_UUID);
        if (flySpeed != null && flySpeed.hasModifier(STOP_MODIFIER)) flySpeed.removeModifier(PETTING_STOP_UUID);

        // Combat/Follow
        LivingEntity forcedTarget = null;
        boolean attackWithOwner = data.getBoolean("attackifownerattacks");
        boolean defendOwner = data.getBoolean("attackifownerattacked");

        if (attackWithOwner && owner.getLastHurtMob() != null) {
            forcedTarget = owner.getLastHurtMob();
        } else if (defendOwner && owner.getLastHurtByMob() != null) {
            forcedTarget = owner.getLastHurtByMob();
        } else if (pet.getLastHurtByMob() != null) {
            forcedTarget = pet.getLastHurtByMob();
        }

        if (forcedTarget != null && isValidCombatTarget(pet, owner, forcedTarget)) {
            if (pet.getTarget() != forcedTarget) pet.setTarget(forcedTarget);
            
            if (pet instanceof net.minecraft.world.entity.monster.warden.Warden warden) {
                warden.increaseAngerAt(forcedTarget, 100, true);
                warden.getBrain().setActiveActivityIfPossible(net.minecraft.world.entity.schedule.Activity.FIGHT);
                pet.getBrain().setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET, forcedTarget);
            }
        } else {
            pet.setTarget(null);
            
            if (pet instanceof net.minecraft.world.entity.monster.warden.Warden warden) {
                pet.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET);
                pet.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ANGRY_AT);
                pet.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ROAR_TARGET);
                pet.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.DISTURBANCE_LOCATION);
                if (currentTarget != null) {
                    warden.clearAnger(currentTarget);
                }
                warden.clearAnger(owner);
            }

            handleFollow(pet, owner, data);

            if (pet instanceof net.minecraft.world.entity.boss.wither.WitherBoss wither) {
                wither.setAlternativeTarget(1, 0);
                wither.setAlternativeTarget(2, 0);
                if (PettingConfig.hideTamedBossBars) {
                    try {
                        Field bossEventField = net.minecraft.world.entity.boss.wither.WitherBoss.class.getDeclaredField("bossEvent");
                        bossEventField.setAccessible(true);
                        ServerBossEvent bossEvent = (ServerBossEvent) bossEventField.get(wither);
                        if (bossEvent != null) {
                            bossEvent.removeAllPlayers();
                            bossEvent.setVisible(false);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    private static void handleFollow(Mob pet, Player owner, CompoundTag data) {
        double dist = pet.distanceTo(owner);
        double followDist = data.contains("followdistance") ? data.getInt("followdistance") : PettingConfig.followDistance;
        double teleDist = data.contains("teleportdistance") ? data.getInt("teleportdistance") : PettingConfig.teleportDistance;
        if (followDist == 0) followDist = PettingConfig.followDistance;
        if (teleDist == 0) teleDist = PettingConfig.teleportDistance;

        boolean isFlyer = isUniversalFlyingMob(pet, data);

        if (dist >= teleDist) {
            teleportToOwner(pet, owner, isFlyer);
            pet.getNavigation().stop();
            pet.setDeltaMovement(0, 0, 0); 
        } else if (dist > followDist) {
            if (pet.getTarget() == null) {
                if (isFlyer) {
                    pet.getLookControl().setLookAt(owner, 10.0F, (float)pet.getMaxHeadXRot());
                    pet.getMoveControl().setWantedPosition(owner.getX(), owner.getY() + 1.0D, owner.getZ(), 1.0D);
                    if (pet.tickCount % 20 == 0) pet.getNavigation().moveTo(owner, 1.2D);
                } else {
                    if (pet.tickCount % 10 == 0) pet.getNavigation().moveTo(owner, 1.25D);
                }
            }
        }
    }

    private static boolean isUniversalFlyingMob(Mob mob, CompoundTag data) {
        if (data.contains("force_flying") && data.getBoolean("force_flying")) return true;
        if (mob instanceof FlyingAnimal) return true;
        if (mob.getNavigation() instanceof FlyingPathNavigation) return true;
        if (mob.isNoGravity()) return true;

        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        if (id != null) {
            String path = id.getPath().toLowerCase();
            return path.contains("ghast") || path.contains("blaze") || path.contains("phantom") || 
                   path.contains("bat") || path.contains("vex") || path.contains("allay");
        }
        return false;
    }

    private static void teleportToOwner(Mob pet, Player owner, boolean isFlyer) {
        BlockPos ownerPos = owner.blockPosition();
        Level level = pet.level();
        for (int i = 0; i < 10; ++i) {
            BlockPos targetPos = ownerPos.offset(getRandom(-3, 3), getRandom(-2, 2), getRandom(-3, 3));
            if (canTeleport(targetPos, level, isFlyer)) {
                pet.teleportTo(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);
                pet.getNavigation().stop();
                return;
            }
        }
    }

    private static boolean canTeleport(BlockPos pos, Level level, boolean isFlyer) {
        if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) return false;
        if (!level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()) return false;
        if (isFlyer) return true;
        return !level.getBlockState(pos.below()).getCollisionShape(level, pos.below()).isEmpty(); 
    }

    private static int getRandom(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    public static boolean isCustomPet(Entity entity) {
        if (!(entity instanceof Mob)) return false;
        return ((IEntityData) entity).getPersistentData().getBoolean("pettingtamed");
    }

    public static boolean isValidCombatTarget(Mob pet, Player owner, LivingEntity potentialTarget) {
        if (potentialTarget == null || potentialTarget == pet) return false;
        if (potentialTarget == owner) return false;
        if (isOwnerOf(potentialTarget, owner)) return false; 
        return true;
    }

    public static Player getOwner(Mob pet) {
        if (pet instanceof net.minecraft.world.entity.TamableAnimal tamable) {
            LivingEntity owner = tamable.getOwner();
            if (owner instanceof Player p) return p;
        }
        String ownerUUIDStr = ((IEntityData) pet).getPersistentData().getString("ownerUUID");
        if (ownerUUIDStr.isEmpty()) return null;
        try { return pet.level().getPlayerByUUID(UUID.fromString(ownerUUIDStr)); } catch (Exception e) { return null; }
    }

    public static boolean isOwnerOf(Entity pet, Entity potentialOwner) {
        if (!(potentialOwner instanceof Player player) || pet == null) return false;
        if (pet instanceof net.minecraft.world.entity.TamableAnimal tamable) {
            if (tamable.isOwnedBy(player)) return true;
        }
        String ownerUUIDStr = ((IEntityData) pet).getPersistentData().getString("ownerUUID");
        if (!ownerUUIDStr.isEmpty()) {
            try { return UUID.fromString(ownerUUIDStr).equals(potentialOwner.getUUID()); } catch (Exception e) { return false; }
        }
        return false;
    }
}

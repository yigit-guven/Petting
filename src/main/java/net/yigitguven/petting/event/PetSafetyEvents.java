package net.yigitguven.petting.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.util.PetHelper;

import java.lang.reflect.Field;

public class PetSafetyEvents {

    private static Field witherIdleHeadUpdatesField = null;
    private static boolean witherFieldLookupFailed = false;

    private static void suppressWitherIdleHeads(WitherBoss wither) {
        if (witherFieldLookupFailed) {
            return;
        }
        try {
            if (witherIdleHeadUpdatesField == null) {
                for (Field f : WitherBoss.class.getDeclaredFields()) {
                    if (f.getName().equals("idleHeadUpdates") || (f.getType() == int[].class && !f.getName().equals("nextHeadUpdate"))) {
                        f.setAccessible(true);
                        witherIdleHeadUpdatesField = f;
                        break;
                    }
                }
            }
            if (witherIdleHeadUpdatesField != null) {
                int[] updates = (int[]) witherIdleHeadUpdatesField.get(wither);
                if (updates != null) {
                    java.util.Arrays.fill(updates, 0);
                }
            }
        } catch (Exception e) {
            witherFieldLookupFailed = true;
        }
    }

    public static void onEntityJoinLevel(Entity entity) {
        if (!(entity instanceof Mob mob) || mob.level().isClientSide()) {
            return;
        }
        if (!PetHelper.isTamed(mob)) {
            LivingEntity rootOwner = PetHelper.getRootOwner(mob);
            if (rootOwner instanceof Player ownerPlayer) {
                PetHelper.setTamed(mob, ownerPlayer);
                mob.setTarget(null);
                if (mob instanceof NeutralMob neutralMob) {
                    neutralMob.stopBeingAngry();
                }
            }
        }
    }

    public static boolean allowDamage(LivingEntity victim, DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        Entity direct = source.getDirectEntity();

        // 0. Peaceful ritual protection: prevent explosion/crystal damage during Golden Dragon summon ritual
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION) && PetHelper.isGoldenDragonRitualActive(victim.level())) {
            return false;
        }

        // 1. Owner protection: pets, minions, projectiles, and spells can never damage their owner
        if (victim instanceof Player player) {
            if ((attacker != null && PetHelper.isRootOwner(attacker, player))
                    || (direct != null && PetHelper.isRootOwner(direct, player))) {
                return false;
            }
        }

        // 2. Configurable: owner attacking their own pets/minions (melee, projectile, or spell)
        LivingEntity rootAttacker = attacker instanceof LivingEntity livingAttacker ? PetHelper.getRootOwner(livingAttacker) : null;
        if (rootAttacker == null && direct instanceof LivingEntity livingDirect) {
            rootAttacker = PetHelper.getRootOwner(livingDirect);
        }
        if (rootAttacker instanceof Player ownerPlayer && PetHelper.isRootOwner(victim, ownerPlayer)) {
            if (!PettingServerConfig.canOwnerAttackPets()) {
                return false;
            }
            return true;
        }

        // 3. Configurable: friendly fire between pets/minions of the same owner
        if (!PettingServerConfig.canPetsAttackPets()) {
            if ((attacker != null && PetHelper.arePetsOfSameOwner(victim, attacker))
                    || (direct != null && PetHelper.arePetsOfSameOwner(victim, direct))) {
                return false;
            }
        }

        // 4. Passenger / vehicle relationship: prevent inadvertent collision/team damage
        if ((attacker != null && (victim.isPassengerOfSameVehicle(attacker) || victim.hasPassenger(attacker) || attacker.hasPassenger(victim)))
                || (direct != null && (victim.isPassengerOfSameVehicle(direct) || victim.hasPassenger(direct) || direct.hasPassenger(victim)))) {
            return false;
        }

        return true;
    }

    public static void afterDamage(LivingEntity victim, DamageSource source, float baseDamageTaken, float damageTaken, boolean blocked) {
        Entity attacker = source.getEntity();
        if (victim instanceof Player player && attacker instanceof LivingEntity threat) {
            PetHelper.alertTamedDragonsOfThreat(player, threat);
        }
        if (attacker instanceof Player ownerPlayer && victim != ownerPlayer) {
            PetHelper.alertTamedDragonsOfTarget(ownerPlayer, victim);
        }
    }

    public static boolean shouldCancelTarget(Mob pet, LivingEntity newTarget) {
        if (newTarget == null) {
            return false;
        }

        // 1. Always: pets and minions can never target their owner
        if (newTarget instanceof Player player && PetHelper.isRootOwner(pet, player)) {
            if (pet instanceof NeutralMob neutralMob) {
                neutralMob.stopBeingAngry();
            }
            return true;
        }

        // 2. Untamed entity with root owner targeting owner or fellow pet
        if (!PetHelper.isTamed(pet)) {
            LivingEntity rootOwner = PetHelper.getRootOwner(pet);
            if (rootOwner instanceof Player player) {
                if (newTarget == player || (!PettingServerConfig.canPetsAttackPets() && PetHelper.arePetsOfSameOwner(pet, newTarget))) {
                    if (pet instanceof NeutralMob neutralMob) {
                        neutralMob.stopBeingAngry();
                    }
                    PetHelper.setTamed(pet, player);
                    return true;
                }
            }
            return false;
        }

        // 3. Prevent built-in hostile behaviors: pets only attack valid targets
        if (PettingServerConfig.shouldDisableBuiltInHostility()) {
            if (!PetHelper.isValidPetTarget(pet, newTarget)) {
                if (pet instanceof NeutralMob neutralMob) {
                    neutralMob.stopBeingAngry();
                }
                return true;
            }
        }

        // 4. Configurable: pets targeting fellow pets of the same owner
        if (!PettingServerConfig.canPetsAttackPets()) {
            if (PetHelper.arePetsOfSameOwner(pet, newTarget)) {
                if (pet instanceof NeutralMob neutralMob) {
                    neutralMob.stopBeingAngry();
                }
                return true;
            }
        }

        return false;
    }

    public static void onEntityTick(Mob mob) {
        if (mob.level().isClientSide()) {
            return;
        }

        if (!PetHelper.isTamed(mob)) {
            LivingEntity target = mob.getTarget();
            if (target instanceof Player player && PetHelper.isRootOwner(mob, player)) {
                mob.setTarget(null);
                PetHelper.setTamed(mob, player);
            }
            return;
        }

        // Manage boss bar visibility for tamed pets
        PetHelper.updatePetBossBarVisibility(mob, !PettingServerConfig.shouldHideTamedBossBars());

        if (!PettingServerConfig.shouldDisableBuiltInHostility()) {
            return;
        }

        // Special handling for Wither Boss (side heads and random idle attacks)
        if (mob instanceof WitherBoss wither) {
            LivingEntity target = wither.getTarget();
            if (target == null || !PetHelper.isValidPetTarget(wither, target)) {
                if (target != null) {
                    wither.setTarget(null);
                }
                wither.setAlternativeTarget(1, 0);
                wither.setAlternativeTarget(2, 0);
            } else {
                int targetId = target.getId();
                wither.setAlternativeTarget(1, targetId);
                wither.setAlternativeTarget(2, targetId);
            }
            suppressWitherIdleHeads(wither);
        }

        // Special handling for Brain-based mobs (Warden, Piglin, etc.)
        if (mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            LivingEntity brainTarget = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
            if (brainTarget != null && !PetHelper.isValidPetTarget(mob, brainTarget)) {
                mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
                if (mob instanceof Warden warden) {
                    warden.clearAnger(brainTarget);
                }
            }
        }

        // Verify current target is valid
        LivingEntity currentTarget = mob.getTarget();
        if (currentTarget != null) {
            if (!PetHelper.isValidPetTarget(mob, currentTarget)) {
                mob.setTarget(null);
                if (mob instanceof NeutralMob neutralMob) {
                    neutralMob.stopBeingAngry();
                }
            } else {
                if (!mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
                    mob.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, currentTarget);
                    if (mob instanceof Warden warden) {
                        warden.setAttackTarget(currentTarget);
                    }
                }
            }
        }
    }
}

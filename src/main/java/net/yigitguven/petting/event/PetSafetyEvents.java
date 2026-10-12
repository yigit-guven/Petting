package net.yigitguven.petting.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.util.PetHelper;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = Petting.MODID)
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

    @SubscribeEvent
    public static void onEntityJoinLevel(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Mob mob)) {
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

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        Entity direct = event.getSource().getDirectEntity();

        // 1. Owner protection: pets, minions, projectiles, and spells can never damage their owner
        if (victim instanceof Player player) {
            if ((attacker != null && PetHelper.isRootOwner(attacker, player))
                    || (direct != null && PetHelper.isRootOwner(direct, player))) {
                event.setCanceled(true);
                return;
            }
        }

        // 2. Configurable: owner attacking their own pets/minions (melee, projectile, or spell)
        LivingEntity rootAttacker = attacker instanceof LivingEntity livingAttacker ? PetHelper.getRootOwner(livingAttacker) : null;
        if (rootAttacker == null && direct instanceof LivingEntity livingDirect) {
            rootAttacker = PetHelper.getRootOwner(livingDirect);
        }
        if (rootAttacker instanceof Player ownerPlayer && PetHelper.isRootOwner(victim, ownerPlayer)) {
            if (!PettingServerConfig.canOwnerAttackPets()) {
                event.setCanceled(true);
            }
            return;
        }

        // 3. Configurable: friendly fire between pets/minions of the same owner
        if (!PettingServerConfig.canPetsAttackPets()) {
            if ((attacker != null && PetHelper.arePetsOfSameOwner(victim, attacker))
                    || (direct != null && PetHelper.arePetsOfSameOwner(victim, direct))) {
                event.setCanceled(true);
                return;
            }
        }

        // 4. Passenger / vehicle relationship: prevent inadvertent collision/team damage
        if ((attacker != null && (victim.isPassengerOfSameVehicle(attacker) || victim.hasPassenger(attacker) || attacker.hasPassenger(victim)))
                || (direct != null && (victim.isPassengerOfSameVehicle(direct) || victim.hasPassenger(direct) || direct.hasPassenger(victim)))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity pet = event.getEntity();
        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (newTarget == null) {
            return;
        }

        // 1. Always: pets and minions can never target their owner
        if (newTarget instanceof Player player && PetHelper.isRootOwner(pet, player)) {
            event.setCanceled(true);
            event.setNewAboutToBeSetTarget(null);
            if (pet instanceof NeutralMob neutralMob) {
                neutralMob.stopBeingAngry();
            }
            return;
        }

        // 2. Untamed entity with root owner targeting owner or fellow pet
        if (!PetHelper.isTamed(pet)) {
            LivingEntity rootOwner = PetHelper.getRootOwner(pet);
            if (rootOwner instanceof Player player) {
                if (newTarget == player || (!PettingServerConfig.canPetsAttackPets() && PetHelper.arePetsOfSameOwner(pet, newTarget))) {
                    event.setCanceled(true);
                    event.setNewAboutToBeSetTarget(null);
                    if (pet instanceof NeutralMob neutralMob) {
                        neutralMob.stopBeingAngry();
                    }
                    if (pet instanceof Mob mob) {
                        PetHelper.setTamed(mob, player);
                    }
                }
            }
            return;
        }

        // 3. Prevent built-in hostile behaviors: pets only attack valid targets
        if (PettingServerConfig.shouldDisableBuiltInHostility()) {
            if (!PetHelper.isValidPetTarget(pet, newTarget)) {
                event.setCanceled(true);
                event.setNewAboutToBeSetTarget(null);
                if (pet instanceof NeutralMob neutralMob) {
                    neutralMob.stopBeingAngry();
                }
                return;
            }
        }

        // 4. Configurable: pets targeting fellow pets of the same owner
        if (!PettingServerConfig.canPetsAttackPets()) {
            if (PetHelper.arePetsOfSameOwner(pet, newTarget)) {
                event.setCanceled(true);
                event.setNewAboutToBeSetTarget(null);
                if (pet instanceof NeutralMob neutralMob) {
                    neutralMob.stopBeingAngry();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }

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

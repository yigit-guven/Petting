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
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        Entity attacker = event.getSource().getEntity();

        if (attacker == null) {
            return;
        }

        // Always: pets can never attack their owner
        if (victim instanceof Player player && attacker instanceof LivingEntity petMob && PetHelper.isOwner(petMob, player)) {
            event.setCanceled(true);
            return;
        }

        // Configurable: owner attacking their own pets
        if (!PettingServerConfig.canOwnerAttackPets() && attacker instanceof Player player && PetHelper.isOwner(victim, player)) {
            event.setCanceled(true);
            return;
        }

        // Configurable: friendly fire between pets of the same owner
        if (!PettingServerConfig.canPetsAttackPets() && attacker instanceof LivingEntity attackerPet) {
            Optional<UUID> owner1 = PetHelper.getOwnerUUID(attackerPet);
            if (owner1.isPresent() && owner1.equals(PetHelper.getOwnerUUID(victim))) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity pet = event.getEntity();
        if (!PetHelper.isTamed(pet)) {
            return;
        }

        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (newTarget == null) {
            return;
        }

        // Always: pets can never target their owner
        if (newTarget instanceof Player player && PetHelper.isOwner(pet, player)) {
            event.setCanceled(true);
            event.setNewAboutToBeSetTarget(null);
            if (pet instanceof NeutralMob neutralMob) {
                neutralMob.stopBeingAngry();
            }
            return;
        }

        // Prevent built-in hostile behaviors: pets only attack like tamed vanilla wolves
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

        // Configurable: pets targeting other pets of the same owner
        if (!PettingServerConfig.canPetsAttackPets()) {
            Optional<UUID> petOwner = PetHelper.getOwnerUUID(pet);
            if (petOwner.isPresent() && petOwner.equals(PetHelper.getOwnerUUID(newTarget))) {
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
        if (!PettingServerConfig.shouldDisableBuiltInHostility()) {
            return;
        }

        if (!(event.getEntity() instanceof Mob mob) || !PetHelper.isTamed(mob)) {
            return;
        }

        if (mob.level().isClientSide()) {
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

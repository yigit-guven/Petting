package net.yigitguven.petting.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.util.PetHelper;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = Petting.MODID)
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
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
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
    public static void onIncomingDamage(LivingAttackEvent event) {
        LivingEntity victim = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        Entity direct = event.getSource().getDirectEntity();

        if (event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION) && PetHelper.isGoldenDragonRitualActive(victim.level())) {
            event.setCanceled(true);
            return;
        }

        if (victim instanceof Player player) {
            if ((attacker != null && PetHelper.isRootOwner(attacker, player))
                    || (direct != null && PetHelper.isRootOwner(direct, player))) {
                event.setCanceled(true);
                return;
            }
        }

        LivingEntity rootAttacker = attacker instanceof LivingEntity livingAttacker ? PetHelper.getRootOwner(livingAttacker) : null;
        if (rootAttacker == null && direct instanceof LivingEntity livingDirect) {
            rootAttacker = PetHelper.getRootOwner(livingDirect);
        }
        if (rootAttacker instanceof Player ownerPlayer && PetHelper.isRootOwner(victim, ownerPlayer)) {
            if (!PettingServerConfig.canOwnerAttackPets()) {
                event.setCanceled(true);
                return;
            }
            return;
        }

        if (!PettingServerConfig.canPetsAttackPets()) {
            if ((attacker != null && PetHelper.arePetsOfSameOwner(victim, attacker))
                    || (direct != null && PetHelper.arePetsOfSameOwner(victim, direct))) {
                event.setCanceled(true);
                return;
            }
        }

        if ((attacker != null && (victim.isPassengerOfSameVehicle(attacker) || victim.hasPassenger(attacker) || attacker.hasPassenger(victim)))
                || (direct != null && (victim.isPassengerOfSameVehicle(direct) || victim.hasPassenger(direct) || direct.hasPassenger(victim)))) {
            event.setCanceled(true);
            return;
        }

        if (victim instanceof Player player && attacker instanceof LivingEntity threat) {
            PetHelper.alertTamedDragonsOfThreat(player, threat);
        }
        if (attacker instanceof Player ownerPlayer && victim != ownerPlayer) {
            PetHelper.alertTamedDragonsOfTarget(ownerPlayer, victim);
        }
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity pet = event.getEntity();
        LivingEntity newTarget = event.getNewTarget();
        if (newTarget == null) {
            return;
        }

        if (newTarget instanceof Player player && PetHelper.isRootOwner(pet, player)) {
            event.setNewTarget(null);
            if (pet instanceof NeutralMob neutralMob) {
                neutralMob.stopBeingAngry();
            }
            event.setCanceled(true);
            return;
        }

        if (!PetHelper.isTamed(pet)) {
            LivingEntity rootOwner = PetHelper.getRootOwner(pet);
            if (rootOwner instanceof Player player) {
                if (newTarget == player || (!PettingServerConfig.canPetsAttackPets() && PetHelper.arePetsOfSameOwner(pet, newTarget))) {
                    event.setNewTarget(null);
                    if (pet instanceof NeutralMob neutralMob) {
                        neutralMob.stopBeingAngry();
                    }
                    if (pet instanceof Mob mob) {
                        PetHelper.setTamed(mob, player);
                    }
                    event.setCanceled(true);
                    return;
                }
            }
            return;
        }

        if (PettingServerConfig.shouldDisableBuiltInHostility()) {
            if (!PetHelper.isValidPetTarget(pet, newTarget)) {
                event.setNewTarget(null);
                if (pet instanceof NeutralMob neutralMob) {
                    neutralMob.stopBeingAngry();
                }
                event.setCanceled(true);
                return;
            }
        }

        if (!PettingServerConfig.canPetsAttackPets()) {
            if (PetHelper.arePetsOfSameOwner(pet, newTarget)) {
                event.setNewTarget(null);
                if (pet instanceof NeutralMob neutralMob) {
                    neutralMob.stopBeingAngry();
                }
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
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

        PetHelper.updatePetBossBarVisibility(mob, !PettingServerConfig.shouldHideTamedBossBars());

        if (!PettingServerConfig.shouldDisableBuiltInHostility()) {
            return;
        }

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

        if (mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            LivingEntity brainTarget = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
            if (brainTarget != null && !PetHelper.isValidPetTarget(mob, brainTarget)) {
                mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
                if (mob instanceof Warden warden) {
                    warden.clearAnger(brainTarget);
                }
            }
        }

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

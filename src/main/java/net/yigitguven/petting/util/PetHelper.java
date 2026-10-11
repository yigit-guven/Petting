package net.yigitguven.petting.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.yigitguven.petting.ai.PetAttackOwnerTargetGoal;
import net.yigitguven.petting.ai.PetDefendOwnerTargetGoal;
import net.yigitguven.petting.ai.PetFollowOwnerGoal;
import net.yigitguven.petting.ai.PetMeleeAttackGoal;
import net.yigitguven.petting.ai.PetSitGoal;
import net.yigitguven.petting.data.CombatMode;
import net.yigitguven.petting.data.PetData;
import net.yigitguven.petting.data.PetOrder;
import net.minecraft.world.item.ItemStack;
import net.yigitguven.petting.init.PettingModAttachments;

import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.data.PetSavedData;

public class PetHelper {

    public static boolean isVanillaTamed(LivingEntity entity) {
        if (entity instanceof TamableAnimal tamable && tamable.isTame()) {
            return true;
        }
        if (entity instanceof AbstractHorse horse && horse.isTamed()) {
            return true;
        }
        if (entity instanceof OwnableEntity ownable) {
            if (ownable.getOwnerReference() != null && ownable.getOwnerReference().getUUID() != null) {
                return true;
            }
            if (ownable.getOwner() != null) {
                return true;
            }
        }
        return false;
    }

    public static boolean isTamed(LivingEntity entity) {
        if (entity.hasData(PettingModAttachments.PET_DATA) && entity.getData(PettingModAttachments.PET_DATA).isTamed()) {
            return true;
        }
        return isVanillaTamed(entity);
    }

    public static Optional<UUID> getOwnerUUID(LivingEntity entity) {
        if (entity.hasData(PettingModAttachments.PET_DATA)) {
            PetData data = entity.getData(PettingModAttachments.PET_DATA);
            if (data.isTamed() && data.getOwnerUUID() != null) {
                return Optional.of(data.getOwnerUUID());
            }
        }
        if (entity instanceof OwnableEntity ownable) {
            if (ownable.getOwnerReference() != null && ownable.getOwnerReference().getUUID() != null) {
                return Optional.of(ownable.getOwnerReference().getUUID());
            }
            if (ownable.getOwner() != null) {
                return Optional.of(ownable.getOwner().getUUID());
            }
        }
        return Optional.empty();
    }

    @Nullable
    public static Player getOwner(LivingEntity entity) {
        Optional<UUID> uuidOpt = getOwnerUUID(entity);
        if (uuidOpt.isEmpty()) {
            return null;
        }
        return entity.level().getPlayerByUUID(uuidOpt.get());
    }

    public static boolean isOwner(LivingEntity entity, Player player) {
        Optional<UUID> uuidOpt = getOwnerUUID(entity);
        return uuidOpt.isPresent() && uuidOpt.get().equals(player.getUUID());
    }

    public static PetOrder getOrder(LivingEntity entity) {
        if (!entity.hasData(PettingModAttachments.PET_DATA)) {
            return PetOrder.FOLLOW;
        }
        return entity.getData(PettingModAttachments.PET_DATA).getOrder();
    }

    public static void setOrder(LivingEntity entity, PetOrder order) {
        if (entity.hasData(PettingModAttachments.PET_DATA)) {
            PetData data = entity.getData(PettingModAttachments.PET_DATA);
            data.setOrder(order);
            entity.setData(PettingModAttachments.PET_DATA, data);
        }
    }

    public static CombatMode getCombatMode(LivingEntity entity) {
        if (!entity.hasData(PettingModAttachments.PET_DATA)) {
            return PettingServerConfig.getDefaultCombatMode();
        }
        return entity.getData(PettingModAttachments.PET_DATA).getCombatMode();
    }

    public static void setCombatMode(LivingEntity entity, CombatMode mode) {
        if (entity.hasData(PettingModAttachments.PET_DATA)) {
            PetData data = entity.getData(PettingModAttachments.PET_DATA);
            data.setCombatMode(mode);
            entity.setData(PettingModAttachments.PET_DATA, data);
        }
    }

    public static ItemStack getCollar(LivingEntity entity) {
        if (!entity.hasData(PettingModAttachments.PET_DATA)) {
            return ItemStack.EMPTY;
        }
        return entity.getData(PettingModAttachments.PET_DATA).getCollar();
    }

    public static void setCollar(LivingEntity entity, ItemStack collar) {
        PetData data = entity.getData(PettingModAttachments.PET_DATA);
        data.setCollar(collar != null ? collar : ItemStack.EMPTY);
        entity.setData(PettingModAttachments.PET_DATA, data);
    }

    public static void setTamed(Mob mob, Player owner) {
        PetData data = mob.getData(PettingModAttachments.PET_DATA);
        data.setTamed(true);
        data.setOwnerUUID(owner.getUUID());
        data.setOrder(PetOrder.FOLLOW);
        data.setCombatMode(PettingServerConfig.getDefaultCombatMode());
        mob.setData(PettingModAttachments.PET_DATA, data);

        mob.setPersistenceRequired();
        mob.setTarget(null);
        mob.setLastHurtByMob(null);
        if (mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        }
        if (mob instanceof NeutralMob neutralMob) {
            neutralMob.stopBeingAngry();
        }
        mob.targetSelector.getAvailableGoals().stream().filter(WrappedGoal::isRunning).forEach(WrappedGoal::stop);

        syncEntityTameStatus(mob);
        injectGoals(mob);

        if (mob.level() instanceof ServerLevel serverLevel) {
            PetSavedData.get(serverLevel).addPet(owner.getUUID(), mob.getUUID());
        }
    }

    public static void syncEntityTameStatus(Mob mob) {
        if (mob == null || mob.level().isClientSide()) {
            return;
        }
        if (!isTamed(mob)) {
            return;
        }

        UUID ownerUUID = getOwnerUUID(mob).orElse(null);
        Player owner = (ownerUUID != null && mob.level() instanceof ServerLevel sl) ? sl.getPlayerByUUID(ownerUUID) : null;

        if (mob instanceof AbstractHorse horse) {
            if (!horse.isTamed()) {
                horse.setTamed(true);
            }
            if (owner != null) {
                horse.setOwner(owner);
            }
            horse.setTemper(horse.getMaxTemper());
            horse.goalSelector.getAvailableGoals().removeIf(wrapped -> wrapped.getGoal() instanceof RunAroundLikeCrazyGoal);
        }

        if (mob instanceof TamableAnimal tamable) {
            if (!tamable.isTame()) {
                tamable.setTame(true, false);
            }
            if (owner != null) {
                tamable.setOwner(owner);
            }
        }

        syncModdedTame(mob, ownerUUID, owner);
    }

    private static void syncModdedTame(Mob mob, @Nullable UUID ownerUUID, @Nullable Player owner) {
        Class<?> clazz = mob.getClass();
        try {
            for (Method m : clazz.getMethods()) {
                String name = m.getName();
                if ((name.equals("setTamed") || name.equals("setTame")) && m.getParameterCount() == 1 && m.getParameterTypes()[0] == boolean.class) {
                    m.invoke(mob, true);
                } else if (name.equals("setOwner") && m.getParameterCount() == 1 && LivingEntity.class.isAssignableFrom(m.getParameterTypes()[0])) {
                    if (owner != null) {
                        m.invoke(mob, owner);
                    }
                } else if (name.equals("setOwnerUUID") && m.getParameterCount() == 1 && m.getParameterTypes()[0] == UUID.class) {
                    if (ownerUUID != null) {
                        m.invoke(mob, ownerUUID);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void setUntamed(Mob mob) {
        if (mob.hasData(PettingModAttachments.PET_DATA)) {
            PetData data = mob.getData(PettingModAttachments.PET_DATA);
            ItemStack collar = data.getCollar();
            if (!collar.isEmpty()) {
                if (mob.level() instanceof ServerLevel serverLevel) {
                    mob.spawnAtLocation(serverLevel, collar);
                }
                data.setCollar(ItemStack.EMPTY);
            }
            data.setTamed(false);
            data.setOwnerUUID(null);
            data.setOrder(PetOrder.FOLLOW);
            data.setCombatMode(PettingServerConfig.getDefaultCombatMode());
            mob.setData(PettingModAttachments.PET_DATA, data);
        }
        if (mob instanceof AbstractHorse horse) {
            horse.setTamed(false);
            horse.setOwner(null);
        }
        if (mob instanceof TamableAnimal tamable) {
            tamable.setTame(false, false);
            tamable.setOwner(null);
        }
        if (mob.level() instanceof ServerLevel serverLevel) {
            PetSavedData.get(serverLevel).removePet(mob.getUUID());
        }
    }

    public static int getPetCount(Player player, ServerLevel serverLevel) {
        return PetSavedData.get(serverLevel).getPetCount(player.getUUID());
    }

    public static boolean hasReachedPetLimit(Player player, ServerLevel serverLevel) {
        int maxPets = PettingServerConfig.getMaxPetCount();
        if (maxPets < 0) {
            return false;
        }
        return getPetCount(player, serverLevel) >= maxPets;
    }

    public static void purgeBuiltInTargetGoals(Mob mob) {
        if (!isTamed(mob)) {
            return;
        }
        if (PettingServerConfig.shouldDisableBuiltInHostility()) {
            mob.targetSelector.removeAllGoals(goal -> !(goal instanceof PetDefendOwnerTargetGoal) && !(goal instanceof PetAttackOwnerTargetGoal));
        }
    }

    public static boolean isValidPetTarget(LivingEntity pet, @Nullable LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (getOrder(pet) == PetOrder.SIT || getCombatMode(pet) == CombatMode.PASSIVE) {
            return false;
        }
        Player owner = getOwner(pet);
        if (owner == null) {
            return false;
        }

        // Rule: Pets can never target their owner
        if (target == owner) {
            return false;
        }

        // Rule: Pets cannot target fellow pets of the same owner unless friendly fire is enabled
        if (!PettingServerConfig.canPetsAttackPets() && isOwner(target, owner)) {
            return false;
        }

        // If the pet already has this target and is alive
        if (pet instanceof Mob mob) {
            if (mob.getTarget() == target) {
                return true;
            }
            if (mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                    && mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null) == target) {
                return true;
            }
        }

        // 1. Protect owner: target attacked the owner
        if (PettingServerConfig.canPetsDefendOwner() && target == owner.getLastHurtByMob()) {
            return true;
        }

        // 2. Attack for owner: owner attacked the target
        if (PettingServerConfig.canPetsAttackOwnerTarget() && target == owner.getLastHurtMob()) {
            return true;
        }

        // 3. Self-defense: target attacked the pet
        if (PettingServerConfig.canPetsRetaliate() && target == pet.getLastHurtByMob()) {
            return true;
        }

        return false;
    }

    public static void injectGoals(Mob mob) {
        mob.goalSelector.getAvailableGoals().removeIf(wrappedGoal -> wrappedGoal.getGoal() instanceof RunAroundLikeCrazyGoal
                || wrappedGoal.getGoal() instanceof PetSitGoal
                || wrappedGoal.getGoal() instanceof PetMeleeAttackGoal
                || wrappedGoal.getGoal() instanceof PetFollowOwnerGoal);
        purgeBuiltInTargetGoals(mob);

        mob.goalSelector.addGoal(1, new PetSitGoal(mob));
        if (mob instanceof PathfinderMob pathfinderMob) {
            mob.goalSelector.addGoal(2, new PetMeleeAttackGoal(pathfinderMob, 1.2D, true));
            mob.goalSelector.addGoal(3, new PetFollowOwnerGoal(pathfinderMob, 1.25D, 4.0F, 2.0F, 20.0F));
        }

        boolean hasDefendGoal = mob.targetSelector.getAvailableGoals().stream()
                .anyMatch(wrappedGoal -> wrappedGoal.getGoal() instanceof PetDefendOwnerTargetGoal);
        if (!hasDefendGoal) {
            mob.targetSelector.addGoal(1, new PetDefendOwnerTargetGoal(mob));
        }

        boolean hasAttackGoal = mob.targetSelector.getAvailableGoals().stream()
                .anyMatch(wrappedGoal -> wrappedGoal.getGoal() instanceof PetAttackOwnerTargetGoal);
        if (!hasAttackGoal) {
            mob.targetSelector.addGoal(2, new PetAttackOwnerTargetGoal(mob));
        }
    }
}

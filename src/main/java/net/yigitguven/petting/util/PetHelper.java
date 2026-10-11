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
import net.yigitguven.petting.ai.PetRangedAttackGoal;
import net.yigitguven.petting.ai.PetSitGoal;
import net.yigitguven.petting.data.CollarSettings;
import net.yigitguven.petting.data.CombatMode;
import net.yigitguven.petting.data.PetData;
import net.yigitguven.petting.data.PetOrder;
import net.minecraft.world.item.ItemStack;
import net.yigitguven.petting.init.PettingDataComponents;
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

    public static boolean isSitting(LivingEntity entity) {
        if (entity.hasData(PettingModAttachments.PET_DATA)
                && entity.getData(PettingModAttachments.PET_DATA).getOrder() == PetOrder.SIT) {
            return true;
        }
        if (entity instanceof TamableAnimal tamable && tamable.isInSittingPose()) {
            return true;
        }
        return false;
    }

    public static void setOrder(LivingEntity entity, PetOrder order) {
        if (entity.hasData(PettingModAttachments.PET_DATA)) {
            PetData data = entity.getData(PettingModAttachments.PET_DATA);
            data.setOrder(order);
            entity.setData(PettingModAttachments.PET_DATA, data);
        }
        if (entity instanceof TamableAnimal tamable) {
            boolean sit = (order == PetOrder.SIT);
            tamable.setOrderedToSit(sit);
            tamable.setInSittingPose(sit);
        }
        if (entity instanceof Mob mob) {
            mob.setShiftKeyDown(order == PetOrder.SIT);
            if (order != PetOrder.SIT && isFlyingPet(mob)) {
                mob.setNoGravity(false);
            }
        }
        syncModdedSit(entity, order == PetOrder.SIT);
    }

    public static CombatMode getCombatMode(LivingEntity entity) {
        ItemStack collar = getCollar(entity);
        if (!collar.isEmpty() && collar.has(PettingDataComponents.COLLAR_SETTINGS)) {
            return collar.get(PettingDataComponents.COLLAR_SETTINGS).combatMode();
        }
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
        ItemStack collar = getCollar(entity);
        if (!collar.isEmpty()) {
            CollarSettings current = getCollarSettings(entity);
            if (current.combatMode() != mode) {
                collar.set(PettingDataComponents.COLLAR_SETTINGS, current.withCombatMode(mode));
                setCollar(entity, collar);
            }
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
        if (collar != null && !collar.isEmpty() && collar.has(PettingDataComponents.COLLAR_SETTINGS)) {
            data.setCombatMode(collar.get(PettingDataComponents.COLLAR_SETTINGS).combatMode());
        }
        entity.setData(PettingModAttachments.PET_DATA, data);
    }

    public static CollarSettings getCollarSettings(LivingEntity entity) {
        ItemStack collar = getCollar(entity);
        if (!collar.isEmpty()) {
            if (collar.has(PettingDataComponents.COLLAR_SETTINGS)) {
                return collar.get(PettingDataComponents.COLLAR_SETTINGS);
            }
            CollarSettings defaults = CollarSettings.fromConfig();
            if (entity.hasData(PettingModAttachments.PET_DATA)) {
                defaults = defaults.withCombatMode(entity.getData(PettingModAttachments.PET_DATA).getCombatMode());
            }
            collar.set(PettingDataComponents.COLLAR_SETTINGS, defaults);
            setCollar(entity, collar);
            return defaults;
        }
        return CollarSettings.fromConfig();
    }

    public static void setCollarSettings(LivingEntity entity, CollarSettings settings) {
        ItemStack collar = getCollar(entity);
        if (!collar.isEmpty()) {
            collar.set(PettingDataComponents.COLLAR_SETTINGS, settings);
            setCollar(entity, collar);
        }
        setCombatMode(entity, settings.combatMode());
    }

    public static boolean isPetRidingEnabledFor(@Nullable Mob pet) {
        if (pet == null) {
            return false;
        }
        return PettingServerConfig.isPetRidingAllowed(pet.getType());
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
            boolean sit = (getOrder(mob) == PetOrder.SIT);
            tamable.setOrderedToSit(sit);
            tamable.setInSittingPose(sit);
        }

        syncModdedTame(mob, ownerUUID, owner);
        syncModdedSit(mob, getOrder(mob) == PetOrder.SIT);
    }

    public static void syncModdedSit(LivingEntity entity, boolean sit) {
        Class<?> clazz = entity.getClass();
        try {
            for (Method m : clazz.getMethods()) {
                String name = m.getName();
                if ((name.equals("setOrderedToSit") || name.equals("setInSittingPose") || name.equals("setSitting") || name.equals("sit"))
                        && m.getParameterCount() == 1 && m.getParameterTypes()[0] == boolean.class) {
                    m.invoke(entity, sit);
                } else if (sit && name.equals("sitDown") && m.getParameterCount() == 0) {
                    m.invoke(entity);
                } else if (!sit && name.equals("standUp") && m.getParameterCount() == 0) {
                    m.invoke(entity);
                }
            }
        } catch (Exception ignored) {
        }
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

        CollarSettings settings = getCollarSettings(pet);

        // 1. Protect owner: target attacked the owner
        if (target == owner.getLastHurtByMob()) {
            return true;
        }

        // 2. Attack for owner: owner attacked the target (Aggressive mode only)
        if (settings.combatMode() == CombatMode.AGGRESSIVE && target == owner.getLastHurtMob()) {
            return true;
        }

        // 3. Self-defense: target attacked the pet
        if (settings.retaliateWhenHurt() && target == pet.getLastHurtByMob()) {
            return true;
        }

        // 4. Defend fellow pets
        if (settings.defendOtherPets()) {
            for (LivingEntity otherPet : pet.level().getEntitiesOfClass(LivingEntity.class, pet.getBoundingBox().inflate(16.0D))) {
                if (otherPet != pet && isTamed(otherPet) && isOwner(otherPet, owner) && target == otherPet.getLastHurtByMob()) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isFlyingPet(Mob mob) {
        if (mob.isNoGravity()) {
            return true;
        }
        if (mob.getNavigation() instanceof net.minecraft.world.entity.ai.navigation.FlyingPathNavigation) {
            return true;
        }
        if (mob.getMoveControl() instanceof net.minecraft.world.entity.ai.control.FlyingMoveControl) {
            return true;
        }
        if (mob instanceof net.minecraft.world.entity.monster.Ghast
                || mob instanceof net.minecraft.world.entity.monster.Phantom
                || mob instanceof net.minecraft.world.entity.boss.wither.WitherBoss
                || mob instanceof net.minecraft.world.entity.monster.Blaze
                || mob instanceof net.minecraft.world.entity.monster.Vex
                || mob instanceof net.minecraft.world.entity.ambient.Bat
                || mob instanceof net.minecraft.world.entity.animal.allay.Allay
                || mob instanceof net.minecraft.world.entity.animal.bee.Bee
                || mob instanceof net.minecraft.world.entity.animal.parrot.Parrot
                || mob instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon) {
            return true;
        }
        String name = mob.getClass().getSimpleName().toLowerCase();
        return name.contains("fly") || name.contains("bird") || name.contains("dragon") || name.contains("drake");
    }

    public static boolean isSwimmingPet(Mob mob) {
        if (mob instanceof net.minecraft.world.entity.animal.fish.WaterAnimal) {
            return true;
        }
        if (mob instanceof net.minecraft.world.entity.monster.Guardian) {
            return true;
        }
        if (mob.getNavigation() instanceof net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation) {
            return true;
        }
        return mob.isInWater();
    }

    public static void forgiveAttacksAndClearTarget(Mob mob) {
        mob.setTarget(null);
        mob.setLastHurtByMob(null);
        mob.setLastHurtMob(null);
        if (mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        }
        if (mob.getBrain().hasMemoryValue(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)) {
            mob.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        }
        if (mob instanceof NeutralMob neutralMob) {
            neutralMob.stopBeingAngry();
        }
        if (mob instanceof net.minecraft.world.entity.boss.wither.WitherBoss wither) {
            wither.setAlternativeTarget(0, 0);
            wither.setAlternativeTarget(1, 0);
            wither.setAlternativeTarget(2, 0);
        }
        mob.targetSelector.getAvailableGoals().stream().filter(WrappedGoal::isRunning).forEach(WrappedGoal::stop);
        mob.goalSelector.getAvailableGoals().stream()
                .filter(w -> w.getGoal() instanceof PetMeleeAttackGoal || w.getGoal() instanceof PetRangedAttackGoal)
                .filter(WrappedGoal::isRunning)
                .forEach(WrappedGoal::stop);
    }

    public static void injectGoals(Mob mob) {
        mob.goalSelector.getAvailableGoals().removeIf(wrappedGoal -> wrappedGoal.getGoal() instanceof RunAroundLikeCrazyGoal
                || wrappedGoal.getGoal() instanceof PetSitGoal
                || wrappedGoal.getGoal() instanceof net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal
                || wrappedGoal.getGoal() instanceof net.minecraft.world.entity.ai.goal.FollowOwnerGoal
                || wrappedGoal.getGoal() instanceof PetRangedAttackGoal
                || wrappedGoal.getGoal() instanceof PetMeleeAttackGoal
                || wrappedGoal.getGoal() instanceof PetFollowOwnerGoal);
        purgeBuiltInTargetGoals(mob);

        mob.goalSelector.addGoal(1, new PetSitGoal(mob));
        if (mob instanceof PathfinderMob pathfinderMob) {
            mob.goalSelector.addGoal(2, new PetRangedAttackGoal(pathfinderMob, 1.25D, 20, 16.0F));
            mob.goalSelector.addGoal(3, new PetMeleeAttackGoal(pathfinderMob, 1.2D, true));
            mob.goalSelector.addGoal(4, new PetFollowOwnerGoal(pathfinderMob, 1.25D, 4.0F, 2.0F, 20.0F));
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

    public static boolean isInteractionItem(Mob mob, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof net.yigitguven.petting.item.CollarItem && getCollar(mob).isEmpty()) {
            return true;
        }
        if (net.minecraft.world.item.DyeColor.getColor(stack) != null) {
            ItemStack currentCollar = getCollar(mob);
            if (!currentCollar.isEmpty() && currentCollar.getItem() instanceof net.yigitguven.petting.item.CollarItem currentItem) {
                if (currentItem.getColor() != net.minecraft.world.item.DyeColor.getColor(stack)) {
                    return true;
                }
            }
        }
        if (PetEggHelper.canTame(mob, stack.getItem())) {
            return true;
        }
        if (stack.is(net.minecraft.world.item.Items.NAME_TAG)
                || stack.is(net.minecraft.world.item.Items.LEAD)
                || stack.is(net.minecraft.world.item.Items.SHEARS)) {
            return true;
        }
        return false;
    }
}

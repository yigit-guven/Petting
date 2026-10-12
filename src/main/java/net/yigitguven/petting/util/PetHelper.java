package net.yigitguven.petting.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.Vec3;
import net.yigitguven.petting.ai.PetAttackOwnerTargetGoal;
import net.yigitguven.petting.ai.PetDefendOwnerTargetGoal;
import net.yigitguven.petting.ai.PetFollowOwnerGoal;
import net.yigitguven.petting.ai.PetGuardTargetGoal;
import net.yigitguven.petting.ai.PetMeleeAttackGoal;
import net.yigitguven.petting.ai.PetRangedAttackGoal;
import net.yigitguven.petting.ai.PetSitGoal;
import net.yigitguven.petting.ai.PetWanderGoal;
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

    private static final String[] MODDED_OWNER_METHOD_NAMES = new String[]{
        "getOwner", "getSummoner", "getCaster", "getMaster", "getLeader", "getOwnerEntity", "getCreator"
    };

    private static @Nullable net.minecraft.world.entity.Entity findModdedOwner(net.minecraft.world.entity.Entity entity) {
        if (entity == null) {
            return null;
        }
        Class<?> clazz = entity.getClass();
        for (String methodName : MODDED_OWNER_METHOD_NAMES) {
            try {
                Method method = clazz.getMethod(methodName);
                if (net.minecraft.world.entity.Entity.class.isAssignableFrom(method.getReturnType()) && method.getParameterCount() == 0) {
                    Object result = method.invoke(entity);
                    if (result instanceof net.minecraft.world.entity.Entity owner && owner != entity) {
                        return owner;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public static @Nullable LivingEntity getRootOwner(@Nullable net.minecraft.world.entity.Entity entity) {
        if (entity == null) {
            return null;
        }
        if (entity instanceof Player player) {
            return player;
        }

        java.util.Set<net.minecraft.world.entity.Entity> visited = new java.util.HashSet<>();
        net.minecraft.world.entity.Entity current = entity;

        while (current != null && visited.add(current)) {
            if (current instanceof Player player) {
                return player;
            }

            // 1. Direct Petting Data Attachment
            if (current instanceof LivingEntity living && living.hasData(PettingModAttachments.PET_DATA)) {
                PetData data = living.getData(PettingModAttachments.PET_DATA);
                if (data.isTamed() && data.getOwnerUUID() != null) {
                    Player player = current.level().getPlayerByUUID(data.getOwnerUUID());
                    if (player != null) {
                        return player;
                    }
                }
            }

            // 2. OwnableEntity (Vanilla tamables, Vex, EvokerFangs, Modded ownables)
            if (current instanceof OwnableEntity ownable) {
                net.minecraft.world.entity.Entity owner = ownable.getOwner();
                if (owner != null && owner != current) {
                    current = owner;
                    continue;
                }
                if (ownable.getOwnerReference() != null && ownable.getOwnerReference().getUUID() != null) {
                    UUID ownerUuid = ownable.getOwnerReference().getUUID();
                    Player player = current.level().getPlayerByUUID(ownerUuid);
                    if (player != null) {
                        return player;
                    }
                }
            }

            // 3. TraceableEntity (Projectiles, AreaEffectCloud, PrimedTnt, Vex, Fangs)
            if (current instanceof net.minecraft.world.entity.TraceableEntity traceable) {
                net.minecraft.world.entity.Entity owner = traceable.getOwner();
                if (owner != null && owner != current) {
                    current = owner;
                    continue;
                }
            }

            // 4. Common modded summon/minion getter methods via reflection
            net.minecraft.world.entity.Entity moddedOwner = findModdedOwner(current);
            if (moddedOwner != null && moddedOwner != current) {
                current = moddedOwner;
                continue;
            }

            break;
        }

        return null;
    }

    public static Optional<UUID> getRootOwnerUUID(@Nullable net.minecraft.world.entity.Entity entity) {
        if (entity == null) {
            return Optional.empty();
        }
        LivingEntity root = getRootOwner(entity);
        if (root instanceof Player player) {
            return Optional.of(player.getUUID());
        }
        if (entity instanceof LivingEntity living) {
            return getOwnerUUID(living);
        }
        return Optional.empty();
    }

    public static boolean isRootOwner(@Nullable net.minecraft.world.entity.Entity entity, @Nullable Player player) {
        if (entity == null || player == null) {
            return false;
        }
        LivingEntity root = getRootOwner(entity);
        if (root == player) {
            return true;
        }
        return getRootOwnerUUID(entity).map(uuid -> uuid.equals(player.getUUID())).orElse(false);
    }

    public static boolean areEntitiesAllied(@Nullable net.minecraft.world.entity.Entity a, @Nullable net.minecraft.world.entity.Entity b) {
        if (a == null || b == null) {
            return false;
        }
        if (a == b) {
            return true;
        }
        // Vehicle / passenger relationship
        if (a.isPassengerOfSameVehicle(b) || a.hasPassenger(b) || b.hasPassenger(a)) {
            return true;
        }
        // One is the root owner of the other
        if (a instanceof Player playerA && isRootOwner(b, playerA)) {
            return true;
        }
        if (b instanceof Player playerB && isRootOwner(a, playerB)) {
            return true;
        }
        // Both share the same root owner player
        Optional<UUID> ownerA = getRootOwnerUUID(a);
        Optional<UUID> ownerB = getRootOwnerUUID(b);
        if (ownerA.isPresent() && ownerB.isPresent() && ownerA.get().equals(ownerB.get())) {
            return true;
        }
        return false;
    }

    public static boolean arePetsOfSameOwner(@Nullable net.minecraft.world.entity.Entity a, @Nullable net.minecraft.world.entity.Entity b) {
        if (a == null || b == null || a == b) {
            return false;
        }
        if (a instanceof Player || b instanceof Player) {
            return false;
        }
        Optional<UUID> ownerA = getRootOwnerUUID(a);
        Optional<UUID> ownerB = getRootOwnerUUID(b);
        return ownerA.isPresent() && ownerB.isPresent() && ownerA.get().equals(ownerB.get());
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
                if (ownable.getOwner() instanceof Player) {
                    return Optional.of(ownable.getOwner().getUUID());
                }
                // If owned by another entity, resolve root owner
                LivingEntity root = getRootOwner(entity);
                if (root instanceof Player) {
                    return Optional.of(root.getUUID());
                }
                return Optional.of(ownable.getOwner().getUUID());
            }
        }
        return Optional.empty();
    }

    @Nullable
    public static Player getOwner(LivingEntity entity) {
        LivingEntity root = getRootOwner(entity);
        if (root instanceof Player player) {
            return player;
        }
        Optional<UUID> uuidOpt = getOwnerUUID(entity);
        if (uuidOpt.isEmpty()) {
            return null;
        }
        return entity.level().getPlayerByUUID(uuidOpt.get());
    }

    public static boolean isOwner(net.minecraft.world.entity.Entity entity, Player player) {
        return isRootOwner(entity, player);
    }

    public static boolean isOwner(LivingEntity entity, Player player) {
        return isRootOwner(entity, player);
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
        if (!PettingServerConfig.isPetInventoryEnabled()) {
            return PettingServerConfig.getDefaultCombatMode();
        }
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
        syncHomeRestrictions(entity, getCollarSettings(entity));
    }

    public static ItemStack getCollar(LivingEntity entity) {
        if (!PettingServerConfig.isPetInventoryEnabled()) {
            return ItemStack.EMPTY;
        }
        if (!entity.hasData(PettingModAttachments.PET_DATA)) {
            return ItemStack.EMPTY;
        }
        return entity.getData(PettingModAttachments.PET_DATA).getCollar();
    }

    public static void setCollar(LivingEntity entity, ItemStack collar) {
        if (!PettingServerConfig.isPetInventoryEnabled()) {
            return;
        }
        PetData data = entity.getData(PettingModAttachments.PET_DATA);
        data.setCollar(collar != null ? collar : ItemStack.EMPTY);
        if (collar != null && !collar.isEmpty() && collar.has(PettingDataComponents.COLLAR_SETTINGS)) {
            data.setCombatMode(collar.get(PettingDataComponents.COLLAR_SETTINGS).combatMode());
        }
        entity.setData(PettingModAttachments.PET_DATA, data);
    }

    public static ItemStack getSaddle(LivingEntity entity) {
        if (!PettingServerConfig.isPetInventoryEnabled()) {
            return ItemStack.EMPTY;
        }
        if (entity instanceof AbstractHorse horse) {
            ItemStack horseSaddle = horse.getItemBySlot(EquipmentSlot.SADDLE);
            if (!horseSaddle.isEmpty()) {
                return horseSaddle;
            }
        }
        if (entity.hasData(PettingModAttachments.PET_DATA)) {
            return entity.getData(PettingModAttachments.PET_DATA).getSaddle();
        }
        return ItemStack.EMPTY;
    }

    public static void setSaddle(LivingEntity entity, ItemStack saddle) {
        if (!PettingServerConfig.isPetInventoryEnabled()) {
            return;
        }
        ItemStack item = saddle != null ? saddle : ItemStack.EMPTY;
        if (entity instanceof AbstractHorse horse) {
            horse.setItemSlot(EquipmentSlot.SADDLE, item);
            horse.setDropChance(EquipmentSlot.SADDLE, 1.0F);
        }
        if (entity.hasData(PettingModAttachments.PET_DATA)) {
            PetData data = entity.getData(PettingModAttachments.PET_DATA);
            data.setSaddle(item);
            entity.setData(PettingModAttachments.PET_DATA, data);
        }
    }

    public static boolean hasSaddle(LivingEntity entity) {
        return !getSaddle(entity).isEmpty();
    }

    public static boolean isRideAttackOnClick(LivingEntity entity) {
        if (!entity.hasData(PettingModAttachments.PET_DATA)) {
            return true;
        }
        return entity.getData(PettingModAttachments.PET_DATA).isRideAttackOnClick();
    }

    public static void setRideAttackOnClick(LivingEntity entity, boolean enabled) {
        if (entity.hasData(PettingModAttachments.PET_DATA)) {
            PetData data = entity.getData(PettingModAttachments.PET_DATA);
            data.setRideAttackOnClick(enabled);
            entity.setData(PettingModAttachments.PET_DATA, data);
        }
    }

    public static boolean isRideAutoAttackHostiles(LivingEntity entity) {
        if (!entity.hasData(PettingModAttachments.PET_DATA)) {
            return false;
        }
        return entity.getData(PettingModAttachments.PET_DATA).isRideAutoAttackHostiles();
    }

    public static void setRideAutoAttackHostiles(LivingEntity entity, boolean enabled) {
        if (entity.hasData(PettingModAttachments.PET_DATA)) {
            PetData data = entity.getData(PettingModAttachments.PET_DATA);
            data.setRideAutoAttackHostiles(enabled);
            entity.setData(PettingModAttachments.PET_DATA, data);
        }
    }

    public static CollarSettings getCollarSettings(LivingEntity entity) {
        if (!PettingServerConfig.isPetInventoryEnabled()) {
            return CollarSettings.fromConfig();
        }
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
        if (!PettingServerConfig.isPetInventoryEnabled()) {
            return;
        }
        ItemStack collar = getCollar(entity);
        if (!collar.isEmpty()) {
            collar.set(PettingDataComponents.COLLAR_SETTINGS, settings);
            setCollar(entity, collar);
        }
        setCombatMode(entity, settings.combatMode());
        syncHomeRestrictions(entity, settings);
    }

    public static void syncHomeRestrictions(LivingEntity entity, CollarSettings settings) {
        if (entity instanceof Mob mob) {
            if (settings.combatMode() == CombatMode.WANDER || settings.combatMode() == CombatMode.GUARD) {
                int radius = Math.max(2, settings.followDistance());
                if (!mob.hasHome()) {
                    mob.setHomeTo(mob.blockPosition(), radius);
                } else {
                    mob.setHomeTo(mob.getHomePosition(), radius);
                }
            } else {
                if (mob.hasHome()) {
                    mob.clearHome();
                }
            }
        }
    }

    public static boolean isPetRidingEnabledFor(@Nullable Mob pet) {
        if (!PettingServerConfig.isPetInventoryEnabled()) {
            return false;
        }
        if (pet == null) {
            return false;
        }
        if (pet instanceof net.minecraft.world.entity.animal.equine.AbstractHorse) {
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
        if (!PettingServerConfig.isPetInventoryEnabled()) {
            data.setCollar(ItemStack.EMPTY);
            data.setSaddle(ItemStack.EMPTY);
        }
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
        updatePetBossBarVisibility(mob, !PettingServerConfig.shouldHideTamedBossBars());

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
        if (mob.hasHome()) {
            mob.clearHome();
        }
        if (mob.hasData(PettingModAttachments.PET_DATA)) {
            PetData data = mob.getData(PettingModAttachments.PET_DATA);
            ItemStack collar = data.getCollar();
            if (!collar.isEmpty()) {
                if (mob.level() instanceof ServerLevel serverLevel) {
                    mob.spawnAtLocation(serverLevel, collar);
                }
                data.setCollar(ItemStack.EMPTY);
            }
            ItemStack saddle = data.getSaddle();
            if (!saddle.isEmpty()) {
                if (mob.level() instanceof ServerLevel serverLevel) {
                    mob.spawnAtLocation(serverLevel, saddle);
                }
                data.setSaddle(ItemStack.EMPTY);
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
            mob.targetSelector.removeAllGoals(goal -> !(goal instanceof PetDefendOwnerTargetGoal)
                    && !(goal instanceof PetAttackOwnerTargetGoal)
                    && !(goal instanceof PetGuardTargetGoal));
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
        if (!PettingServerConfig.canPetsAttackPets() && isRootOwner(target, owner)) {
            return false;
        }

        // If the pet already has this target and is alive
        if (pet instanceof Mob mob) {
            if (mob.getTarget() == target) {
                CollarSettings cs = getCollarSettings(pet);
                if (cs.combatMode() == CombatMode.GUARD && mob.hasHome()) {
                    double leashDist = mob.getHomeRadius() + 10.0D;
                    if (target.distanceToSqr(Vec3.atBottomCenterOf(mob.getHomePosition())) > leashDist * leashDist) {
                        return false;
                    }
                }
                return true;
            }
            if (mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                    && mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null) == target) {
                CollarSettings cs = getCollarSettings(pet);
                if (cs.combatMode() == CombatMode.GUARD && mob.hasHome()) {
                    double leashDist = mob.getHomeRadius() + 10.0D;
                    if (target.distanceToSqr(Vec3.atBottomCenterOf(mob.getHomePosition())) > leashDist * leashDist) {
                        return false;
                    }
                }
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

        // 5. Guard mode: untamed hostile monsters within patrol/leash area
        if (settings.combatMode() == CombatMode.GUARD && target instanceof net.minecraft.world.entity.monster.Enemy && !isTamed(target)) {
            if (pet instanceof Mob mob && mob.hasHome()) {
                double leashDist = mob.getHomeRadius() + 10.0D;
                if (target.distanceToSqr(Vec3.atBottomCenterOf(mob.getHomePosition())) > leashDist * leashDist) {
                    return false;
                }
            }
            return true;
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
                || wrappedGoal.getGoal() instanceof PetFollowOwnerGoal
                || wrappedGoal.getGoal() instanceof PetWanderGoal);
        purgeBuiltInTargetGoals(mob);

        mob.goalSelector.addGoal(1, new PetSitGoal(mob));
        if (mob instanceof PathfinderMob pathfinderMob) {
            mob.goalSelector.addGoal(2, new PetRangedAttackGoal(pathfinderMob, 1.25D, 20, 16.0F));
            mob.goalSelector.addGoal(3, new PetMeleeAttackGoal(pathfinderMob, 1.2D, true));
            mob.goalSelector.addGoal(4, new PetFollowOwnerGoal(pathfinderMob, 1.25D, 4.0F, 2.0F, 20.0F));
            mob.goalSelector.addGoal(5, new PetWanderGoal(pathfinderMob, 1.0D));
        } else {
            mob.goalSelector.addGoal(5, new PetWanderGoal(mob, 1.0D));
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

        boolean hasGuardGoal = mob.targetSelector.getAvailableGoals().stream()
                .anyMatch(wrappedGoal -> wrappedGoal.getGoal() instanceof PetGuardTargetGoal);
        if (!hasGuardGoal) {
            mob.targetSelector.addGoal(3, new PetGuardTargetGoal(mob));
        }
    }

    public static boolean isInteractionItem(Mob mob, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (PettingServerConfig.isPetInventoryEnabled()) {
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

    private static final java.util.Map<Class<?>, java.util.List<java.lang.reflect.Field>> BOSS_EVENT_FIELDS_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    private static java.lang.reflect.Field endDragonFightEventField = null;
    private static boolean endDragonFightFieldFailed = false;

    public static void updatePetBossBarVisibility(Mob mob, boolean visible) {
        if (mob == null || mob.level().isClientSide()) {
            return;
        }

        // 1. Check EnderDragon via EnderDragonFight
        if (mob instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon dragon) {
            net.minecraft.world.level.dimension.end.EnderDragonFight fight = dragon.getDragonFight();
            if (fight != null) {
                setFightBossEventVisible(fight, visible);
            }
        }

        // 2. Generic field lookup for WitherBoss and any modded boss mob
        java.util.List<java.lang.reflect.Field> fields = BOSS_EVENT_FIELDS_CACHE.computeIfAbsent(mob.getClass(), PetHelper::findBossEventFields);
        for (java.lang.reflect.Field field : fields) {
            try {
                Object obj = field.get(mob);
                if (obj instanceof net.minecraft.server.level.ServerBossEvent bossEvent) {
                    if (bossEvent.isVisible() != visible) {
                        bossEvent.setVisible(visible);
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static java.util.List<java.lang.reflect.Field> findBossEventFields(Class<?> clazz) {
        java.util.List<java.lang.reflect.Field> list = new java.util.ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Mob.class && current != Object.class) {
            for (java.lang.reflect.Field f : current.getDeclaredFields()) {
                if (net.minecraft.server.level.ServerBossEvent.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    list.add(f);
                }
            }
            current = current.getSuperclass();
        }
        return list;
    }

    private static void setFightBossEventVisible(net.minecraft.world.level.dimension.end.EnderDragonFight fight, boolean visible) {
        if (endDragonFightFieldFailed) {
            return;
        }
        try {
            if (endDragonFightEventField == null) {
                for (java.lang.reflect.Field f : net.minecraft.world.level.dimension.end.EnderDragonFight.class.getDeclaredFields()) {
                    if (net.minecraft.server.level.ServerBossEvent.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        endDragonFightEventField = f;
                        break;
                    }
                }
            }
            if (endDragonFightEventField != null) {
                Object obj = endDragonFightEventField.get(fight);
                if (obj instanceof net.minecraft.server.level.ServerBossEvent bossEvent) {
                    if (bossEvent.isVisible() != visible) {
                        bossEvent.setVisible(visible);
                    }
                }
            }
        } catch (Exception e) {
            endDragonFightFieldFailed = true;
        }
    }

    public static boolean isBedBlock(net.minecraft.world.level.block.state.BlockState state) {
        if (state == null) {
            return false;
        }
        return state.is(net.minecraft.tags.BlockTags.BEDS)
                || state.getBlock() instanceof net.minecraft.world.level.block.BedBlock
                || state.getBlock().getDescriptionId().contains("bed");
    }

    public static boolean isCushionBlock(net.minecraft.world.level.block.state.BlockState state) {
        if (state == null) {
            return false;
        }
        return state.getBlock().getDescriptionId().contains("cushion")
                || state.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "cushions")));
    }

    public static boolean isCushionEntity(@Nullable net.minecraft.world.entity.Entity entity) {
        if (entity == null) {
            return false;
        }
        return entity instanceof net.minecraft.world.entity.decoration.Cushion
                || entity.getType().getDescriptionId().contains("cushion")
                || entity.getClass().getSimpleName().toLowerCase().contains("cushion");
    }

    public static boolean isRespawnPointValid(@Nullable ServerLevel level, @Nullable net.yigitguven.petting.data.CollarRespawnPoint respawnPoint) {
        if (level == null || respawnPoint == null) {
            return false;
        }
        net.minecraft.core.BlockPos pos = respawnPoint.pos().pos();
        if (!level.hasChunkAt(pos)) {
            level.getChunk(pos);
        }

        if (respawnPoint.isCushion()) {
            net.minecraft.world.phys.AABB searchBox = new net.minecraft.world.phys.AABB(pos).inflate(1.5);
            boolean hasCushionEntity = !level.getEntitiesOfClass(net.minecraft.world.entity.decoration.Cushion.class, searchBox, c -> !c.isRemoved() && c.isAlive()).isEmpty();
            if (hasCushionEntity) {
                return true;
            }
            boolean hasModdedCushion = !level.getEntitiesOfClass(net.minecraft.world.entity.Entity.class, searchBox,
                    e -> !e.isRemoved() && e.isAlive() && isCushionEntity(e)).isEmpty();
            if (hasModdedCushion) {
                return true;
            }
            return isCushionBlock(level.getBlockState(pos));
        } else {
            return isBedBlock(level.getBlockState(pos));
        }
    }

    public static boolean respawnPetAtBoundPoint(Mob mob, ServerLevel currentLevel, ServerLevel targetLevel, net.yigitguven.petting.data.CollarRespawnPoint respawnPoint) {
        net.minecraft.core.BlockPos pos = respawnPoint.pos().pos();
        Vec3 respawnPos = new Vec3(pos.getX() + 0.5, pos.getY() + (respawnPoint.isCushion() ? 0.25 : 0.6), pos.getZ() + 0.5);

        if (mob.isVehicle()) {
            mob.ejectPassengers();
        }
        if (mob.isPassenger()) {
            mob.stopRiding();
        }

        mob.setHealth(mob.getMaxHealth());
        mob.removeAllEffects();
        mob.clearFire();
        mob.fallDistance = 0.0F;
        mob.deathTime = 0;
        mob.hurtTime = 0;
        mob.setDeltaMovement(Vec3.ZERO);
        mob.setTarget(null);
        mob.setLastHurtByMob(null);
        mob.setInvulnerableTime(60);
        mob.getNavigation().stop();
        mob.syncVelocity = false;

        net.minecraft.world.entity.Entity respawnedEntity;
        if (targetLevel != currentLevel) {
            respawnedEntity = mob.teleport(new net.minecraft.world.level.portal.TeleportTransition(
                    targetLevel, respawnPos, Vec3.ZERO, mob.getYRot(), 0.0F,
                    net.minecraft.world.level.portal.TeleportTransition.DO_NOTHING
            ));
        } else {
            currentLevel.getChunkSource().removeEntity(mob);
            respawnedEntity = mob.teleport(new net.minecraft.world.level.portal.TeleportTransition(
                    targetLevel, respawnPos, Vec3.ZERO, mob.getYRot(), 0.0F,
                    net.minecraft.world.level.portal.TeleportTransition.DO_NOTHING
            ));
            currentLevel.getChunkSource().addEntity(respawnedEntity != null ? respawnedEntity : mob);
        }

        if (respawnedEntity == null) {
            return false;
        }

        if (respawnedEntity instanceof Mob respawnedMob) {
            respawnedMob.getNavigation().stop();
            respawnedMob.setDeltaMovement(Vec3.ZERO);
            respawnedMob.syncVelocity = false;
            setOrder(respawnedMob, PetOrder.SIT);
            if (respawnedMob instanceof net.minecraft.world.entity.boss.wither.WitherBoss wither) {
                wither.setAlternativeTarget(0, 0);
                wither.setAlternativeTarget(1, 0);
                wither.setAlternativeTarget(2, 0);
            }
        }

        final java.util.UUID newUuid = respawnedEntity.getUUID();
        if (!newUuid.equals(mob.getUUID())) {
            getOwnerUUID(mob).ifPresent(ownerUuid -> {
                PetSavedData.get(currentLevel).removePet(mob.getUUID());
                PetSavedData.get(currentLevel).addPet(ownerUuid, newUuid);
            });
        }

        targetLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.TOTEM_OF_UNDYING, respawnPos.x, respawnPos.y + 0.5, respawnPos.z, 25, 0.4, 0.4, 0.4, 0.1);
        targetLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HEART, respawnPos.x, respawnPos.y + 0.8, respawnPos.z, 8, 0.3, 0.3, 0.3, 0.0);
        targetLevel.playSound(null, respawnPos.x, respawnPos.y, respawnPos.z, net.minecraft.sounds.SoundEvents.TOTEM_USE, net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);

        Player owner = getOwner(mob);
        if (owner != null) {
            net.minecraft.network.chat.Component targetType = net.minecraft.network.chat.Component.translatable(respawnPoint.isCushion() ? "petting.term.cushion" : "petting.term.bed");
            owner.sendSystemMessage(net.minecraft.network.chat.Component.translatable("petting.action.pet_respawned", mob.getName(), targetType));
        }

        final net.minecraft.world.entity.Entity finalEntity = respawnedEntity;
        targetLevel.getServer().schedule(new net.minecraft.server.TickTask(targetLevel.getServer().getTickCount(), () -> {
            if (finalEntity.isAlive()) {
                finalEntity.setDeltaMovement(Vec3.ZERO);
                finalEntity.syncVelocity = false;
                if (finalEntity instanceof Mob m) {
                    m.getNavigation().stop();
                }
            }
        }));

        return true;
    }
}
